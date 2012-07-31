package com.soartech.simjr.sensors.radar;

import com.soartech.math.Vector3;
import com.soartech.simjr.sim.Entity;

public class RadarController
{
    private Entity entity;
    private RadarMode radarMode;
    private double azimuthCenter;
    private double inclinationCenter;
    private double lowRange;
    private double highRange;
    private double azimuthSlew;
    private double inclinationSlew;
    
    public RadarController(Entity e, RadarMode radarMode)
    {
        this.entity = e;
        
        // Largest possible values (these will get paired down in setRadarMode)
        lowRange = 0.0;
        highRange = Double.MAX_VALUE;
        azimuthCenter = 0.0;
        inclinationCenter = 0.0;
        azimuthSlew = 2*Math.PI;
        inclinationSlew = 2*Math.PI;
        
        this.setRadarMode(radarMode);
    }
    
    /**
     * Tests whether a position is within this radar range
     * 
     * @param otherPos The position to test.
     * @return True iff otherPos is within this radar range.
     */
    public boolean isInRange(Vector3 otherPos)
    {
        Vector3 agentPos = entity.getPosition();
        Vector3 displacement = otherPos.subtract(agentPos);
        
        double distance = displacement.length();
        
        if (!RadarScanDistanceRange.contains(lowRange, highRange, distance))
            return false;

        double otherAzimuthAngle = Math.atan2(displacement.y, displacement.x);
        if (!RadarScanAngleRange.contains(radarMode.getAzimuthBounds(), entity.getHeading(), azimuthCenter, azimuthSlew, otherAzimuthAngle))
            return false;

        double otherInclinationAngle = Math.asin(displacement.z / distance);
        if (!RadarScanAngleRange.contains(radarMode.getInclinationBounds(), entity.getPitch(), inclinationCenter, inclinationSlew, otherInclinationAngle))
            return false;
        
        return true;
        
    }
    
    public void setAzimuthCenter(double center)
    {
        if (Math.abs(center) > radarMode.getInclinationBounds().getSpreadRadians())
            center = Math.signum(center)*radarMode.getInclinationBounds().getSpreadRadians();
        this.azimuthCenter = center;
    }
    
    public RadarMode getRadarMode()
    {
        return radarMode;
    }
    
    public void setRadarMode(RadarMode radarMode)
    {
        this.radarMode = radarMode;
        setAzimuthCenter(azimuthCenter);
        setInclinationCenter(inclinationCenter);
        setLowRange(lowRange);
        setHighRange(highRange);
    }

    public void setInclinationCenter(double center)
    {
        if (Math.abs(center) > radarMode.getInclinationBounds().getSpreadRadians())
            center = Math.signum(center)*radarMode.getInclinationBounds().getSpreadRadians();
        this.inclinationCenter = center;
    }

    public void setAzimuthSlew(double azimuthSlew)
    {
        this.azimuthSlew = azimuthSlew;
    }
    
    public void setInclinationSlew(double inclinationSlew)
    {
        this.inclinationSlew = inclinationSlew;
    }
    
    public void setLowRange(double lowRange)
    {
        if (lowRange < radarMode.getRangeBounds().getLow())
            lowRange = radarMode.getRangeBounds().getLow();
        if (lowRange > radarMode.getRangeBounds().getHigh())
            lowRange = radarMode.getRangeBounds().getHigh();
        
        this.lowRange = lowRange;
    }
    
    public void setHighRange(double highRange)
    {
        if (highRange < radarMode.getRangeBounds().getLow())
            highRange = radarMode.getRangeBounds().getLow();
        if (highRange > radarMode.getRangeBounds().getHigh())
            highRange = radarMode.getRangeBounds().getHigh();

        this.highRange = highRange;
    }

    /**
     * Center of the radar (azimuth), in radians.
     */
    public double getAzimuthCenter()
    {
        return azimuthCenter;
    }

    /**
     * Center of the radar (inclination), in radians.
     */
    public double getInclinationCenter()
    {
        return inclinationCenter;
    }

    /**
     * Low range of the radar (in meters.)
     */
    public double getLowRange()
    {
        return lowRange;
    }

    /**
     * High range of the radar (in meters.)
     */
    public double getHighRange()
    {
        return highRange;
    }

    /**
     * Azimuth slew (spread) in radians.
     */
    public double getAzimuthSlew()
    {
        return azimuthSlew;
    }

    /**
     * Inclination slew (spread) in radians.
     */
    public double getInclinationSlew()
    {
        return inclinationSlew;
    }
}
