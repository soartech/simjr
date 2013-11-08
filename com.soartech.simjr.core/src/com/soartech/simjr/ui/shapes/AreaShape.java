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
 * Created on Jun 20, 2007
 */
package com.soartech.simjr.ui.shapes;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.soartech.shapesystem.FillStyle;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.Shape;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.shapes.Hull;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.entities.AbstractPolygon;

/**
 * @author ray
 */
public class AreaShape extends EntityShape implements EntityConstants
{
    public static final String NAME = "area";
    
    public static final EntityShapeFactory FACTORY = new SimpleHullFactory();
    
    public static class SimpleHullFactory extends AbstractEntityShapeFactory {

        /* (non-Javadoc)
         * @see com.soartech.simjr.ui.shapes.EntityShapeFactory#create(com.soartech.simjr.sim.Entity, com.soartech.shapesystem.ShapeSystem)
         */
        public EntityShape create(Entity entity, ShapeSystem system)
        {
            return new AreaShape(Adaptables.adapt(entity, AbstractPolygon.class), system, false);
        }
        
        /* (non-Javadoc)
         * @see com.soartech.simjr.ui.pvd.AbstractEntityShapeFactory#createSelection(java.lang.String, com.soartech.simjr.Entity)
         */
        @Override
        public Shape createSelection(String id, Entity selected)
        {
            final AbstractPolygon polygon = Adaptables.adapt(selected, AbstractPolygon.class);
            final List<String> points = polygon.getPointNames();
            
            return new Hull(id, LAYER_SELECTION, createSelectionStyle(), points, true);
        }

        public String toString() { return NAME; }
    };
    
    public static class ComplexHullFactory extends AbstractEntityShapeFactory {

        /* (non-Javadoc)
         * @see com.soartech.simjr.ui.shapes.EntityShapeFactory#create(com.soartech.simjr.sim.Entity, com.soartech.shapesystem.ShapeSystem)
         */
        public EntityShape create(Entity entity, ShapeSystem system)
        {
            return new AreaShape(Adaptables.adapt(entity, AbstractPolygon.class), system, false);
        }
        
        /* (non-Javadoc)
         * @see com.soartech.simjr.ui.pvd.AbstractEntityShapeFactory#createSelection(java.lang.String, com.soartech.simjr.Entity)
         */
        @Override
        public Shape createSelection(String id, Entity selected)
        {
            final AbstractPolygon polygon = Adaptables.adapt(selected, AbstractPolygon.class);
            final List<String> points = polygon.getPointNames();
            
            return new Hull(id, LAYER_SELECTION, createSelectionStyle(), points, false);
        }

        public String toString() { return NAME; }
    };
    
    private final AbstractPolygon polygon;
    private Hull hull;
    private boolean updateHull = false;
    
    /**
     * @param area
     * @param system
     */
    public AreaShape(AbstractPolygon polygon, ShapeSystem system, boolean convex)
    {
        super(polygon.getEntity(), system);
        
        this.polygon = polygon;
        
        String name = getRootFrame().getName();
        
        Map<String, Object> props = polygon.getEntity().getProperties();
        
        ShapeStyle style = new ShapeStyle().setFillStyle(FillStyle.FILLED).
                                            setLineThickness(Scalar.createPixel(1));
        
        
        Color lineColor = (Color) EntityTools.getLineColor(polygon.getEntity(), Color.GRAY);
        style.setLineColor(lineColor);
        
        Color fillColor = (Color) EntityTools.getFillColor(polygon.getEntity(), Color.LIGHT_GRAY);
        style.setFillColor(fillColor);
        
        
        Number opacity = (Number) EntityTools.getProperty(props, PROPERTY_SHAPE_OPACITY, 0.5f);
        if(opacity != null)
        {
            style.setOpacity(opacity.floatValue());
        }
        
        hull = new Hull(name + ".hull", LAYER_AREA, style, polygon.getPointNames(), convex);
                
        createLabel(0, 0, polygon.getName());
        
        addHitableShape(hull);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.pvd.EntityShape#onPropertyChanged(com.soartech.simjr.Entity, java.lang.String)
     */
    @Override
    public void onPropertyChanged(Entity entity, String propertyName)
    {
        super.onPropertyChanged(entity, propertyName);
        
        if(propertyName.equals(PROPERTY_POINTS))
        {
            updateHull = true;
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.pvd.EntityShape#update()
     */
    @Override
    public void update()
    {
        super.update();
        
        if(updateHull)
        {
            hull.setPointNames(polygon.getPointNames());
            updateHull = false;
        }
    }

    
}
