/* The Government has unlimited rights to this software. All other parties have
 * no rights to use, distribute, reproduce, modify, reverse engineer, or
 * otherwise utilize this software.
 *
 * All ownership rights are retained by Soar Technology, Inc.
 *
 * (C)2015 SoarTech, Proprietary, All Rights Reserved.
 */

package com.soartech.simjr.ui.shapes;

import java.awt.Image;
import java.net.URL;
import java.util.Map;

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
 * Shape for creating an entity shape with fixed size in meters.
 * 
 * @author eric.tucker
 */

public class FixedSizeEntityShape extends EntityShape
{
    public static final String NAME = "fixedSize";

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
        @Override
        public EntityShape create(Entity entity, ShapeSystem system)
        {
            return new FixedSizeEntityShape(this, entity, system);
        }

        /* (non-Javadoc)
         * @see com.soartech.simjr.ui.shapes.AbstractEntityShapeFactory#createSelection(java.lang.String, com.soartech.simjr.Entity)
         */
        @Override
        public Shape createSelection(String id, Entity selected)
        {
            final FixedSizeEntityShape shape = (FixedSizeEntityShape) selected.getProperty(PROPERTY);

            final double width = shape != null ? shape.width : 10.0;
            final double height = shape != null ? shape.height : 10.0;

            return new Box(id, EntityConstants.LAYER_SELECTION,
                    new Position(selected.getName() + ".bodyFrame"),
                    Rotation.createRelative(selected.getName() + ".bodyFrame"),
                    createSelectionStyle(),
                    Scalar.createMeter(width * 1.25),
                    Scalar.createMeter(height * 1.25));
        }


        @Override
        public String toString() { return NAME; }
    };

    private SwingPrimitiveRendererFactory rendererFactory;
    private String finalImagePrefix;
    private double width, height;
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
                final String shadowImageId = prefix + "/shadow.png";
                hasShadow = rendererFactory.getImage(shadowImageId) != null;
                return;
            }
            else
            {
                URL url = FixedSizeEntityShape.class.getResource("/" + defaultImageId);
                if(url != null)
                {
                    for(String force : EntityConstants.ALL_FORCES)
                    {
                        final String id = prefix + "/" + force + ".png";
                        rendererFactory.loadImage(FixedSizeEntityShape.class, id, id);
                    }
                    final String shadowImageId = prefix + "/shadow.png";
                    final String destroyedImageId = prefix + "/destroyed.png";
                    hasShadow = rendererFactory.loadImage(FixedSizeEntityShape.class, shadowImageId, shadowImageId) != null;
                    rendererFactory.loadImage(FixedSizeEntityShape.class, destroyedImageId, destroyedImageId);

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
                                Scalar.createMeter(width),
                                Scalar.createMeter(height),
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
                         Scalar.createMeter(width),
                         Scalar.createMeter(height),
                         finalImagePrefix + "/shadow.png", shapeStyle);

        return shadow;
    }

    /**
     * @param factory
     * @param entity
     * @param system
     */
    public FixedSizeEntityShape(Factory factory, Entity entity, ShapeSystem system)
    {
        super(entity, system);

        entity.setProperty(PROPERTY, this);

        this.rendererFactory = factory.rendererFactory;
        Map<String, Object> props = entity.getProperties();

        // Due to the way the shapes are drawn, the width of the image corresponds with the length of the entity
        // and the height of the image corresponds with the width of the entity.
        Number w = (Number) EntityTools.getProperty(props, EntityConstants.PROPERTY_SHAPE_ENTITY_LENGTH_METERS, null);
        Number h = (Number) EntityTools.getProperty(props, EntityConstants.PROPERTY_SHAPE_ENTITY_WIDTH_METERS, null);

        width = w != null ? w.doubleValue() : 10.0;
        height = h != null ? h.doubleValue() : 10.0;

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

    public ImageShape getBodyShape()
    {
        return myBody;
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
        return String.format("%s (%fx%f), %s", finalImagePrefix, width, height, hasShadow);
    }
}
