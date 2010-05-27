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
 * Created on Aug 7, 2007
 */
package com.soartech.simjr.ui.shapes;

import java.awt.Color;

import com.soartech.shapesystem.FillStyle;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.Shape;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.shapes.Circle;
import com.soartech.shapesystem.swing.SwingPrimitiveRendererFactory;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;

/**
 * @author ray
 */
public abstract class AbstractEntityShapeFactory implements EntityShapeFactory
{
    private final ShapeStyle style = new ShapeStyle();
    
    public AbstractEntityShapeFactory()
    {
        style.setLineColor(Color.DARK_GRAY);
        style.setLineThickness(Scalar.createPixel(3));
        style.setFillStyle(FillStyle.FILLED);
        style.setFillColor(Color.GREEN);
        style.setOpacity(.35f);
    }
    
    protected ShapeStyle createSelectionStyle()
    {
        return style.copy();
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.shapes.EntityShapeFactory#initialize(com.soartech.shapesystem.swing.SwingPrimitiveRendererFactory)
     */
    public void initialize(SwingPrimitiveRendererFactory rendererFactory)
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.pvd.EntityShapeFactory#createSelection(java.lang.String, com.soartech.simjr.Entity)
     */
    public Shape createSelection(String id, Entity selected)
    {
        return new Circle(id, EntityConstants.LAYER_SELECTION,
                new Position(selected.getName()), Rotation.IDENTITY, createSelectionStyle(),
                Scalar.createPixel(20));    
    }

}
