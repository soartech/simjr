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
package com.soartech.simjr.ui.pvd;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;

import com.soartech.math.Vector3;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.ScalarUnit;
import com.soartech.shapesystem.SimplePosition;
import com.soartech.shapesystem.swing.SwingCoordinateTransformer;

/**
 * @author ray
 */
public class GridManager
{
    private static final Stroke GRID_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
    private final SwingCoordinateTransformer transformer;
    private boolean gridVisible = true;
    
    /**
     * @param transformer
     */
    public GridManager(SwingCoordinateTransformer transformer)
    {
        this.transformer = transformer;
    }
    
    /**
     * @return True if the grid is visible
     */
    public boolean isVisible()
    {
        return gridVisible;
    }

    /**
     * Enable or disable drawing of the grid
     * 
     * @param gridVisible true to draw the grid, false otherwise
     */
    public void setVisible(boolean gridVisible)
    {
        this.gridVisible = gridVisible;
    }
    
    /**
     * Draw the grid on the given graphics context
     * 
     * @param g2d graphics context
     */
    public void draw(Graphics2D g2d)
    {
        if(!gridVisible)
        {
            return;
        }
        
        Dimension dim = transformer.getSize();
        Vector3 topLeft = transformer.screenToMeters(0, 0);
        Vector3 bottomRight = transformer.screenToMeters(dim.getWidth(), dim.getHeight());
        
        double gridIncrement = transformer.screenToMeters(75.0); 
        if(gridIncrement < 10.0)
        {
            gridIncrement = 10.0;
        }
        else if(gridIncrement < 50.0)
        {
            gridIncrement = 50.0;
        }
        else if(gridIncrement < 100.0)
        {
            gridIncrement = 100.0;
        }
        else if(gridIncrement < 500.0)
        {
            gridIncrement = 500.0;
        }
        else if(gridIncrement < 1000.0)
        {
            gridIncrement = 1000.0;
        }
        else if(gridIncrement < 5000.0)
        {
            gridIncrement = 5000.0;
        }
        else if(gridIncrement < 10000.0)
        {
            gridIncrement = 10000.0;
        }
        
        double gridPixels = transformer.metersXToScreen(gridIncrement) - transformer.metersXToScreen(0);
        // TODO: JCC
        gridPixels = Math.abs(transformer.scalarToPixels(new Scalar(gridIncrement, ScalarUnit.Meters)));

        
        double gridStartMeters = getNextGridMultiple(topLeft.x, gridIncrement);
        double gridStartScreen = transformer.metersXToScreen(gridStartMeters);
        
        g2d.setStroke(GRID_STROKE);
        g2d.setColor(Color.LIGHT_GRAY);
        
        double xMeters = gridStartMeters;
        double x = gridStartScreen;
        while(x < dim.getWidth())
        {
            g2d.drawLine((int) x, 0, (int) x, dim.height);
            g2d.drawString(Integer.toString((int) xMeters) + "m", (int)(x + 3), 15);
            x += gridPixels;
            xMeters += gridIncrement;
        }
        
        gridStartMeters = getNextGridMultiple(bottomRight.y, gridIncrement);
        gridStartScreen = transformer.metersYToScreen(gridStartMeters);
        
        double yMeters = gridStartMeters;
        double y = gridStartScreen;
        while(y > 0)
        {
            g2d.drawLine(0, (int) y, dim.width, (int) y);
            g2d.drawString(Integer.toString((int) yMeters) + "m", 3, (int)(y - 5));
            y -= gridPixels;
            yMeters += gridIncrement;
        }
        
    }
    
    private double getNextGridMultiple(double value, double incr)
    {
        double r = 0.0;
        if(value >= 0)
        {
            while(r < value)
            {
                r += incr;
            }
        }
        else
        {
            while(r > value)
            {
                r -= incr;
            }
            r += incr;
        }
        return r;
    }

}
