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
 * Created on Jun 20, 2007
 */
package com.soartech.simjr.ui.shapes;

import javax.swing.ImageIcon;

import com.soartech.shapesystem.FillStyle;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.Shape;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.shapes.Box;
import com.soartech.shapesystem.shapes.Frame;
import com.soartech.shapesystem.shapes.ImageShape;
import com.soartech.shapesystem.swing.SwingPrimitiveRendererFactory;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.ui.SimulationImages;
/**
 * @author ray
 */
public class ImageEntityShape extends EntityShape
{
    public static final String NAME = "image";
    
    public static Factory create(String imageFile, String shadowImageFile, String destroyedImageFile)
    {
        return new Factory(imageFile, shadowImageFile, destroyedImageFile);
    }
    public static Factory create(String imageFile, String shadowImageFile)
    {
        return new Factory(imageFile, shadowImageFile, null);
    }
    public static Factory create(String imageFile)
    {
        return new Factory(imageFile, null, null);
    }

    private static class Factory extends AbstractEntityShapeFactory 
    {
        private String imageFile;
        private ImageIcon image;
        private String shadowImageFile;
        private ImageIcon shadowImage;
        private String destroyedImageFile;
        private ImageIcon destroyedImage;
        
        public Factory(String imageFile, String shadowImageFile, String destroyedImageFile)
        {
            this.imageFile = imageFile;
            this.image = SimulationImages.loadImageFromJar(imageFile);
            this.shadowImageFile = shadowImageFile;
            this.destroyedImageFile = destroyedImageFile;
            if(shadowImageFile != null)
            {
                this.shadowImage = SimulationImages.loadImageFromJar(shadowImageFile);
            }
            if(destroyedImageFile != null)
            {
                this.destroyedImage = SimulationImages.loadImageFromJar(destroyedImageFile);
            }            
        }
        
        /* (non-Javadoc)
         * @see com.soartech.simjr.ui.shapes.AbstractEntityShapeFactory#initialize(com.soartech.shapesystem.swing.SwingPrimitiveRendererFactory)
         */
        @Override
        public void initialize(SwingPrimitiveRendererFactory rendererFactory)
        {
            super.initialize(rendererFactory);
            
            rendererFactory.addImage(imageFile, image.getImage());
            if(shadowImage != null)
            {
                rendererFactory.addImage(shadowImageFile, shadowImage.getImage());
            }
            if(destroyedImage != null)
            {
                rendererFactory.addImage(destroyedImageFile, destroyedImage.getImage());
            }
        }

        /* (non-Javadoc)
         * @see com.soartech.simjr.ui.shapes.EntityShapeFactory#create(com.soartech.simjr.Entity, com.soartech.shapesystem.ShapeSystem)
         */
        public EntityShape create(Entity entity, ShapeSystem system)
        {
            return new ImageEntityShape(this, entity, system);
        }
        
        /* (non-Javadoc)
         * @see com.soartech.simjr.ui.shapes.AbstractEntityShapeFactory#createSelection(java.lang.String, com.soartech.simjr.Entity)
         */
        @Override
        public Shape createSelection(String id, Entity selected)
        {
            return new Box(id, EntityConstants.LAYER_SELECTION,
                    new Position(selected.getName() + ".bodyFrame"), 
                    Rotation.createRelative(selected.getName() + ".bodyFrame"), 
                    createSelectionStyle(),
                    Scalar.createPixel(image.getIconWidth() + 5),
                    Scalar.createPixel(image.getIconHeight() + 5));    
        }


        public String toString() { return NAME; }
    };
    
    private Factory factory;
    
    /**
     * @param entity
     * @param system
     */
    public ImageEntityShape(Factory factory, Entity entity, ShapeSystem system)
    {
        super(entity, system);
        
        this.factory = factory;
                
        String name = getRootFrame().getName();
        
        Frame bodyFrame = getBodyFrame();
                
        String layer = EntityTools.getProperty(entity.getProperties(), EntityConstants.PROPERTY_SHAPE_LAYER, EntityConstants.LAYER_AIR).toString();

        ImageShape body = new ImageShape(name + ".body", layer,
                                new Position(bodyFrame.getName()),
                                Rotation.createRelative(bodyFrame.getName()),
                                Scalar.createPixel(factory.image.getIconWidth()),
                                Scalar.createPixel(factory.image.getIconHeight()),
                                factory.imageFile,
                                new ShapeStyle());
                
        createLabel(12, 12, name);
        createShadow();
        myBody = body;
        addHitableShape(body);
      
        deadBody = new ImageShape(name + ".destroyed", EntityConstants.LAYER_SHADOWS,
                new Position(bodyFrame.getName()),
                Rotation.createRelative(bodyFrame.getName()),
                Scalar.createPixel(factory.image.getIconWidth()),
                Scalar.createPixel(factory.image.getIconHeight()),
                factory.destroyedImageFile,
                new ShapeStyle());
    }
    
    private void createShadow()
    {
        if(factory.shadowImage == null)
        {
            return;
        }
        String name = getRootFrame().getName();
        
        Frame shadowFrame = getShadowFrame();
        
        ShapeStyle shadowStyle = new ShapeStyle();
        shadowStyle.setFillStyle(FillStyle.FILLED);
        shadowStyle.setOpacity(0.8f);
        
        ImageShape shadow = new ImageShape(name + ".shadow", EntityConstants.LAYER_SHADOWS,
                         new Position(shadowFrame.getName()),
                         Rotation.createRelative(name),
                         Scalar.createPixel(factory.shadowImage.getIconWidth()),
                         Scalar.createPixel(factory.shadowImage.getIconHeight()),
                         factory.shadowImageFile, shadowStyle);
        
        addShape(shadow);
    }
    private ImageShape myBody;
    private ImageShape deadBody;
    @Override
    protected void destroyed()
    {
        super.destroyed();
        this.removeShape(myBody);
        this.addShape(deadBody); 
    }
}
