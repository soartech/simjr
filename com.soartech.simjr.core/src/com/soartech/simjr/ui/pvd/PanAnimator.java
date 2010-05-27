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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import com.soartech.math.Vector3;
import com.soartech.shapesystem.SimplePosition;
import com.soartech.shapesystem.swing.SwingCoordinateTransformer;

/**
 * @author ray
 */
public class PanAnimator
{
    private final SwingCoordinateTransformer transformer;
    private final Timer animationTimer;
    private int animationSteps;
    private double animationPanDx, animationPanDy;
    
    /**
     * @param transformer
     */
    public PanAnimator(SwingCoordinateTransformer transformer)
    {
        this.transformer = transformer;
        
        animationTimer = new Timer(50, new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                animate();
            }});
    }
    
    boolean isAnimating()
    {
        return animationTimer.isRunning();
    }
    
    private void animate()
    {
        transformer.setPanOffset(transformer.getPanOffsetX() + animationPanDx, 
                                 transformer.getPanOffsetY() + animationPanDy);
        
        animationSteps--;
        if(animationSteps == 0)
        {
            animationTimer.stop();
        }
        
        transformer.getComponent().repaint();
    }
    
    public void panToPosition(Vector3 p)
    {
        if(isAnimating())
        {
            animationTimer.stop();
        }
        
        // find the current location of (x,y) in meters
        SimplePosition currentPosition = transformer.metersToScreen(p.x, p.y);

        // find the position of the center of the screen
        double desiredX = transformer.getSize().getWidth() / 2;
        double desiredY = transformer.getSize().getHeight() / 2;

        // add the difference to the current offset to create the new offset
        double offsetX = desiredX - currentPosition.x;
        double offsetY = desiredY - currentPosition.y;
        double distance = new Vector3(offsetX, offsetY, 0.0).length();
        
        double pixelsPerSecond = distance * 2;
        double updatesPerSecond = 1000 / animationTimer.getDelay();
        animationSteps = (int) ((distance / pixelsPerSecond) * updatesPerSecond);
        animationPanDx = offsetX / animationSteps;
        animationPanDy = offsetY / animationSteps;
        
        if(animationSteps > 0)
        {
            animationTimer.start();
        }
    }

}
