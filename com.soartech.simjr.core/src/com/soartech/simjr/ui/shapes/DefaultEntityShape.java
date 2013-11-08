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

import java.awt.Image;
import java.net.URL;

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
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.entities.DamageStatus;
/**
 * @author ray
 */
public class DefaultEntityShape extends EntityShape
{
    public static final String NAME = "default";
    
    public static final String PROPERTY = "shape.info";
    
    public static class Factory extends AbstractEntityShapeFactory 
    {        
        private SwingPrimitiveRendererFactory rendererFactory;
        
        public Factory()
        {
        }
        
        /* (non-Javadoc)
         * @see com.soartech.simjr.ui.shapes.AbstractEntityShapeFactory#initialize(com.soartech.shapesystem.swing.SwingPrimitiveRendererFactory)
         */
        @Override
        public void initialize(SwingPrimitiveRendererFactory rendererFactory)
        {
            super.initialize(rendererFactory);
            
            this.rendererFactory = rendererFactory;
        }

        /* (non-Javadoc)
         * @see com.soartech.simjr.ui.shapes.EntityShapeFactory#create(com.soartech.simjr.Entity, com.soartech.shapesystem.ShapeSystem)
         */
        public EntityShape create(Entity entity, ShapeSystem system)
        {
            return new DefaultEntityShape(this, entity, system);
        }
        
        /* (non-Javadoc)
         * @see com.soartech.simjr.ui.shapes.AbstractEntityShapeFactory#createSelection(java.lang.String, com.soartech.simjr.Entity)
         */
        @Override
        public Shape createSelection(String id, Entity selected)
        {
            final DefaultEntityShape shape = (DefaultEntityShape) selected.getProperty(PROPERTY);
            
            final int width = shape != null ? shape.width : 10;
            final int height = shape != null ? shape.height : 10;
            
            return new Box(id, EntityConstants.LAYER_SELECTION,
                    new Position(selected.getName() + ".bodyFrame"), 
                    Rotation.createRelative(selected.getName() + ".bodyFrame"), 
                    createSelectionStyle(),
                    Scalar.createPixel(width + 5),
                    Scalar.createPixel(height + 5));    
        }


        public String toString() { return NAME; }
    };
    
    private SwingPrimitiveRendererFactory rendererFactory;
    private String finalImagePrefix;
    private int width, height;
    private ImageShape myBody;
    private boolean hasShadow;
    
    private void loadImages()
    {
        EntityPrototype prototype = getEntity().getPrototype();
        
        while(prototype != null) {
            final String prefix = "simjr/images/shapes/entities/" + prototype.getId();
            final String defaultImageId = prefix + "/friendly.png";
            
            // Decent fallback prefix
            finalImagePrefix = prefix;

            final Image testImage = rendererFactory.getImage(defaultImageId);
            if(testImage != null)
            {
                width = testImage.getWidth(null);
                height = testImage.getHeight(null);
                finalImagePrefix = prefix;
                final String shadowImageId = prefix + "/shadow.png";
                hasShadow = rendererFactory.getImage(shadowImageId) != null;
                return;
            }
            else
            {
                URL url = DefaultEntityShape.class.getResource("/" + defaultImageId);
                if(url != null) 
                {
                    for(String force : EntityConstants.ALL_FORCES)
                    {
                        final String id = prefix + "/" + force + ".png";
                        final Image img = rendererFactory.loadImage(getClass().getClassLoader(), id, id);
                        if(img != null)
                        {
                            width = img.getWidth(null);
                            height = img.getHeight(null);
                        }
                    }
                    final String shadowImageId = prefix + "/shadow.png";
                    final String destroyedImageId = prefix + "/destroyed.png";
                    hasShadow = rendererFactory.loadImage(getClass().getClassLoader(), shadowImageId, shadowImageId) != null;
                    rendererFactory.loadImage(getClass().getClassLoader(), destroyedImageId, destroyedImageId);
                    
                    finalImagePrefix = prefix;
                    return;
                }
            }
            prototype = prototype.getParent();
        }
    }
    
    private String getBodyImage()
    {
        final String force = EntityTools.getForce(getEntity()) ;
        final DamageStatus damage = EntityTools.getDamage(getEntity());
        
        if(damage == DamageStatus.destroyed)
        {
            return finalImagePrefix + "/destroyed.png";
        }
        else
        {
            return finalImagePrefix + "/" + force + ".png";
        }
    }
    
    @Override
    public ImageShape createBodyShape(String shapeId, ShapeStyle shapeStyle)
    {
    	Frame bodyFrame = getBodyFrame();
        
        String layer = EntityTools.getProperty(getEntity().getProperties(), 
                EntityConstants.PROPERTY_SHAPE_LAYER, 
                EntityConstants.LAYER_AIR).toString();

    	ImageShape body = new ImageShape(shapeId, layer,
                                new Position(bodyFrame.getName()),
                                Rotation.createRelative(bodyFrame.getName()),
                                Scalar.createPixel(width),
                                Scalar.createPixel(height),
                                getBodyImage(),
                                shapeStyle);
    	
    	return body;
    }
    
    public ImageShape createShadowShape(String shapeId, ShapeStyle shapeStyle)
    {
        if (!hasShadow)
        {
        	return null;
        }
        
        ImageShape shadow = new ImageShape(shapeId, EntityConstants.LAYER_SHADOWS,
                         new Position(getShadowFrame().getName()),
                         Rotation.createRelative(getRootFrame().getName()),
                         Scalar.createPixel(width),
                         Scalar.createPixel(height),
                         finalImagePrefix + "/shadow.png", shapeStyle);
        
        return shadow;
    }
    
    /**
     * @param entity
     * @param system
     */
    public DefaultEntityShape(Factory factory, Entity entity, ShapeSystem system)
    {
        super(entity, system);
        
        entity.setProperty(PROPERTY, this);
        
        this.rendererFactory = factory.rendererFactory;
        loadImages();
        
        String root = getRootFrame().getName();
        
        createBody(root + ".body");
        createLabel(12, 12, root);
        
        if (hasShadow)
        {
        	createShadow(root + ".shadow");
        }

        addHitableShape(myBody);
    }
    
    private void createBody(String name)
    {
        myBody = createBodyShape(name, new ShapeStyle());
    }
    
    private void createShadow(String name)
    {
        ShapeStyle shadowStyle = new ShapeStyle();
        shadowStyle.setFillStyle(FillStyle.FILLED);
        shadowStyle.setOpacity(0.8f);
        
        Shape shadow = createShadowShape(name, shadowStyle);
        
        addShape(shadow);
    }
    
    @Override
    protected void destroyed()
    {
        super.destroyed();

        myBody.setImage(getBodyImage());
    }

    @Override
    protected void updateForce()
    {
        myBody.setImage(getBodyImage());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("%s (%dx%d), %s", finalImagePrefix, width, height, hasShadow);
    }
}
