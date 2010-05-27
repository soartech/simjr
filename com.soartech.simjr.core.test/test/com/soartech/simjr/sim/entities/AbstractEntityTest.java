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
 * Created on Oct 28, 2007
 */
package com.soartech.simjr.sim.entities;

import junit.framework.TestCase;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.DefaultEntityPrototype;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityCapability;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.LazyEntityPropertyValue;
import com.soartech.simjr.sim.SimpleTerrain;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.SimulationListenerAdapter;
import com.soartech.simjr.sim.Tickable;

public class AbstractEntityTest extends TestCase
{
    private Simulation sim;
    
    private static class TestEntity extends AbstractEntity
    {
        public TestEntity(String name)
        {
            super(name, DefaultEntityPrototype.newBuilder().id("test").category("test").domain("test").subcategory("test").build());
        }
    }
    private static class TickableCapability extends AbstractEntityCapability implements Tickable
    {
        public boolean ticked = false;
        public double tickDt = 0.0;
        
        @Override
        public void tick(double dt)
        {
            ticked = true;
            tickDt = dt;
        }
    };
    
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.sim = new Simulation(SimpleTerrain.createExampleTerrain());
    }

    protected void tearDown() throws Exception
    {
        sim.shutdown();
        super.tearDown();
    }

    public void testTickableCapabilitiesGetTicked() throws Exception
    {
        final Entity entity = new TestEntity(getName());
        final TickableCapability cap = new TickableCapability();
        entity.addCapability(cap);
        entity.tick(3.14159);
        assertTrue(cap.ticked);
        assertEquals(3.14159, cap.tickDt, 0.0001);
    }
    
    public void testGetPropertyHandlesLazyPropertyValues() throws Exception
    {
        final Entity entity = new TestEntity(getName());

        final String expectedValue = "This is the expected value";
        
        final LazyEntityPropertyValue lazyValue = new LazyEntityPropertyValue()
        {
            @Override
            public Object getValue()
            {
                return expectedValue;
            }
        };
        
        entity.setProperty("testLazyProperty", lazyValue);
        final Object actualValue = entity.getProperty("testLazyProperty");
        assertSame(expectedValue, actualValue);
    }
    
    public  void testMovementProperties() throws Exception
    {
        final Entity entity = new TestEntity(getName());
        sim.addEntity(entity);
        
        final Vector3 position = new Vector3(-50, -25, 10);
        final Vector3 velocity = new Vector3(1, 2, 4);
        final double orientation = Math.PI / 4.0;
        final String mgrs = sim.getTerrain().toMgrs(position);
        final Geodetic.Point lla = sim.getTerrain().toGeodetic(position);
        
        entity.setPosition(position);
        entity.setVelocity(velocity);
        entity.setOrientation(orientation);
        
        entity.updateProperties();
        
        assertEquals(position, entity.getProperty(EntityConstants.PROPERTY_POSITION));
        assertEquals(velocity, entity.getProperty(EntityConstants.PROPERTY_VELOCITY));
        assertEquals(orientation, entity.getProperty(EntityConstants.PROPERTY_ORIENTATION));
        assertEquals(mgrs, entity.getProperty(EntityConstants.PROPERTY_MGRS));
        assertEquals(Math.toDegrees(lla.latitude), entity.getProperty(EntityConstants.PROPERTY_LATITUDE));
        assertEquals(Math.toDegrees(lla.longitude), entity.getProperty(EntityConstants.PROPERTY_LONGITUDE));
    }
    
    public void testEnforceAgl() throws Exception
    {
        final double desiredAgl = 5.6;
        
        final Entity entity = new TestEntity(getName());
        entity.setProperty(EntityConstants.PROPERTY_ENFORCE_AGL, desiredAgl);
        
        sim.addEntity(entity);
        
        entity.setPosition(new Vector3(-50, -50, 0));
        entity.setVelocity(new Vector3(2.0, 3.0, 5.0));
        
        assertEquals(desiredAgl, EntityTools.getAboveGroundLevel(entity), 0.1);
        
        sim.addListener(new SimulationListenerAdapter() {

            /* (non-Javadoc)
             * @see com.soartech.simjr.SimulationListenerAdapter#onTick(double)
             */
            @Override
            public void onTick(double dt)
            {
                synchronized(sim.getLock())
                {
                    assertEquals(desiredAgl, EntityTools.getAboveGroundLevel(entity), 0.1);
                }
            }});
        
        sim.setPaused(false);
        
        while(sim.getTime() < 3.0)
        {
            Thread.sleep(500);
        }
        
        sim.setPaused(true);
        
        assertEquals(desiredAgl, EntityTools.getAboveGroundLevel(entity), 0.1);
    }
    
    public void testDestroyedEntityDoesNotMove() throws Exception
    {
        final Entity entity = new TestEntity(getName());
        
        sim.addEntity(entity);
        
        final Vector3 initialPosition = new Vector3(-50, -50, 0);
        entity.setPosition(initialPosition);
        entity.setVelocity(new Vector3(2.0, 3.0, 5.0));
        entity.setProperty(EntityConstants.PROPERTY_DAMAGE, DamageStatus.destroyed);
        
        sim.addListener(new SimulationListenerAdapter() {

            /* (non-Javadoc)
             * @see com.soartech.simjr.SimulationListenerAdapter#onTick(double)
             */
            @Override
            public void onTick(double dt)
            {
                synchronized(sim.getLock())
                {
                    assertEquals(initialPosition, entity.getPosition());
                }
            }});
        
        sim.setPaused(false);
        
        while(sim.getTime() < 2.0)
        {
            Thread.sleep(500);
        }
        
        sim.setPaused(true);
        
        assertEquals(initialPosition, entity.getPosition());
    }
 
    public void testAttachIsCalledOnAddedCapabilities()
    {
        final Entity entity = new TestEntity(getName());
        
        final EntityCapability cap = new AbstractEntityCapability() {};
            
        entity.addCapability(cap);
        assertSame(entity, cap.getEntity());
    }
    public void testDetachIsCalledOnRemovedCapabilities()
    {
        final Entity entity = new TestEntity(getName());
        
        final EntityCapability cap = new AbstractEntityCapability() {};
            
        entity.addCapability(cap);
        assertSame(entity, cap.getEntity());
        
        entity.removeCapability(cap);
        assertNull(cap.getEntity());
    }
    public void testDetachIsNotCalledIfCapabilityIsAlreadyRemoved()
    {
        final Entity entity = new TestEntity(getName());
        
        final boolean detachCalled[] = new boolean[1]; 
        final EntityCapability cap = new AbstractEntityCapability() {

            @Override
            public void detach()
            {
                super.detach();
                detachCalled[0] = true;
            }
        };
            
        entity.addCapability(cap);
        assertSame(entity, cap.getEntity());
        
        entity.removeCapability(cap);
        assertNull(cap.getEntity());
        assertTrue(detachCalled[0]);
        detachCalled[0] = false;
        entity.removeCapability(cap);
        assertFalse(detachCalled[0]);
    }
}
