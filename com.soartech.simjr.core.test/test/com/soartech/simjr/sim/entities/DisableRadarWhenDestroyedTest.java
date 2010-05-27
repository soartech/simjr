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
package com.soartech.simjr.sim.entities;

import junit.framework.TestCase;

import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPrototypes;
import com.soartech.simjr.sim.SimpleTerrain;
import com.soartech.simjr.sim.Simulation;

public class DisableRadarWhenDestroyedTest extends TestCase
{
    private Simulation sim;

    
    /* (non-Javadoc)
     * @see com.soartech.simjr.SimJrTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        sim = new Simulation(SimpleTerrain.createExampleTerrain(), false);
    }


    /* (non-Javadoc)
     * @see com.soartech.simjr.SimJrTestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        sim.shutdown();
        super.tearDown();
    }
    
    public void testRadarRangeIsSetToZeroWhenDestroyed()
    {
        DefaultEntity sam = new DefaultEntity("sam", EntityPrototypes.NULL);
        sam.addCapability(new DisableRadarWhenDestroyed());
        
        EntityVisibleRange radar = new EntityVisibleRange(sam, EntityConstants.PROPERTY_RADAR);
        sam.setProperty(radar.getProperty(), radar);
        radar.setVisibleRange(1000.0);
        
        sim.addEntity(sam);
        
        sim.tick(1.0);
        assertEquals(radar.getVisibleRange(), 1000.0);
        
        sam.setProperty(EntityConstants.PROPERTY_DAMAGE, DamageStatus.destroyed);
        assertEquals(radar.getVisibleRange(), 1000.0);

        sim.tick(1.0);
        assertEquals(radar.getVisibleRange(), 0.0);
    }

}
