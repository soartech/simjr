package com.soartech.simjr.sim.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.soartech.math.Polygon;
import com.soartech.math.Vector3;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPrototype;

public class Building extends Container
{
    
    public enum Type
    {
        industrial,
        residential,
        commercial,
        military,
        other;
    }
    
    private Type type = Type.other;
    private double height = 10;// in meters
    
    public Building(String name, EntityPrototype prototype)
    {
        super(name, prototype);
    }
    
    public void setType(Type type)
    {
        this.type = type;
    }
    
    public Type getType()
    {
        return type;
    }
    
    /**
     * @return a convex hull polygon
     */
    public Polygon getBase()
    {
        Collection<Vector3> points = new ArrayList<Vector3>();
        //get the names of the points from our properties
        Object pointProp = this.getProperty(EntityConstants.PROPERTY_POINTS);
        if(pointProp instanceof List)
        {
            List l = (List)pointProp;
            for(Object entityName : l)
            {//get the entity belonging to the point, and get it's position
                Entity e = this.getSimulation().getEntity(entityName.toString());
                if(e != null)
                {
                    points.add(e.getPosition());
                }
            }
        }
        return Polygon.createConvexHull(points);
    }
    
    public void setHeight(double height)
    {
        this.height = height;
    }
    
    public double getHeight()
    {
        return height;
    }
}
