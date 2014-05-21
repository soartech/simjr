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
 * Created on Jul 11, 2007
 */
package com.soartech.simjr.ui.pvd;

import java.awt.Color;

import com.soartech.math.Vector3;
import com.soartech.shapesystem.CoordinateTransformer;
import com.soartech.shapesystem.FillStyle;
import com.soartech.shapesystem.PrimitiveRenderer;
import com.soartech.shapesystem.PrimitiveRendererFactory;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.SimplePosition;
import com.soartech.shapesystem.shapes.Capsule;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;

/**
 * @author ray
 */
public class DistanceToolShape extends Capsule
{
    private static final ShapeStyle SHAPE_STYLE = createStyle();
    private static final ShapeStyle LABEL_STYLE = createLabelStyle();
    
    private Entity start;
    private Entity end;
    private SimplePosition startPixels;
    private SimplePosition endPixels;
    private double distance = 0.0;
    
    /**
     * @param start
     * @param end
     */
    public DistanceToolShape(Entity start, Entity end)
    {
        super(start.getName() + "." + end.getName() + ".distance", 
              EntityConstants.LAYER_SELECTION, SHAPE_STYLE, 
              start.getName(), end.getName(), Scalar.createPixel(3));
        
        this.start = start;
        this.end = end;
    }

    
    private static ShapeStyle createStyle()
    {
        return new ShapeStyle().setLineColor(Color.DARK_GRAY).
                                setLineThickness(Scalar.createPixel(1)).
                                setFillStyle(FillStyle.FILLED).
                                setFillColor(new Color(61, 197, 235)).
                                setOpacity(.35f);
    }
    
    private static ShapeStyle createLabelStyle()
    {
        return new ShapeStyle().setLineColor(Color.BLACK).
                                setLineThickness(Scalar.createPixel(3));
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.shapes.Capsule#draw(com.soartech.shapesystem.PrimitiveRendererFactory)
     */
    @Override
    public void draw(PrimitiveRendererFactory rendererFactory)
    {
        super.draw(rendererFactory);
        
        PrimitiveRenderer r = rendererFactory.createPrimitiveRenderer(LABEL_STYLE);
        
        String text = String.format("%.0f m", new Object[] { distance });
        
        SimplePosition textPos = new SimplePosition((startPixels.x + endPixels.x) / 2,
                (startPixels.y + endPixels.y) / 2);
        r.drawText(textPos, text);
    }


    /* (non-Javadoc)
     * @see com.soartech.shapesystem.shapes.Capsule#calculateBase(com.soartech.shapesystem.ShapeSystem, com.soartech.shapesystem.CoordinateTransformer)
     */
    @Override
    protected void calculateBase(ShapeSystem system, CoordinateTransformer transformer)
    {
        super.calculateBase(system, transformer);
        
        Vector3 startPos = start.getPosition();
        Vector3 endPos = end.getPosition();
        
        startPixels = transformer.metersToScreen(startPos.x, startPos.y);
        endPixels = transformer.metersToScreen(endPos.x, endPos.y);
        distance = Vector3.getLateralDistance(startPos, endPos);
    }
    
    
}
