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
 * Created on Apr 15, 2009
 */
package com.soartech.simjr.ui.editor;

import java.awt.Color;

import com.soartech.math.Vector3;
import com.soartech.shapesystem.FillStyle;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.RotationType;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.shapes.Cross;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPrototypes;
import com.soartech.simjr.sim.entities.AbstractEntity;
import com.soartech.simjr.ui.shapes.AbstractEntityShapeFactory;
import com.soartech.simjr.ui.shapes.EntityShape;
import com.soartech.simjr.ui.shapes.EntityShapeFactory;

/**
 * @author ray
 */
public class TerrainImageEntity extends AbstractEntity
{
    private final TerrainImageListener mapPanel;
    
    public TerrainImageEntity(String name, TerrainImageListener mapPanel)
    {
        super(name, EntityPrototypes.NULL);
        
        this.mapPanel = mapPanel;
        
        setProperty("shape", Shape.FACTORY);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.entities.AbstractEntity#setPosition(com.soartech.spatr.math.Vector3)
     */
    @Override
    public void setPosition(Vector3 position)
    {
        super.setPosition(position);
        mapPanel.terrainImageMoved(this);
    }
    
    public void setPositionQuietly(Vector3 position)
    {
        super.setPosition(position);
        mapPanel.terrainImageMoved(this);
    }

    static class Shape extends EntityShape
    {
        public static final String NAME = "TerrainImageEntity";
        
        public static final EntityShapeFactory FACTORY = new Factory();
        public static class Factory extends AbstractEntityShapeFactory {

            public EntityShape create(Entity entity, ShapeSystem system)
            {
                return new Shape(entity, system);
            }        
            public String toString() { return NAME; }
        };
        
        
        /**
         * @param entity
         * @param system
         */
        public Shape(Entity entity, ShapeSystem system)
        {
            super(entity, system);
            
            String name = getRootFrame().getName();
            
            ShapeStyle style = new ShapeStyle();
            style.setFillStyle(FillStyle.FILLED);
            style.setFillColor(Color.YELLOW);
            style.setLineColor(Color.BLUE);
            style.setLineThickness(Scalar.createPixel(5.0));
            style.setOpacity(0.7f);
            
            Cross cross = new Cross(name + ".body", EntityConstants.LAYER_WAYPOINT, 
                                    new Position(name),
                                    Rotation.fromDegrees(45.0, RotationType.WORLD),
                                    style, 
                                    Scalar.createPixel(16),
                                    Scalar.createPixel(16));
                    
            createLabel(10, 10, name).setText("Terrain Image");
            
            
            addHitableShape(cross);
        }

    }

}
