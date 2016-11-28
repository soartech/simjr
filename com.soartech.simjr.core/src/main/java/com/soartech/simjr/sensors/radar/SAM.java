
package com.soartech.simjr.sensors.radar;


public class SAM extends RadarMode
{
    @Override
    public int getAllowedTargetCount()
    {
        return 1;
    }

    @Override
    public DetectionMode getTargetDetectionMode()
    {
        return DetectionMode.SAM;
    }

    @Override
    public DetectionMode getRegularDetectionMode()
    {
        return DetectionMode.RANGE_WHILE_SEARCH;
    }

    @Override
    public boolean switchToRWSWhenNoTargets()
    {
        return true;
    }

    @Override
    public boolean onlyShowTargets()
    {
        return false;
    }
    
    @Override
    public RadarDegreeBound getAzimuthBounds()
    {
        return new RadarDegreeBound(120);
    }

    @Override
    public RadarBound getRangeBounds()
    {
        return new RadarBound(0.0, 140000.0);
    }

    @Override
    public RadarDegreeBound getInclinationBounds()
    {
        return new RadarDegreeBound(90);
    }
    
    public String toString()
    {
        return getRadarName();
    }
    
    public static String getRadarName()
    {
        return "SAM";
    }
}
