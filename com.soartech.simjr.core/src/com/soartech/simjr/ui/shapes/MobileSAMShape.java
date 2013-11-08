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
import com.soartech.shapesystem.shapes.Frame;
import com.soartech.shapesystem.shapes.Line;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityTools;

/**
 * @author dunham
 */
public class MobileSAMShape extends EntityShape
{
    public static final String NAME = "mobile-sam";
    
    public static final EntityShapeFactory FACTORY = new Factory();
    public static class Factory extends AbstractEntityShapeFactory {

        public EntityShape create(Entity entity, ShapeSystem system)
        {
            return new MobileSAMShape(entity, system);
        }        
        public String toString() { return NAME; }
    };

    private final ShapeStyle style = new ShapeStyle();
    /**
     * @param entity
     * @param system
     */
    public MobileSAMShape(Entity entity, ShapeSystem system)
    {
        super(entity, system);
        
        String name = getRootFrame().getName();
        
        style.setFillStyle(FillStyle.FILLED);
        updateForce();
        style.setLineColor(Color.DARK_GRAY);
        
        Box body = new Box(name + ".body", EntityConstants.LAYER_GROUND, 
                                new Position(name),
                                Rotation.createRelative(name),
                                style, 
                                Scalar.createPixel(19),
                                Scalar.createPixel(10));
        Box cab = new Box(name + ".cab", EntityConstants.LAYER_GROUND, 
                Position.createRelativePixel(11,0,name),
                Rotation.createRelative(name),
                style, 
                Scalar.createPixel(5),
                Scalar.createPixel(10));
        
        
        //This is probably a crappy way of drawing these missiles.
        // (three seperate lines all with seperate start/end pts)
        // It would be cool if we could just use the MissileShape, but I do not know how.
        ShapeStyle missileStyle = new ShapeStyle();
        missileStyle.setLineColor(Color.WHITE);
        missileStyle.setLineThickness(Scalar.createPixel(2.0));
        //Missile1
        Frame start1 = new Frame(name + ".m1.start", EntityConstants.LAYER_GROUND,
                Position.createRelativePixel(-7, -3, name), Rotation.IDENTITY);
        Frame end1 = new Frame(name + ".m1.end", EntityConstants.LAYER_GROUND,
                Position.createRelativePixel(6, -3, name), Rotation.IDENTITY);
        Line m1 = new Line(name + ".m1.line", EntityConstants.LAYER_GROUND, 
                missileStyle, start1.getName(), end1.getName());
        //Missile2
        Frame start2 = new Frame(name + ".m2.start", EntityConstants.LAYER_GROUND,
                Position.createRelativePixel(-7, 0, name), Rotation.IDENTITY);
        Frame end2 = new Frame(name + ".m2.end", EntityConstants.LAYER_GROUND,
                Position.createRelativePixel(6, 0, name), Rotation.IDENTITY);
        Line m2 = new Line(name + ".m2.line", EntityConstants.LAYER_GROUND, 
                missileStyle, start2.getName(), end2.getName());
        //Missile3
        Frame start3 = new Frame(name + ".m3.start", EntityConstants.LAYER_GROUND,
                Position.createRelativePixel(-7, 3, name), Rotation.IDENTITY);
        Frame end3 = new Frame(name + ".m3.end", EntityConstants.LAYER_GROUND,
                Position.createRelativePixel(6, 3, name), Rotation.IDENTITY);
        Line m3 = new Line(name + ".m3.line", EntityConstants.LAYER_GROUND, 
                missileStyle, start3.getName(), end3.getName());
        
        createLabel(16, 16, name);
        
        addHitableShape(body);
        addShape(cab);
        addShape(start1);
        addShape(start2);
        addShape(start3);
        addShape(end1);
        addShape(end2);
        addShape(end3);
        addShape(m1);
        addShape(m2);
        addShape(m3);
    }
    @Override
    protected void updateForce()
    {
        String force = EntityTools.getForce(getEntity());
        style.setFillColor(getForceColor(force));
    }

}
