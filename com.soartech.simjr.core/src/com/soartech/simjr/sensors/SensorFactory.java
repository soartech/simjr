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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

import com.soartech.simjr.util.ExtendedProperties;

public class SensorFactory
{
    private static final Logger logger = Logger.getLogger(SensorFactory.class);
    
    private static ExtendedProperties properties = new ExtendedProperties();
    private static final String PROPFILENAME = "/simjr.sensors.properties";
    static
    {
        InputStream stream = Sensor.class.getResourceAsStream(PROPFILENAME);
        if(stream != null)
        {
            try
            {
                properties.load(stream);
            }
            catch (IOException e)
            {
                logger.error(e);
            }
            finally
            {
                try
                {
                    stream.close();
                }
                catch (IOException e)
                {
                }
            }
        }
        else
        {
            logger.error("Failed to load sensor properties file "+PROPFILENAME);
        }
    }
    
    /**
     * Given the name of a sensor in the sensor "database", construct a new sensor
     * object of the appropriate type
     * 
     * @param name The sensor name, e.g. "gods-eye" or "generic-radar"
     * @return New sensor object
     */
    public static Sensor load(String name)
    {
        String klass = properties.getProperty(name+".class", null);
        if(klass == null)
        {
            throw new IllegalArgumentException("Unknown sensor '" + name + "'");
        }
        
        Object retval = null;
        try {
            // TODO: This probably needs to be done differently to load sensors from other OSGI plugins
            // investigate Bundle.loadClass or other OSGI specific mechanisms.
            Class<?> sensorClass = Class.forName(klass);        
            Constructor<?> constructor = sensorClass.getConstructor(String.class, ExtendedProperties.class);        
            retval = constructor.newInstance(name, properties);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to instantiate an instance of sensor '"+name+"'", e);
        }

        if ( retval instanceof Sensor ) {
            return (Sensor) retval;
        } else {
            return null;
        }
    }    
}
