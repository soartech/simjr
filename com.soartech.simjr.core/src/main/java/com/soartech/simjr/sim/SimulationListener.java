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
 * Created on May 22, 2007
 */
package com.soartech.simjr.sim;


/**
 * Listener interface for Simulation events
 * 
 * @author ray
 */
public interface SimulationListener
{
    /**
     * Called after the time has been set manually with {@link Simulation#setTime(double)}.
     * 
     * @param oldTime the previous time of the simulation
     */
    void onTimeSet(double oldTime);
    
    /**
     * Called when a new entity is added to the simulation
     * 
     * @param e The entity
     */
    void onEntityAdded(Entity e);
    
    /**
     * Called when an entity is removed from the simulation
     * 
     * @param e The entity
     */
    void onEntityRemoved(Entity e);
    
    /**
     * Called when the simulation is paused
     */
    void onPause();
    
    /**
     * Called when the simulation is started
     */
    void onStart();
    
    /**
     * Called each time the simulation is "ticked" forward while it is running.
     * 
     * @param dt Amount of time, in simulation seconds, since the last 
     *      simulation tick.
     */
    void onTick(double dt);
    
    /**
     * Called when a weapon detonates on a target or particular location
     * 
     * @param detonation The detonation event
     */
    void onDetonation(Detonation detonation);
}
