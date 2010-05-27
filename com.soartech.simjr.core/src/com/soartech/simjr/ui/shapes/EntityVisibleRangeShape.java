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
 * Created on May 8, 2008
 */
package com.soartech.simjr.ui.shapes;

import java.awt.Color;

import com.soartech.shapesystem.FillStyle;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.RotationType;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.shapes.Arc;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.entities.EntityVisibleRange;

/**
 * Responsible for displaying the arc (and shadow) for an EntityVisibleRange object on an entity.
 * 
 * @author ray
 */
public class EntityVisibleRangeShape
{
    private EntityShape parent;
    private String property;
    private Color color;
    private Arc shape;
    private Arc shadow;
    private boolean needsUpdate = true;

    /**
     * @param parent The owning entity shape
     * @param property The property that the EntityVisibleRange object is in
     * @param color The color for the arc
     */
    public EntityVisibleRangeShape(EntityShape parent, String property, Color color)
    {
        this.parent = parent;
        this.property = property;
        this.color = color;
    }
    
    public void setNeedsUpdate()
    {
        this.needsUpdate = true;
    }

    public void update()
    {
        if(!needsUpdate)
        {
            return;
        }
        needsUpdate = false;

        EntityVisibleRange range = EntityVisibleRange.get(parent.getEntity(), property);
        Boolean visible = (Boolean) parent.getEntity().getProperty(property + ".visible");
        if(range == null || range.getVisibleRange() == 0.0 || (visible != null && !visible.booleanValue()))
        {
            parent.removeShape(shape);
            shape = null;
            parent.removeShape(shadow);
            shadow = null;
            return;
        }
        
        double angle = range.getVisibleAngle();
        Scalar radius = Scalar.createMeter(range.getVisibleRange());
        Rotation startAngle = Rotation.fromRadians(-angle / 2.0, RotationType.WORLD);
        Rotation endAngle = Rotation.fromRadians(angle / 2.0, RotationType.WORLD);

        if(shape == null)
        {
            ShapeStyle style = new ShapeStyle();
            style.setFillColor(color);
            style.setLineColor(color);
            style.setFillStyle(FillStyle.FILLED);
            style.setOpacity(0.3f);
            
            shape = new Arc(parent.getRootFrame().getName() + "." + property, 
                                        EntityConstants.LAYER_AREA,
                                        new Position(parent.getBodyFrame().getName()),
                                        Rotation.createRelative(parent.getBodyFrame().getName()),
                                        style,
                                        radius,
                                        startAngle,
                                        endAngle);
            parent.addShape(shape);
            
            if(parent.getShadowFrame() != null)
            {
                ShapeStyle shadowStyle = new ShapeStyle();
                shadowStyle.setFillColor(Color.GRAY);
                shadowStyle.setLineColor(Color.GRAY);
                shadowStyle.setFillStyle(FillStyle.FILLED);
                shadowStyle.setOpacity(0.2f);
                
                shadow = new Arc(parent.getRootFrame().getName() + "." + property + ".shadow", 
                        EntityConstants.LAYER_SHADOWS,
                        new Position(parent.getShadowFrame().getName()),
                        Rotation.createRelative(parent.getRootFrame().getName()),
                        shadowStyle,
                        radius,
                        startAngle,
                        endAngle);
                
                parent.addShape(shadow);
            }
        }
        else
        {
            shape.setRadius(radius);
            shape.setStartAngle(startAngle);
            shape.setEndAngle(endAngle);
            
            shadow.setRadius(radius);
            shadow.setStartAngle(startAngle);
            shadow.setEndAngle(endAngle);
        }
        
    }
    

}
