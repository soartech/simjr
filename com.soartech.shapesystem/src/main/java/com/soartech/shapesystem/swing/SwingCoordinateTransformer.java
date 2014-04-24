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
 * Created on Nov 12, 2008
 */
package com.soartech.shapesystem.swing;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;

import com.soartech.math.Vector3;
import com.soartech.shapesystem.CoordinateTransformer;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.ScalarUnit;
import com.soartech.shapesystem.SimplePosition;

public class SwingCoordinateTransformer implements CoordinateTransformer
{
    private final JComponent component;
    private double panOffsetRight = 0;
    private double panOffsetUp = 0;
    private double scale = 0.01;
    private double rotation = 0.0;
    
    /**
     * @param component
     */
    public SwingCoordinateTransformer(JComponent component)
    {
        this.component = component;
    }
    
    public double getRotation()
    {
        return rotation;
    }
    
    public void setRotation(double newRotation)
    {
        this.rotation = newRotation;
    }

    private double getMaxScale()
    {
        // Vector3 extents = getTerrain().getExtents();
        // Dimension d = getSize(); // TODO allow for screen resize
        // return Math.min( d.getWidth() / extents.x, d.getHeight() /
        // extents.y);
        return 100.;
    }
    
    /** sets the pan offset in screen pixels */
    public void setPanOffset(double x, double y)
    {
        panOffsetRight = x;
        panOffsetUp = y;
    }
    
    public double getPanOffsetX()
    {
        return panOffsetRight;
    }
    
    public double getPanOffsetY()
    {
        return panOffsetUp;
    }
    
    /**
     * @param s scale in pixels / meters
     */
    public void setScale(double s)
    {
        if (s <= getMaxScale())
        {
            scale = s;
        }
    }
    
    public double getScale()
    {
        return scale;
    }
    
    public JComponent getComponent()
    {
        return component;
    }
    
    public Dimension getSize()
    {
        return component.getSize();
    }
    
    /**
     * @param pixels
     * @return Distance covered by {@code pixels} in meters.
     */
    public double screenToMeters(double pixels)
    {
        return pixels / scale;
    }
    
    
    /* (non-Javadoc)
     * @see com.soartech.shapesystem.CoordinateTransformer#screenToMeters(double, double)
     */
    public Vector3 screenToMeters(double x, double y)
    {
        Dimension d = component.getSize();
        double mx = (x - panOffsetRight) / scale;
        double my = ((y-panOffsetUp) - d.getHeight()) / -scale;
        
        // Undo the rotation when converting to meters
        double[] pt = {mx, my};
        AffineTransform.getRotateInstance(-rotation, 0.0, 0.0).transform(pt, 0, pt, 0, 1);
        mx = pt[0];
        my = pt[1];
        
        return new Vector3(mx, my, 0.0);
    }
    
    /* (non-Javadoc)
     * @see com.soartech.shapesystem.CoordinateTransformer#metersToScreen(double, double)
     */
    public SimplePosition metersToScreen(double x, double y)
    {
        // Rotate the meters before converting it to screen coordinates
        double[] pt = {x, y};
        AffineTransform.getRotateInstance(rotation, 0.0, 0.0).transform(pt, 0, pt, 0, 1);
        x = pt[0];
        y = pt[1];
        
        Dimension d = component.getSize();
        return new SimplePosition((x) * scale + panOffsetRight, 
                                  d.getHeight() - ((y) * scale)+ panOffsetUp);
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.CoordinateTransformer#metersXToScreen(double)
     */
    public double metersXToScreen(double x)
    {
        return metersToScreen(x, 0.0).x;
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.CoordinateTransformer#metersYToScreen(double)
     */
    public double metersYToScreen(double y)
    {
        return metersToScreen(0.0, y).y;
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.CoordinateTransformer#scalarToPixels(com.soartech.shapesystem.Scalar)
     */
    public double scalarToPixels(Scalar s)
    {
        if(s.getUnit() == ScalarUnit.Meters)
        {
            return s.getValue() * scale;
        }
        else if(s.getUnit() == ScalarUnit.Pixels)
        {
            return s.getValue();
        }
        throw new UnsupportedOperationException("Don't know how to convert scalar " + s + " to pixels");
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.CoordinateTransformer#supportsSingleWorldCoordinates()
     */
    public boolean supportsSingleWorldCoordinates()
    {
        return false;
    }
    
}