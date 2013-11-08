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
 * Created on Jan 30, 2010
 */
package com.soartech.simjr.sim;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;

/**
 * A lazy geodetic coordinate calculator for use as the value of the 
 * {@link EntityConstants#PROPERTY_LATITUDE} property (and friends) of an 
 * entity.
 * 
 * @author ray
 */
public class LazyGeodeticProperty
{
    private final Entity entity;
    private Vector3 lastPosition;
    private double lastLatitude;
    private double lastLongitude;
    private double lastAltitude;
    
    /**
     * @return a lazy property value for latitude
     */
    public LazyEntityPropertyValue latitude()
    {
        return new LazyEntityPropertyValue()
        {
            public Object getValue(){  return update().lastLatitude;  }
            public String toString() { return getValue().toString(); }
        };
    }
    
    /**
     * @return a lazy property value for longitude
     */
    public LazyEntityPropertyValue longitude()
    {
        return new LazyEntityPropertyValue()
        {
            public Object getValue(){ return update().lastLongitude; }
            public String toString() { return getValue().toString(); }
        };
    }
    
    /**
     * @return a lazy property value for altitude
     */
    public LazyEntityPropertyValue altitude()
    {
        return new LazyEntityPropertyValue()
        {
            public Object getValue() { return update().lastAltitude; }
            public String toString() { return getValue().toString(); }
        };
    }
    
    public LazyGeodeticProperty(Entity entity)
    {
        this.entity = entity;
    }
    
    private LazyGeodeticProperty update()
    {
        final Simulation sim = entity.getSimulation();
        
        if(sim == null) { return this; }
        
        synchronized(sim.getLock())
        {
            final Vector3 newPos = entity.getPosition();
            if(!newPos.equals(lastPosition))
            {
                lastPosition = newPos;
                Geodetic.Point lla = sim.getTerrain().toGeodetic(lastPosition);
                lastLatitude = Math.toDegrees(lla.latitude);
                lastLongitude = Math.toDegrees(lla.longitude);
                lastAltitude = lla.altitude;
            }
        }
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        update();
        return lastLatitude + "/" + lastLongitude + "/" + lastAltitude;
    }
}
