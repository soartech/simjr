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

import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.adaptables.AbstractAdaptable;
import com.soartech.simjr.adaptables.Adaptables;

/**
 * Default implementation of {@link SimulationTickPolicy}. This policy uses
 * wall clock time and is relatively low-precision. The {@code dt} passed
 * to {@link Simulation#tick(double)} will vary. When the tick is scaled with
 * {@link ScalableTickPolicy}, {@code dt} will scale as well so fidelity will
 * be lost.
 * 
 * @author ray
 */
public class DefaultSimulationTickPolicy extends AbstractAdaptable implements SimulationTickPolicy, ScalableTickPolicy
{
    private static final Logger logger = Logger.getLogger(DefaultSimulationTickPolicy.class);
    
    private final AtomicReference<Double> timeFactor = new AtomicReference<Double>(1.0);
    private final AtomicReference<Double> tickPeriod = new AtomicReference<Double>(SimJrProps.get("simjr.simulation.tickPeriod", 0.200));
    private long lastTick = 0; // only accessed from this thread
    
    /**
     * Create a new policy and install it on the given simulation
     * 
     * @param sim the simulation
     * @return the new policy
     */
    public static DefaultSimulationTickPolicy install(Simulation sim)
    {
        final SimulationThread thread = Adaptables.adapt(sim, SimulationThread.class);
        final DefaultSimulationTickPolicy policy = new DefaultSimulationTickPolicy();
        thread.setTickPolicy(policy);
        return policy;
    }
    
    public DefaultSimulationTickPolicy()
    {
        logger.info("created with tickPeriod " + tickPeriod);
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.SimulationTickPolicy#getTickPeriod()
     */
    public double getTickPeriod()
    {
        return tickPeriod.get();
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.SimulationTickPolicy#setTickPeriod(double)
     */
    public void setTickPeriod(double tickPeriod)
    {
        this.tickPeriod.set(tickPeriod);
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.ScalableTickPolicy#getTimeFactor()
     */
    public double getTimeFactor()
    {
        return timeFactor.get();
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.ScalableTickPolicy#setTimeFactor(double)
     */
    public void setTimeFactor(double timeFactor)
    {
        this.timeFactor.set(timeFactor);
    }
    

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.SimulationTickPolicy#pause(com.soartech.simjr.sim.Simulation)
     */
    public void pause(Simulation sim)
    {
        lastTick = 0;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.SimulationTickPolicy#tick(com.soartech.simjr.sim.Simulation)
     */
    public void tick(Simulation sim)
    {
        tickSimulation(sim);
        try
        {
            // Sleep a little bit to give up the processor
            long ms = (long) (getTickPeriod() * 1000); 
            Thread.sleep(ms);
        }
        catch (InterruptedException e)
        {
        }
    }
    
    private void tickSimulation(Simulation sim)
    {
        if(sim.isPaused())
        {
            lastTick = 0;
            return;
        }
        
        // skip first tick after init or pause
        if(lastTick == 0)
        {
            lastTick = System.currentTimeMillis();
            return;
        }
        
        final long newTick = System.currentTimeMillis();
        
        // If someone changes the system time while running, this can
        // happen. So, we'll just skip it and hope everything works out
        // on the next tick.
        if(newTick <= lastTick)
        {
            logger.warn("System time went backwards. Skipping tick.");
            lastTick = newTick;
            return;
        }
        
        final double dt = ((newTick - lastTick) / 1000.0) * timeFactor.get();
        lastTick = newTick;
    
        sim.tick(dt);
    }

}
