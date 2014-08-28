package com.soartech.simjr.ui.shapes;

import com.soartech.shapesystem.ShapeSystem;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.entities.AbstractPolygon;

public class BuildingShape extends AreaShape
{
    protected Entity parent = null;
    
    public static class Factory extends AbstractEntityShapeFactory {
        
        @Override
        public BuildingShape create(Entity entity, ShapeSystem system)
        {
            return new BuildingShape(Adaptables.adapt(entity, AbstractPolygon.class), system, true);
        }        
        public String toString() { return NAME; }
    };
    
    public BuildingShape(AbstractPolygon polygon, ShapeSystem system,
            boolean convex)
    {
        super(polygon, system, convex);
        //TODO: add ability to create and draw doors
        //TODO: change color of building depending on type
    }
    
    
}
