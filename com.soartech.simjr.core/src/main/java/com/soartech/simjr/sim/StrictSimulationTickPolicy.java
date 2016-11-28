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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.adaptables.AbstractAdaptable;
import com.soartech.simjr.adaptables.Adaptables;

/**
 * This is a "strict" implementation of {@link SimulationTickPolicy}, i.e. the
 * {@code dt} passed to {@link Simulation#tick(double)} will always be the same
 * value, i.e. {@link #getTickPeriod()}. Use this policy for a better chance at
 * deterministic runs.
 * 
 * <p>This policy supports a batch mode ({@link #isBatch()}. When enabled, no
 * sleep will be inserted between ticks, i.e. the simulation thread will run as
 * fast as possible.
 * 
 * @author ray
 */
public class StrictSimulationTickPolicy extends AbstractAdaptable implements  SimulationTickPolicy, ScalableTickPolicy
{
    private static final Logger logger = LoggerFactory.getLogger(StrictSimulationTickPolicy.class);
    
    private final AtomicReference<Double> timeFactor = new AtomicReference<Double>(1.0);
    private final AtomicReference<Double> tickPeriod = new AtomicReference<Double>(SimJrProps.get("simjr.simulation.tickPeriod", 0.200));
    private final AtomicBoolean batch = new AtomicBoolean(false);
    
    /**
     * Install this policy on the given simulation.
     * 
     * @param sim the simulation
     * @return the new policy
     */
    public static StrictSimulationTickPolicy install(Simulation sim)
    {
        final SimulationThread thread = Adaptables.adapt(sim, SimulationThread.class);
        final StrictSimulationTickPolicy newPolicy = new StrictSimulationTickPolicy();
        thread.setTickPolicy(newPolicy);
        return newPolicy;
    }
    
    protected StrictSimulationTickPolicy()
    {
        logger.info("created with tickPeriod " + tickPeriod);        
    }
    
    public boolean isBatch()
    {
        return batch.get();
    }
    
    public void setBatch(boolean batch)
    {
        this.batch.set(batch);
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
        if(tickPeriod <= 0.0)
        {
            throw new IllegalArgumentException("tickPeriod must be > 0.0");
        }
        this.tickPeriod.set(tickPeriod);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.SimulationTickPolicy#tick(com.soartech.simjr.sim.Simulation)
     */
    public void tick(Simulation sim)
    {
        sim.tick(getTickPeriod());
        
        if(!isBatch())
        {
            try
            {
                // Sleep a little bit to give up the processor
                long ms = (long) ((getTickPeriod() / getTimeFactor() ) * 1000); 
                Thread.sleep(ms);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt(); // preserve interrupted flag
            }
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.SimulationTickPolicy#pause(com.soartech.simjr.sim.Simulation)
     */
    public void pause(Simulation sim)
    {
        // Nothing to do
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
        if(timeFactor <= 0.0)
        {
            throw new IllegalArgumentException("timeFactor must be > 0.0");
        }
        this.timeFactor.set(timeFactor);
    }

}
