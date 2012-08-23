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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.soartech.math.LineSegmentDistance;
import com.soartech.math.Vector3;
import com.soartech.shapesystem.CoordinateTransformer;
import com.soartech.shapesystem.FillStyle;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.PrimitiveRenderer;
import com.soartech.shapesystem.PrimitiveRendererFactory;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.Shape;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.SimplePosition;
import com.soartech.shapesystem.shapes.Circle;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.entities.Cylinder;

/**
 * @author ray
 */
public class CircularRegionShape extends EntityShape
{
    public static final String NAME = "cylinder";
    private boolean updateWidth = false;
    private Circle region;
    
    public static final EntityShapeFactory FACTORY = new Factory();
    public static class Factory extends AbstractEntityShapeFactory {

        public EntityShape create(Entity entity, ShapeSystem system)
        {
            return new CircularRegionShape(entity, system);
        }
        
        public Shape createSelection(String id, Entity selected)
        {
            Circle highlightedRegion;
            final Map<String, Object> props = selected.getProperties();
            Number lineWidth = (Number) EntityTools.getProperty(props, EntityConstants.PROPERTY_SHAPE_WIDTH_METERS,null);
            if(lineWidth == null || Math.abs(lineWidth.doubleValue()) <.5)
            {
                highlightedRegion = new Circle(id, EntityConstants.LAYER_AREA,
                        new Position(selected.getName()), Rotation.IDENTITY, createSelectionStyle(),
                        Scalar.createPixel(20));    
            }
            else
            {
                highlightedRegion = 
                        new Circle(id, EntityConstants.LAYER_AREA,
                                new Position(selected.getName()), Rotation.IDENTITY, createSelectionStyle(),
                                Scalar.createMeter(lineWidth.doubleValue()/2));         
            }
            return highlightedRegion;
        }
        
        public String toString() { return NAME; }
    };
    
    private static final ShapeStyle labelStyle = new ShapeStyle();
    static
    {
        labelStyle.setFillStyle(FillStyle.FILLED);
        labelStyle.setFillColor(new Color(0xF0, 0xF0, 0xE0));
        labelStyle.setOpacity(0.75f);
    }
    
    /**
     * @param entity
     * @param system
     */
    public CircularRegionShape(Entity entity, ShapeSystem system)
    {
        super(entity, system);
        
        final Map<String, Object> props = entity.getProperties();
        String layer = EntityTools.getProperty(props, 
               EntityConstants.PROPERTY_SHAPE_LAYER, 
               EntityConstants.LAYER_AREA).toString();
        String name = getRootFrame().getName();
        
        final ShapeStyle style = new ShapeStyle();
        style.setFillStyle(FillStyle.FILLED);
        final Color fillColor =(Color) EntityTools.getFillColor(entity, Color.LIGHT_GRAY);
        style.setFillColor(fillColor);
        
        
        final Number opacity = (Number) EntityTools.getProperty(props, EntityConstants.PROPERTY_SHAPE_OPACITY, 0.5f);
        if(opacity != null)
        {
            style.setOpacity(opacity.floatValue());
        }
        
        Number lineWidth = (Number) EntityTools.getProperty(props, EntityConstants.PROPERTY_SHAPE_WIDTH_METERS,null);
        if(lineWidth == null || Math.abs(lineWidth.doubleValue()) <.5)
        {
            region = new Circle(name + ".body", EntityConstants.LAYER_AREA, 
                                new Position(name),
                                Rotation.IDENTITY,
                                style, 
                                Scalar.createPixel(10)
                                );
        }
        else
        {
            region = new Circle(name + ".body", EntityConstants.LAYER_AREA, 
                    new Position(name),
                    Rotation.IDENTITY,
                    style, 
                    Scalar.createMeter(lineWidth.doubleValue()/2)
                    );
            
        }
        createLabel(10, 10, name);
        addHitableShape(region);
       
    }
    
    @Override
    public void update()
    {
        super.update();
        if(updateWidth)
        {
            
            updateWidth = false;
            updateWidth();
        }
    }
    
    private void updateWidth()
    {
        removeShape(region);
        final Map<String, Object> props = getEntity().getProperties();
        String layer = EntityTools.getProperty(props, 
               EntityConstants.PROPERTY_SHAPE_LAYER, 
               EntityConstants.LAYER_ROUTE).toString();
        String name = getRootFrame().getName();
        
        final ShapeStyle style = new ShapeStyle();
        style.setFillStyle(FillStyle.FILLED);
        final Color fillColor =(Color) EntityTools.getFillColor(getEntity(), Color.LIGHT_GRAY);
        style.setFillColor(fillColor);
        //final Color lineColor = (Color) EntityTools.getProperty(props, EntityConstants.PROPERTY_SHAPE_LINE_COLOR, Color.BLUE);
        //style.setLineColor(lineColor);
        
        final Number opacity = (Number) EntityTools.getProperty(props, EntityConstants.PROPERTY_SHAPE_OPACITY, 0.5);
        if(opacity != null)
        {
            style.setOpacity(opacity.floatValue());
        }
        
        Number lineWidth = (Number) EntityTools.getProperty(props, EntityConstants.PROPERTY_SHAPE_WIDTH_METERS,null);
        if(Math.abs(lineWidth.doubleValue()) <.5)
        {
            region = new Circle(name + ".body", EntityConstants.LAYER_AREA, 
                                new Position(name),
                                Rotation.IDENTITY,
                                style, 
                                Scalar.createPixel(10)
                                );
        }
        else
        {
            region = new Circle(name + ".body", EntityConstants.LAYER_AREA, 
                    new Position(name),
                    Rotation.IDENTITY,
                    style, 
                    Scalar.createMeter(lineWidth.doubleValue()/2)
                    );
            
        }
        addHitableShape(region);
    }
    
    @Override
    public void onPropertyChanged(Entity entity, String propertyName)
    {
        super.onPropertyChanged(entity, propertyName);
        if(EntityConstants.PROPERTY_SHAPE_WIDTH_METERS.equals(propertyName))
        {
            this.updateWidth = true;
        }
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.pvd.EntityShapeFactory#createSelection(java.lang.String, com.soartech.simjr.Entity)
     */
    public Shape createSelection(String id, Entity selected)
    {
       /*
        *  return new Circle(id, EntityConstants.LAYER_SELECTION,
                new Position(selected.getName()), Rotation.IDENTITY, createSelectionStyle(),
                Scalar.createPixel(20));   */ 
        return null;
    }

}
