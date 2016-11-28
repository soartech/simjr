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
 */ 
/*
    Common utilities for working with entity weapons in scripts.
    
    Load with: requireScript("weapons");
*/

/**
    Add a weapon to an entity's weapon platform.
    
    @param entity The entity
    @param weaponName The name of the weapon type. Must be in simjr.weapons.properties
    @param count Initial load count
    @return The added weapon or null if the entity does not have a weapon platform
*/
function addWeapon(entity, weaponName, count)
{
    var weapons = EntityTools.getWeaponPlatform(entity);
    if(weapons == null)
    {
        logger.error("simjr.weapons.js:addWeapon(): Entity '" + entity.getName() + "' has no weapon platform");
        return null;
    }
    var weapon = Packages.com.soartech.simjr.weapons.Weapon.load(weaponName, count);
    weapons.addWeapon(weapon);
    return weapon;
}

function fireWeapon(entity, weaponName, target)
{
    var weapons = EntityTools.getWeaponPlatform(entity);
    if(weapons == null)
    {
        logger.error("simjr.weapons.js:fireWeapon(): Entity '" + entity.getName() + "' has no weapon platform");
        return null;
    }
    var weapon = weapons.getWeapon(weaponName);
    weapon.fire(1, target);
    return weapon;
}
