package com.soartech.simjr.sensors.radar;

public class RadarBound
{
    private final double high;
    private final double low;

    public RadarBound(double low, double high)
    {
        this.low = low;
        this.high = high;
    }

    public double getHigh()
    {
        return high;
    }

    public double getLow()
    {
        return low;
    }

    public static RadarBound NoBounds()
    {
        return new RadarBound(Double.MIN_NORMAL, Double.MAX_VALUE);
    }
}
