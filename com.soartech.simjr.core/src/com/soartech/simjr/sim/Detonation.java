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
 * Created on Jun 27, 2007
 */
package com.soartech.simjr.sim;

import java.util.List;

import org.apache.log4j.Logger;

import com.soartech.math.Vector3;
import com.soartech.simjr.sim.entities.DamageStatus;
import com.soartech.simjr.weapons.Weapon;

/**
 * Represents a detonation in the simulation.
 * 
 * @author ray
 */
public class Detonation
{
    private static final Logger logger = Logger.getLogger(Detonation.class);
    
    private final Simulation sim;
    private final Weapon weapon;
    private final Entity target;
    private final Vector3 location;
    private List<Entity> damagedEntities;
    
    /**
     * Construct a new detonation
     * 
     * @param weapon The weapon
     * @param target The target of the detonation, or null
     * @param location The location of the detonation. May be null only if
     *      target is non-null.
     */
    public Detonation(Simulation sim, Weapon weapon, Entity target, Vector3 location)
    {
        this.sim = sim;
        this.weapon = weapon;
        this.target = target;
        if(location != null)
        {
            this.location = location;
        }
        else
        {
            if(target == null)
            {
                throw new IllegalArgumentException("location may not be null if target is null");
            }
            this.location = target.getPosition();
        }
    }

    /**
     * @return The location of detonation. Never null.
     */
    public Vector3 getLocation()
    {
        return location;
    }

    /**
     * @return The target of the detonation, or null if no specific target was
     *      given.
     */
    public Entity getTarget()
    {
        return target;
    }

    /**
     * @return The weapon that detonated.
     */
    public Weapon getWeapon()
    {
        return weapon;
    }
    
    public void detonate()
    {
        if(target != null)
        {
            // Just hit the exact target they were firing at
            logger.info("Weapon '" + weapon.getName() + "' hit target '" + target.getName() + "'");
            target.setProperty(EntityConstants.PROPERTY_DAMAGE, DamageStatus.destroyed);
            
            damagedEntities.add(target);
        }
        else
        {
            // Destroy anything near the impact point
            damagedEntities = sim.getEntities(location, 100.0);
            for(Entity t : damagedEntities)
            {
                // TODO: Maybe add a more general "damagable" property or something?
                if(t.getPrototype().hasSubcategory("control")) continue;
                
                logger.info("Weapon '" + weapon.getName() + "' hit target '" + t.getName() + "' (indirect)");
                t.setProperty(EntityConstants.PROPERTY_DAMAGE, DamageStatus.destroyed);
            }
        }
    }
    
    /**
     * @return A list of entities damaged by the detonation
     */
    public List<Entity> getDamagedEntities()
    {
        return damagedEntities;
    }
}
