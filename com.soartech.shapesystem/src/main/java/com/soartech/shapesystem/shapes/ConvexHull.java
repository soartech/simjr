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
 * Created on May 21, 2007
 */
package com.soartech.shapesystem.shapes;

import java.util.ArrayList;
import java.util.List;

import com.soartech.math.Polygon;
import com.soartech.math.Vector3;
import com.soartech.shapesystem.CoordinateTransformer;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.PrimitiveRenderer;
import com.soartech.shapesystem.PrimitiveRendererFactory;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.Shape;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.SimplePosition;

/**
 * @author ray
 */
public class ConvexHull extends Shape
{
    private List<String> pointNames;
    
    /**
     * @param name
     * @param layer
     * @param style
     * @param points
     */
    public ConvexHull(String name, String layer,
            ShapeStyle style, List<String> points)
    {
        super(name, layer, new Position(), Rotation.IDENTITY, style);
        
        this.pointNames = new ArrayList<String>(points);
    }

    public List<String> getPointNames()
    {
        return new ArrayList<String>(pointNames);
    }
    
    public void setPointNames(List<String> pointNames)
    {
        this.pointNames = new ArrayList<String>(pointNames);
    }
    
    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#calculateBase(com.soartech.shapesystem.ShapeSystem, com.soartech.shapesystem.CoordinateTransformer)
     */
    @Override
    protected void calculateBase(ShapeSystem system,
            CoordinateTransformer transformer)
    {
        if(pointNames.size() < 3)
        {
            system.errorInShape(this, "ConvexHull shape must contain at least 3 points.");
            return;
        }
        
        for(String name : pointNames)
        {
            Shape s = system.getShape(name);
            if(s != null)
            {
                points.add(new SimplePosition(s.calculate(system, transformer).position));
            }
            else
            {
                points.add(new SimplePosition());
            }
        }

    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#draw(com.soartech.shapesystem.PrimitiveRendererFactory)
     */
    @Override
    public void draw(PrimitiveRendererFactory rendererFactory)
    {
        List<Vector3> spatPoints = new ArrayList<Vector3>();
        for (SimplePosition p : points)
        {
        	spatPoints.add(new Vector3(p.x, -p.y, 0.0));
        }
        
        Polygon poly = Polygon.createConvexHull(spatPoints);
        
        List<SimplePosition> finalPoints = new ArrayList<SimplePosition>();
        for(Vector3 v : poly.getPoints())
        {
            finalPoints.add(new SimplePosition(v.x, v.y));
        }
        
        PrimitiveRenderer renderer = rendererFactory.createPrimitiveRenderer(style);
        
        renderer.drawPolygon(finalPoints);
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#getBaseReferences()
     */
    @Override
    protected List<String> getBaseReferences()
    {
        return new ArrayList<String>(pointNames);
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#hitTest(double, double, double)
     */
    @Override
    public boolean hitTest(double x, double y, double tolerance)
    {
        if(!isVisible() || points.isEmpty())
        {
            return false;
        }
        List<Vector3> hullPoints = new ArrayList<Vector3>();
        for(SimplePosition p : points)
        {
            hullPoints.add(new Vector3(p.x, -p.y, 0.0));
        }
        Polygon p = Polygon.createConvexHull(hullPoints);
        return p.contains(new Vector3(x, y, 0.0));
    }
}
