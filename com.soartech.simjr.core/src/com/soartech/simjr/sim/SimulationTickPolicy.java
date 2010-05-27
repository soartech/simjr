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
 * Created on Sep 19, 2009
 */
package com.soartech.simjr.sim;

/**
 * @author ray
 */
public interface SimulationTickPolicy
{
    /**
     * Returns the nominal period of each simulation tick. It can be expected
     * that the dt value passed to Entity.tick() will generally be about
     * getTickPeriod() * getTimeFactor().
     * 
     * @return The nominal period of each simulation tick.
     */
    public double getTickPeriod();
    
    /**
     * Set the desired tick period. Smaller values will give more fine-grained
     * ticks at the expense of CPU usage.
     * 
     * @param tickPeriod Desired tick period
     */
    public void setTickPeriod(double tickPeriod);

    /**
     * This method is responsible for ticking the simulation (with {@link Simulation#tick(double)}
     * according to its policy.
     * 
     * @param sim the simulation
     */
    void tick(Simulation sim);
    
    /**
     * This method is called when the simulation is paused. It allows the
     * policy to maintain consistency when the simulation is paused. When
     * the simulation is restarted, {@link #tick(Simulation)} will be called
     * again.
     * 
     * @param sim the simulation
     */
    void pause(Simulation sim);
    
}