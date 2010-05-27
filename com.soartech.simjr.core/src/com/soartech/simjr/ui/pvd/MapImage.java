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
 * Created on Jun 14, 2007
 */
package com.soartech.simjr.ui.pvd;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;

import com.soartech.math.Vector3;
import com.soartech.shapesystem.CoordinateTransformer;
import com.soartech.shapesystem.SimplePosition;
import com.soartech.simjr.ui.SimulationImages;

/**
 * @author ray
 */
public class MapImage
{
    /**
     * The origin, in meters, of the center of the map 
     */
    private Vector3 centerMeters = Vector3.ZERO;
    
    /**
     * Number of meters represented by each pixel in the image
     */
    private double metersPerPixel = 1.0;
    
    private File imageFile;
    
    /**
     * The image
     */
    private Image image;
    
    private float opacity = 1.0f;
    
    /**
     * Construct a new map image
     * 
     * @param imageFile The file to load the image from
     * @param originMeters The origin, in meters, of the lower left corner of 
     *          the map
     * @param metersPerPixel Meter per pixel in the image
     */
    public MapImage(File imageFile, Vector3 originMeters, double metersPerPixel)
    {
        this.centerMeters = originMeters;
        this.metersPerPixel = metersPerPixel;
        setImage(imageFile);
    }
    
    /**
     * Construct a map image centered on the origin and 1 meter per pixel
     */
    public MapImage()
    {
        this(null, Vector3.ZERO, 1.0);
    }
    
    public void setImage(File imageFile)
    {
        this.imageFile = imageFile;
        if(this.imageFile != null)
        {
            this.image = SimulationImages.loadImageFromJar(imageFile.toString()).getImage();
        }
        else
        {
            this.image = null;
        }
    }
    
    public Image getImage()
    {
        return image;
    }
    
    /**
     * @return the imageFile
     */
    public File getImageFile()
    {
        return imageFile;
    }

    public float getOpacity()
    {
        return opacity;
    }
    
    public void setOpacity(float opacity)
    {
        this.opacity = opacity;
    }
    
    /**
     * @return the centerMeters
     */
    public Vector3 getCenterMeters()
    {
        return centerMeters;
    }

    /**
     * @param centerMeters the centerMeters to set
     */
    public void setCenterMeters(Vector3 centerMeters)
    {
        this.centerMeters = centerMeters;
    }

    /**
     * @return the metersPerPixel
     */
    public double getMetersPerPixel()
    {
        return metersPerPixel;
    }

    /**
     * @param metersPerPixel the metersPerPixel to set
     */
    public void setMetersPerPixel(double metersPerPixel)
    {
        this.metersPerPixel = metersPerPixel;
    }

    /**
     * Draw the map onto the given graphics context
     * 
     * @param g2d The graphics context
     * @param transformer The coordinate transformer
     */
    public void draw(Graphics2D g2dIn, CoordinateTransformer transformer)
    {
        if(this.image == null)
        {
            return;
        }
     
        final Graphics2D g2d = (Graphics2D) g2dIn.create();
        try
        {
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getOpacity());
            g2d.setComposite(ac);
            
            final SimplePosition centerPixels = transformer.metersToScreen(centerMeters.x, centerMeters.y);
            
            final double widthInMeters = image.getWidth(null) * this.metersPerPixel;
            final double heightInMeters = image.getHeight(null) * this.metersPerPixel;
            
            // Convert width and height from meters to pixels
            double widthInPixels = transformer.metersXToScreen(widthInMeters) -
                                   transformer.metersXToScreen(0.0);
            double heightInPixels = -(transformer.metersYToScreen(heightInMeters) -
                                    transformer.metersYToScreen(0.0));
            
            g2d.drawImage(image, 
                          (int) (centerPixels.x - widthInPixels / 2), 
                          (int) (centerPixels.y - heightInPixels / 2), 
                          (int) widthInPixels, (int) heightInPixels, 
                          null);
        }
        finally
        {
            g2d.dispose();
        }
    }
}
