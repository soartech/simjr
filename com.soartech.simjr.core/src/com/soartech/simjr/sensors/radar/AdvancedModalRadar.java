package com.soartech.simjr.sensors.radar;

import java.util.List;

import com.soartech.simjr.sensors.AbstractSensor;
import com.soartech.simjr.sensors.Detection;
import com.soartech.simjr.sensors.RadarSensor;

public class AdvancedModalRadar extends AbstractSensor implements RadarSensor
{

    public AdvancedModalRadar(String name)
    {
        super(name);
    }

    @Override
    public void tick(double dt)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<Detection> getDetections()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
