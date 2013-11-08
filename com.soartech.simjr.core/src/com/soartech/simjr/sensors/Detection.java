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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.soartech.simjr.sim.Entity;

/**
 * Simple data structure that represents a detection from a sensor. It has extensible
 * properties so that future sensor implementations can add additional information.
 * 
 * @author rdf
 */
public class Detection
{
    private final Entity target;
    private final Sensor source;
    private final Map<String,Object> properties;
    private final DetectionType type;
    
    public Detection(Sensor source, Entity target, Map<String,Object> properties, DetectionType type) {
        this.source = source;
        this.target = target;
        this.properties = new HashMap<String,Object>(properties);
        this.type = type;
    }
    
    public Detection(Sensor source, Entity target, DetectionType type) {
        this.source = source;
        this.target = target;
        this.properties = Collections.emptyMap();
        this.type = type;
    }
    
    /**
     * @return an unmodifiable version of the detections property map
     */
    public Map<String,Object> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }
    
    /**
     * Returns the entity that was detected. Some implementations may return
     * null for false detections.
     * 
     * @return the detected/target entity
     */
    public Entity getTargetEntity() {
        return this.target;
    }
    
    /**
     * Returns the sensor that detected the entity.
     * 
     * @return the sensor that detected the entity
     */
    public Sensor getSourceSensor() {
        return this.source;
    }
    
    /**
     * Returns a property of the detection or null if none has been set.
     * 
     * @param key name of property
     * @return the value of the property or null if none has been set.
     */
    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    public DetectionType getType() 
    {
        return this.type;
    }
}
