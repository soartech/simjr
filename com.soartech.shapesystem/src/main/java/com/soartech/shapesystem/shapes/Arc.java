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

import com.soartech.shapesystem.CoordinateTransformer;
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
public class Arc extends Shape
{

    private Scalar radius;
    private Rotation startAngle;
    private Rotation endAngle;

    private double cachedRadius;
    
    /**
     * @param name
     * @param layer
     * @param pos
     * @param rot
     * @param style
     */
    public Arc(String name, String layer, Position pos, Rotation rot,
            ShapeStyle style, 
            Scalar radius, Rotation a1, Rotation a2)
    {
        super(name, layer, pos, rot, style);
        
        this.radius = radius;
        this.startAngle = a1;
        this.endAngle = a2;
    }
    

    /**
     * @return the endAngle
     */
    public Rotation getEndAngle()
    {
        return endAngle;
    }


    /**
     * @param endAngle the endAngle to set
     */
    public void setEndAngle(Rotation endAngle)
    {
        this.endAngle = endAngle;
    }


    /**
     * @return the radius
     */
    public Scalar getRadius()
    {
        return radius;
    }


    /**
     * @param radius the radius to set
     */
    public void setRadius(Scalar radius)
    {
        this.radius = radius;
    }


    /**
     * @return the startAngle
     */
    public Rotation getStartAngle()
    {
        return startAngle;
    }


    /**
     * @param startAngle the startAngle to set
     */
    public void setStartAngle(Rotation startAngle)
    {
        this.startAngle = startAngle;
    }


    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#calculateBase(com.soartech.shapesystem.ShapeSystem, com.soartech.shapesystem.CoordinateTransformer)
     */
    @Override
    protected void calculateBase(ShapeSystem system,
            CoordinateTransformer transformer)
    {
        points.add(new SimplePosition());
        if(startAngle.getType() != RotationType.WORLD ||
           endAngle.getType() != RotationType.WORLD)
        {
            system.errorInShape(this, "Arc angles cannot use 'relative_to' or 'point_at'.");
        }
        
        angles.add(SimpleRotation.fromDegrees(startAngle.getDegrees()));
        angles.add(SimpleRotation.fromDegrees(endAngle.getDegrees()));
        
        cachedRadius = transformer.scalarToPixels(radius);
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#draw(com.soartech.shapesystem.PrimitiveRendererFactory)
     */
    @Override
    public void draw(PrimitiveRendererFactory rendererFactory)
    {
        PrimitiveRenderer renderer = rendererFactory.createPrimitiveRenderer(style);
        
        renderer.drawArc(points.get(0), cachedRadius, angles.get(0), angles.get(1));

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
        // TODO: Constrain to arc and handle tolerance.
        return distance(x, y) < cachedRadius;
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
        // TODO: Constrain to arc
        return points.get(0).distance(x, y);
    }

}
