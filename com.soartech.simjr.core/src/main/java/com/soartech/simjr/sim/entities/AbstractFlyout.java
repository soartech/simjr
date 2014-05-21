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
 * Created on Mar 26, 2009
 */
package com.soartech.simjr.sim.entities;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.math.Angles;
import com.soartech.math.Vector3;
import com.soartech.simjr.sim.Detonation;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.weapons.AbstractFlyoutWeapon;
import com.soartech.simjr.weapons.Weapon;

/**
 * Basic implementation of a flyout (missile/bomb) that flies at a target and destroys it.
 *
 * @author ray
 */
public abstract class AbstractFlyout extends AbstractEntity
{
    private static final Logger logger = LoggerFactory.getLogger(AbstractFlyout.class);

    // This value is based on the calculation in
    // close-air-support*propose*establish-firing-time in laser-guided-strike.soar
    // in helo-soar. This value, in m/s, causes the missile impact and the
    // agent's "splash" call to sync up.
    public static final double DEFAULT_SPEED = 600.0;

    private AbstractFlyoutWeapon weapon;
    private Entity target, shooter;
    private Vector3 staticTarget;

    private double speed = DEFAULT_SPEED;
    private boolean hit = false;

    private static final Map<String, Integer> flyoutIds = new HashMap<String, Integer>();

    /**
     * Construct a missile that hits an entity target.
     *
     * @param weapon The associated weapon
     * @param target THe target entity
     */
    public AbstractFlyout(AbstractFlyoutWeapon weapon, Entity target, EntityPrototype prototype)
    {
        this(weapon, target, null, prototype);
    }

    /**
     * Construct a missile that hits a target position
     *
     * @param weapon The associated weapon
     * @param staticTarget The target position
     */
    public AbstractFlyout(AbstractFlyoutWeapon weapon, Vector3 staticTarget, EntityPrototype prototype)
    {
        this(weapon, null, staticTarget, prototype);
    }

    private AbstractFlyout(AbstractFlyoutWeapon weapon, Entity target, Vector3 staticTarget, EntityPrototype prototype)
    {
        super(generateName(weapon, target, staticTarget), prototype);

        logger.info(prototype.getSubcategory() + " '" + getName() + "' created.");

        this.weapon = weapon;
        this.target = target;
        this.staticTarget = staticTarget;

        setProperty("target", target != null ? target : staticTarget);
        setProperty("weapon", weapon);
    }

    /**
     * @return the speed
     */
    public double getSpeed()
    {
        return speed;
    }

    /**
     * @param speed the speed of the missile
     */
    public void setSpeed(double speed)
    {
        this.speed = speed;
    }
    
    public double getClosingSpeed()
    {
        if (target==null)
        {
            return this.getSpeed();
        } else
        {
            return ((this.getVelocity()).subtract(target.getVelocity())).length();
        }
    }
    
    public int getTimeToTarget()
    {
        Vector3 targetPos = target != null ? target.getPosition() : staticTarget;
        return (int)Math.round(targetPos.distance(getPosition())/this.getClosingSpeed());
        
    }
    
    public double getRangeToTarget()
    {
        Vector3 targetPos = target != null ? target.getPosition() : staticTarget;
        return targetPos.distance(getPosition());
    }
    
    public double getBearingToTarget()
    {
        Vector3 targetPos = target != null ? target.getPosition() : staticTarget;
        targetPos.subtract(getPosition());
        return Math.toDegrees(Angles.boundedAngleRadians(Angles.getBearing(targetPos)));
    }
    
    public Entity getTarget()
    {
        return target;
    }

    public Vector3 getStaticTarget()
    {
        return staticTarget;
    }

    public Weapon getWeapon()
    {
        return weapon;
    }

    public void setShooter(Entity shooter)
    {
        this.shooter=shooter;
    }

    public Entity getShooter()
    {
        return shooter;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.entities.AbstractEntity#tick(double)
     */
    @Override
    public void processTick(double dt)
    {
        if(hit)
        {
            return;
        }

        Vector3 targetPos = target != null ? target.getPosition() : staticTarget;
        Vector3 dir = targetPos.subtract(getPosition()).normalized();

        setHeading(Math.atan2(dir.y, dir.x));
        setVelocity(dir.multiply(speed));

        super.processTick(dt);

        Vector3 newPos = getPosition();
        if(newPos.subtract(targetPos).length() < speed * dt * 1.5)
        {
            hit = true;

            getSimulation().detonate(new Detonation(getSimulation(), weapon, target, staticTarget));
            //if (shooter != null) 
            //{
            //    removeFlyOut(shooter, this);
            //}
            weapon.removeFlyout(this);
            getSimulation().removeEntity(this);
        }
    }
    
    /**
     * Generate a unique name for a missile. Exactly one of target or
     * staticTarget must be non-null.
     *
     * @param weapon The associated weapon
     * @param target The target
     * @param staticTarget The target position
     * @return A new name
     */
    private static String generateName(Weapon weapon, Entity target, Vector3 staticTarget)
    {
        int id = generateFlyoutId(weapon);
        if(target != null)
        {
            return weapon.getEntity().getName() + "." +
                   weapon.getName() + "." +
                   id + "." +
                   target.getName();
        }
        else if(staticTarget != null)
        {
            return weapon.getEntity().getName() + "." +
                   weapon.getName() + "." +
                   id + "." +
                   staticTarget;
        }
        else
        {
            throw new IllegalArgumentException("One of target and staticTarget must be non-null");
        }
    }

    /**
     * Generate a new unique flyout id for the given weapon.
     *
     * @param weapon The weapon
     * @return A new flyout id.
     */
    private static int generateFlyoutId(Weapon weapon)
    {
        synchronized (flyoutIds)
        {
            String key = weapon.getEntity().getName() + "." + weapon.getName();
            Integer id = flyoutIds.get(key);

            if(id == null)
            {
                id = 0;
            }
            flyoutIds.put(key, id + 1);
            return id;
        }
    }

}
