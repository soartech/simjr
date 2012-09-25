package com.soartech.simjr.sensors.radar;


public class RangeWhileSearch extends RadarMode
{
    @Override
    public int getAllowedTargetCount()
    {
        return 0;
    }

    @Override
    public DetectionMode getTargetDetectionMode()
    {
        return DetectionMode.RANGE_WHILE_SEARCH;
    }

    @Override
    public DetectionMode getRegularDetectionMode()
    {
        return DetectionMode.RANGE_WHILE_SEARCH;
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
        return new RadarBound(0, 140000.0);
    }

    @Override
    public RadarDegreeBound getInclinationBounds()
    {
        return new RadarDegreeBound(10);
    }
    
    public String toString()
    {
        return getRadarName();
    }
    
    public static String getRadarName()
    {
        return "RWS";
    }
}
