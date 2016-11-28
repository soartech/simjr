package com.soartech.simjr.sensors.radar;

import junit.framework.TestCase;

import com.soartech.math.Vector3;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sensors.DetectionType;
import com.soartech.simjr.sensors.SensorPlatform;
import com.soartech.simjr.sim.EntityPrototypes;
import com.soartech.simjr.sim.SimpleTerrain;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.entities.Vehicle;
import com.soartech.simjr.util.ExtendedProperties;

public class AdvancedModalRadarTest extends TestCase
{

    public void testBasicSensing() 
    {
        Simulation sim = new Simulation(SimpleTerrain.createExampleTerrain(), false);
        
        Vehicle fwa = new Vehicle("fwa", EntityPrototypes.NULL);
        SensorPlatform sp = Adaptables.adapt(fwa, SensorPlatform.class);
        assertNotNull(sp);
        
        AdvancedModalRadar sensor = new AdvancedModalRadar("advanced-modal-radar", new ExtendedProperties());
        assertTrue(sensor.isEnabled());
        assertTrue(sensor.getDetections().isEmpty());
        
        sp.addSensor("main-radar",sensor);
        assertEquals(1, sp.getSensors().size());
        assertEquals(sensor, sp.getSensorByName("main-radar"));
        assertEquals(fwa, sp.getEntity());
        
        sim.addEntity(fwa);        
        assertEquals(1, sim.getEntities().size());
        
        sim.tick(1.0);
        
        // After the first tick there should still be no detections
        assertTrue(sensor.getDetections().isEmpty());
        
        // Now add another vehicle right in front of the sensor and tick it. There should be one detection
        Vehicle target = new Vehicle("target", EntityPrototypes.NULL);
        target.setPosition(new Vector3(0.,50000.,0.));
        target.setVelocity(new Vector3(0.,200.0,0.));
        sim.addEntity(target);
        
        sim.tick(1.0);
        assertEquals(1, sensor.getDetections().size());
        assertEquals(target, sensor.getDetections().get(0).getTargetEntity());
        assertEquals(DetectionType.RADAR, sensor.getDetections().get(0).getType());

        // Now add another vehicle out of range of the sensor and tick it. There should be still be one detection
        Vehicle target2 = new Vehicle("target-2", EntityPrototypes.NULL);
        target2.setPosition(new Vector3(0., 210000., 0.));
        sim.addEntity(target2);
        
        sim.tick(1.0);
        assertEquals(1, sensor.getDetections().size());
        assertEquals(target, sensor.getDetections().get(0).getTargetEntity());
        assertEquals(DetectionType.RADAR, sensor.getDetections().get(0).getType());
        
        // Move that vehicle to another out of range location
        target2.setPosition(new Vector3(50000., -1., 0.));
        target2.setVelocity(new Vector3(0.,200.0,0.));
        sim.tick(1.0);
        assertEquals(1, sensor.getDetections().size());
        assertEquals(target, sensor.getDetections().get(0).getTargetEntity());
        assertEquals(DetectionType.RADAR, sensor.getDetections().get(0).getType());

        // Now move it in range
        target2.setPosition(new Vector3(5000.,50000.,0.));
        sim.tick(1.0);
        assertEquals(2, sensor.getDetections().size());
        assertEquals(target, sensor.getDetections().get(0).getTargetEntity());
        assertEquals(DetectionType.RADAR, sensor.getDetections().get(0).getType());
        assertEquals(DetectionType.RADAR, sensor.getDetections().get(1).getType());
        assertEquals(sensor, sensor.getDetections().get(0).getSourceSensor());
        assertEquals(sensor, sensor.getDetections().get(1).getSourceSensor());
    }
}
