package com.soartech.simjr.sim.entities;

import com.soartech.math.Vector3;

/**
 * A polygon that assumes its vertices are constant and never change position.
 * This has a performance benefit because subsequent calls to getPosition() don't need
 * to acquire the simulation lock.
 */
public class StaticPolygon extends DefaultPolygon
{
    private Vector3 center = null;
    
    public StaticPolygon()
    {
        super("");
    }
    
    public StaticPolygon(String defaultName)
    {
        super(defaultName);
    }
    
    @Override
    public Vector3 getPosition()
    {
        if (null != center)
        {
            return center;
        }
        
        if (super.hasPosition())
        {
            center = super.getPosition();
            return center;
        }
        
        return super.getPosition();
    }
}
