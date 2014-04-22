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
import com.soartech.simjr.services.DefaultServiceManager;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.sim.EntityPrototypeDatabase;

/**
 * @author ray
 */
public class EntityElement implements ModelElement
{
//    private static final String NAME_XPATH = "@simjr:name";
//    public static final String NAME = EntityElement.class.getCanonicalName() + ".name";
//    public static final String PROTOTYPE = EntityElement.class.getCanonicalName() + ".prototype";
//    public static final String FORCE = EntityElement.class.getCanonicalName() + ".force";
//    public static final String VISIBLE = EntityElement.class.getCanonicalName() + ".visible";
//    public static final String LABEL_VISIBLE = EntityElement.class.getCanonicalName() + ".labelVisible";
//    public static final String MIN_ALTITUDE = EntityElement.class.getCanonicalName() + ".minAltitude";
//    public static final String MAX_ALTITUDE = EntityElement.class.getCanonicalName() + ".maxAltitude";
//    public static final String ROUTE_WIDTH = EntityElement.class.getCanonicalName() + ".routeWidth";
    
    public enum ModelData
    {
        NAME("name", ""),
        PROTOTYPE("prototype", ""),
        FORCE("force", ""),
        VISIBLE("visible", ""),
        LABEL_VISIBLE("labelVisible", ""),
        MIN_ALTITUDE("minAltitude", "0.0"), 
        MAX_ALTITUDE("maxAltitude", "500"), 
        ROUTE_WIDTH("routeWidth", "100");

        public final String attributeName;
        public final String xPathStr;
        public final String defaultValue;

        private ModelData(String attributeName, String defaultValue)
        {
            this.attributeName = attributeName;
            this.xPathStr = "@simjr:".concat(attributeName);
            this.defaultValue = defaultValue;
        }
    }    
    
    private final Model model;
    private final Element element;
    private final XPath namePath;
    private final XPath prototypePath;
    private final XPath forcePath;
    private final XPath visiblePath;
    private final XPath labelVisiblePath;
    private final XPath minAltitudePath;
    private final XPath maxAltitudePath;
    private final XPath routeWidthPath;
    private final LocationElement location;
    private final OrientationElement orientation;
    private final ScriptBlockElement initScript;
    private final PointElementList points;
    private final CapabilitiesElement capabilities;
    
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
        EntityPrototypeDatabase epd = new DefaultServiceManager().findService(EntityPrototypeDatabase.class);
        EntityPrototype ep = epd.getPrototype(prototype);
        
        if(ep != null)
        {
            Object prototypeVisibility = ep.getProperty(EntityConstants.PROPERTY_VISIBLE);
            if(prototypeVisibility != null && prototypeVisibility instanceof Boolean)  {
                defaultVisibility = (Boolean)prototypeVisibility;
            }
        }
        root.setAttribute("visible", defaultVisibility.toString(), Model.NAMESPACE);
        root.setAttribute("labelVisible", defaultVisibility.toString(), Model.NAMESPACE);
        
        root.setAttribute(ModelData.MIN_ALTITUDE.attributeName, ModelData.MIN_ALTITUDE.defaultValue, Model.NAMESPACE);
        root.setAttribute(ModelData.MAX_ALTITUDE.attributeName, ModelData.MAX_ALTITUDE.defaultValue, Model.NAMESPACE);
        root.setAttribute(ModelData.ROUTE_WIDTH.attributeName, ModelData.ROUTE_WIDTH.defaultValue, Model.NAMESPACE);
        
        root.addContent(LocationElement.buildDefault(model));
        root.addContent(OrientationElement.buildDefault(model));
        root.addContent(ScriptBlockElement.buildDefault(model, "initScript", SimJrProps.get("simjr.editor.entityInitScript.default", "")));
        root.addContent(PointElementList.buildDefault(model));
        root.addContent(ThreeDDataElement.buildDefault(model));
        root.addContent(CapabilitiesElement.buildDefault(model));
        return root;
    }
    
    public static void setName(Model model, Element element, String newName)
    {
        model.setText(model.newXPath(ModelData.NAME.xPathStr), element, newName, null);
    }
    
    /**
     * @param model
     */
    private EntityElement(Model model, Element element)
    {
        this.model = model;
        this.element = element;
        this.namePath = model.newXPath(ModelData.NAME.xPathStr);
        this.prototypePath = model.newXPath(ModelData.PROTOTYPE.xPathStr);
        this.forcePath = model.newXPath(ModelData.FORCE.xPathStr);
        this.visiblePath = model.newXPath(ModelData.VISIBLE.xPathStr);
        this.labelVisiblePath = model.newXPath(ModelData.LABEL_VISIBLE.xPathStr);
        this.minAltitudePath = model.newXPath(ModelData.MIN_ALTITUDE.xPathStr);
        this.maxAltitudePath = model.newXPath(ModelData.MAX_ALTITUDE.xPathStr);
        this.routeWidthPath = model.newXPath(ModelData.ROUTE_WIDTH.xPathStr);
        
        this.location = new LocationElement(this);
        this.orientation = new OrientationElement(this);
        this.initScript = ScriptBlockElement.attach(model, this.element, "initScript");
        this.points = PointElementList.attach(model, this);
        this.capabilities = new CapabilitiesElement(this);
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
        if(model.setText(namePath, element, name, new ModelChangeEvent(model, this, ModelData.NAME.xPathStr)))
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
        return setThreeDField(ModelData.PROTOTYPE.xPathStr, prototypePath, getPrototype(), prototype);
    }
    
    public String getForce()
    {
        return new ModelStringGetter(forcePath).get();
    }
    
    public UndoableEdit setForce(String newForce)
    {
        return setThreeDField(ModelData.FORCE.xPathStr, forcePath, getForce(), newForce);
    }
    
    public boolean isVisible()
    {
        return new ModelBooleanGetter(visiblePath).get();
    }
    
    public UndoableEdit setVisible(boolean visible)
    {
        return setThreeDField(ModelData.VISIBLE.xPathStr, visiblePath, isVisible(), visible);
    }
    
    public boolean isLabelVisible()
    {
        // TODO: JCC - this used to default to true if the property didn't exist
        return new ModelBooleanGetter(visiblePath).get();
    }
    
    public UndoableEdit setLabelVisible(boolean labelVisible)
    {
        return setThreeDField(ModelData.LABEL_VISIBLE.xPathStr, labelVisiblePath, isLabelVisible(), labelVisible);
    }
    
    public LocationElement getLocation()
    {
        return location;
    }
    
    public OrientationElement getOrientation()
    {
        return orientation;
    }
    
    public ScriptBlockElement getInitScript()
    {
        return initScript;
    }
    
    public PointElementList getPoints()
    {
        return points;
    }
    
    public CapabilitiesElement getCapabilities()
    {
        return capabilities;
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
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getName() + " (" + getPrototype() + ")";
    }

    public double getRouteWidth()
    {
        return new ModelLegacyDoubleGetter(routeWidthPath).get();
    }

    public UndoableEdit setRouteWidth(final Double newWidth)
    {
        return setThreeDField(ModelData.ROUTE_WIDTH.xPathStr, routeWidthPath, getRouteWidth(), newWidth);
    }

    public double getMinAltitude()
    {
        return new ModelLegacyDoubleGetter(minAltitudePath).get();
    }

    public UndoableEdit setMinAltitude(final Double newMinAltitude)
    {
        return setThreeDField(ModelData.MIN_ALTITUDE.xPathStr, minAltitudePath, getMinAltitude(), newMinAltitude);
    }

    public double getMaxAltitude()
    {
        return new ModelLegacyDoubleGetter(maxAltitudePath).get();
    }

    public UndoableEdit setMaxAltitude(final Double newMaxAltitude)
    {
        return setThreeDField(ModelData.MAX_ALTITUDE.xPathStr, maxAltitudePath, getMaxAltitude(), newMaxAltitude);
    }
    
    private UndoableEdit setThreeDField(final String fieldName, final XPath xPath, final Object oldValue, final Object newValue)
    {
        addThreeDContentIfNecessary(xPath);
        if (model.setText(xPath, getElement(), newValue.toString(), null))
        {
            model.fireChange(new ModelChangeEvent(model, this, fieldName));
            return new ThreeDEdit(fieldName, xPath, oldValue.toString(), newValue.toString());
        }
        return null;
    }
    
    private class ThreeDEdit extends AbstractUndoableEdit
    {
        private static final long serialVersionUID = -3199715918362623766L;
        
        private final XPath xPath;
        private final String fieldName, oldValue, newValue;
        
        public ThreeDEdit(String fieldName, XPath xPath, String oldValue, String newValue)
        {
            this.fieldName = fieldName;
            this.xPath = xPath;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
        
        @Override
        public void redo()
        {
            super.redo();
            model.setText(xPath, getElement(), newValue, null);
            model.fireChange(new ModelChangeEvent(model, this, fieldName));
        }
        public void undo()
        {
            super.undo();
            model.setText(xPath, getElement(), oldValue, null);
            model.fireChange(new ModelChangeEvent(model, this, fieldName));
        }
    }

    private void addThreeDContentIfNecessary(XPath xPath)
    {
        try
        {
            model.getText(xPath, getElement());
        }
        catch(Exception unused)
        {
            getElement().addContent(ThreeDDataElement.buildDefault(model));
        }
    }
    

    private abstract class ModelFieldGetter<T>
    {
        protected T value;

        public ModelFieldGetter(T value)
        {
            this.value = value;
        }

        public T get()
        {
            return value;
        }
    }
    
    private class ModelLegacyDoubleGetter extends ModelFieldGetter<Double>
    {
        public ModelLegacyDoubleGetter(XPath pathToDoubleValue)
        {
            super(EntityElement.this.model.getDouble(
                    pathToDoubleValue,
                    EntityElement.this.getElement()));
        }

        @Override
        public Double get()
        {
            // Workaround -If this is a legacy file, just report a value of 0;
            return value.equals(Double.NaN) ? 0 : value;
        }
    }

    private class ModelBooleanGetter extends ModelFieldGetter<Boolean>
    {
        public ModelBooleanGetter(XPath pathToBooleanValue)
        {
            super(EntityElement.this.model.getBoolean(
                    pathToBooleanValue,
                    EntityElement.this.getElement()));
        }
    } 

    private class ModelStringGetter extends ModelFieldGetter<String>
    {
        public ModelStringGetter(XPath pathToStringValue)
        {
            super(EntityElement.this.model.getText(
                    pathToStringValue,
                    EntityElement.this.getElement()));
        }
    }    
}
