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
package com.soartech.simjr.sim.entities;

import junit.framework.TestCase;

import com.soartech.math.Angles;
import com.soartech.math.Vector3;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPropertyListener;
import com.soartech.simjr.sim.EntityPrototypes;

public class EntityVisibleRangeTest extends TestCase
{
    public class Listener implements EntityPropertyListener
    {
        private String property;
        private boolean called = false;
        
        public Listener(String property)
        {
            this.property = property;
        }
        
        public void onPropertyChanged(Entity entity, String propertyName)
        {
            if(propertyName.equals(property))
            {
                called = true;
            }
        }
        
        public boolean checkAndReset()
        {
            boolean temp = called;
            called = false;
            return temp;
        }
    }
    
    public void testAlternateProperty()
    {
        Vehicle a = new Vehicle("a", EntityPrototypes.NULL);
        Listener listener = new Listener(EntityConstants.PROPERTY_RADAR);
        a.addPropertyListener(listener);
        
        assertNull(EntityVisibleRange.get(a, EntityConstants.PROPERTY_RADAR));
        
        EntityVisibleRange vr = new EntityVisibleRange(a, EntityConstants.PROPERTY_RADAR);
        assertFalse(listener.checkAndReset());
        a.setProperty(EntityConstants.PROPERTY_RADAR, vr);
        assertTrue(listener.checkAndReset());
        assertSame(vr, EntityVisibleRange.get(a, EntityConstants.PROPERTY_RADAR));
        
        vr.setVisibleRange(50.0);
        assertTrue(listener.checkAndReset());
        vr.setVisibleAngle(Math.toRadians(30));
        assertTrue(listener.checkAndReset());
    }

    public void testIsInRange()
    {
        Vehicle a = new Vehicle("a", EntityPrototypes.NULL);
        Listener listener = new Listener(EntityConstants.PROPERTY_VISIBLE_RANGE);
        a.addPropertyListener(listener);
        a.setHeading(Angles.navRadiansToMathRadians(0.0)); // point up
        
        assertNull(EntityVisibleRange.get(a));
        
        EntityVisibleRange vr = new EntityVisibleRange(a);
        assertFalse(listener.checkAndReset());
        a.setProperty(EntityConstants.PROPERTY_VISIBLE_RANGE, vr);
        assertTrue(listener.checkAndReset());
        assertSame(vr, EntityVisibleRange.get(a));
        
        vr.setVisibleAngle(Math.toRadians(45.0));
        vr.setVisibleRange(50.0);
        assertTrue(listener.checkAndReset());
        
        assertFalse(vr.isInRange(new Vector3(25, 0, 0)));
        a.setHeading(Angles.navRadiansToMathRadians(Math.toRadians(90.0))); // point right
        assertTrue(vr.isInRange(new Vector3(25, 0, 0)));
        assertFalse(vr.isInRange(new Vector3(0, 25, 0)));
        assertFalse(vr.isInRange(new Vector3(51, 0, 0))); // just out of range
        
        vr.setVisibleAngle(Math.toRadians(30)); // constrain view angle
        assertTrue(listener.checkAndReset());
        assertTrue(vr.isInRange(new Vector3(25, 0, 0)));
        assertFalse(vr.isInRange(new Vector3(25, 25, 0)));
        assertFalse(vr.isInRange(new Vector3(25, -25, 0)));
    }
}
