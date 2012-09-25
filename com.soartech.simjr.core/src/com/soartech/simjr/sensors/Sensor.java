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

import java.util.List;

import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.Tickable;

public interface Sensor extends Tickable
{

    /**
     * Sets the owning entity of the sensor.
     * 
     * @param entity
     */
    public void setEntity(Entity entity);
    
    /**
     * Returns the owning entity of the sensor.
     * 
     * @return
     */
    public Entity getEntity();

    /**
     * Returns the name of the sensor type.
     * 
     * @return
     */
    public String getName();

    /**
     * Turns the sensor on (true) and off (false).
     */
    public void setEnabled(boolean enabled);
    
    /**
     * Returns true if sensor is enable and false otherwise.
     * 
     * @return
     */
    public boolean isEnabled();
    
    /**
     * A tick function that should get called every tick cycle that allows the 
     * sensor implementation to do any processing that it needs to do.
     * 
     * @param dt
     */
    @Override
    public void tick(double dt);
    
    /**
     * Returns the detections as seen by the sensor. 
     * 
     * @return
     */
    public List<Detection> getDetections();
   
}
