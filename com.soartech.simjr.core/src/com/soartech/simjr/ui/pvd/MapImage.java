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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.soartech.math.Vector3;
import com.soartech.shapesystem.CoordinateTransformer;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.ScalarUnit;
import com.soartech.shapesystem.SimplePosition;
import com.soartech.shapesystem.swing.SwingCoordinateTransformer;

/**
 * @author ray
 */
public class MapImage
{
    private static final Logger logger = Logger.getLogger(MapImage.class);
    
    private static class SingleMapImage
    {
        Vector3 centerMeters = Vector3.ZERO;
        double metersPerPixel = 1.0;
        File imageFile = null;
        BufferedImage image = null;
        float opacity = 1.0f;
        String name = "map";
    }

    private Map<Integer, SingleMapImage> imageList = new HashMap<Integer, SingleMapImage>();

    private SingleMapImage getOrCreate(int index)
    {
        SingleMapImage i = imageList.get(index);
        if (i == null)
        {
            i = new SingleMapImage();
            imageList.put(index, i);
        }
        return i;
    }

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
        SingleMapImage i = getOrCreate(0);
        i.centerMeters = originMeters;
        i.metersPerPixel = metersPerPixel;
        setImage(imageFile);
    }

    /**
     * Construct a map image centered on the origin and 1 meter per pixel
     */
    public MapImage()
    {
        this(null, Vector3.ZERO, 1.0);
    }

    public void setImage(int index, BufferedImage image)
    {
        SingleMapImage i = getOrCreate(index);
        i.imageFile = null;
        i.image = image;
    }

    public void setImage(BufferedImage image)
    {
        setImage(0, image);
    }

    public void setImage(int index, File imageFile) 
    {
        SingleMapImage i = getOrCreate(index);
        i.imageFile = imageFile;
        i.image = null;

        if (i.imageFile != null && !i.imageFile.getPath().equals(""))
        {
            try 
            {
                i.image = ImageIO.read(imageFile);
            }
            catch (IOException e)
            {
                logger.error("Unable to read image: " + imageFile, e);
            }
        }
    }

    public void setImage(File imageFile)
    {
        setImage(0, imageFile);
    }

    public BufferedImage getImage(int index)
    {
        SingleMapImage i = imageList.get(index);
        return i==null ? null : i.image;
    }

    public BufferedImage getImage()
    {
        return getImage(0);
    }

    public File getImageFile(int index)
    {
        SingleMapImage i = imageList.get(index);
        return i==null ? null : i.imageFile;
    }

    /**
     * @return the imageFile
     */
    public File getImageFile()
    {
        return getImageFile(0);
    }

    public float getOpacity(int index)
    {
        SingleMapImage i = imageList.get(index);
        return i==null ? null : i.opacity;
    }

    public float getOpacity()
    {
        return getOpacity(0);
    }

    public void setOpacity(int index, float opacity)
    {
        SingleMapImage i = getOrCreate(index);
        i.opacity = opacity;
    }

    public void setOpacity(float opacity)
    {
        setOpacity(0, opacity);
    }

    /**
     * @return the centerMeters
     */
    public Vector3 getCenterMeters(int index)
    {
        SingleMapImage i = imageList.get(index);
        return i==null ? null : i.centerMeters;
    }

    public Vector3 getCenterMeters()
    {
        return getCenterMeters(0);
    }

    /**
     * @param centerMeters the centerMeters to set
     */
    public void setCenterMeters(int index, Vector3 centerMeters)
    {
        SingleMapImage i = getOrCreate(index);
        i.centerMeters = centerMeters;
    }

    public void setCenterMeters(Vector3 centerMeters)
    {
        setCenterMeters(0, centerMeters);
    }

    /**
     * @return the metersPerPixel
     */
    public double getMetersPerPixel(int index)
    {
        SingleMapImage i = imageList.get(index);
        return i==null ? 0.0 : i.metersPerPixel;
    }

    public double getMetersPerPixel()
    {
        return getMetersPerPixel(0);
    }

    /**
     * @param metersPerPixel the metersPerPixel to set
     */
    public void setMetersPerPixel(int index, double metersPerPixel)
    {
        SingleMapImage i = getOrCreate(index);
        i.metersPerPixel = metersPerPixel;
    }

    public void setMetersPerPixel(double metersPerPixel)
    {
        setMetersPerPixel(0, metersPerPixel);
    }

    /**
     * Draw the map onto the given graphics context
     *
     * @param g2d The graphics context
     * @param transformer The coordinate transformer
     */
    public void draw(Graphics2D g2dIn, CoordinateTransformer transformer)
    {
        final Graphics2D g2d = (Graphics2D) g2dIn.create();

        int minIndex = 0;
        int maxIndex = -1;
        for (int index : imageList.keySet())
        {
            if (maxIndex < minIndex)
            {
                minIndex = maxIndex = index;
            }

            if (index < minIndex)
            {
                minIndex = index;
            }

            if (index > maxIndex)
            {
                maxIndex = index;
            }
        }

        try {
            for (int index = maxIndex; index >= minIndex; --index)
            {
                SingleMapImage i = imageList.get(index);
                if (i == null || i.image == null) continue;

                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, i.opacity);
                g2d.setComposite(ac);

                final SimplePosition centerPixels = transformer.metersToScreen(i.centerMeters.x, i.centerMeters.y);

                final double widthInMeters = i.image.getWidth(null) * i.metersPerPixel;
                final double heightInMeters = i.image.getHeight(null) * i.metersPerPixel;

                // Convert width and height from meters to pixels
                double widthInPixels = transformer.scalarToPixels(new Scalar(widthInMeters, ScalarUnit.Meters));
                double heightInPixels = transformer.scalarToPixels(new Scalar(heightInMeters, ScalarUnit.Meters));

                // Rotate the map according to the SwingCoordinateTransformer
                double rotation = ((SwingCoordinateTransformer)transformer).getRotation();
                g2d.rotate(-rotation, centerPixels.x, centerPixels.y);

                g2d.drawImage(i.image,
                        (int) (centerPixels.x - widthInPixels / 2),
                        (int) (centerPixels.y - heightInPixels / 2),
                        (int) widthInPixels, (int) heightInPixels,
                        null);
            }
        }
        finally
        {
            g2d.dispose();
        }
    }

    public void setName(int index, String name)
    {
        SingleMapImage i = imageList.get(index);
        i.name = name;
    }

    public String getName(int index)
    {
        SingleMapImage i = imageList.get(index);
        return i.name;
    }
}
