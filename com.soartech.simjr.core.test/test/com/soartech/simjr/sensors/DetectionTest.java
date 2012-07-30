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

import java.util.HashMap;

import junit.framework.TestCase;

import com.soartech.simjr.sim.EntityPrototypes;
import com.soartech.simjr.sim.entities.Vehicle;

public class DetectionTest extends TestCase
{
    public void testBasicProperties() {
        
        Vehicle veh = new Vehicle("fwa", EntityPrototypes.NULL);
        
        HashMap<String,Object> props = new HashMap<String,Object>();
        props.put("test-prop-1", Integer.valueOf(10));
        props.put("test-prop-2", "MyTestString");
        props.put("test-prop-3", 3.0);
        
        Sensor sensor = SensorFactory.load("generic-radar");
        Detection detection = new Detection(sensor, veh, props, DetectionType.RADAR);
        
        // This property is used to test if the detection makes a copy of the properties
        props.put("test-prop-4", "Shouldn't end up in detection properties.");
        
        assertEquals(sensor, detection.getSourceSensor());
        assertEquals(veh, detection.getTargetEntity());
        assertEquals(DetectionType.RADAR, detection.getType());
        assertEquals(10, detection.getProperty("test-prop-1"));
        assertEquals("MyTestString", detection.getProperty("test-prop-2"));
        assertEquals(3.0, detection.getProperty("test-prop-3"));
        assertNull( detection.getProperty("test-prop-4") );
    }
}
