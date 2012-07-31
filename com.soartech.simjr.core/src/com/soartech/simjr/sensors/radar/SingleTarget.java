package com.soartech.simjr.sensors.radar;


public class SingleTarget extends RadarMode
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
        return DetectionMode.NONE;
    }

    @Override
    public boolean switchToRWSWhenNoTargets()
    {
        return false;
    }

    @Override
    public boolean onlyShowTargets()
    {
        return true;
    }
    
    @Override
    public RadarDegreeBound getAzimuthBounds()
    {
        // TODO: This need to be configurable
        return new RadarDegreeBound(15);
    }

    @Override
    public RadarBound getRangeBounds()
    {
        // TODO: This need to be configurable
        return new RadarBound(0.0, 140000.0);
    }

    @Override
    public RadarDegreeBound getInclinationBounds()
    {
        // TODO: This need to be configurable
        return new RadarDegreeBound(30);
    }

    public String toString()
    {
        return getRadarName();
    }
    
    public static String getRadarName()
    {
        return "ST";
    }
}
