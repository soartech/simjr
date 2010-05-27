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
 * Created on Sep 21, 2009
 */
package com.soartech.simjr.controllers;

import java.util.Arrays;

import junit.framework.TestCase;

import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.SimpleTerrain;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.entities.AbstractPolygon;

public class SegmentFollowerTest extends TestCase
{
    private Simulation sim;
    private Entity truck;

    protected void setUp() throws Exception
    {
        super.setUp();
        this.sim = new Simulation(SimpleTerrain.createExampleTerrain(), false);
        this.truck = sim.getEntityPrototypes().getPrototype("truck").createEntity("truck");
        this.sim.addEntity(truck);
    }

    protected void tearDown() throws Exception
    {
        this.sim.shutdown();
        this.sim = null;
        super.tearDown();
    }
    
    public void testCorrectlyAttachesToEntityWhenAddedAsCapability() throws Exception
    {
        final SegmentFollower follower = new SegmentFollower();
        truck.addCapability(follower);
        assertSame(follower, Adaptables.adapt(truck, SegmentFollower.class));
        assertSame(truck, follower.getEntity());
    }
    
    public void testDoesNotCreateARouteWhenNoSegmentsAreSet() throws Exception
    {
        final SegmentFollower follower = new SegmentFollower();
        truck.addCapability(follower);
        assertNull(sim.getEntity("truck:SegmentFollower:route:0"));
    }
    
    public void testDoesNotCreateARouteWhenThereIsOnlyOneSegment() throws Exception
    {
        final SegmentFollower follower = new SegmentFollower();
        truck.addCapability(follower);
        assertNull(sim.getEntity("truck:SegmentFollower:route:0"));
        
        final Entity a = waypoint("A");
        follower.setSegments(new SegmentInfo(a.getName(), 1.0, null));
        assertNull(sim.getEntity("truck:SegmentFollower:route:0"));
    }
    
    public void testConstructsNewRouteWhenSegmentsAreSet() throws Exception
    {
        final SegmentFollower follower = new SegmentFollower();
        truck.addCapability(follower);
        
        assertNull(sim.getEntity("truck:SegmentFollower:route:0"));
        
        final Entity a = waypoint("A");
        final Entity b = waypoint("B");
        final Entity c = waypoint("C");
        
        final SegmentInfo segments = new SegmentInfo("A", 1.0, new SegmentInfo("B", 2.0, new SegmentInfo("C", 3.0, null)));
        follower.setSegments(segments);
        final AbstractPolygon route = Adaptables.adapt(sim.getEntity("truck:SegmentFollower:route:0"), AbstractPolygon.class);
        assertNotNull(route);
        assertEquals(Arrays.asList(a, b, c), route.getPoints());
    }
    
    public void testDestroysExistingRouteWhenSegmentsAreSet() throws Exception
    {
        final SegmentFollower follower = new SegmentFollower();
        truck.addCapability(follower);
        
        final SegmentInfo segments = new SegmentInfo("A", 1.0, new SegmentInfo("B", 2.0, new SegmentInfo("C", 3.0, null)));
        follower.setSegments(segments);
        assertNotNull(sim.getEntity("truck:SegmentFollower:route:0"));

        follower.setSegments(null);
        assertNull(sim.getEntity("truck:SegmentFollower:route:0"));
    }
    
    private Entity waypoint(String name) throws Exception
    {
        Entity wp = sim.getEntityPrototypes().getPrototype("waypoint").createEntity(name);
        sim.addEntity(wp);
        return wp;
    }
}
