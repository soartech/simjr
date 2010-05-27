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
 * Created on Feb 11, 2008
 */
package com.soartech.simjr.weapons;

import java.util.ArrayList;
import java.util.List;

import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Entity;

/**
 * Basic implementation of {@link WeaponPlatform} interface.
 * 
 * @author ray
 */
public class DefaultWeaponPlatform extends AbstractEntityCapability implements WeaponPlatform
{
    private final List<Weapon> weapons = new ArrayList<Weapon>();
    
    public DefaultWeaponPlatform()
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.AbstractEntityCapability#attach(com.soartech.simjr.sim.Entity)
     */
    @Override
    public void attach(Entity entity)
    {
        super.attach(entity);
        for(Weapon w : weapons)
        {
            w.setEntity(entity);
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.AbstractEntityCapability#detach()
     */
    @Override
    public void detach()
    {
        for(Weapon w : weapons)
        {
            w.setEntity(null);
        }
        super.detach();
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.weapons.WeaponPlatform#addWeapon(com.soartech.simjr.weapons.Weapon)
     */
    public void addWeapon(Weapon weapon)
    {
        if(!weapons.contains(weapon))
        {
            weapons.add(weapon);
            weapon.setEntity(getEntity());
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.weapons.WeaponPlatform#getWeapon(java.lang.String)
     */
    public Weapon getWeapon(String name)
    {
        for(Weapon weapon : weapons)
        {
            if(name.equals(weapon.getName()))
            {
                return weapon;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.weapons.WeaponPlatform#getWeapons()
     */
    public List<Weapon> getWeapons()
    {
        return weapons;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.weapons.WeaponPlatform#removeWeapon(com.soartech.simjr.weapons.Weapon)
     */
    public void removeWeapon(Weapon weapon)
    {
        if(weapons.remove(weapon))
        {
            weapon.setEntity(null);
        }
    }

}
