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

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.jdom.Element;
import org.jdom.xpath.XPath;

/**
 * @author ray
 */
public class ThreeDDataElement
{
    public static final String THREEDDATA = ThreeDDataElement.class.getCanonicalName() + ".threeddata";
    
    private final EntityElement entity;
    private final XPath minAltitude;
    private final XPath maxAltitude;
    private final XPath routeWidth;
    private final XPath view3DSupported;
    //private final XPath AreaType;

    
    public static Element buildDefault(Model model)
    {
        Element root = model.newElement("threeddata");
        root.setAttribute("minAltitude", "0.0", Model.NAMESPACE);
        root.setAttribute("maxAltitude", "0.0", Model.NAMESPACE);
        root.setAttribute("routeWidth", "0.0", Model.NAMESPACE);
        root.setAttribute("view3DSupported", "false", Model.NAMESPACE);
        return root;
    }
    
    /**
     * @param model
     */
    public ThreeDDataElement(EntityElement entity)
    {
        this.entity = entity;
        this.minAltitude = this.entity.getModel().newXPath("simjr:threeddata/@simjr:minAltitude");
        this.maxAltitude = this.entity.getModel().newXPath("simjr:threeddata/@simjr:maxAltitude");
        this.routeWidth = this.entity.getModel().newXPath("simjr:threeddata/@simjr:routeWidth");
        this.view3DSupported = this.entity.getModel().newXPath("simjr:threeddata/@simjr:view3DSupported");
        
    }
    
    public EntityElement getEntity()
    {
        return entity;
    }

    public boolean get3dSupported()
    {
        boolean supported = this.entity.getModel().getBoolean(view3DSupported, entity.getElement());
        return supported;
        
    }
    public UndoableEdit set3dSupported(boolean supported)
    {
        return setThreeDData(getMinAltitude(), getMaxAltitude(), getRouteWidth(), supported);
    }
    public double getMinAltitude()
    {
        Double value = this.entity.getModel().getDouble(minAltitude, entity.getElement());
        //Workaround -If this is a legacy file, just report a value of 0;
        if(value.isNaN())
        {
           
            return 0;
        }
        return value;
    }
    
    public UndoableEdit setMinAltitude(double altitude)
    {
        return setThreeDData(altitude, getMaxAltitude(), getRouteWidth(),get3dSupported());
    }
    
    public double getMaxAltitude()
    {
        Double value = this.entity.getModel().getDouble(maxAltitude, entity.getElement());
        //Workaround -If this is a legacy file, just report a value of 0;
        if(value.isNaN())
        {
           
            return 0;
        }
        return value;
    }
    
    public UndoableEdit setMaxAltitude(double altitude)
    {
        return setThreeDData(getMinAltitude(), altitude, getRouteWidth(),get3dSupported());
    }
    public double getRouteWidth()
    {
        Double value = this.entity.getModel().getDouble(routeWidth, entity.getElement());
        //Workaround -If this is a legacy file, just report a value of 0;
        if(value.isNaN())
        {
           
            return 0;
        }
        return value;
    }
    
    public UndoableEdit setRouteWidth(double width)
    {
        return setThreeDData(getMinAltitude(), getMaxAltitude(), width,get3dSupported());
    }
   
    public UndoableEdit setThreeDData(double min, double max, double width, boolean threeDSupported)
    {
        final Model model = this.entity.getModel();
        final Element context = entity.getElement();
        
        final double oldMinAltitude = this.getMinAltitude();
        final double oldMaxAltitude = this.getMaxAltitude();
        final double oldRouteWidth = this.getRouteWidth();
        final boolean oldthreeDSupported= this.get3dSupported();
        
        /*
         * This try catch is here so that older style senario files will have data added
         */
        boolean changedOccured;
        try{
            changedOccured = model.setText(minAltitude, context, Double.toString(min), null);
            changedOccured = model.setText(maxAltitude, context, Double.toString(max), null) | changedOccured;        
            changedOccured = model.setText(routeWidth, context, Double.toString(width), null) | changedOccured;
            changedOccured = model.setText(view3DSupported, context,Boolean.toString(threeDSupported), null) | changedOccured;
        }
        catch(Exception e)
        {
            entity.getElement().addContent(ThreeDDataElement.buildDefault(model));
            changedOccured = model.setText(minAltitude, context, Double.toString(min), null);
            changedOccured = model.setText(maxAltitude, context, Double.toString(max), null) | changedOccured;        
            changedOccured = model.setText(routeWidth, context, Double.toString(width), null) | changedOccured;
            changedOccured = model.setText(view3DSupported, context,Boolean.toString(threeDSupported), null) | changedOccured;
        }
        
        UndoableEdit edit = null;
        if(changedOccured)
        {
            model.fireChange(newEvent(THREEDDATA));
            edit = new Edit(oldMinAltitude, oldMaxAltitude, oldRouteWidth, oldthreeDSupported);
        }
        return edit;
    }
    
    private ModelChangeEvent newEvent(String prop)
    {
        return new ModelChangeEvent(this.entity.getModel(), this, prop);
    }
    
    private class Edit extends AbstractUndoableEdit
    {
        private static final long serialVersionUID = -8050360351980227293L;

        private final double oldMinAltitude, oldMaxAltitude, oldRouteWidth;
        private final boolean oldThreeDSupported;
        private final double newMinAltitude, newMaxAltitude, newRouteWidth;
        private final boolean newThreeDSupported;
        
        public Edit(double oldMinAltitude, double oldMaxAltitude, double oldRouteWidth, boolean oldThreeDSupported)
        {
            this.oldMinAltitude = oldMinAltitude;
            this.oldMaxAltitude = oldMaxAltitude;
            this.oldRouteWidth = oldRouteWidth;
            this.oldThreeDSupported = oldThreeDSupported;
            this.newMinAltitude = getMinAltitude();
            this.newMaxAltitude = getMaxAltitude();
            this.newRouteWidth = getRouteWidth();
            this.newThreeDSupported = get3dSupported();
        }

        /* (non-Javadoc)
         * @see javax.swing.undo.AbstractUndoableEdit#redo()
         */
        @Override
        public void redo() throws CannotRedoException
        {
            super.redo();
            setThreeDData(newMinAltitude, newMaxAltitude, newRouteWidth, newThreeDSupported);
        }

        /* (non-Javadoc)
         * @see javax.swing.undo.AbstractUndoableEdit#undo()
         */
        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();
            setThreeDData(oldMinAltitude, oldMaxAltitude, oldRouteWidth, oldThreeDSupported);
        }
    }

}
