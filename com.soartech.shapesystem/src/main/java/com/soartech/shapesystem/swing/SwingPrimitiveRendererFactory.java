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
package com.soartech.shapesystem.swing;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import com.soartech.shapesystem.CoordinateTransformer;
import com.soartech.shapesystem.PrimitiveRenderer;
import com.soartech.shapesystem.PrimitiveRendererFactory;
import com.soartech.shapesystem.ShapeStyle;

/**
 * @author ray
 */
public class SwingPrimitiveRendererFactory implements PrimitiveRendererFactory
{
    private Graphics2D graphics2D;
    private int viewportWidth;
    private int viewportHeight;
    private Composite originalComposite;
    private Map<String, Image> images = new HashMap<String, Image>();
    private CoordinateTransformer transformer;
    
    public SwingPrimitiveRendererFactory(CoordinateTransformer transformer)
    {
        this.transformer = transformer;
    }
    
    /**
     * @return the graphics2D
     */
    public Graphics2D getGraphics2D()
    {
        return graphics2D;
    }

    /**
     * @param graphics2D the graphics2D to set
     */
    public void setGraphics2D(Graphics2D graphics2D, int width, int height)
    {
        this.graphics2D = graphics2D;
        this.viewportWidth = width;
        this.viewportHeight = height;
        this.originalComposite = graphics2D.getComposite();
    }

    public Image getImage(String id)
    {
        return images.get(id);
    }
    
    public void addImage(String id, Image image)
    {
        images.put(id, image);
    }
    
    public Image loadImage(ClassLoader loader, String id, String path)
    {
        URL url = loader.getResource('/' + path);
        Image image = null;
        if(url != null)
        {
            image = new ImageIcon(url).getImage();
        }
        else if(new File(path).exists())
        {
            image = new ImageIcon(path).getImage();
        }
        else
        {
            return null;
        }
        
        images.put(id, image);
        return image;
    }

    /**
     * @return the viewportHeight
     */
    public int getViewportHeight()
    {
        return viewportHeight;
    }

    /**
     * @return the viewportWidth
     */
    public int getViewportWidth()
    {
        return viewportWidth;
    }

    /**
     * @return the transformer
     */
    public CoordinateTransformer getTransformer()
    {
        return transformer;
    }

    /**
     * @return the original composite used by the graphics context
     */
    public Composite getOriginalComposite()
    {
        return originalComposite;
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.PrimitiveRendererFactory#createPrimitiveRenderer(com.soartech.shapesystem.ShapeStyle)
     */
    public PrimitiveRenderer createPrimitiveRenderer(ShapeStyle style)
    {
        return new SwingPrimitiveRenderer(this, style);
    }

}
