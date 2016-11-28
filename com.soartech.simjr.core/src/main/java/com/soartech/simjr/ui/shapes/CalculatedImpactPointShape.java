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
 * Created on May 8, 2008
 */
package com.soartech.simjr.ui.shapes;

import java.awt.Color;

import com.soartech.math.Vector3;
import com.soartech.shapesystem.FillStyle;
import com.soartech.shapesystem.LineStyle;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.shapes.Circle;
import com.soartech.shapesystem.shapes.Frame;
import com.soartech.shapesystem.shapes.Line;
import com.soartech.simjr.sim.EntityConstants;

/**
 * Responsible for displaying the arc (and shadow) for an EntityVisibleRange object on an entity.
 * 
 * @author ray
 */
public class CalculatedImpactPointShape
{
    private EntityShape parent;
    private Frame ipFrame;
    private Line line;
    private Circle circle;
    private boolean needsUpdate = true;

    /**
     * @param parent The owning entity shape
     */
    public CalculatedImpactPointShape(EntityShape parent)
    {
        this.parent = parent;
    }
    
    public void setNeedsUpdate()
    {
        this.needsUpdate = true;
    }

    public void update()
    {
        if(!needsUpdate)
        {
            return;
        }
        needsUpdate = false;
        
        final Vector3 ccip = (Vector3) parent.getEntity().getProperty(EntityConstants.PROPERTY_CCIP);
        if(ccip == null)
        {
            parent.removeShape(ipFrame);
            ipFrame = null;
            parent.removeShape(line);
            line = null;
            parent.removeShape(circle);
            circle = null;
            return;
        }

        final Position framePosition = Position.createWorldMeters(ccip.x, ccip.y);
        if(ipFrame == null)
        {
            ShapeStyle style = new ShapeStyle();
            style.setFillColor(Color.GREEN);
            style.setLineColor(Color.GREEN);
            style.setFillStyle(FillStyle.NONE);
            style.setLineStyle(LineStyle.DASHED);
            style.setOpacity(0.6f);
            
            String name = parent.getRootFrame().getName() + ".ccip";
            ipFrame = new Frame(name, "", framePosition, Rotation.IDENTITY);
            line = new Line(name + ".line", "", style, parent.getRootFrame().getName(), ipFrame.getName());
            
            style = style.copy();
            style.setLineStyle(LineStyle.SOLID);
            circle = new Circle(name + ".circle", "", 
                                Position.createRelativeMeters(0, 0, ipFrame.getName()),
                                Rotation.IDENTITY, style, Scalar.createPixel(7));
            
            parent.addShape(ipFrame);
            parent.addShape(line);
            parent.addShape(circle);
        }
        else
        {
            ipFrame.setPosition(framePosition);
        }        
    }
}
