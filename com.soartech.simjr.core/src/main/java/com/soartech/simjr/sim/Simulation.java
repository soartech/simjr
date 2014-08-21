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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.math.Vector3;
import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.AbstractAdaptable;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.services.SimulationService;

/**
 * The main simulation class
 * 
 * @author ray
 */
public class Simulation extends AbstractAdaptable implements SimulationService
{
    private static final Logger logger = LoggerFactory.getLogger(Simulation.class);
    
    public static Simulation findService(ServiceManager services)
    {
        return services.findService(Simulation.class);
    }
    
    private Object lock = new String("Simulation lock");
    
    private final AtomicReference<Double> time = new AtomicReference<Double>(0.0);
    private boolean paused = true;
    private SimulationThread thread;
    private List<SimulationListener> listeners = new CopyOnWriteArrayList<SimulationListener>();
    private Terrain terrain;
    private EntityPrototypeDatabase prototypes = new EntityPrototypeDatabase();
    {
        try
        {
            prototypes.load();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to load default prototypes: " + e.getMessage(), e);
        }
    }
    private List<Entity> entities = new CopyOnWriteArrayList<Entity>();
    private Map<String, Entity> entitiesByName = new HashMap<String, Entity>();
    
    private static class TimerEntry
    {
        double time;
        Runnable runnable;
        public boolean repeat;
        public double period;
    }
    
    private List<TimerEntry> timers = new ArrayList<TimerEntry>();
    private List<TimerEntry> newTimers = new ArrayList<TimerEntry>();
    /**
     * Construct a new simulation and start a {@link SimulationThread} to run it.
     * The simulation is initially paused.
     * 
     * @param terrain the terrain to use
     */
    public Simulation(Terrain terrain)
    {
        this(terrain, true);
    }
    
    /**
     * Construct a new simulation.
     * 
     * @param terrain the terrain to use
     * @param startThread if true, a {@link SimulationThread} is also created and started.
     *  If false, no thread is created and the caller must manually {@link #tick(double)}
     *  the sim.
     */
    public Simulation(Terrain terrain, boolean startThread)
    {
        this.terrain = terrain;
        this.thread = startThread ? new SimulationThread(this) : null;
    }
    
    /**
     * Returns a lock object that clients can synchronize on to gain exclusive
     * access to the simulation. While the lock is held, the simulation will
     * not move forward or be modifiable except by the locking thread.
     * 
     * @return A lock object that clients can synchronize on to gain exclusive
     *      access to the simulation.
     */
    public Object getLock()
    {
        return lock;
    }
  
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#start()
     */
    public void start(ProgressMonitor progress) throws SimulationException
    {
    }

    /**
     * Shutdown the simulation, cleaning up all threads and resources.
     */
    public void shutdown()
    {
        if(thread != null)
        {
            this.thread.shutdown();
            this.thread = null;
        }
    }
    
    public Terrain getTerrain()
    {
        return terrain;
    }
    
    public void setTerrain(Terrain terrain)
    {
        this.terrain = terrain;
    }
    
    public void addListener(SimulationListener listener)
    {
        listeners.add(listener);
    }
    
    public void removeListener(SimulationListener listener)
    {
        listeners.remove(listener);
    }
    
    public void setEntityPrototypes(EntityPrototypeDatabase prototypes)
    {
        this.prototypes = prototypes;
    }
    
    public EntityPrototypeDatabase getEntityPrototypes()
    {
        return prototypes;
    }
    
    /**
     * Add an entity to the simulation. An onEntityAdded() event will be fired.
     * 
     * @param e The entity to add
     * @return The entity
     * @throws IllegalArgumentException If an entity with the same name is
     *      already in the simulation.
     */
    public Entity addEntity(Entity e)
    {
        synchronized (lock)
        {
            if(null != getEntity(e.getName()))
            {
                throw new IllegalArgumentException("Sim already contains an entity named " + e.getName());
            }
            
            e.setSimulation(this);
            if(!entities.contains(e))
            {
                entities.add(e);
                entitiesByName.put(e.getName(), e);
                for(SimulationListener listener : listeners)
                {
                    listener.onEntityAdded(e);
                }
            }
        }
        return e;
    }
    
    /**
     * Remove an entity from the simulation. An onEntityRemoved() event will
     * be fired.
     * 
     * @param e The entity to remove.
     */
    public void removeEntity(Entity e)
    {
        synchronized (lock)
        {
            if(entities.remove(e))
            {
                entitiesByName.remove(e.getName());
                for(SimulationListener listener : listeners)
                {
                    listener.onEntityRemoved(e);
                }
                e.setSimulation(null);
            }
        }
    }
    
    /**
     * @return A list (copy) of all entities in the simulation
     */
    public List<Entity> getEntities()
    {
        // Lock and return a copy to avoid synchronization issues.
        synchronized (lock)
        {
            return new ArrayList<Entity>(entities);
        }
    }
    
    /**
     * @return The list of all entities. It is assumed that the returned list
     *       will not be modified and will only be used while the sim lock
     *       is held.
     */
    public List<Entity> getEntitiesFast()
    {
        return entities;
    }

    /**
     * Lookup an entity by name
     * 
     * @param name The name of the entity
     * @return The entity or null if not found
     */
    public Entity getEntity(String name)
    {
        synchronized (lock)
        {
            return entitiesByName.get(name);
        }
    }
    
    /**
     * Looks up an entity by name, and assumes that the simulation lock is held.
     * 
     * @param name The name of the entity
     * @return The entity with the corresponding name or null if none exists
     */
    public Entity getEntityFast(String name)
    {
        return entitiesByName.get(name);
    }
    
    /**
     * Returns all entities within a certain distance of the given X/Y point.
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @param tolerance The search radius
     * @return List of entities within "tolerance" of (x, y)
     */
    public List<Entity> getEntities(double x, double y, double tolerance)
    {
        final Vector3 searchPos = new Vector3(x, y, 0.0);
        final List<Entity> r = new ArrayList<Entity>();
        final double toleranceSquared = tolerance * tolerance;
        synchronized(lock)
        {
            for(Entity e : entities)
            {
                Vector3 pos = e.getPosition();
                Vector3 xyPos = new Vector3(pos.x, pos.y, 0.0);
                
                if(xyPos.distanceSquared(searchPos) < toleranceSquared)
                {
                    r.add(e);
                }
            }
        }
        return r;
    }
    
    public List<Entity> getEntities(Vector3 location, double tolerance)
    {
        final List<Entity> r = new ArrayList<Entity>();
        final double toleranceSquared = tolerance * tolerance;
        synchronized(lock)
        {
            for(Entity e : entities)
            {
                if(location.distanceSquared(e.getPosition()) < toleranceSquared)
                {
                    r.add(e);
                }
            }
        }
        return r;
        
    }
    
    /**
     * @return The current simulation time
     */
    public double getTime()
    {
        return time.get();
    }
    
    /**
     * Set the current simulation time. Fires {@link SimulationListener#onTimeSet(double)}.
     * 
     * @param time the new simulation time
     * @throws IllegalArgumentException if time is negative
     */
    public void setTime(double time)
    {
        if(time < 0.0)
        {
            throw new IllegalArgumentException("Simulation time must be zero or positive, got " + time);
        }
        logger.info("Manually setting simulation time to " + time);
        final double oldTime = getTime();
        this.time.set(time);
        for(SimulationListener listener : listeners)
        {
            listener.onTimeSet(oldTime);
        }
    }
    
    /**
     * @return True if the simulation is currently paused
     */
    public boolean isPaused()
    {
        synchronized(lock)
        {
            return paused;
        }
    }
    
    /**
     * Set the pause state of the simulation. If the simulation was created
     * with a thread, it will start running when it is unpaused.
     * 
     * @param paused The pause state of the simulation
     */
    public void setPaused(boolean paused)
    {
        synchronized(lock)
        {
            this.paused = paused;
        }
        
        // Listeners are notified outside with the sim unlocked
        // in case they need to do a synchronous interaction with a thread
        // that needs to lock the sim.
        for(SimulationListener listener : listeners)
        {
            if(paused)
            {
                listener.onPause();
            }
            else
            {
                listener.onStart();
            }
        }
    }
    
    /**
     * Set a timer in the simulation.
     * 
     * <p>The timer callback is called after the simulation tick has completed.
     * The simulation lock is <b>not</b> held while the timer is called, but
     * the simulation thread (the code calling {@link #tick(double)}) will not
     * proceed until the timer callback returns. 
     * 
     * @param period The period in seconds (simulation time) of the timer.
     * @param repeat If true, the timer will be called repeatedly. Otherwise,
     *      it will be called once after the timeout has expired
     * @param runnable The code to run
     */
    public void setTimer(double period, boolean repeat, Runnable runnable)
    {
        synchronized(getLock())
        {
            logger.info(String.format("Adding %s timer with period %8.2f: %s", repeat ? "repeating" : "one-shot", period, runnable));
            final TimerEntry e = new TimerEntry();
            e.time = time.get() + period;
            e.period = period;
            e.runnable = runnable;
            e.repeat = repeat;
          
            newTimers.add(e);
        }
    }
    
    public void detonate(Detonation detonation)
    {
        synchronized(lock)
        {
            detonation.detonate();
            for(SimulationListener listener : listeners)
            {
                listener.onDetonation(detonation);
            }
        }
    }
    
    /**
     * Perform a manual tick of the simulation, independent of the simulation thread and
     * the system clock.
     * 
     * @param dt The time that has passed.
     */
    public void tick(double dt)
    {
        synchronized (lock)
        {
            time.set(time.get() + dt);
            // Entities may be removed/added during this loop, but we're using
            // CopyOnWrite array list so it's ok.
            for(Entity e : entities)
            {
                e.tick(dt);
            }
        }
        
        updateTimers();
        
        for(SimulationListener listener : listeners)
        {
            listener.onTick(dt);
        }
    }

    /**
     * Update timers during a tick
     */
    private void updateTimers()
    {
        synchronized(lock)
        {
            timers.addAll(newTimers);
            newTimers.clear();
            final Iterator<TimerEntry> it = timers.iterator();
            while(it.hasNext())
            {
                final TimerEntry te = it.next();
                if(time.get() >= te.time)
                {
                    if(!te.repeat)
                    {
                        it.remove();
                    }
                    else
                    {
                        te.time = te.time + te.period;
                    }
                    te.runnable.run();
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.adaptables.AbstractAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class<?> klass)
    {
        // The sim creates and maintains the prototype database. This makes it
        // discoverable through the ServiceManager.findService() interface.
        if(klass.isAssignableFrom(EntityPrototypeDatabase.class))
        {
            return prototypes;
        }
        
        final Object fromThread = Adaptables.adapt(thread, klass);
        if(fromThread != null)
        {
            return fromThread;
        }
        
        return super.getAdapter(klass);
    }
    
    
}
