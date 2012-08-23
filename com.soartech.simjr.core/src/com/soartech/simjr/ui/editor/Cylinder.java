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
 * Created on June 27, 2011
 */
package com.soartech.simjr.ui.editor;

import java.awt.Color;

import com.soartech.math.Vector3;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPropertyListener;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.Simulation;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.shader.CommonAttributes;

/**
 * @author Dan Silverglate
 */
public class Cylinder extends ExtrudedPolygon implements EntityPropertyListener
{
    double diameter = 100.0;
    
    public Cylinder(Simulation sim)
    {
        super("Cylinder");
        
        this.sim = sim;
        
        this.endCapComp = new SceneGraphComponent("EndCaps");
        addChild(endCapComp);
    }
    
    private void setColor(Color color)
    {
        Appearance ap = new Appearance();
        ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
        ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, color);
        ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
        ap.setAttribute(CommonAttributes.TRANSPARENCY, .5);
        ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
        setAppearance(ap);
        
        ap = new Appearance();
        ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
        ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, color);
        ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
        ap.setAttribute(CommonAttributes.TRANSPARENCY, .5);
        ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
        ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
        ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.black);
        endCapComp.setAppearance(ap);
    }
   
    private void rebuild()
    {
        double[][] path = buildCircle();
        IndexedFaceSetFactory sides = buildSides(path, minAltitude, maxAltitude, true);
        setGeometry(sides.getIndexedFaceSet());
        IndexedFaceSetFactory endCaps = buildEndCapFaces(path, minAltitude, maxAltitude, false);
        endCapComp.setGeometry(endCaps.getIndexedFaceSet());
    }
    
    public double[][] buildCircle()
    {
        int n = 32;
        double[][] path = new double[n][2];
        double angle = 0;
        double delta = (Math.PI*2)/n;
        double radius = diameter/2;
        for (int i=0; i<n; i++) {
            angle = i*delta;
            path[i][0] = radius*Math.cos(angle);
            path[i][1] = -radius*Math.sin(angle);                  
        }
        
        return path;
    }
    public void updateFromEntity(Entity entity)
    {
        if (this.entity != entity)
        {
            this.entity = entity;
            entity.addPropertyListener(this);
        }
        Vector3 pos = entity.getPosition();
        MatrixBuilder.euclidean().translate(pos.x, 0, -pos.y).assignTo(this);
        diameter = ((Double)entity.getProperty(EntityConstants.PROPERTY_SHAPE_WIDTH_METERS)).doubleValue();;
        minAltitude = ((Double)entity.getProperty(EntityConstants.PROPERTY_MINALTITUDE)).doubleValue();
        maxAltitude = ((Double)entity.getProperty(EntityConstants.PROPERTY_MAXALTITUDE)).doubleValue();
        final Color color =(Color) EntityTools.getFillColor(entity, Color.LIGHT_GRAY);
        setColor(color);
        rebuild();
    }
       
    public void testAndUpdateFromEntity(Entity entity) { }
    
    public void onPropertyChanged(Entity entity, String propertyName)
    {
        System.out.println("onPropertyChanged("+entity.getClass().getName()+", "+propertyName+")");
        
        if (propertyName.equals(EntityConstants.PROPERTY_SHAPE_WIDTH_METERS))
        {
            diameter = ((Double)entity.getProperty(EntityConstants.PROPERTY_SHAPE_WIDTH_METERS)).doubleValue();
            rebuild();
        }
        else if (propertyName.equals(EntityConstants.PROPERTY_MINALTITUDE))
        {
            minAltitude = ((Double)entity.getProperty(EntityConstants.PROPERTY_MINALTITUDE)).doubleValue();
            rebuild();
        }
        else if (propertyName.equals(EntityConstants.PROPERTY_MAXALTITUDE))
        {
            maxAltitude = ((Double)entity.getProperty(EntityConstants.PROPERTY_MAXALTITUDE)).doubleValue();
            rebuild();
        }
    }
}
