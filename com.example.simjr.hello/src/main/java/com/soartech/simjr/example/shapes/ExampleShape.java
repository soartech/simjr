/**
 * 
 */
package com.soartech.simjr.example.shapes;


import java.awt.Color;
import java.util.Map;

import com.soartech.shapesystem.FillStyle;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.shapes.Circle;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.ui.shapes.AbstractEntityShapeFactory;
import com.soartech.simjr.ui.shapes.EntityShape;
import com.soartech.simjr.ui.shapes.EntityShapeFactory;

/**
 * @author aron
 *
 */
public class ExampleShape extends EntityShape
{
    public static final String NAME = "example";
    
    //define a static factory that can instantiate this EntityShape 
    public static final EntityShapeFactory FACTORY = new Factory();
    public static class Factory extends AbstractEntityShapeFactory {

        public EntityShape create(Entity entity, ShapeSystem system)
        {
            return new ExampleShape(entity, system);
        }        
        public String toString() { return NAME; }
    };
    
    public ExampleShape(Entity entity, ShapeSystem system)
    {
        super(entity, system);
        
        //define your custom shape
        final Map<String, Object> props = entity.getProperties();
        String name = getRootFrame().getName();
        
        final ShapeStyle style = new ShapeStyle();
        style.setFillStyle(FillStyle.FILLED);
        final Color fillColor = (Color) EntityTools.getProperty(props, EntityConstants.PROPERTY_SHAPE_FILL_COLOR, Color.RED);
        style.setFillColor(fillColor);
        final Color lineColor = (Color) EntityTools.getProperty(props, EntityConstants.PROPERTY_SHAPE_LINE_COLOR, Color.BLACK);
        style.setLineColor(lineColor);
        
        final Number opacity = (Number) EntityTools.getProperty(props, EntityConstants.PROPERTY_SHAPE_OPACITY, 1.0);
        if(opacity != null)
        {
            style.setOpacity(opacity.floatValue());
        }
        
        Circle body = new Circle(name + ".body", 
                EntityConstants.LAYER_AIR, 
                new Position(name), 
                Rotation.IDENTITY, 
                style, 
                Scalar.createPixel(6));
      
        createLabel(10, 10, name);
        
        addHitableShape(body);
    }

}
