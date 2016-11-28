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
package com.soartech.simjr.weapons;

import java.util.ArrayList;
import java.util.List;

import com.soartech.math.Vector3;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.sim.EntityPrototypeDatabase;
import com.soartech.simjr.sim.entities.AbstractFlyout;

/**
 * Base class for a weapon that produces a flyout like a bomb or missile
 * 
 * @author ray
 */
public abstract class AbstractFlyoutWeapon extends Weapon
{
    private final List<AbstractFlyout> flyouts = new ArrayList<AbstractFlyout>();
    
    public AbstractFlyoutWeapon(String name, int count, int maxCount)
    {
        super(name, count, maxCount);
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.weapons.Weapon#fire(int, com.soartech.simjr.sim.Entity)
     */
    public void fire(int count, Entity target)
    {
        if(!prepareToFire(count, target))
        {
            return;
        }
        
        this.launchFlyout(createFlyoutEntity(target));
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.weapons.Weapon#fire(int, com.soartech.spatr.math.Vector3)
     */
    public void fire(int count, Vector3 target)
    {
        if(!prepareToFire(count, target))
        {
            return;
        }

        this.launchFlyout(createFlyoutEntity(target));
    }
    
    private void launchFlyout(AbstractFlyout flyout)
    {
        flyout.setPosition(getEntity().getPosition());
        flyout.setShooter(getEntity());
        this.addFlyout(flyout);
        
        getEntity().getSimulation().addEntity(flyout);        
    }

    protected abstract AbstractFlyout createFlyoutEntity(Entity target);
    protected abstract AbstractFlyout createFlyoutEntity(Vector3 staticTarget);
    
    public List<AbstractFlyout> getFlyouts()
    {
        return flyouts;
    }
    
    public void addFlyout(AbstractFlyout f)
    {
        if(!flyouts.contains(f))
        {
            flyouts.add(f);
        }
    }

    public void removeFlyout(AbstractFlyout f)
    {
        flyouts.remove(f);
    }

    protected EntityPrototype getFlyoutPrototype()
    {
        final EntityPrototypeDatabase db = getEntity().getSimulation().getEntityPrototypes();
        
        return db.getPrototype(getProperty(getName() + ".prototype", "flyout"));
    }

}
