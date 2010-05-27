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
 * Created on May 14, 2008
 */
package com.soartech.simjr.weapons;

import junit.framework.TestCase;

public class WeaponTest extends TestCase
{

    public void testLoadAgMissile()
    {
        Weapon w = Weapon.load("test-ag-missile", 5);
        assertNotNull(w);
        assertTrue(w instanceof MissileWeapon);
        assertEquals("test-ag-missile", w.getName());
        assertEquals(5, w.getCount());
        assertEquals(5, w.getMaxCount());
    }
    public void testLoadAaMissile()
    {
        Weapon w = Weapon.load("test-aa-missile", 2);
        assertNotNull(w);
        assertTrue(w instanceof MissileWeapon);
        assertEquals("test-aa-missile", w.getName());
        assertEquals(2, w.getCount());
        assertEquals(2, w.getMaxCount());
    }
    public void testLoadBomb()
    {
        Weapon w = Weapon.load("test-bomb", 3);
        assertNotNull(w);
        assertTrue(w instanceof MissileWeapon);
        assertEquals("test-bomb", w.getName());
        assertEquals(3, w.getCount());
        assertEquals(3, w.getMaxCount());
    }
    
    public void testLoadCannon()
    {
        Weapon w = Weapon.load("test-cannon", 5123);
        assertNotNull(w);
        assertTrue(w instanceof CannonWeapon);
        assertEquals("test-cannon", w.getName());
        assertEquals(5123, w.getCount());
        assertEquals(5123, w.getMaxCount());
    }
    
    public void testLoadLaser()
    {
        Weapon w = Weapon.load("test-laser-dsg", 1);
        assertNotNull(w);
        assertTrue(w instanceof CannonWeapon);
        assertEquals("test-laser-dsg", w.getName());
        assertEquals(1, w.getCount());
        assertEquals(1, w.getMaxCount());
    }

}
