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

import java.util.List;

import com.soartech.simjr.sim.Entity;

/**
 * <p>Capability interface available to entities with weapons. Typically accessed 
 * via adaptables like this:
 * 
 * <pre>
 * WeaponPlatform weapons = Adaptables.adapt(entity, WeaponPlatform.class);
 * </pre>
 * 
 * @author ray
 */
public interface WeaponPlatform
{
    /**
     * @return The owning entity
     */
    Entity getEntity();
    
    /**
     * @return List of weapons
     */
    List<Weapon> getWeapons();
    
    /**
     * Retrieve a weapon by name
     * 
     * @param name Name of the weapon
     * @return The weapon or null if not found
     */
    Weapon getWeapon(String name);

    /**
     * Add a weapon to this platform. {@link Weapon#setEntity(Entity)} will
     * be set.
     * 
     * @param weapon The weapon to add.
     */
    void addWeapon(Weapon weapon);
    
    /**
     * Remove a weapon from this platform. The weapon's entity will be set
     * to null.
     * 
     * @param weapon The weapon to remove.
     */
    void removeWeapon(Weapon weapon);
}
