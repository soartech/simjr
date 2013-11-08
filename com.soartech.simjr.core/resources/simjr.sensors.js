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
 */ 
/*
    Common utilities for working with entity sensors in scripts.
    
    Load with: requireScript("sensors");
*/

/**
    Add a sensor to an entity's sensor platform.
    
    @param entity The entity
    @param sensorType The name of the sensor type. Must be in simjr.sensors.properties
    @param name The name of the sensor used as a unique sensor id w.r.t. the vehicle (e.g "main radar", ...)
    @return The added sensor or null if the entity does not have a sensor platform or the sensor type can't be found
*/
function addSensor(entity, sensorType, sensorName)
{
    var sensorPlatform = EntityTools.getSensorPlatform(entity);
    if ( sensorPlatform == null)
    {
        logger.error("simjr.sensors.js:addSensor(): Entity '" + entity.getName() + "' has no sensor platform");
        return null;
    }
    var sensor = Packages.com.soartech.simjr.sensors.Sensor.load(sensorType);
    sensorPlatform.addSensor(sensorName, sensor);
    return sensor;
}
