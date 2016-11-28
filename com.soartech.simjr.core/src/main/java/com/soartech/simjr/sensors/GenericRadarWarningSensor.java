package com.soartech.simjr.sensors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.util.ExtendedProperties;

public class GenericRadarWarningSensor extends AbstractSensor
{
    private List<Detection> detections = new ArrayList<Detection>();
    
    public GenericRadarWarningSensor(String name, ExtendedProperties props) 
    {
        super(name);
    }

    @Override
    public void tick(double dt)
    {
        // Clearing out old detections
        detections.clear();

        if ( isEnabled() ) {
            for (Entity entity : this.getEntity().getSimulation().getEntitiesFast() ) {
                // Creating radar warning detections by iterating over the radar detections
                // of the other entities in the sim.
                if ( entity != this.getEntity() ) {
                    SensorPlatform sensorPlatform = Adaptables.adapt(entity, SensorPlatform.class);

                    if ( sensorPlatform != null ) {
                        for (Sensor sensor : sensorPlatform.getSensors()) {
                            RadarSensor radar = Adaptables.adapt(sensor, RadarSensor.class);
                            if ( radar != null ) {
                                for ( Detection detection : radar.getDetections() ) {
                                    if ( detection.getTargetEntity() == this.getEntity() ) {
                                        detections.add(new Detection(this, 
                                                                     detection.getSourceSensor().getEntity(),
                                                                     DetectionType.RADAR_WARNING));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<Detection> getDetections()
    {
        return Collections.unmodifiableList(detections);
    }

}
