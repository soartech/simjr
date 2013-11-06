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
 * Created on Jan 30, 2010
 */
package com.soartech.simjr.sim;

import junit.framework.TestCase;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.simjr.sim.entities.DefaultEntity;

public class LazyGeodeticPropertyTest extends TestCase
{
    private Simulation sim;
    private Entity entity;
    private Geodetic.Point lla;
    private LazyGeodeticProperty lgp;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        sim = new Simulation(SimpleTerrain.createExampleTerrain(), false);
        entity = new DefaultEntity("test", new DefaultEntityPrototype());
        sim.addEntity(entity);
        entity.setPosition(new Vector3(1000, 5000, 1234.0));
        lla = sim.getTerrain().toGeodetic(entity.getPosition());
        lgp = new LazyGeodeticProperty(entity);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        sim.shutdown();
        super.tearDown();
    }

    public void testCalculatesLatitudeInDegrees()
    {
        final LazyEntityPropertyValue p = lgp.latitude();
        assertEquals(Math.toDegrees(lla.latitude), ((Double)p.getValue()).doubleValue(), 0.0001);
    }
    public void testCalculatesLongitudeInDegrees()
    {
        final LazyEntityPropertyValue p = lgp.longitude();
        assertEquals(Math.toDegrees(lla.longitude), ((Double)p.getValue()).doubleValue(), 0.0001);
    }
    public void testCalculatesAltitudeInMeters()
    {
        final LazyEntityPropertyValue p = lgp.altitude();
        assertEquals(lla.altitude, ((Double)p.getValue()).doubleValue(), 0.0001);
    }
}
