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

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.simjr.adaptables.AbstractAdaptable;
import com.soartech.simjr.adaptables.Adaptables;

/**
 * This is the thread responsible for running the simulation, i.e. periodically
 * calling Simulation.tick() at the correct rate based on current simulation
 * rate settings, etc.  This class is only intended for internal use by the
 * Simulation class.
 * 
 * @author ray
 */
public class SimulationThread extends AbstractAdaptable implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(SimulationThread.class);
    
    private final Simulation sim;
    
    /**
     * A signal object that we use to tell the thread to start or stop.
     */
    private Object signal = new String("Simulation thread signal");
    private volatile boolean shutdown = false;
    private final AtomicReference<SimulationTickPolicy> tickPolicy = new AtomicReference<SimulationTickPolicy>(new DefaultSimulationTickPolicy());
    private final Thread thread;
    
    public SimulationThread(Simulation sim)
    {
        this.sim = sim;
        sim.addListener(new Listener());
        
        logger.info("Starting simulation thread");
        thread = new Thread(this, "Sim Jr. simulation thread");
        thread.start();
    }

    public SimulationTickPolicy getTickPolicy()
    {
        return tickPolicy.get();
    }
    
    public void setTickPolicy(SimulationTickPolicy tickPolicy)
    {
        if(tickPolicy == null)
        {
            throw new IllegalArgumentException("tickPolicy cannot be null");
        }
        this.tickPolicy.set(tickPolicy);
    }
    
    public void shutdown()
    {
        synchronized(signal)
        {
            shutdown = true;
            signal.notify();
        }
        
        try
        {
            logger.info("Waiting for simulation thread to exit");
            thread.join();
        }
        catch (InterruptedException e)
        {
            logger.error("Interrupted while waiting for sim thread to exit", e);
            Thread.currentThread().interrupt();
        }
    }
    
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.adaptables.Adaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class<?> klass)
    {
        final Object fromPolicy = Adaptables.adapt(tickPolicy.get(), klass);
        if(fromPolicy != null)
        {
            return fromPolicy;
        }
        return super.getAdapter(klass);
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        logger.info("Simulation thread started");
        while(!shutdown)
        {
            synchronized(signal)
            {
                // Wait patiently while the sim is paused. We'll get a signal
                // when it is started again.
                while(!shutdown && sim.isPaused())
                {
                    try
                    {
                        signal.wait();
                    }
                    catch (InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            
            if(!shutdown)
            {
                tickPolicy.get().tick(sim);
            }
        }
    }
    
    private class Listener extends SimulationListenerAdapter
    {
        public void onStart()
        {
            synchronized(signal)
            {
                signal.notify();
            }
        }

        /* (non-Javadoc)
         * @see com.soartech.simjr.sim.SimulationListenerAdapter#onPause()
         */
        @Override
        public void onPause()
        {
            tickPolicy.get().pause(sim);
        }
        
        
    }
}
