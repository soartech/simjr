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

import com.soartech.shapesystem.FillStyle;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.shapes.Box;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityTools;

/**
 * @author glenn
 */
public class TruckShape extends EntityShape
{
    public static final String NAME = "truck";
    private Box cab, body;
    private Entity parent;
    private final int TRUCK_WIDTH = 10;
    public static final EntityShapeFactory FACTORY = new Factory();
    public static class Factory extends AbstractEntityShapeFactory {
    
        public EntityShape create(Entity entity, ShapeSystem system)
        {
            return new TruckShape(entity, system);
        }        
        public String toString() { return NAME; }
    };

    private final ShapeStyle style = new ShapeStyle();
    /**
     * @param entity
     * @param system
     */
    public TruckShape(Entity entity, ShapeSystem system)
    {
        super(entity, system);
        parent = entity;
        String name = getRootFrame().getName();
        style.setFillStyle(FillStyle.FILLED);
        updateForce();
        style.setLineColor(Color.DARK_GRAY);
        
        this.body = createBodyShape(name + ".body", style);
        
        this.cab = new Box(name + ".cab", EntityConstants.LAYER_GROUND, 
                Position.createRelativePixel(TRUCK_WIDTH,0,name ),
                Rotation.createRelative(name),
                style, 
                Scalar.createPixel(5),
                Scalar.createPixel(TRUCK_WIDTH));
                
        createLabel(16, 16, name);
        
        addHitableShape(body);
        addShape(cab);
    }
    
    @Override
    public Box createBodyShape(String shapeId, ShapeStyle shapeStyle)
    {
    	Box body = new Box(shapeId, EntityConstants.LAYER_GROUND, 
    			new Position(getRootFrame().getName()),
                Rotation.createRelative(getRootFrame().getName()),
                shapeStyle, 
                Scalar.createPixel(15),
                Scalar.createPixel(TRUCK_WIDTH));
    	
    	return body;
    }
    
    @Override
    protected void updateForce()
    {
        String force = EntityTools.getForce(getEntity());
        style.setFillColor(getForceColor(force));
    }
    
    @Override
    public void update()
    {
       super.update();
       //hack to fix the error with rotating truck shape
       //TODO: properly fix the cab rotation
       double heading = parent.getHeading() *2;
       cab.setPosition(Position.createRelativePixel(TRUCK_WIDTH * Math.cos(heading), TRUCK_WIDTH * Math.sin(heading),getRootFrame().getName()));
    }
}
