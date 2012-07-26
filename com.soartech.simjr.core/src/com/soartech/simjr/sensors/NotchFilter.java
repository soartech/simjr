package com.soartech.simjr.sensors;

import com.soartech.math.Angles;
import com.soartech.math.Vector3;
import com.soartech.simjr.sim.Entity;

/**
 * Determines if an entity is in another's notch (i.e., the entity is
 * relatively indistinguishable from the ground.) 
 */
public class NotchFilter
{
    // TODO: These have to be configurable
    
    // Notch width in degrees (e.g., 3 here means the notch is from 87 to 90 degrees)
    private static double NOTCH_WIDTH = 3;
    // The minimum speed (in m/s) for an entity to show up on radar.
    private static double MINIMUM_SPEED = 20;
    
    private Entity source;

    public NotchFilter(Entity source)
    {
        this.source = source;
    }
    
    public boolean inNotch(Vector3 otherPosition, Vector3 otherVelocity)
    {
        if (otherVelocity.length() < MINIMUM_SPEED)
        {
            return true;
        }
        
        double angle = otherPosition.subtract(source.getPosition()).cosAngle(otherVelocity);
        double angleDiff = Angles.angleDifference(angle, Math.PI/2);
        
        return Math.abs(Math.toDegrees(angleDiff)) <= NOTCH_WIDTH;
    }
}