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
 * Created on Oct 26, 2007
 */
package com.soartech.simjr.sim;

import junit.framework.TestCase;

import com.soartech.math.Vector3;

public class EntityPropertyAdaptersTest extends TestCase
{
    private Simulation sim;
    private Entity entity;
    
    protected void setUp() throws Exception
    {
        super.setUp();
        
        sim = new Simulation(SimpleTerrain.createExampleTerrain(), false);
        entity = sim.getEntityPrototypes().getPrototype("waypoint").createEntity("EntityPropertyAdaptersTest");
        
        sim.addEntity(entity);
    }

    protected void tearDown() throws Exception
    {
        sim.shutdown();
        
        super.tearDown();
    }
    
    public void testSetMgrsProperty()
    {
        String mgrs = (String) entity.getProperty(EntityConstants.PROPERTY_MGRS);
        assertEquals("17TLG3436151711", mgrs);
        
        Vector3 oldPosition = entity.getPosition();
        
        String newMgrs = "17TLG3486151911";
        entity.setProperty(EntityConstants.PROPERTY_MGRS, newMgrs);
        assertEquals(newMgrs, entity.getProperty(EntityConstants.PROPERTY_MGRS));
        assertFalse(oldPosition.equals(entity.getPosition()));
    }
    
    public void testBadMgrsThrowsIllegalArgumentException()
    {
        try
        {
            // TODO: When I first wrote this with "bad mgrs" as the string,
            // no exception was thrown. The SPAT-R MGRS code needs to be fixed
            // to do more validation on MGRS strings.
            entity.setProperty(EntityConstants.PROPERTY_MGRS, "xxxxxxx");
            fail("Expected IllegalArgumentException to be thrown");
        }
        catch(IllegalArgumentException e)
        {
        }
    }
    
    public void testSetPositionProperty()
    {
        entity.setProperty(EntityConstants.PROPERTY_POSITION, "(0, 1, 2)");
        
        assertEquals(new Vector3(0, 1, 2), entity.getPosition());
    }
    
    public void testSetVelocityProperty()
    {
        entity.setProperty(EntityConstants.PROPERTY_VELOCITY, "(0, 1, 2)");
        
        assertEquals(new Vector3(0, 1, 2), entity.getVelocity());
    }

}
