package com.soartech.simjr.ui.shapes;

import java.awt.Color;

import javax.swing.ImageIcon;

import com.soartech.shapesystem.FillStyle;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.shapes.Circle;
import com.soartech.shapesystem.swing.SwingPrimitiveRendererFactory;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.entities.Group;
import com.soartech.simjr.ui.SimulationImages;

/**
 * @author lampi
 */
public class GroupContainerShape extends EntityShape
{
    private static final String IMAGE_NAME = "simjr/images/lock.gif";
    public static final String NAME = "group";
    public static final EntityShapeFactory FACTORY = new Factory();
    private Entity parent = null;
    private ShapeStyle style;
    private Circle body = null;
    
    public static class Factory extends AbstractEntityShapeFactory {
    
        public EntityShape create(Entity entity, ShapeSystem system)
        {
            return new GroupContainerShape(entity, system);
        }        
        public String toString() { return NAME; }
        
        @Override
        public void initialize(SwingPrimitiveRendererFactory rendererFactory)
        {
            super.initialize(rendererFactory);
            ImageIcon image = SimulationImages.loadImageFromJar(IMAGE_NAME);
            
            rendererFactory.addImage(IMAGE_NAME, image.getImage());
        }
    };


    /**
     * @param entity
     * @param system
     */
    public GroupContainerShape(Entity entity, ShapeSystem system)
    {
        super(entity, system);
        parent = entity;
        updateImage();
    }
    
    @Override
    protected void updateForce()
    {
        super.updateForce();
        updateImage();
    }
    
    @Override
    public void update()
    {
       super.update();
       updateImage();
    }
    
    private void updateImage()
    {
        //TODO: Draw the NATO icon based on type 
        if(parent instanceof Group)
        {
            Group.Type type = ((Group)parent).getType();
            String name = getRootFrame().getName();
            
            style = new ShapeStyle();
            style.setFillStyle(FillStyle.FILLED);
            
            switch(type)
            {//TODO: Switch to drawing NATO icon
                case fireteam:
                    style.setLineColor(Color.RED);
                    break;
                case squad:
                    style.setLineColor(Color.ORANGE);
                    break;
                case platoon:
                    style.setLineColor(Color.YELLOW);
                    break;
                case company:
                    style.setLineColor(Color.GREEN);
                    break;
                case batallion:
                    style.setLineColor(Color.BLUE);
                    break;
                case regiment:
                    style.setLineColor(Color.GRAY);
                    break;
                case division:
                    style.setLineColor(Color.BLACK);
                    break;
            }
            
            Circle body = new Circle(name + ".body", EntityConstants.LAYER_GROUND, 
                                    new Position(name),
                                    Rotation.IDENTITY,
                                    style, 
                                    Scalar.createPixel(4));
            createLabel(5, 5, name);
            if(this.body != null)
            {
                removeShape(this.body);
            }
            addHitableShape(body);
            this.body = body;
        }
    }
}
