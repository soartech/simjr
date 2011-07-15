
package com.soartech.simjr.scenario;

import java.io.File;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.jdom.Element;
import org.jdom.xpath.XPath;

import com.soartech.simjr.util.UndoTools;

/**
 * @author mjquist
 *
 */
public class TerrainTypeElement implements ModelElement
{
    public static final String ADDED = TerrainTypeElement.class.getCanonicalName() + ".added";
    public static final String REMOVED = TerrainTypeElement.class.getCanonicalName() + ".removed";
    public static final String HREF = TerrainTypeElement.class.getCanonicalName() + ".href";
    public static final String METERS_PER_PIXEL = TerrainTypeElement.class.getCanonicalName() + ".metersPerPixel";
    
    public static boolean isProperty(String p)
    {
        return p.startsWith(TerrainTypeElement.class.getCanonicalName());
    }
    
    private final TerrainElement parent;
    private final XPath terrainTypePath;
    private final XPath terrainTypeHref;
    
    public static TerrainTypeElement attach(TerrainElement parent)
    {
        return new TerrainTypeElement(parent);
    }
    
    private TerrainTypeElement(TerrainElement parent)
    {
        this.parent = parent;
        
        this.terrainTypePath = this.parent.getModel().newXPath("/simjr:scenario/simjr:terrain/simjr:terrainType");
        this.terrainTypeHref = this.parent.getModel().newXPath("/simjr:scenario/simjr:terrain/simjr:terrainType/@simjr:href");
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.scenario.ModelElement#getElement()
     */
    public Element getElement()
    {
        return (Element) getModel().getNode(terrainTypePath, null);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.scenario.ModelElement#getModel()
     */
    public Model getModel()
    {
        return getParent().getModel();
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.scenario.ModelElement#getParent()
     */
    public ModelElement getParent()
    {
        return parent;
    }

    public boolean hasTerrainType()
    {
        return getModel().getNode(terrainTypePath, null) != null;
    }

    public UndoableEdit clearTerrainType()
    {
        UndoableEdit edit = null;
        Element terrainTypeElement = (Element) getModel().getNode(terrainTypePath, null);
        if(terrainTypeElement != null)
        {
            edit = new ClearEdit();
            terrainTypeElement.getParentElement().removeContent(terrainTypeElement);
            getModel().fireChange(new ModelChangeEvent(getModel(), this, REMOVED));
        }
        return edit;
    }
    
    public String getTerrainTypeHref()
    {
        return getModel().getText(terrainTypeHref, null);
    }
    
    /**
     * @return The terrain type file as an absolute path, or null if none is set.
     */
    public File getTerrainTypeFile()
    {
        if(!hasTerrainType())
        {
            return null;
        }
        
        final File href = new File(getTerrainTypeHref());
        if(href.isAbsolute())
        {
            return href;
        }
        
        final File modelFile = getModel().getFile();
        
        return modelFile != null ? new File(modelFile.getParent(), href.toString()) : href;
    }
    
    public UndoableEdit setTerrainTypeHref(String href)
    {
        final UndoableEdit create = getOrCreateTerrainTypeElement("");
        final UndoableEdit set;
        final String oldHref = getTerrainTypeHref();
        if(getModel().setText(terrainTypeHref, null, href, new ModelChangeEvent(getModel(), this, HREF)))
        {
            set = new SetTerrainTypeHrefEdit(oldHref);
        }
        else
        {
            set = null;
        }
        return UndoTools.createCompound(create, set);
    }
    
    private UndoableEdit getOrCreateTerrainTypeElement(String href)
    {
        final UndoableEdit edit;
        Element terrainTypeElement = (Element) getModel().getNode(terrainTypePath, null);
        if(terrainTypeElement == null)
        {
            terrainTypeElement = buildTerrainTypeElement(href);
            getParent().getElement().addContent(terrainTypeElement);
            getModel().fireChange(new ModelChangeEvent(getModel(), this, ADDED));
            
            edit = new CreateEdit();
        }
        else
        {
            edit = null;
        }
        return edit;
    }
    
    private Element buildTerrainTypeElement(String href)
    {
        Element root = getModel().newElement("terrainType");
        root.setAttribute("href", href, Model.NAMESPACE);
        root.addContent(LocationElement.buildDefault(getModel()));
        return root;
    }

    private class SetTerrainTypeHrefEdit extends AbstractUndoableEdit
    {
        private static final long serialVersionUID = 3822586829878438892L;
        final String newHref = getTerrainTypeHref();
        final String oldHref;

        public SetTerrainTypeHrefEdit(String oldHref)
        {
            this.oldHref = oldHref;
        }
        
        @Override
        public void redo() throws CannotRedoException
        {
            super.redo();
            setTerrainTypeHref(newHref);
        }
        
        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();
            setTerrainTypeHref(oldHref);
        }
    }

    private class CreateEdit extends AbstractUndoableEdit
    {
        private static final long serialVersionUID = 2557435858151443282L;
        final Element element = getElement();
        final Element parent = element.getParentElement();
        final int index = parent.indexOf(element);

        @Override
        public void redo() throws CannotRedoException
        {
            super.redo();
            parent.addContent(index, element);
            getModel().fireChange(new ModelChangeEvent(getModel(), TerrainTypeElement.this, ADDED));
        }

        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();
            parent.removeContent(element);
            getModel().fireChange(new ModelChangeEvent(getModel(), TerrainTypeElement.this, REMOVED));
        }
    }

    private class ClearEdit extends AbstractUndoableEdit
    {
        private static final long serialVersionUID = 2266025921911874109L;
        final String oldHref = getTerrainTypeHref();

        @Override
        public void redo() throws CannotRedoException
        {
            super.redo();
            clearTerrainType();
        }

        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();
            getOrCreateTerrainTypeElement(oldHref);
        }
    }
}
