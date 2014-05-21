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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Entity;

public class DefaultSensorPlatform extends AbstractEntityCapability implements SensorPlatform
{
    private final Map<String,Sensor> sensors = new HashMap<String,Sensor>();
    
    public DefaultSensorPlatform() 
    {
        // Intentionally left blank
    }
    
    @Override
    public void attach(Entity e)
    {
        super.attach(e);
        for(Sensor s : sensors.values())
        {
            s.setEntity(e);
        }
    }
    
    @Override
    public void detach()
    {
        for(Sensor s : sensors.values())
        {
            s.setEntity(null);
        }
        super.detach();
    }

    @Override
    public Collection<Sensor> getSensors()
    {
        return Collections.unmodifiableCollection( sensors.values() );
    }

    @Override
    public Sensor getSensorByName(String name)
    {
        return sensors.get(name);
    }

    @Override
    public void addSensor(String name, Sensor sensor)
    {
        sensors.put(name, sensor);
        sensor.setEntity(getEntity());
    }

    @Override
    public void removeSensor(String name)
    {
        Sensor removedSensor = sensors.remove(name);
        if( removedSensor != null )
        {
            removedSensor.setEntity(null);
        }
    }
    
    @Override
    public void tick(double dt) {
        for ( Sensor sensor : sensors.values() ) {
            if ( sensor.isEnabled() ) 
            {
                sensor.tick(dt);                
            }
        }
    }
    
}