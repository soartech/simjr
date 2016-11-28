/*
 * Copyright (c) 2012, Soar Technology, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * * Neither the name of Soar Technology, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without the specific prior written permission of Soar Technology, Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on July 24, 2012
 */
package com.soartech.simjr.sensors;

import junit.framework.TestCase;

import com.soartech.math.Vector3;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.EntityPrototypes;
import com.soartech.simjr.sim.SimpleTerrain;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.entities.Vehicle;
import com.soartech.simjr.util.ExtendedProperties;

public class GenericVisualSensorTest extends TestCase
{

    public void testBasicSensing()
    {
        Simulation sim = new Simulation(SimpleTerrain.createExampleTerrain(), false);
        
        Vehicle fwa = new Vehicle("fwa", EntityPrototypes.NULL);
        SensorPlatform sp = Adaptables.adapt(fwa, SensorPlatform.class);
        assertNotNull(sp);
        
        GenericVisualSensor sensor = new GenericVisualSensor("generic-visual-sensor", new ExtendedProperties());
        sensor.setVisualAngle(Math.PI);
        assertTrue(sensor.isEnabled());
        assertTrue(sensor.getDetections().isEmpty());
        
        sp.addSensor("main-visual",sensor);
        assertEquals(1, sp.getSensors().size());
        assertEquals(sensor, sp.getSensorByName("main-visual"));
        assertEquals(fwa, sp.getEntity());
        
        sim.addEntity(fwa);        
        assertEquals(1, sim.getEntities().size());
        
        sim.tick(1.0);
        
        // After the first tick there should still be no detections
        assertTrue(sensor.getDetections().isEmpty());
        
        // Now add another vehicle right in front of the sensor and tick it. There should be one detection
        Vehicle target = new Vehicle("target", EntityPrototypes.NULL);
        target.setPosition(new Vector3(0.,500.,0.));
        sim.addEntity(target);
        
        sim.tick(1.0);
        assertEquals(1, sensor.getDetections().size());
        assertEquals(target, sensor.getDetections().get(0).getTargetEntity());
        assertEquals(DetectionType.VISIBLE, sensor.getDetections().get(0).getType());

        // Now add another vehicle out of range of the sensor and tick it. There should be still be one detection
        Vehicle target2 = new Vehicle("target-2", EntityPrototypes.NULL);
        target2.setPosition(new Vector3(0., 8000., 0.));
        sim.addEntity(target2);
        
        sim.tick(1.0);
        assertEquals(1, sensor.getDetections().size());
        assertEquals(target, sensor.getDetections().get(0).getTargetEntity());
        assertEquals(DetectionType.VISIBLE, sensor.getDetections().get(0).getType());
        
        // Move that vehicle to another out of range location
        target2.setPosition(new Vector3(5000., -1., 0.));
        sim.tick(1.0);
        assertEquals(1, sensor.getDetections().size());
        assertEquals(target, sensor.getDetections().get(0).getTargetEntity());
        assertEquals(DetectionType.VISIBLE, sensor.getDetections().get(0).getType());

        // Now move it in range
        target2.setPosition(new Vector3(5000.,1.,0.));
        sim.tick(1.0);
        assertEquals(2, sensor.getDetections().size());
        assertEquals(target, sensor.getDetections().get(0).getTargetEntity());
        assertEquals(DetectionType.VISIBLE, sensor.getDetections().get(0).getType());
        assertEquals(DetectionType.VISIBLE, sensor.getDetections().get(1).getType());
        assertEquals(sensor, sensor.getDetections().get(0).getSourceSensor());
        assertEquals(sensor, sensor.getDetections().get(1).getSourceSensor());
    }

}
