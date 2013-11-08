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
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.Shape;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.SimplePosition;
import com.soartech.shapesystem.SimpleRotation;

/**
 * @author ray
 */
public class Box extends Shape
{

    private Scalar width;
    private Scalar height;
    
    private SimplePosition center = new SimplePosition();
    
    private CoordinateTransformer lastTransform = null;

    /**
     * @param name
     * @param layer
     * @param pos
     * @param rot
     * @param style
     */
    public Box(String name, String layer, Position pos, Rotation rot,
            ShapeStyle style, Scalar width, Scalar height)
    {
        super(name, layer, pos, rot, style);

        this.width = width;
        this.height = height;
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#calculateBase(com.soartech.shapesystem.ShapeSystem, com.soartech.shapesystem.CoordinateTransformer)
     */
    @Override
    protected void calculateBase(ShapeSystem system,
            CoordinateTransformer transformer)
    {
        lastTransform = transformer;
        
        double halfWidth = transformer.scalarToPixels(width) / 2.0;
        double halfHeight = transformer.scalarToPixels(height) / 2.0;
        
        points.add(new SimplePosition(-halfWidth, -halfHeight));
        points.add(new SimplePosition( halfWidth, -halfHeight));
        points.add(new SimplePosition( halfWidth,  halfHeight));
        points.add(new SimplePosition(-halfWidth,  halfHeight));
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#draw(com.soartech.shapesystem.PrimitiveRendererFactory)
     */
    @Override
    public void draw(PrimitiveRendererFactory rendererFactory)
    {
        if (lastTransform == null)
        {
            // This should never be true but if it is, it's okay to skip a frame until we get it again
            return;
        }
        
        double halfWidth = lastTransform.scalarToPixels(width) / 2.0;
        double halfHeight = lastTransform.scalarToPixels(height) / 2.0;
        
        List<SimplePosition> bounds = new ArrayList<SimplePosition>();
        bounds.add(new SimplePosition(-halfWidth, -halfHeight));
        bounds.add(new SimplePosition( halfWidth, -halfHeight));
        bounds.add(new SimplePosition( halfWidth,  halfHeight));
        bounds.add(new SimplePosition(-halfWidth,  halfHeight));
        
        center.x = 0.0;
        center.y = 0.0;
        
        for (int i = 0;i < points.size();i++)
        {
            center.x += points.get(i).x;
            center.y += points.get(i).y;
        }
        
        center.x /= points.size();
        center.y /= points.size();
        
        for (SimplePosition p : bounds)
        {
            p.rotate(SimpleRotation.fromDegrees(-angles.get(0).inDegrees()));
            p.translate(center);
        }
        
        bounds.add(bounds.get(0));
        
        PrimitiveRenderer renderer = rendererFactory.createPrimitiveRenderer(style);
        renderer.drawPolygon(bounds);
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#getBaseReferences()
     */
    @Override
    protected List<String> getBaseReferences()
    {
        return new ArrayList<String>();
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
            hullPoints.add(new Vector3(p.x, p.y, 0.0));
        }
        Polygon p = Polygon.createConvexHull(hullPoints);
        return p.contains(new Vector3(x, y, 0.0));
    }

}
