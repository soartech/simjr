package com.soartech.simjr.sensors.radar;

public class RadarDegreeBound
{

    private double spread;
    
    public RadarDegreeBound(double spread)
    {
        this.setSpread(spread);
    }

    public static RadarDegreeBound NoBounds() {
        return new RadarDegreeBound(360.0);
    }

    /**
     * Maximum spread of radar, in degrees.
     */
    public double getSpread()
    {
        return spread;
    }
    
    /**
     * Maximum spread of radar, in radians.
     */
    public double getSpreadRadians()
    {
        return Math.toRadians(getSpread());
    }

    public void setSpread(double spread)
    {
        this.spread = spread;
    }
    
}
