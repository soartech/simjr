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
import java.util.Map;

import com.soartech.shapesystem.FillStyle;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.entities.DefaultCompoundPolygon;

/**
 * @author ray
 */
public class CompoundAreaShape extends EntityShape implements EntityConstants
{
    public static final String NAME = "compoundarea";
    
    public static final EntityShapeFactory FACTORY = new CompoundAreaShapeFactory();
    
    public static class CompoundAreaShapeFactory extends AbstractEntityShapeFactory {

        /* (non-Javadoc)
         * @see com.soartech.simjr.ui.shapes.EntityShapeFactory#create(com.soartech.simjr.sim.Entity, com.soartech.shapesystem.ShapeSystem)
         */
        public EntityShape create(Entity entity, ShapeSystem system)
        {
            return new CompoundAreaShape(Adaptables.adapt(entity, DefaultCompoundPolygon.class), system);
        }
        
        public String toString() { return NAME; }
    };
    
    private final DefaultCompoundPolygon compoundPolygon;
    private boolean updateShape = false;
    
    /**
     * @param area
     * @param system
     */
    public CompoundAreaShape(DefaultCompoundPolygon defaultCompoundPolygon, ShapeSystem system)
    {
        super(defaultCompoundPolygon.getEntity(), system);
        
        this.compoundPolygon = defaultCompoundPolygon;
        
        String name = getRootFrame().getName();
        
        Map<String, Object> props = defaultCompoundPolygon.getEntity().getProperties();
        
        ShapeStyle style = new ShapeStyle().setFillStyle(FillStyle.NONE).
                                            setLineThickness(Scalar.createPixel(1));
        
        Color lineColor = (Color) EntityTools.getProperty(props, PROPERTY_SHAPE_LINE_COLOR, Color.GRAY);
        style.setLineColor(lineColor);
        
        Number opacity = (Number) EntityTools.getProperty(props, PROPERTY_SHAPE_OPACITY, 0.5f);
        if(opacity != null)
        {
            style.setOpacity(opacity.floatValue());
        }
        
        createLabel(0, 0, defaultCompoundPolygon.getName());
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.pvd.EntityShape#onPropertyChanged(com.soartech.simjr.Entity, java.lang.String)
     */
    @Override
    public void onPropertyChanged(Entity entity, String propertyName)
    {
        super.onPropertyChanged(entity, propertyName);
        
        if(propertyName.equals(PROPERTY_POLYGONS))
        {
            updateShape = true;
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.pvd.EntityShape#update()
     */
    @Override
    public void update()
    {
        super.update();
    }

    
}
