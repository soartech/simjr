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
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;

/**
 * 3D Construct to represent a route.  Generally made up of rectangular tubes.  
 * The the bottom of the tubes lie on a plane defined by the minAltitude and 
 * are centered at the waypoints.  The top of the tubes is at the maxAltitude.
 * 
 * @author Dan Silverglate
 */
public class Route extends AbstractConstruct implements EntityPropertyListener, Separable
{
    List<?> points = null;
    double minAltitude = 0.0;
    double maxAltitude = 100.0;
    double width = 10.0;
    
    /**
     * Test constructor.
     * 
     * @param waypoints
     * @param width
     * @param height
     */
    public Route(double[][] waypoints, double width, double height)
    {
        this(waypoints, width, height, false);
    }
    
    /**
     * Test constructor.
     * 
     * @param waypoints
     * @param width
     * @param height
     * @param generateJoiners
     */
    public Route(double[][] waypoints, double width, double height, boolean generateJoiners)
    {
        super("Route");
        
        //IndexedFaceSetFactory ifsf = buildRoute(waypoints, width, height, generateJoiners);
        //setGeometry(ifsf.getIndexedFaceSet());
        buildRouteInParts(waypoints, width, height);
        
        Appearance ap = new Appearance();
        ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
        ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(0f, 0f, 1f));
        ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
        ap.setAttribute(CommonAttributes.TRANSPARENCY, .5);
        ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
        ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
        ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.black);
        
        setAppearance(ap);
    }
    
    
    /**
     * Main constructor.
     * 
     * @param sim
     */
    public Route(Simulation sim)
    {
        super("Route");
        
        this.sim = sim;
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
        width = ((Double)entity.getProperty(EntityConstants.PROPERTY_SHAPE_WIDTH_METERS)).doubleValue();
        Color color = (Color) EntityTools.getLineColor(entity, Color.yellow);
        setColor(color);
        rebuild();
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.editor.AbstractConstruct#testAndUpdateFromEntity(com.soartech.simjr.sim.Entity)
     */
    public void testAndUpdateFromEntity(Entity entity)
    {
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
    public void rebuild()
    {
        // test if the entity that has changed matches any of the points that make up this route
        if (points != null)
        {
            double[][] path = new double[points.size()][3];
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
                    path[index][1] = minAltitude;
                    path[index][2] = -vec.y;
                }
                
                ++index;
            }
            
            buildRouteInParts(path, width, maxAltitude-minAltitude);
        }
        
        Vector3 pos = entity.getPosition();
        setupLabel(entity.getName(), pos.x, maxAltitude, -pos.y);
    }

    /**
     * Use separate boxes in order to support bounding box sorting of 
     * transparent objects. 
     * 
     * @param waypoints
     * @param width
     * @param height
     */
    public void buildRouteInParts(double[][] waypoints, double width, double height)
    {
        int numBoxes = waypoints.length-1;
        //This check fixes a race condition where sometimes the route is populated before points are actually added.  ~ Josh Haley
        if (numBoxes < 0) return;
        
        while (this.getChildComponentCount() > 0)
        {
            this.removeChild(this.getChildComponent(0));
        }
        label = null;

        for (int i=0; i<numBoxes; i++)
        {
            IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
            double[][] vc = new double[8][3];
            int[][] fi = new int[4][4];
            
            double[] wp1 = waypoints[i];
            double[] wp2 = waypoints[i+1];

            double[] a = vc[0];
            double[] b = vc[1];
            double[] c = vc[2];
            double[] d = vc[3];
            double[] e = vc[4];
            double[] f = vc[5];
            double[] g = vc[6];
            double[] h = vc[7];
            
            double[] vec = new double[3];
            Rn.subtract(vec, wp2, wp1);
            
            double[] up = new double[3];
            Rn.setToValue(up, 0, -1, 0);
            
            double[] perp = new double[3];            
            double[] perpN = new double[3];
            Rn.crossProduct(perp, vec, up);
            Rn.normalize(perpN, perp);
            Rn.times(perp, -width/2.0, perpN);
            Rn.add(a, wp1, perp);
            Rn.times(perp, width/2.0, perpN);
            Rn.add(b, wp1, perp);
            
            Rn.crossProduct(perp, vec, perpN);
            Rn.normalize(perpN, perp);
            Rn.times(perp, height, perpN);
            Rn.add(c, b, perp);
            Rn.add(d, a, perp);
            
            Rn.add(e, a, vec);
            Rn.add(f, b, vec);
            Rn.add(g, c, vec);
            Rn.add(h, d, vec);
            
            fi[0][0] = 1;
            fi[0][1] = 5;
            fi[0][2] = 6;
            fi[0][3] = 2;
            
            fi[1][0] = 2;
            fi[1][1] = 6;
            fi[1][2] = 7;
            fi[1][3] = 3;
            
            fi[2][0] = 3;
            fi[2][1] = 7;
            fi[2][2] = 4;
            fi[2][3] = 0;
            
            fi[3][0] = 0;
            fi[3][1] = 4;
            fi[3][2] = 5;
            fi[3][3] = 1;
            
            ifsf.setVertexCount(vc.length);
            ifsf.setVertexCoordinates(vc);
            ifsf.setFaceCount(fi.length);
            ifsf.setFaceIndices(fi);
            ifsf.setGenerateFaceNormals(true);
            ifsf.setGenerateEdgesFromFaces(true);
            ifsf.update();
            
            SceneGraphComponent segment = new SceneGraphComponent("Segment_"+i);
            segment.setGeometry(ifsf.getIndexedFaceSet());
            this.addChild(segment);
        }
    }

    /**
     * No longer used in favor of buildRouteInParts
     * 
     * @param waypoints
     * @param width
     * @param height
     * @param generateJoiners
     * @return
     */
    public IndexedFaceSetFactory buildRoute(double[][] waypoints, double width, double height, boolean generateJoiners)
    {   
        int numBoxes = waypoints.length-1;
        //This check fixes a race condition where sometimes the route is populated before points are actually added.  ~ Josh Haley
        if(numBoxes <0)
            return null;
        int numJoiners = 0;
        if (generateJoiners)
        {
            numJoiners = numBoxes - 1;
        }
        
        
        IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
        
        double[][] vc = new double[(numBoxes) * 8][3];
        int[][] fi = new int[(numBoxes+numJoiners) * 4][4];
        
        for (int i=0; i<numBoxes; i++)
        {     
            double[] wp1 = waypoints[i];
            double[] wp2 = waypoints[i+1];

            int vertextIndex = 8 * i;
            
            double[] a = vc[vertextIndex];
            double[] b = vc[vertextIndex + 1];
            double[] c = vc[vertextIndex + 2];
            double[] d = vc[vertextIndex + 3];
            double[] e = vc[vertextIndex + 4];
            double[] f = vc[vertextIndex + 5];
            double[] g = vc[vertextIndex + 6];
            double[] h = vc[vertextIndex + 7];
            
            double[] vec = new double[3];
            Rn.subtract(vec, wp2, wp1);
            
            double[] up = new double[3];
            Rn.setToValue(up, 0, -1, 0);
            
            double[] perp = new double[3];            
            double[] perpN = new double[3];
            Rn.crossProduct(perp, vec, up);
            Rn.normalize(perpN, perp);
            Rn.times(perp, -width/2.0, perpN);
            Rn.add(a, wp1, perp);
            Rn.times(perp, width/2.0, perpN);
            Rn.add(b, wp1, perp);
            
            Rn.crossProduct(perp, vec, perpN);
            Rn.normalize(perpN, perp);
            Rn.times(perp, height, perpN);
            Rn.add(c, b, perp);
            Rn.add(d, a, perp);
            
            Rn.add(e, a, vec);
            Rn.add(f, b, vec);
            Rn.add(g, c, vec);
            Rn.add(h, d, vec);
           
            int faceIndex = 4 * i;
            
            fi[faceIndex][0] = 1 + vertextIndex;
            fi[faceIndex][1] = 5 + vertextIndex;
            fi[faceIndex][2] = 6 + vertextIndex;
            fi[faceIndex][3] = 2 + vertextIndex;
            
            fi[faceIndex + 1][0] = 2 + vertextIndex;
            fi[faceIndex + 1][1] = 6 + vertextIndex;
            fi[faceIndex + 1][2] = 7 + vertextIndex;
            fi[faceIndex + 1][3] = 3 + vertextIndex;
            
            fi[faceIndex + 2][0] = 3 + vertextIndex;
            fi[faceIndex + 2][1] = 7 + vertextIndex;
            fi[faceIndex + 2][2] = 4 + vertextIndex;
            fi[faceIndex + 2][3] = vertextIndex;
            
            fi[faceIndex + 3][0] = vertextIndex;
            fi[faceIndex + 3][1] = 4 + vertextIndex;
            fi[faceIndex + 3][2] = 5 + vertextIndex;
            fi[faceIndex + 3][3] = 1 + vertextIndex;
        }
        
        if (generateJoiners)
        {
            for (int i=0; i<numJoiners; i++)
            {
                int vertextIndex = 8 * i;
                int faceIndex = 4 * (i + numBoxes);
            
                fi[faceIndex][0] = 5 + vertextIndex;
                fi[faceIndex][1] = 9 + vertextIndex;
                fi[faceIndex][2] = 10 + vertextIndex;
                fi[faceIndex][3] = 6 + vertextIndex;
                
                fi[faceIndex + 1][0] = 6 + vertextIndex;
                fi[faceIndex + 1][1] = 10 + vertextIndex;
                fi[faceIndex + 1][2] = 11 + vertextIndex;
                fi[faceIndex + 1][3] = 7 + vertextIndex;
                
                fi[faceIndex + 2][0] = 7 + vertextIndex;
                fi[faceIndex + 2][1] = 11 + vertextIndex;
                fi[faceIndex + 2][2] = 8 + vertextIndex;
                fi[faceIndex + 2][3] = 4 + vertextIndex;
                
                fi[faceIndex + 3][0] = 4 + vertextIndex;
                fi[faceIndex + 3][1] = 8 + vertextIndex;
                fi[faceIndex + 3][2] = 9 + vertextIndex;
                fi[faceIndex + 3][3] = 5 + vertextIndex;
            }
        }
        
        ifsf.setVertexCount(vc.length);
        ifsf.setVertexCoordinates(vc);
        ifsf.setFaceCount(fi.length);
        ifsf.setFaceIndices(fi);
        ifsf.setGenerateFaceNormals(true);
        ifsf.setGenerateEdgesFromFaces(true);
        ifsf.update();
        
        return ifsf;
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
        else if (propertyName.equals(EntityConstants.PROPERTY_SHAPE_WIDTH_METERS))
        {
            width = ((Double)entity.getProperty(EntityConstants.PROPERTY_SHAPE_WIDTH_METERS)).doubleValue();
            rebuild();
        }
        else if (propertyName.equals(EntityConstants.PROPERTY_POINTS))
        {
            points = (List<?>)entity.getProperty(EntityConstants.PROPERTY_POINTS);
            rebuild();
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.editor.Separable#getSubComponents()
     */
    public List<SceneGraphComponent> getSubComponents()
    {
        return this.getChildComponents();
    }
}
