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
 * Created on May 8, 2008
 */
package com.soartech.simjr.weapons;

import com.soartech.math.Vector3;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.entities.AbstractFlyout;
import com.soartech.simjr.sim.entities.Missile;

/**
 * @author ray
 */
public class MissileWeapon extends AbstractFlyoutWeapon
{
    private double speed;
    
    /**
     * @param name
     * @param type
     * @param count
     * @param maxCount
     */
    public MissileWeapon(String name, int count, int maxCount)
    {
        super(name, count, maxCount);
        
        setMaxSpeed(Weapon.getProperty(name + ".maxSpeed", 300.0));
    }

    /**
     * @return the speed
     */
    public double getMaxSpeed()
    {
        return speed;
    }

    /**
     * @param speed the speed to set
     */
    public void setMaxSpeed(double speed)
    {
        this.speed = speed;
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.weapons.FlyoutWeapon#createFlyoutEntity(com.soartech.simjr.sim.Entity)
     */
    @Override
    protected AbstractFlyout createFlyoutEntity(Entity target)
    {
        Missile missile = new Missile(this, target, getFlyoutPrototype());
        missile.setSpeed(getMaxSpeed());
        return missile;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.weapons.FlyoutWeapon#createFlyoutEntity(com.soartech.spatr.math.Vector3)
     */
    @Override
    protected AbstractFlyout createFlyoutEntity(Vector3 staticTarget)
    {
        Missile missile = new Missile(this, staticTarget, getFlyoutPrototype());
        missile.setSpeed(getMaxSpeed());
        return missile;
    }
}
