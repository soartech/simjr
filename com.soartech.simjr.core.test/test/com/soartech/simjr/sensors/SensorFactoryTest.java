/*
 * Copyright (c) 2010, Soar Technology, Inc.
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

import com.soartech.simjr.sensors.radar.AdvancedModalRadar;

public class SensorFactoryTest extends TestCase
{
    
    public void testLoadGodsEyeSensor() 
    {
        Sensor s = SensorFactory.load("gods-eye");
        assertNotNull(s);
        assertTrue(s instanceof GodsEyeSensor);
        assertEquals("gods-eye",s.getName());
        assertNull(s.getEntity());
        assertTrue(s.getDetections().isEmpty());
    }
    
    public void testLoadGenericRadarSensor() 
    {
        Sensor s = SensorFactory.load("generic-radar");
        assertNotNull(s);
        assertTrue(s instanceof GenericRadarSensor);
        assertEquals("generic-radar",s.getName());
        assertNull(s.getEntity());
        assertTrue(s.getDetections().isEmpty());        
    }
    
    public void testLoadGenericVisualSensor() 
    {
        Sensor s = SensorFactory.load("generic-visual");
        assertNotNull(s);
        assertTrue(s instanceof GenericVisualSensor);
        assertEquals("generic-visual",s.getName());
        assertNull(s.getEntity());
        assertTrue(s.getDetections().isEmpty());
    }
    
    public void testLoadGenericRadarWarningSensor()
    {
        Sensor s = SensorFactory.load("generic-radar-warning");
        assertNotNull(s);
        assertTrue(s instanceof GenericRadarWarningSensor);
        assertEquals("generic-radar-warning",s.getName());
        assertNull(s.getEntity());
        assertTrue(s.getDetections().isEmpty());
    }
    
    public void testLoadAdvancedModalRadarSensor()
    {
        Sensor s = SensorFactory.load("advanced-modal-radar");
        assertNotNull(s);
        assertTrue(s instanceof AdvancedModalRadar);
        assertEquals("advanced-modal-radar",s.getName());
        assertNull(s.getEntity());
        assertTrue(s.getDetections().isEmpty());
    }

}
