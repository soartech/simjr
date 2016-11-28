package com.soartech.simjr.sensors.radar;

public class RadarScanDistanceRange
{
    public static boolean contains(double low, double high, double distance)
    {
        return low <= distance && distance <= high;
    }
}
