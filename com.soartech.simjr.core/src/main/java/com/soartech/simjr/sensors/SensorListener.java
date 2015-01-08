/* The Government has unlimited rights to this software. All other parties have
 * no rights to use, distribute, reproduce, modify, reverse engineer, or
 * otherwise utilize this software.
 *
 * All ownership rights are retained by Soar Technology, Inc.
 *
 * (C)2015 SoarTech, Proprietary, All Rights Reserved.
 */
package com.soartech.simjr.sensors;

/**
 * @author eric.tucker
 */
public interface SensorListener
{
    /**
     * Called whenever a new detection is generated.
     * @param detection Detection that was generated.
     */
    public void generatedDetection(Detection detection);

    /**
     * Called whenever a detection is no longer detected by the sensor.
     * @param detection Detection that has been destroyed.
     */
    public void destroyedDetection(Detection detection);
}
