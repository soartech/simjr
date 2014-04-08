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
import com.soartech.shapesystem.FillStyle;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.PrimitiveRenderer;
import com.soartech.shapesystem.PrimitiveRendererFactory;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.RotationType;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.Shape;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.SimplePosition;
import com.soartech.shapesystem.SimpleRotation;

/**
 * @author ray
 */
public class Capsule extends Shape
{

    private String start;
    private String end;
    private Scalar radius;
    
    private SimplePosition calculatedStart;
    private SimplePosition calculatedEnd;
    private double cachedRadius;

    /**
     * @param name
     * @param layer
     * @param style
     * @param start
     * @param end
     * @param radius
     */
    public Capsule(String name, String layer, ShapeStyle style,
                   String start, String end, Scalar radius)
    {
        super(name, layer, new Position(), Rotation.IDENTITY, style);
        
        this.start = start;
        this.end = end;
        this.radius = radius;
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#calculateBase(com.soartech.shapesystem.ShapeSystem, com.soartech.shapesystem.CoordinateTransformer)
     */
    @Override
    protected void calculateBase(ShapeSystem system,
            CoordinateTransformer transformer)
    {
        Shape startShape = system.getShape(start);
        if(startShape != null)
        {
            calculatedStart = startShape.calculate(system, transformer).position;
        }
        else
        {
            calculatedStart = new SimplePosition();
        }
        
        Shape endShape = system.getShape(end);
        if(endShape != null)
        {
            calculatedEnd = endShape.calculate(system, transformer).position;
        }
        else
        {
            calculatedEnd = new SimplePosition();
        }
        
        cachedRadius = transformer.scalarToPixels(radius);
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#draw(com.soartech.shapesystem.PrimitiveRendererFactory)
     */
    @Override
    public void draw(PrimitiveRendererFactory rendererFactory)
    {
        // First we calculate the angle between the two points
        //
        // The ordering in y is swapped because positive y is "down" in screen coordinates
        double del_x =  (calculatedEnd.x - calculatedStart.x);
        double del_y = -(calculatedEnd.y - calculatedStart.y);
        SimpleRotation angle = SimpleRotation.fromDegrees(Rotation.fromRadians(Math.atan2(del_y, del_x), RotationType.WORLD).getDegrees());

        // Calculate corner points of inner-box part of capsule
        SimpleRotation perp_top = SimpleRotation.fromDegrees(angle.inDegrees() + 90.0);
        SimpleRotation perp_bot = SimpleRotation.fromDegrees(angle.inDegrees() - 90.0);

        SimplePosition top_offset = new SimplePosition(cachedRadius, 0);
        SimplePosition bot_offset = new SimplePosition(cachedRadius, 0);
        top_offset.rotate(perp_top);
        bot_offset.rotate(perp_bot);
        
        //      -1---------------2-
        //    /                     \
        //   |   s               e   |
        //    \                     /
        //      -3---------------4-
        
        // "Top" points
        SimplePosition p1 = new SimplePosition(calculatedStart);
        SimplePosition p2 = new SimplePosition(calculatedEnd);
        p1.translate(top_offset);
        p2.translate(top_offset);
        
        // "Bottom" points
        SimplePosition p3 = new SimplePosition(calculatedStart);
        SimplePosition p4 = new SimplePosition(calculatedEnd);
        p3.translate(bot_offset);
        p4.translate(bot_offset);
        
        PrimitiveRenderer renderer = rendererFactory.createPrimitiveRenderer(style);

        // draw the center rectangle
        if (style.getFillStyle() == FillStyle.FILLED)
        {
           List<SimplePosition> bounds = new ArrayList<SimplePosition>();
           bounds.add(p1);
           bounds.add(p2);
           bounds.add(p4);
           bounds.add(p3);
           bounds.add(p1);

           renderer.drawPolygon(bounds);
        }
        else
        {
           renderer.drawLine(p1, p2);
           renderer.drawLine(p3, p4);
        }

        renderer.drawArc(calculatedStart, cachedRadius, 
                SimpleRotation.fromDegrees(perp_top.inDegrees()+180), 
                SimpleRotation.fromDegrees(perp_bot.inDegrees()+180));
        renderer.drawArc(calculatedEnd, cachedRadius, 
                SimpleRotation.fromDegrees(perp_top.inDegrees()),     
                SimpleRotation.fromDegrees(perp_bot.inDegrees()));
        
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#getBaseReferences()
     */
    @Override
    protected List<String> getBaseReferences()
    {
        List<String> refs = new ArrayList<String>();
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
        // TODO: Use tolerance.
        return distance(x, y) <= cachedRadius;
    }

    /*
     * (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#distance(double, double)
     */
    @Override
    public double distance(double x, double y)
    {
        if(!isVisible() || calculatedStart == null || calculatedEnd == null)
        {
            return Double.MAX_VALUE;
        }
        
        Vector3 start = new Vector3(calculatedStart.x, calculatedStart.y, 0.0);
        Vector3 end = new Vector3(calculatedEnd.x, calculatedEnd.y, 0.0);
        Vector3 dir = end.subtract(start).normalized();
        
        return LineSegmentDistance.toPoint(start, end, dir, new Vector3(x, y, 0.0));
    }

}
