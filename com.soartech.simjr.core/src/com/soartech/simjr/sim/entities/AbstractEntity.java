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
package com.soartech.simjr.sim.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import com.soartech.math.Angles;
import com.soartech.math.Vector3;
import com.soartech.simjr.adaptables.AbstractAdaptable;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.DefaultEntityMotionIntegrator;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityAccelerationProvider;
import com.soartech.simjr.sim.EntityCapability;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityMotionIntegrator;
import com.soartech.simjr.sim.EntityPositionProvider;
import com.soartech.simjr.sim.EntityPropertyAdapter;
import com.soartech.simjr.sim.EntityPropertyAdapters;
import com.soartech.simjr.sim.EntityPropertyListener;
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.LazyEntityPropertyValue;
import com.soartech.simjr.sim.LazyGeodeticProperty;
import com.soartech.simjr.sim.LazyMgrsProperty;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.Tickable;

/**
 * Base entity implementation that handles the details of properties and
 * basic movement. Typical sub-classes will only override updateProperties()
 * and tick()
 *
 * @author ray
 */
public abstract class AbstractEntity extends AbstractAdaptable implements Entity
{
    private static final Logger logger = Logger.getLogger(AbstractEntity.class);

    private final AtomicReference<Simulation> sim = new AtomicReference<Simulation>();
    private final String name;
    private final EntityPrototype prototype;
    private Vector3 position = Vector3.ZERO;
    private Vector3 velocity = Vector3.ZERO;
    
    private double heading = Angles.navRadiansToMathRadians(0);
    private double pitch = 0.;
    private double roll = 0;

    private final List<EntityCapability> capabilities = new CopyOnWriteArrayList<EntityCapability>();
    private final List<Tickable> tickableCaps = new CopyOnWriteArrayList<Tickable>();

    private final Stack<EntityPositionProvider> positionProviderStack = new Stack<EntityPositionProvider>();
    private EntityPositionProvider positionProvider;

    private final Stack<EntityAccelerationProvider> accelerationProviderStack = new Stack<EntityAccelerationProvider>();
    private EntityAccelerationProvider accelerationProvider = EntityAccelerationProvider.NO_ACCELERATION_MODEL;
    
    private EntityMotionIntegrator motionIntegrator = DefaultEntityMotionIntegrator.getInstance();
    
    private Entity parent = null;
    
    /**
     * The base properties of the entity, i.e. those that are not calculated.
     */
    private final Map<String, Object> baseProperties = new ConcurrentHashMap<String, Object>();

    /**
     * The final properties of the entity including calculated properties.
     * Set to null if when properties need to be recalculated.
     */
    private final AtomicReference<Map<String, Object>> finalProperties = new AtomicReference<Map<String,Object>>();

    private final Map<String, EntityPropertyAdapter> propertyAdapters = new ConcurrentHashMap<String, EntityPropertyAdapter>();

    private final List<EntityPropertyListener> propListeners =
       Collections.synchronizedList(new ArrayList<EntityPropertyListener>());

    public AbstractEntity(String name, EntityPrototype prototype)
    {
        this.name = name;
        this.prototype = prototype;

        // Copy default properties from prototype
        baseProperties.putAll(prototype.getProperties());

        // Calculate lat/lon/alt properties lazily
        final LazyGeodeticProperty lgp = new LazyGeodeticProperty(this);
        baseProperties.put(EntityConstants.PROPERTY_LATITUDE, lgp.latitude());
        baseProperties.put(EntityConstants.PROPERTY_LONGITUDE, lgp.longitude());
        baseProperties.put(EntityConstants.PROPERTY_ALTITUDE, lgp.altitude());

        // Calculate MGRS property lazily
        baseProperties.put(EntityConstants.PROPERTY_MGRS, new LazyMgrsProperty(this));

        baseProperties.remove("capabilities"); // caps is a special property of the prototype

        setProperty(EntityConstants.PROPERTY_CLASS, getClass().getCanonicalName());
        setProperty(EntityConstants.PROPERTY_NAME, name);
        setProperty(EntityConstants.PROPERTY_PROTOTYPE, this.prototype);
    }

    /**
     * Method overloaded by sub-classes to add properties to the entity's
     * property set. This approach is taken rather than overriding
     * getProperties() for performance reasons. Sub-classes should AWLAYS
     * call the super version first.
     *
     * @param properties Property map to fill in.
     */
    protected void updateProperties(Map<String, Object> properties)
    {
        properties.put(EntityConstants.PROPERTY_POSITION, getPosition());
        properties.put(EntityConstants.PROPERTY_VELOCITY, velocity);
        properties.put(EntityConstants.PROPERTY_ORIENTATION, heading);
        properties.put(EntityConstants.PROPERTY_YAW, heading);
        properties.put(EntityConstants.PROPERTY_PITCH, pitch);
        properties.put(EntityConstants.PROPERTY_ROLL, roll);
    }

    public void firePropertyChanged(String name)
    {
        synchronized(propListeners)
        {
            for(EntityPropertyListener listener : new ArrayList<EntityPropertyListener>(propListeners))
            {
                listener.onPropertyChanged(this, name);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.Entity#getPrototype()
     */
    public EntityPrototype getPrototype()
    {
        return prototype;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#getPosition()
     */
    public Vector3 getPosition()
    {
        return positionProvider != null ? positionProvider.getPosition() : position;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#hasPosition()
     */
    public boolean hasPosition()
    {
        return positionProvider != null ? positionProvider.hasPosition() : true;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#getVelocity()
     */
    public Vector3 getVelocity()
    {
        return velocity;
    }

    @Override
    public Vector3 getAcceleration()
    {
        return accelerationProvider.getAcceleration(getPosition(), getVelocity(), getSimulation().getTime());
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#getProperties()
     */
    final public Map<String, Object> getProperties()
    {
        // Since updateProperties() often relies on the sim, we only return
        // the base properties until the entity is added to the sim.
        if(sim.get() == null)
        {
            return baseProperties;
        }

        final Map<String, Object> temp = finalProperties.get();
        if(temp != null)
        {
            return temp;
        }

        // Recalculate properties
        final HashMap<String, Object> newFinalProps = new HashMap<String, Object>(baseProperties);
        finalProperties.set(newFinalProps);

        updateProperties(newFinalProps);

        return newFinalProps;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.Entity#getProperty(java.lang.String)
     */
    final public Object getProperty(String name)
    {
        final Object value = getProperties().get(name);
        if(value instanceof LazyEntityPropertyValue)
        {
            return ((LazyEntityPropertyValue) value).getValue();
        }
        return value;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#updateProperties()
     */
    public final void updateProperties()
    {
        finalProperties.set(null);
    }

    private EntityPropertyAdapter getPropertyAdapter(String name)
    {
        EntityPropertyAdapter adapter = propertyAdapters.get(name);

        return adapter != null ? adapter : EntityPropertyAdapters.getDefaultAdapter(name);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#setProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(String name, Object value)
    {
        final Map<String, Object> tempFinalProps = finalProperties.get();
        if(value != null)
        {
            final EntityPropertyAdapter adapter = getPropertyAdapter(name);
            if(adapter != null)
            {
                adapter.setValue(this, value);
                return;
            }

            baseProperties.put(name, value);
            if(tempFinalProps != null)
            {
                tempFinalProps.put(name, value);
            }
        }
        else
        {
            baseProperties.remove(name);
            if(tempFinalProps != null)
            {
                tempFinalProps.remove(name);
            }
        }

        firePropertyChanged(name);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#addPropertyAdapter(com.soartech.simjr.EntityPropertyAdapter)
     */
    public void addPropertyAdapter(EntityPropertyAdapter adapter)
    {
        propertyAdapters.put(adapter.getProperty(), adapter);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#removePropertyAdapter(com.soartech.simjr.EntityPropertyAdapter)
     */
    public void removePropertyAdapter(EntityPropertyAdapter adapter)
    {
        propertyAdapters.values().remove(adapter);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#addPropertyListener(com.soartech.simjr.EntityPropertyListener)
     */
    public void addPropertyListener(EntityPropertyListener listener)
    {
        propListeners.add(listener);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#removePropertyListener(com.soartech.simjr.EntityPropertyListener)
     */
    public void removePropertyListener(EntityPropertyListener listener)
    {
        propListeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#getSimulation()
     */
    public Simulation getSimulation()
    {
        return sim.get();
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#setSimulation(com.soartech.simjr.Simulation)
     */
    public void setSimulation(Simulation sim)
    {
        if(!this.sim.compareAndSet(null, sim))
        {
            if (sim == null)
            {
                this.sim.set(sim);
                return;
            }
            else
            {
                throw new IllegalStateException("sim is already set");
            }
        }

        // Once the sim is set, reset the position in case enforce-agl is on.
        setPosition(this.getPosition());
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#getOrientation()
     */
    public double getHeading()
    {
        return heading;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#setOrientation(double)
     */
    public void setHeading(double radians)
    {
        double rotations = radians/(2.*Math.PI);
        this.heading = (rotations - Math.floor(rotations))*2.*Math.PI;
    }

    public double getOrientation()
    {
        return getHeading();
    }

    public void setOrientation(double radians)
    {
        setHeading(radians);
    }

    /*
     * (non-Javadoc)
     * @see com.soartech.simjr.sim.Entity#getPitch()
     */
    public double getPitch() {
        return pitch;
    }

    /*
     * (non-Javadoc)
     * @see com.soartech.simjr.sim.Entity#setPitch(double)
     */
    public void setPitch(double radians) {
        double rotations = radians/(2.*Math.PI);
        this.pitch = (rotations - Math.floor(rotations))*2.*Math.PI;
        if ( this.pitch > Math.PI ) 
        {
            this.pitch -= 2.*Math.PI;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.soartech.simjr.sim.Entity#getRoll()
     */
    public double getRoll() {
        return roll;
    }

    /*
     * (non-Javadoc)
     * @see com.soartech.simjr.sim.Entity#setRoll(double)
     */
    public void setRoll(double radians) {
        double rotations = radians/(2.*Math.PI);
        this.roll = (rotations - Math.floor(rotations))*2.*Math.PI;
        if ( this.roll > Math.PI ) 
        {
            this.roll -= 2.*Math.PI;
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#setPosition(com.soartech.spatr.math.Vector3)
     */
    public void setPosition(Vector3 position)
    {
        Double forceAgl = EntityTools.getEnforcedAboveGroundLevel(this);
        if(forceAgl == null)
        {
            this.position = position;
        }
        else
        {
            this.position = sim.get().getTerrain().clampPointToGround(position, forceAgl);
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#setVelocity(com.soartech.spatr.math.Vector3)
     */
    public void setVelocity(Vector3 velocity)
    {
        this.velocity = velocity;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Entity#tick(double)
     */
    public final void tick(double dt)
    {
        processTick(dt);

        // Force recalculation of properties
        finalProperties.set(null);
    }

    /**
     * The primary implementation of the tick method. Sub-classes may override
     * this method but must call the super implementation. Sub-classes should
     * not override tick.
     *
     * @param dt The time passed to tick().
     */
    protected void processTick(double dt)
    {
        for(Tickable tickable : tickableCaps)
        {
            tickable.tick(dt);
        }

        Entity container = (Entity) baseProperties.get(EntityConstants.PROPERTY_CONTAINER);
        if(container != null)
        {
            // Set position directly, ignoring enforced AGL while in container
            position = container.getPosition();
        }
        else if(canUpdatePosition())
        {
            updatePosition(dt);
        }
    }

    /**
     * Returns true if the entity can currently move. This is called by tick() to
     * decide whether {@link #updatePosition(double)} is called. The default
     * implementation returns true if the entity is not destroyed.
     *
     * <p>Sub-classes may override this method. They should combine any additional
     * conditions with the return value of the super method.
     *
     * @return True if the entity can move
     */
    protected boolean canUpdatePosition()
    {
        // Only update position if we're not destroyed
        return DamageStatus.destroyed != EntityTools.getDamage(baseProperties);
    }

    /**
     * Update the current position of the entity with the given time delta.
     * The default implementation of this method simply multiplies the current
     * velocity by dt and adds that vector to the current position. It is not
     * called if the entity is destroyed, or it is currently in a container
     * entity.
     *
     * <p>This method may be overridden by sub-classes.  Sub-classes need not
     * call the super implementation.
     *
     * @param dt
     */
    protected void updatePosition(double dt)
    {
        motionIntegrator.updateEntity(this, accelerationProvider, dt);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.Entity#addCapability(com.soartech.simjr.sim.EntityCapability)
     */
    public void addCapability(EntityCapability capability)
    {
        capability.attach(this);
        capabilities.add(capability);
        final boolean tickable = capability instanceof Tickable;
        if(tickable)
        {
            tickableCaps.add((Tickable) capability);
        }

        logger.debug("Added capability " + (tickable? "tickable " : " ") + "'" + capability + "' to entity '" + name + "'");

        final EntityPositionProvider posProvider = Adaptables.adapt(capability, EntityPositionProvider.class);
        if(posProvider != null)
        {
            if(this.positionProvider != null)
            {
                positionProviderStack.push(this.positionProvider);
            }
            positionProvider = posProvider;
        }
        
        final EntityAccelerationProvider accelProvider = Adaptables.adapt(capability, EntityAccelerationProvider.class);
        if (accelProvider != null)
        {
            accelerationProviderStack.push(this.accelerationProvider);
            accelerationProvider = accelProvider;
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.Entity#removeCapability(com.soartech.simjr.sim.EntityCapability)
     */
    public void removeCapability(EntityCapability capability)
    {
        tickableCaps.remove(capability);
        if(capabilities.remove(capability))
        {
            capability.detach();
            logger.info("Removed capability '" + capability + "' from entity '" + name + "'");
        }
        if(capability == positionProvider)
        {
            positionProvider = !this.positionProviderStack.empty() ? this.positionProviderStack.pop() : null;
        }
        if(capability == accelerationProvider)
        {
            accelerationProvider = !this.accelerationProviderStack.empty() ? this.accelerationProviderStack.pop() : null;
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.adaptables.AbstractAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class<?> klass)
    {
        // First check "this"
        Object o = super.getAdapter(klass);

        // Next check capabilities
        if(o == null)
        {
            o = Adaptables.findAdapter(capabilities, klass);
        }

        return o;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public void setParent(Entity parent)
    {
        this.parent = parent;
    }
    
    @Override
    public Entity getParent()
    {
        return parent;
    }
}
