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

import com.google.common.collect.Lists;
import com.soartech.math.Polygon;
import com.soartech.math.Vector3;
import com.soartech.shapesystem.CoordinateTransformer;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.PrimitiveRenderer;
import com.soartech.shapesystem.PrimitiveRendererFactory;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.Shape;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.SimplePosition;
import com.soartech.shapesystem.TextStyle;

/**
 * @author ray
 */
public class Text extends Shape
{

    private String text;

    /**
     * @param name
     * @param layer
     * @param pos
     * @param rot
     * @param style
     */
    public Text(String name, String layer, Position pos, Rotation rot,
            TextStyle style, String text)
    {
        super(name, layer, pos, rot, style);
        
        this.text = text;
    }

    
    /**
     * @return the text
     */
    public String getText()
    {
        return text;
    }


    /**
     * @param text the text to set
     */
    public void setText(String text)
    {
        this.text = text;
    }


    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#calculateBase(com.soartech.shapesystem.ShapeSystem, com.soartech.shapesystem.CoordinateTransformer)
     */
    @Override
    protected void calculateBase(ShapeSystem system,
            CoordinateTransformer transformer)
    {
        points.add(new SimplePosition());

    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#draw(com.soartech.shapesystem.PrimitiveRendererFactory)
     */
    @Override
    public void draw(PrimitiveRendererFactory rendererFactory)
    {
        PrimitiveRenderer renderer = rendererFactory.createPrimitiveRenderer(style);
        
        renderer.drawText(points.get(0), text);
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#getBaseReferences()
     */
    @Override
    protected List<String> getBaseReferences()
    {
        return new ArrayList<String>();
    }
    
    /**
     * Approximate the bounding box of this text shape.
     */
    private List<SimplePosition> getPoints()
    {
        // TODO: Get actual font information for bounding box size.
        // This (incorrectly) assumes a fixed 5x5 character.
        final List<SimplePosition> pts = Lists.newArrayList();
        SimplePosition p = points.get(0);
        pts.add(new SimplePosition(p.x, p.y));
        pts.add(new SimplePosition(p.x + 5 * text.length(), p.y));
        pts.add(new SimplePosition(p.x, p.y - 5));
        pts.add(new SimplePosition(p.x, p.y));
        return pts;
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
        Polygon polygon = Util.createPlanarConvexHull(getPoints());
        // TODO: Use tolerance.
        return polygon.contains(new Vector3(x, y, 0.0));
    }

    /*
     * (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#distance(double, double)
     */
    @Override
    public double distance(double x, double y)
    {
        if(!isVisible() || points.isEmpty())
        {
            return Double.MAX_VALUE;
        }
        Polygon polygon = Util.createPlanarConvexHull(getPoints());
        // TODO: Use tolerance.
        return polygon.distance(new Vector3(x, y, 0.0));
    }

}
