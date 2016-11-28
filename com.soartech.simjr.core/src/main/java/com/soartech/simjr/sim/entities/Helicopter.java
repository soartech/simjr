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

import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.weapons.Weapon;
import com.soartech.simjr.weapons.WeaponPlatform;

/**
 * @author ray
 */
public class Helicopter extends Vehicle
{
    /**
     * @param name
     */
    public Helicopter(String name, EntityPrototype prototype)
    {
        super(name, prototype);
        
        // TODO: RWA defaults to ah-64?
//        EntityTypeInfo typeInfo = EntityTypeInfo.get(this);
//        typeInfo.setSubcategory("ah-64");
        
        // TODO: RWA weapons
        WeaponPlatform weapons = Adaptables.adapt(this, WeaponPlatform.class);
        weapons.addWeapon(Weapon.load("20mm-cannon", 1000));
        weapons.addWeapon(Weapon.load("hellfire", 4));
        //addWeapon(new Weapon("Hydra70-HE", "rockets", 4, 4));
        
        // Civilian Huey has 400 gal tank & 2.2hr flight time
        replaceFuelModel(new FuelModel("gallons", 400.0, 0.05));
    }
}
