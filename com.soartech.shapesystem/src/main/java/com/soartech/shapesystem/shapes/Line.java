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

import com.soartech.math.LineSegmentDistance;
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
public class Line extends Shape
{

    private String start;
    private String end;
    
    private SimplePosition calculatedStart;
    private SimplePosition calculatedEnd;

    /**
     * @param name
     * @param layer
     * @param pos
     * @param rot
     * @param style
     */
    public Line(String name, String layer, ShapeStyle style,
                String start, String end)
    {
        super(name, layer, new Position(), Rotation.IDENTITY, style);
        
        this.start = start;
        this.end = end;
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#calculateBase(com.soartech.shapesystem.ShapeSystem, com.soartech.shapesystem.CoordinateTransformer)
     */
    @Override
    protected void calculateBase(ShapeSystem system,
            CoordinateTransformer transformer)
    {
        final Shape startShape = system.getShape(start);
        if(startShape != null)
        {
            calculatedStart = startShape.calculate(system, transformer).position;
        }
        else
        {
            calculatedStart = new SimplePosition();
        }
        
        final Shape endShape = system.getShape(end);
        if(endShape != null)
        {
            calculatedEnd = endShape.calculate(system, transformer).position;
        }
        else
        {
            calculatedEnd = new SimplePosition();
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#draw(com.soartech.shapesystem.PrimitiveRendererFactory)
     */
    @Override
    public void draw(PrimitiveRendererFactory rendererFactory)
    {
        final PrimitiveRenderer renderer = rendererFactory.createPrimitiveRenderer(style);
        
        renderer.drawLine(calculatedStart, calculatedEnd);
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#getBaseReferences()
     */
    @Override
    protected List<String> getBaseReferences()
    {
        final List<String> refs = new ArrayList<String>();
        refs.add(start);
        refs.add(end);
        return refs;
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#hitTest(double, double, double)
     */
    @Override
    public boolean hitTest(double x, double y, double tolerance)
    {
        if(!isVisible() || calculatedStart == null || calculatedEnd == null)
        {
            return false;
        }
        
        final Vector3 start = new Vector3(calculatedStart.x, calculatedStart.y, 0.0);
        final Vector3 end = new Vector3(calculatedEnd.x, calculatedEnd.y, 0.0);
        final Vector3 dir = end.subtract(start);
        
        final double d = LineSegmentDistance.toPoint(start, end, dir, new Vector3(x, y, 0.0));
        
        return d <= tolerance;
    }

}
