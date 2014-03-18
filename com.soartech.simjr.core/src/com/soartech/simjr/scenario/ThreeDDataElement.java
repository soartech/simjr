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

import java.lang.Double;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

import org.jdom.Element;
import org.jdom.xpath.XPath;

import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.scenario.model.ModelChangeEvent;

/**
 * @author Haley
 */
public class ThreeDDataElement
{
    public static final String THREEDDATA = ThreeDDataElement.class.getCanonicalName() + ".threeddata";

    private final EntityElement entity;
    private final Model model;

    private enum ModelData
    {
        MIN_ALTITUDE("minAltitude", "0.0"), 
        MAX_ALTITUDE("maxAltitude", "500"), 
        ROUTE_WIDTH("routeWidth", "100"),
        VIEW_3D_SUPPORTED("view3DSupported", "true");

        public final String attributeName;
        public final String xPathStr;
        public final String defaultValue;

        private ModelData(String attributeName, String defaultValue)
        {
            this.attributeName = attributeName;
            this.xPathStr = "simjr:threeddata/@simjr:".concat(attributeName);
            this.defaultValue = defaultValue;
        }
    }

    private final XPath minAltitude;
    private final XPath maxAltitude;
    private final XPath routeWidth;
    private final XPath view3DSupported;

    public static Element buildDefault(Model model)
    {
        Element root = model.newElement("threeddata");
        root.setAttribute(ModelData.MIN_ALTITUDE.attributeName, ModelData.MIN_ALTITUDE.defaultValue, Model.NAMESPACE);
        root.setAttribute(ModelData.MAX_ALTITUDE.attributeName, ModelData.MAX_ALTITUDE.defaultValue, Model.NAMESPACE);
        root.setAttribute(ModelData.ROUTE_WIDTH.attributeName, ModelData.ROUTE_WIDTH.defaultValue, Model.NAMESPACE);
        root.setAttribute(ModelData.VIEW_3D_SUPPORTED.attributeName, ModelData.VIEW_3D_SUPPORTED.defaultValue, Model.NAMESPACE);
        return root;
    }
    
    public ThreeDDataElement(EntityElement entity)
    {
        this.entity = entity;
        this.model = this.entity.getModel();
        this.minAltitude = this.model.newXPath(ModelData.MIN_ALTITUDE.xPathStr);
        this.maxAltitude = this.model.newXPath(ModelData.MAX_ALTITUDE.xPathStr);
        this.routeWidth = this.model.newXPath(ModelData.ROUTE_WIDTH.xPathStr);
        this.view3DSupported = this.model.newXPath(ModelData.VIEW_3D_SUPPORTED.xPathStr);
    }

    public EntityElement getEntity()
    {
        return entity;
    }
    
    private UndoableEdit setThreeDField(final XPath xPath, final Object oldValue, final Object newValue)
    {
        addThreeDContentIfNecessary(xPath);
        if (model.setText(xPath, entity.getElement(), newValue.toString(), null))
        {
            model.fireChange(new ModelChangeEvent(model, this, THREEDDATA));
            return new ThreeDEdit(xPath, oldValue.toString(), newValue.toString());
        }
        return null;
    }
    
    public boolean get3dSupported()
    {
        return new ModelBooleanGetter(view3DSupported).get();
    }

    public UndoableEdit set3dSupported(final Boolean newSupported)
    {
        return setThreeDField(view3DSupported, get3dSupported(), newSupported);
    }

    public double getMinAltitude()
    {
        return new ModelLegacyDoubleGetter(minAltitude).get();
    }

    public UndoableEdit setMinAltitude(final Double newMinAltitude)
    {
        return setThreeDField(minAltitude, getMinAltitude(), newMinAltitude);
    }

    public double getMaxAltitude()
    {
        return new ModelLegacyDoubleGetter(maxAltitude).get();
    }

    public UndoableEdit setMaxAltitude(final Double newMaxAltitude)
    {
        return setThreeDField(maxAltitude, getMaxAltitude(), newMaxAltitude);
    }

    public double getRouteWidth()
    {
        return new ModelLegacyDoubleGetter(routeWidth).get();
    }

    public UndoableEdit setRouteWidth(final Double newWidth)
    {
        return setThreeDField(routeWidth, getRouteWidth(), newWidth);
    }
    
    private class ThreeDEdit extends AbstractUndoableEdit
    {
        private static final long serialVersionUID = -3199715918362623766L;
        
        private final XPath xPath;
        private final String oldValue, newValue;
        
        public ThreeDEdit(XPath xPath, String oldValue, String newValue)
        {
            this.xPath = xPath;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
        
        @Override
        public void redo()
        {
            super.redo();
            model.setText(xPath, entity.getElement(), newValue, null);
            model.fireChange(new ModelChangeEvent(model, ThreeDDataElement.this, THREEDDATA));
        }
        public void undo()
        {
            super.undo();
            model.setText(xPath, entity.getElement(), oldValue, null);
            model.fireChange(new ModelChangeEvent(model, ThreeDDataElement.this, THREEDDATA));
        }
    }

    private void addThreeDContentIfNecessary(XPath xPath)
    {
        try
        {
            model.getText(routeWidth, entity.getElement());
        }
        catch(Exception unused)
        {
            entity.getElement().addContent(ThreeDDataElement.buildDefault(model));
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
            super(ThreeDDataElement.this.model.getDouble(
                    pathToDoubleValue,
                    ThreeDDataElement.this.entity.getElement()));
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
        public ModelBooleanGetter(XPath pathToDoubleValue)
        {
            super(ThreeDDataElement.this.model.getBoolean(
                    pathToDoubleValue,
                    ThreeDDataElement.this.entity.getElement()));
        }
    }
}
