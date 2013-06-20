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
package com.soartech.simjr.scenario;

import java.util.List;
import java.util.ListIterator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import org.jdom.Element;
import org.jdom.xpath.XPath;

import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.scenario.model.ModelChangeEvent;
import com.soartech.simjr.scenario.model.ModelElement;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPrototype;

/**
 * @author ray
 */
public class EntityElement implements ModelElement
{
    private static final String NAME_XPATH = "@simjr:name";
    public static final String NAME = EntityElement.class.getCanonicalName() + ".name";
    public static final String PROTOTYPE = EntityElement.class.getCanonicalName() + ".prototype";
    public static final String FORCE = EntityElement.class.getCanonicalName() + ".force";
    public static final String VISIBLE = EntityElement.class.getCanonicalName() + ".visible";
    
    private final Model model;
    private final Element element;
    private final XPath namePath;
    private final XPath prototypePath;
    private final XPath forcePath;
    private final XPath visiblePath;
    private final LocationElement location;
    private final OrientationElement orientation;
    private final ThreeDDataElement threeDData;
    private final ScriptBlockElement initScript;
    private final PointElementList points;
    
    
    public static EntityElement attach(Model model, Element element)
    {
        return new EntityElement(model, element);
    }
    
    public static Element build(Model model, String name, String prototype)
    {
        Element root = model.newElement("entity");
        
        root.setAttribute("name", name, Model.NAMESPACE);
        root.setAttribute("prototype", prototype, Model.NAMESPACE);
        root.setAttribute("force", "friendly", Model.NAMESPACE);
        
        Boolean defaultVisibility = true;
        EntityPrototype ep = model.getEntityPrototype(prototype);
        if(ep != null)
        {
            Object prototypeVisibility = ep.getProperty(EntityConstants.PROPERTY_VISIBLE);
            if(prototypeVisibility != null && prototypeVisibility instanceof Boolean)  {
                defaultVisibility = (Boolean)prototypeVisibility;
            }
        }
        root.setAttribute("visible", defaultVisibility.toString(), Model.NAMESPACE);
        
        root.addContent(LocationElement.buildDefault(model));
        root.addContent(OrientationElement.buildDefault(model));
        root.addContent(ScriptBlockElement.buildDefault(model, "initScript", SimJrProps.get("simjr.editor.entityInitScript.default", "")));
        root.addContent(PointElementList.buildDefault(model));
        root.addContent(ThreeDDataElement.buildDefault(model));
        return root;
    }
    
    public static void setName(Model model, Element element, String newName)
    {
        model.setText(model.newXPath(NAME_XPATH), element, newName, null);
    }
    
    /**
     * @param model
     */
    private EntityElement(Model model, Element element)
    {
        this.model = model;
        this.element = element;
        this.namePath = model.newXPath(NAME_XPATH);
        this.prototypePath = model.newXPath("@simjr:prototype");
        this.forcePath = model.newXPath("@simjr:force");
        this.visiblePath = model.newXPath("@simjr:visible");
        
        this.location = new LocationElement(this);
        this.orientation = new OrientationElement(this);
        this.threeDData = new ThreeDDataElement(this);
        this.initScript = ScriptBlockElement.attach(model, this.element, "initScript");
        this.points = PointElementList.attach(model, this);
    }
    

    /* (non-Javadoc)
     * @see com.soartech.simjr.scenario.ModelElement#getParent()
     */
    public ModelElement getParent()
    {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.scenario.ModelElement#getModel()
     */
    public Model getModel()
    {
        return model;
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.scenario.ModelElement#getElement()
     */
    public Element getElement()
    {
        return element;
    }
    
    public String getName()
    {
        return model.getText(namePath, element);
    }
    
    public UndoableEdit setName(String name)
    {
        final String oldName = getName();
        if(model.setText(namePath, element, name, new ModelChangeEvent(model, this, NAME)))
        {
            final CompoundEdit compound = new CompoundEdit();
            compound.addEdit(new SetNameEdit(oldName));
            fixPolygonReferences(oldName, name, compound);
            compound.end();
            return compound;
        }
        return null;
    }
    
    private void fixPolygonReferences(String oldName, String newName, CompoundEdit compound)
    {
        // TODO: Use XPath to update all the references ?
        for(EntityElement e : getModel().getEntities().getEntities())
        {
            final List<String> oldNames = e.getPoints().getPoints();
            final ListIterator<String> it = oldNames.listIterator();
            boolean changed = false;
            while(it.hasNext())
            {
                final String c = it.next();
                if(oldName.equals(c))
                {
                    changed = true;
                    it.set(newName);
                }
            }
            if(changed)
            {
                compound.addEdit(e.getPoints().setPoints(oldNames));
            }
        }
    }
    
    public String getPrototype()
    {
        return model.getText(prototypePath, element);
    }
    
    public UndoableEdit setPrototype(String prototype)
    {
        final String oldProto = getPrototype();
        if(model.setText(prototypePath, element, prototype, new ModelChangeEvent(model, this, PROTOTYPE)))
        {
            return new SetPrototypeEdit(oldProto);
        }
        return null;
    }
    
    public String getForce()
    {
        return model.getText(forcePath, element);
    }
    
    public UndoableEdit setForce(String force)
    {
        final String oldForce = getForce();
        if(model.setText(forcePath, element, force, new ModelChangeEvent(model, this, FORCE)))
        {
            return new SetForceEdit(oldForce);
        }
        return null;
    }
    
    public boolean isVisible()
    {
        return Boolean.parseBoolean(model.getText(visiblePath, element));
    }
    
    public UndoableEdit setVisible(boolean visible)
    {
        final boolean oldVisible = isVisible();
        if(model.setText(visiblePath, element, Boolean.toString(visible), new ModelChangeEvent(model, this, VISIBLE)))
        {
            return new SetVisibleEdit(oldVisible);
        }
        return null;
    }
    public LocationElement getLocation()
    {
        return location;
    }
    
    public OrientationElement getOrientation()
    {
        return orientation;
    }
    
    public ThreeDDataElement getThreeDData()
    {
        return threeDData;
    }

    public ScriptBlockElement getInitScript()
    {
        return initScript;
    }
    
    public PointElementList getPoints()
    {
        return points;
    }
    
    private class SetNameEdit extends AbstractUndoableEdit
    {
        private static final long serialVersionUID = -2556550749747929852L;
        private final String oldName;
        private final String newName = getName();
        
        public SetNameEdit(String oldName)
        {
            this.oldName = oldName;
        }

        @Override
        public void redo() throws CannotRedoException
        {
            super.redo();
            setName(newName);
        }

        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();
            setName(oldName);
        }
        
    }
    private class SetPrototypeEdit extends AbstractUndoableEdit
    {
        private static final long serialVersionUID = 3408336109329832867L;
        private final String oldPrototype;
        private final String newPrototype = getPrototype();
        
        public SetPrototypeEdit(String oldPrototype)
        {
            this.oldPrototype = oldPrototype;
        }

        @Override
        public void redo() throws CannotRedoException
        {
            super.redo();
            setPrototype(newPrototype);
        }

        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();
            setPrototype(oldPrototype);
        }
        
    }
    
    private class SetForceEdit extends AbstractUndoableEdit
    {
        private static final long serialVersionUID = 3408336109329832867L;
        private final String oldForce;
        private final String newForce = getForce();
        
        public SetForceEdit(String oldForce)
        {
            this.oldForce = oldForce;
        }

        @Override
        public void redo() throws CannotRedoException
        {
            super.redo();
            setForce(newForce);
        }

        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();
            setForce(oldForce);
        }
        
    }
    private class SetVisibleEdit extends AbstractUndoableEdit
    {
        private static final long serialVersionUID = 3408336109329832867L;
        private final boolean oldVisible;
        private final boolean newForce = isVisible();
        
        public SetVisibleEdit(boolean oldForce)
        {
            this.oldVisible = oldForce;
        }

        @Override
        public void redo() throws CannotRedoException
        {
            super.redo();
            setVisible(newForce);
        }

        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();
            setVisible(oldVisible);
        }
        
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getName() + " (" + getPrototype() + ")";
    }
    
    
}
