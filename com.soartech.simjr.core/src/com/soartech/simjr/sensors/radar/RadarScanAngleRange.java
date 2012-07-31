package com.soartech.simjr.sensors.radar;

import com.soartech.math.Angles;

public class RadarScanAngleRange
{
    /**
     * 
     * @param physicalBounds physical bounds of the radar
     * @param entityPos The entity's angular position (either pitch or heading)
     * @param center The center of the radar relative to the entityPos (in radians)
     * @param slew How wide the radar sweeps (in radians)
     * @param otherAngle Angular offset of the opposing entity.
     * @return true iff the entity pos is inside this radar's region.
     */
    public static boolean contains(RadarDegreeBound physicalBounds, double entityPos, double center, double slew, double otherAngle)
    {
        entityPos = Angles.boundedAngleRadians(entityPos);
        double centerOfRadar = Angles.boundedAngleRadians(entityPos + center);
        otherAngle = Angles.boundedAngleRadians(otherAngle);
        
        double angularDistanceFromRadarCenter = Math.abs(Angles.angleDifference(centerOfRadar, otherAngle));
        double angularDistanceFromEntityCenter = Math.abs(Angles.angleDifference(entityPos, otherAngle));
        
        boolean inPhysicalBounds = angularDistanceFromEntityCenter <= 0.5 * Math.toRadians(physicalBounds.getSpread()); 
        boolean inCurrentView = angularDistanceFromRadarCenter <= 0.5 * slew;
        
        return inPhysicalBounds && inCurrentView;
    }
}
