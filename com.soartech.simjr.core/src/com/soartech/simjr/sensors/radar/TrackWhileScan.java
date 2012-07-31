package com.soartech.simjr.sensors.radar;


// TODO: These modes are basically data, it might be easier to implement that way
public class TrackWhileScan extends RadarMode
{
    @Override
    public int getAllowedTargetCount()
    {
        return 16; // TODO: Make this configurable.
    }

    @Override
    public DetectionMode getTargetDetectionMode()
    {
        return DetectionMode.TRACK_WHILE_SCAN;
    }

    @Override
    public DetectionMode getRegularDetectionMode()
    {
        return DetectionMode.NONE;
    }

    @Override
    public boolean switchToRWSWhenNoTargets()
    {
        return true;
    }

    @Override
    public boolean onlyShowTargets()
    {
        return true;
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
        return new RadarDegreeBound(90);
    }

    public String toString()
    {
        return getRadarName();
    }
    
    public static String getRadarName()
    {
        return "TWS";
    }
}
