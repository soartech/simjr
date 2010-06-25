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
 * Created on May 17, 2007
 */
package com.soartech.shapesystem;

import java.awt.Color;

/**
 * @author ray
 */
public class ShapeStyle
{
    private LineStyle lineStyle;
    private CapStyle capStyle;
    private JoinStyle joinStyle;
    private FillStyle fillStyle;
    private Scalar lineThickness;
    private Color lineColor;
    private Color fillColor;
    private float opacity;
    
    public ShapeStyle()
    {
        this.lineStyle = LineStyle.SOLID;
        this.capStyle = CapStyle.BUTT;
        this.joinStyle = JoinStyle.ROUND;
        this.fillStyle = FillStyle.NONE;
        this.lineThickness = new Scalar(2.0, ScalarUnit.Pixels);
        this.lineColor = Color.BLACK;
        this.fillColor = Color.BLACK;
        this.opacity = 1.0f;
    }
    
    protected ShapeStyle(ShapeStyle other)
    {
        this.lineStyle = other.lineStyle;
        this.capStyle = other.capStyle;
        this.joinStyle = other.joinStyle;
        this.fillStyle = other.fillStyle;
        this.lineThickness = other.lineThickness;
        this.lineColor = other.lineColor;
        this.fillColor = other.fillColor;
        this.opacity = other.opacity;
    }
    
    public ShapeStyle copy()
    {
        return new ShapeStyle(this);
    }
    /**
     * @return the lineStyle
     */
    public LineStyle getLineStyle()
    {
        return lineStyle;
    }
    /**
     * @param lineStyle the lineStyle to set
     */
    public ShapeStyle setLineStyle(LineStyle lineStyle)
    {
        this.lineStyle = lineStyle;
        return this;
    }
    /**
     * @return the capping style used for drawing line endpoints
     */
    public CapStyle getCapStyle()
    {
        return capStyle;
    }
    /**
     * @param capStyle the capping style used to draw line endpoints
     */
    public ShapeStyle setCapStyle(CapStyle capStyle)
    {
        this.capStyle = capStyle;
        return this;
    }
    /**
     * @return the joining style used for drawing connections between line segments
     */
    public JoinStyle getJoinStyle()
    {
        return joinStyle;
    }
    /**
     * @param joinStyle the joining style used to draw connections between line segments
     */
    public ShapeStyle setJoinStyle(JoinStyle joinStyle)
    {
        this.joinStyle = joinStyle;
        return this;
    }
    /**
     * @return the fillStyle
     */
    public FillStyle getFillStyle()
    {
        return fillStyle;
    }
    /**
     * @param fillStyle the fillStyle to set
     */
    public ShapeStyle setFillStyle(FillStyle fillStyle)
    {
        this.fillStyle = fillStyle;
        return this;
    }
    /**
     * @return the lineThickness
     */
    public Scalar getLineThickness()
    {
        return lineThickness;
    }
    /**
     * @param lineThickness the lineThickness to set
     */
    public ShapeStyle setLineThickness(Scalar lineThickness)
    {
        this.lineThickness = lineThickness;
        return this;
    }
    /**
     * @return the lineColor
     */
    public Color getLineColor()
    {
        return lineColor;
    }
    /**
     * @param lineColor the lineColor to set
     */
    public ShapeStyle setLineColor(Color lineColor)
    {
        this.lineColor = lineColor;
        return this;
    }
    /**
     * @return the fillColor
     */
    public Color getFillColor()
    {
        return fillColor;
    }
    /**
     * @param fillColor the fillColor to set
     */
    public ShapeStyle setFillColor(Color fillColor)
    {
        this.fillColor = fillColor;
        return this;
    }
    /**
     * @return the opacity
     */
    public float getOpacity()
    {
        return opacity;
    }
    /**
     * @param opacity the opacity to set
     */
    public ShapeStyle setOpacity(float opacity)
    {
        this.opacity = opacity;
        return this;
    }
    
    
}
