/*
 * Copyright (c) 2013, Soar Technology, Inc.
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
import com.soartech.shapesystem.shapes.Triangle;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityTools;

/**
 * @author ray
 */
public class RoleShape extends EntityShape
{
    public static final String NAME = "role";
    
    public static final EntityShapeFactory FACTORY = new Factory();

    private ShapeStyle style;
    
    public static class Factory extends AbstractEntityShapeFactory {

        public EntityShape create(Entity entity, ShapeSystem system)
        {
            return new RoleShape(entity, system);
        }        
        public String toString() { return NAME; }
    };

    /**
     * @param entity
     * @param system
     */
    public RoleShape(Entity entity, ShapeSystem system)
    {
        super(entity, system);
        
        String name = getRootFrame().getName();
        
        style = new ShapeStyle();
        style.setFillStyle(FillStyle.FILLED);
        
        style.setLineColor(Color.BLACK);
        updateForce();
        
        Triangle body = new Triangle(name + ".body", EntityConstants.LAYER_GROUND, 
                                     new Position(name),
                                     Rotation.IDENTITY,
                                     style, 
                                     Scalar.createPixel(10), 
                                     Scalar.createPixel(10));
                
        createLabel(5, 5, name);
        
        addHitableShape(body);
    }

    @Override
    protected void updateForce()
    {
        String force = EntityTools.getForce(getEntity());
        style.setFillColor(getForceColor(force));
    }
    
    
}
