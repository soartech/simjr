/*
 * Copyright (c) 2012, Soar Technology, Inc.
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
 */
package com.soartech.simjr.view3D;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import com.soartech.math.Vector3;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPropertyListener;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.Simulation;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;

/**
 * 3D Construct to represent an area.  Implemented as an extruded polygon.
 * The polygon may be convex or concave.
 * 
 * @author Dan Silverglate
 */
public class Area extends ExtrudedPolygon implements EntityPropertyListener
{
    List<?> points = null;
    
    public Area(String name)
    {
        super(name);
    }
   
    /**
     * Test constructor.
     * 
     * @param path
     * @param minHeight
     * @param maxHeight
     */
    public Area(double[][] path, double minHeight, double maxHeight)
    {
        super("Area");
        
        path = cleanPath(path);
        IndexedFaceSetFactory sides = buildSides(path, minHeight, maxHeight);

        setGeometry(sides.getIndexedFaceSet());
        
        Appearance ap = new Appearance();
        ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
        ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(1f, 0f, 0f));
        ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
        ap.setAttribute(CommonAttributes.TRANSPARENCY, .5);
        ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
        ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
        ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.black);
        
        setAppearance(ap);
        
        SceneGraphComponent endCapComp = new SceneGraphComponent("EndCaps");
        
        IndexedFaceSetFactory endCaps = buildEndCapFaces(path, minHeight, maxHeight);

        endCapComp.setGeometry(endCaps.getIndexedFaceSet());
        
        ap = new Appearance();
        ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
        ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(1f, 0f, 0f));
        ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
        ap.setAttribute(CommonAttributes.TRANSPARENCY, .5);
        ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
               
        endCapComp.setAppearance(ap);
        
        addChild(endCapComp);
    }
    
    // not used for now until point property listener is fixed
    public Area(Entity entity, Simulation sim)
    {
        this(sim);
        entity.addPropertyListener(this);
    }
    
    /**
     * Main constructor.
     * 
     * @param sim
     */
    public Area(Simulation sim)
    {
        super("Area");
        
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
        ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
        ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
        ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.black);
        setAppearance(ap);
        
        ap = new Appearance();
        ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
        ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, color);
        ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
        ap.setAttribute(CommonAttributes.TRANSPARENCY, .5);
        ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
        endCapComp.setAppearance(ap);
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.editor.AbstractConstruct#updateFromEntity(com.soartech.simjr.sim.Entity)
     */
    public void updateFromEntity(Entity entity)
    {
        if (this.entity != entity)
        {
            if (entity != null)
            {
                entity.removePropertyListener(this);
            }
            this.entity = entity;
            entity.addPropertyListener(this);
        }
        points = (List<?>)entity.getProperty(EntityConstants.PROPERTY_POINTS);
        minAltitude = ((Double)entity.getProperty(EntityConstants.PROPERTY_MINALTITUDE)).doubleValue();
        maxAltitude = ((Double)entity.getProperty(EntityConstants.PROPERTY_MAXALTITUDE)).doubleValue();
        final Color color =(Color) EntityTools.getFillColor(entity, Color.LIGHT_GRAY);
        setColor(color);
        rebuild();
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.editor.AbstractConstruct#testAndUpdateFromEntity(com.soartech.simjr.sim.Entity)
     */
    public void testAndUpdateFromEntity(Entity entity)
    {
        // test if the entity that has changed matches any of the points that make up this area
        if (points != null)
        {
            Iterator<?> i = points.iterator();
            while (i.hasNext())
            {
                Object obj = i.next();
                if (obj instanceof String)
                {
                    Entity e = sim.getEntity(obj.toString());
                    if (entity == e) {
                        rebuild();
                        return;
                    }
                }
            }
            //System.out.println("NO MATCH!");
        }
    }
    
    /**
     * Called to rebuild the 3D construct whenever a field has changed. 
     */
    private void rebuild()
    {
        if (points != null)
        {
            double[][] path = new double[points.size()][2];
            Iterator<?> i = points.iterator();
            int index = 0;
            while (i.hasNext())
            {
                Object obj = i.next();

                if (obj instanceof String)
                {
                    Entity e = sim.getEntity(obj.toString());
                    Vector3 vec = e.getPosition();
                    path[index][0] = vec.x;
                    path[index][1] = -vec.y;
                }
                
                ++index;
            }
            
            
            path = cleanPath(path);
            
            //Fix race condition where sometimes the area event fires as created when the points have not yet been added ~Josh Haley
            if(path.length == 0)
                return;
            
            IndexedFaceSetFactory sides = buildSides(path, minAltitude, maxAltitude);
            setGeometry(sides.getIndexedFaceSet());
            IndexedFaceSetFactory endCaps = buildEndCapFaces(path, minAltitude, maxAltitude);
            endCapComp.setGeometry(endCaps.getIndexedFaceSet());
        }
        
        Vector3 pos = entity.getPosition();
        setupLabel(entity.getName(), pos.x, maxAltitude, -pos.y);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.EntityPropertyListener#onPropertyChanged(com.soartech.simjr.sim.Entity, java.lang.String)
     */
    public void onPropertyChanged(Entity entity, String propertyName)
    {
        //System.out.println("onPropertyChanged("+entity.getClass().getName()+", "+propertyName+")");

        if (propertyName.equals(EntityConstants.PROPERTY_MINALTITUDE))
        {
            minAltitude = ((Double)entity.getProperty(EntityConstants.PROPERTY_MINALTITUDE)).doubleValue();
            rebuild();
        }
        else if (propertyName.equals(EntityConstants.PROPERTY_MAXALTITUDE))
        {
            maxAltitude = ((Double)entity.getProperty(EntityConstants.PROPERTY_MAXALTITUDE)).doubleValue();
            rebuild();
        }
        else if (propertyName.equals(EntityConstants.PROPERTY_POINTS))
        {
            points = (List<?>)entity.getProperty(EntityConstants.PROPERTY_POINTS);
            rebuild();
        }
    }
}
