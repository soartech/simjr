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

import java.util.List;
import java.util.Map;

import com.soartech.math.Vector3;
import com.soartech.simjr.adaptables.Adaptable;

/**
 * Interface for an entity in the simulation.
 * 
 * @author ray
 */
public interface Entity extends Adaptable
{
    /**
     * @return The simulation the entity is part of
     */
    Simulation getSimulation();
    
    /**
     * Set the simulation this entity is a part of. This method should only
     * be called by Simulation.
     * 
     * @param sim The simulation.
     * @throws IllegalStateException if this method has already been called.
     */
    void setSimulation(Simulation sim);
    
    /**
     * @return The unique name of the entity
     */
    String getName();
    
    /**
     * @return The prototype used to create this entity
     */
    EntityPrototype getPrototype();
    
    /**
     * @return The current position of the entity, if it has one, Vector3.ZERO 
     *  otherwise.
     */
    Vector3 getPosition();
    
    /**
     * Set the current position of the entity
     * 
     * @param position The new position of the entity.
     */
    void setPosition(Vector3 position);
    
    /**
     * Return true if this entity has a meaningful position. An entity like an
     * area or route don't necessarily have a single position that makes sense.
     * 
     * @return True if this entity has a meaningful position.
     */
    boolean hasPosition();
    
    /**
     * @return The current velocity of the entity
     */
    Vector3 getVelocity();
    
    /**
     * Set the current velocity of the entity
     * 
     * @param velocity The new velocity
     */
    void setVelocity(Vector3 velocity);
    
    Vector3 getAcceleration();
    
    // Keeping these around for backwards compatibility with old scripts
    @Deprecated
    /**
     * Get heading (rotation around Z).
     * 
     * @deprecated Replaced by {@link #getHeading()}
     * @return The heading (rotation around Z) of the entity in radians (MATH radians)
     */
    double getOrientation();
    
    /**
     * Set the yaw (rotation around Z of the entity in radians).
     * 
     * @deprecated Replaced by {@link #setHeading(double)}
     * @param radians Angle in radians (MATH radians)
     */
    @Deprecated
    void setOrientation(double radians);
    
    /**
     * @return The heading (rotation around Z) of the entity in radians (MATH radians)
     */
    double getHeading();
    
    /**
     * Set the yaw (rotation around Z of the entity in radians
     * 
     * @param radians Angle in radians (MATH radians)
     */
    void setHeading(double radians);
    
    /**
     * @return the pitch (rotation around Y axis) of the entity in radians (MATH radians)
     */
    double getPitch();
    
    /**
     * Set the pitch (rotation around the Y axis) of the entity in radians (MATH radians)
     * 
     * @param radians Angle in radians (MATH radians)
     */
    void setPitch(double radians);
    
    /**
     * @return the roll (rotation around X axis) of the entity in radians (MATH radians)
     */
    double getRoll();
    
    /**
     * Set the roll (rotation around the X axis) of the entity in radians (MATH radians)
     * 
     * @param radians Angle in radians (MATH radians)
     */
    void setRoll(double radians);
    
    /**
     * Tick method called by the simulation during each update phase.
     * 
     * @param dt The time, in simulation seconds, that has elapsed since the
     *      last update.
     */
    void tick(double dt);
    
    /**
     * Returns this entity's property map, read-only. Note that, unlike
     * {@link #getProperty(String)}, the returned map may contain values
     * of type {@link LazyEntityPropertyValue}.
     * 
     * @return This entity's properties
     */
    Map<String, Object> getProperties();
    
    /**
     * Force update of calculated properties. It is only necessary to call this
     * method when an aspect of the entity has been changed (position, etc) but
     * the simulation is not currently running. It should only be called with the
     * simulation locked.
     */
    void updateProperties();
    
    /**
     * Retrieve the value of a property on this entity. If the value of the property
     * is an instance of {@link LazyEntityPropertyValue}, then the result of its
     * <code>getValue()</code> method is returned.
     * 
     * @param name The name of the property
     * @return The value of the property, or null if the property is not present
     */
    Object getProperty(String name);
    
    /**
     * Set the value of a static property on this entity. If the value
     * changes, a property changed event will be fired to all property
     * listeners. Setting the value to null will remove the property.
     * 
     * @param name The name of the property
     * @param value The new value or null to remove the property
     */
    void setProperty(String name, Object value);
    
    /**
     * Register a property adapter with this entity.
     * 
     * @param adapter The new property adapter
     */
    void addPropertyAdapter(EntityPropertyAdapter adapter);
    
    /**
     * Remove a property adapter previously added with {@link #addPropertyAdapter(EntityPropertyAdapter)}
     * 
     * @param adapter The adapter to remove
     */
    void removePropertyAdapter(EntityPropertyAdapter adapter);
    
    /**
     * Add a property listener to this entity
     * 
     * @param listener The listener
     */
    void addPropertyListener(EntityPropertyListener listener);
    
    /**
     * Remove a property listener from this entity
     * 
     * @param listener The listener
     */
    void removePropertyListener(EntityPropertyListener listener);
    
    /**
     * Manually fire a property changed event for the named property.
     * 
     * @param name The property name
     */
    void firePropertyChanged(String name);
    
    /**
     * Add the given capability to this entity. The capabilty's {@link EntityCapability#attach(Entity)}
     * method will be called.
     * 
     * <p>If <code>capability</code> implements {@link Tickable}, then the {@link Tickable#tick(double)}
     * method will be called each time the entity is ticked.
     * 
     * @param capability the capability to add
     * @throws IllegalStateException if capability is already attached to an entity
     */
    void addCapability(EntityCapability capability);
    
    /**
     * Remove the given capability from this entity. The capability's
     * {@link EntityCapability#detach()} method will be called
     *  
     * @param capability the capability to remove
     */
    void removeCapability(EntityCapability capability);
    
    /**
     * Returns an unmodifiable list of the entities capabilities. Useful for some introspection type
     * tasks that the Adaptable approach doesn't support (e.g. find all capabilities of type X).
     */
    List<EntityCapability> getCapabilities();
    
    /**
     * Sets the parent of an entity
     * 
     * @param parent of the entity
     */
    void setParent(Entity parent);

    /**
     * Gets the parent of an entity
     * 
     * @return the parent
     */
    Entity getParent();
}
