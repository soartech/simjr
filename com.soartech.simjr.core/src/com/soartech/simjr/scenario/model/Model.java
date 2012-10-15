/*
 * Copyright (c) 2010, Soar Technology, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * * Neither the name of Soar Technology, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without the specific prior written permission of Soar Technology, Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on Mar 27, 2009
 */
package com.soartech.simjr.scenario.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.scenario.EntityElementList;
import com.soartech.simjr.scenario.MetadataElement;
import com.soartech.simjr.scenario.ScriptBlockElement;
import com.soartech.simjr.scenario.TerrainElement;
import com.soartech.simjr.util.JDomTools;

/**
 * @author ray
 */
public class Model
{
    private static final Logger logger = Logger.getLogger(Model.class);
    
    public static final String DEFAULT_EXTENSION = "sjx";
    
    public static final Namespace NAMESPACE = Namespace.getNamespace("simjr", "http://simjr.soartech.com/schema/scenario/1.0");
    public static final String VERSION = "1.0";
    public static final String LOADED = "loaded";
    public static final String DIRTY = "dirty";
    public static final String FILE = "file";
    public static final String METADATA = "metadata";
    
    private final List<ModelChangeListener> listeners = new CopyOnWriteArrayList<ModelChangeListener>();
    
    private File file;
    private Document doc = buildDefaultDocument();
    private boolean dirty = false;
    
    private final MetadataElement meta = new MetadataElement(this);
    private EntityElementList entities;
    private final TerrainElement terrain = TerrainElement.attach(this);
    
    public void fireChange(ModelChangeEvent e)
    {
        for(ModelChangeListener listener : listeners)
        {
            listener.onModelChanged(e);
        }
    }
    
    public Model()
    {
        newModel();
    }
    
    public void newModel()
    {
        this.doc = buildDefaultDocument();
        this.entities = EntityElementList.attach(this);
        
        setDirty(false);
        setFile(null);
        
        fireChange(new ModelChangeEvent(this, this, LOADED));
        fireChange(new ModelChangeEvent(this, this, FILE));
    }
    

    public void load(File file) throws ModelException
    {
        try
        {
            this.doc = newBuilder().build(new BufferedReader(new FileReader(file)));
            this.entities = EntityElementList.attach(this);
            setFile(file);
            setDirty(false);
            fireChange(new ModelChangeEvent(this, this, LOADED));
            fireChange(new ModelChangeEvent(this, this, FILE));
        } 
        catch (JDOMException e)
        {
            throw new ModelException(e);
        } 
        catch (IOException e)
        {
            throw new ModelException(e);
        }
    }
    
    public void update(String contents) throws ModelException
    {
        try
        {
            this.doc = newBuilder().build(new StringReader(contents));
            this.entities = EntityElementList.attach(this);
            setDirty(true);
        } 
        catch (JDOMException e)
        {
            throw new ModelException(e);
        } 
        catch (IOException e)
        {
            throw new ModelException(e);
        }
    }
    
    private SAXBuilder newBuilder()
    {
        SAXBuilder builder = new SAXBuilder();
        builder.setValidation(false);
        builder.setIgnoringElementContentWhitespace(true);
        return builder;
    }
    
    public void addModelChangeListener(ModelChangeListener listener)
    {
        listeners.add(listener);
    }
    
    public void removeModelChangeListener(ModelChangeListener listener)
    {
        listeners.remove(listener);
    }
    
    /**
     * @return the meta
     */
    public MetadataElement getMeta()
    {
        return meta;
    }
    
    public TerrainElement getTerrain()
    {
        return terrain;
    }
    
    public ScriptBlockElement getPreLoadScript()
    {
        return ScriptBlockElement.attach(this, doc.getRootElement(), "preLoadScript");
    }
    
    public ScriptBlockElement getPostLoadScript()
    {
        return ScriptBlockElement.attach(this, doc.getRootElement(), "postLoadScript");
    }
    
    public EntityElementList getEntities()
    {
        return entities;
    }
    
    public void save(File file) throws ModelException
    {
        final XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        Writer w = null;
        try
        {
            w = new BufferedWriter(new FileWriter(file));
            outputter.output(doc, w);
        }
        catch (IOException e)
        {
            throw new ModelException(e);
        }
        finally
        {
            if(w != null)
            {
                try
                {
                    w.close();
                    setFile(file);
                    setDirty(false);
                }
                catch (IOException e)
                {
                    throw new ModelException(e);
                }
            }
        }
    }
    
    /**
     * @return the file the model is saved to, or <code>null</code> if it hasn't been saved yet
     */
    public File getFile()
    {
        return file;
    }
    
    private void setFile(File file)
    {
        final boolean changed = file != this.file || (file != null && !file.equals(this.file));
        if(changed)
        {
            this.file = file;
            fireChange(new ModelChangeEvent(this, this, FILE));
        }
    }
    
    public boolean isDirty()
    {
        return dirty;
    }
    
    private void setDirty(boolean newValue)
    {
        final boolean oldValue = this.dirty;
        if(oldValue != newValue)
        {
            this.dirty = newValue;
            fireChange(new ModelChangeEvent(this, this, DIRTY));
        }
    }
    
    public Document getDocument()
    {
        return doc;
    }
    
    public XPath newXPath(String expression)
    {
        try
        {
            XPath xp = XPath.newInstance(expression);
            xp.addNamespace(NAMESPACE);
            return xp;
        }
        catch (JDOMException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public Object getNode(XPath xpath, Object context)
    {
        try
        {
            return xpath.selectSingleNode(context != null ? context : doc);
        }
        catch (JDOMException e)
        {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }
    
    public String getText(XPath xpath, Object context)
    {
        try
        {
            return xpath.valueOf(context != null ? context : doc);
        }
        catch (JDOMException e)
        {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }
    
    public double getDouble(XPath xpath, Object context)
    {
        try
        {
            return xpath.numberValueOf(context != null ? context : doc).doubleValue();
        }
        catch (JDOMException e)
        {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }
    
    public boolean getBoolean(XPath xpath, Object context)
    {
        try
        {
            return xpath.valueOf(context != null ? context : doc).equalsIgnoreCase("true");
        }
        catch (JDOMException e)
        {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }
    
    public boolean setText(XPath xpath, Object context, String text, ModelChangeEvent event)
    {
        final String oldValue = getText(xpath, context);
        if(oldValue.equals(text))
        {
            return false;
        }
        try
        {
            final Object o = xpath.selectSingleNode(context != null ? context : doc);
            if(o instanceof Element)
            {
                ((Element) o).setText(text);
                setDirty(true);
            }
            else if(o instanceof Text)
            {
                ((Text) o).setText(text);
                setDirty(true);
            }
            else if(o instanceof Attribute)
            {
                ((Attribute) o).setValue(text);
            }
            else
            {
                throw new RuntimeException("Unknown result '" + o + "' from xpath '" + xpath + "'");
            }
            if(event != null)
            {
                fireChange(event);
            }
            setDirty(true);
        }
        catch (JDOMException e)
        {
            logger.error(e);
            throw new RuntimeException(e);
        }
        return true;
    }
    
    public Element newElement(String name)
    {
        return new Element(name, NAMESPACE);
    }
    
    private Document buildDefaultDocument()
    {
        final Document doc = new Document();
        final Element root = newElement("scenario");
        doc.setRootElement(root);
        root.setAttribute("version", VERSION, NAMESPACE);
        root.addContent(MetadataElement.buildDefault(this));
        root.addContent(TerrainElement.buildDefault(this));
        root.addContent(EntityElementList.buildDefault(this));
        root.addContent(ScriptBlockElement.buildDefault(this, "preLoadScript", SimJrProps.get("simjr.editor.preLoadScript.default", "")));
        root.addContent(ScriptBlockElement.buildDefault(this, "postLoadScript", SimJrProps.get("simjr.editor.postLoadScript.default", "")));
        return doc;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return JDomTools.prettyXmlString(doc);
    }
}
