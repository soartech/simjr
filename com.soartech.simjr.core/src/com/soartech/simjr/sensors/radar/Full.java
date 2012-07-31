package com.soartech.simjr.sensors.radar;


public class Full extends RadarMode
{
    @Override
    public int getAllowedTargetCount()
    {
        return 1;
    }

    @Override
    public DetectionMode getTargetDetectionMode()
    {
        return DetectionMode.FULL;
    }

    @Override
    public DetectionMode getRegularDetectionMode()
    {
        return DetectionMode.FULL;
    }

    @Override
    public boolean switchToRWSWhenNoTargets()
    {
        return false;
    }

    @Override
    public boolean onlyShowTargets()
    {
        return false;
    }

    @Override
    public RadarDegreeBound getAzimuthBounds()
    {
        return RadarDegreeBound.NoBounds();
    }

    @Override
    public RadarBound getRangeBounds()
    {
        return RadarBound.NoBounds();
    }

    @Override
    public RadarDegreeBound getInclinationBounds()
    {
        return RadarDegreeBound.NoBounds();
    }
    
    public String toString()
    {
        return getRadarName();
    }
    
    public static String getRadarName()
    {
        return "FULL";
    }
}
