/**
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
 * Created on May 13, 2008
 */
package com.soartech.simjr.weapons;

import junit.framework.TestCase;

import com.soartech.math.Vector3;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.EntityPrototypes;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.SimpleTerrain;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.entities.Bomb;
import com.soartech.simjr.sim.entities.Vehicle;

/**
 * @author aron
 */
public class BombWeaponTest extends TestCase
{
    public void testFireCommandCreatesMissileFlyout()
    {
        Simulation sim = new Simulation(SimpleTerrain.createExampleTerrain(), false);
        
        Vehicle fwa = new Vehicle("fwa", EntityPrototypes.NULL);
        WeaponPlatform wp = Adaptables.adapt(fwa, WeaponPlatform.class);
        assertNotNull(wp);
        BombWeapon weapon = new BombWeapon("test", 4, 5);
        wp.addWeapon(weapon);
        
        sim.addEntity(fwa);
        
        assertEquals(1, sim.getEntities().size());
        
        weapon.fire(1, new Vector3(1000, 0, 0));
        assertEquals(2, sim.getEntities().size());
        
        Bomb bomb = Adaptables.findAdapter(sim.getEntities(), Bomb.class);
        assertNotNull(bomb);
        assertEquals(EntityTools.getSpeed(fwa), bomb.getSpeed());
    }
}