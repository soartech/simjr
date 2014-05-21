package com.soartech.simjr.sensors;

import junit.framework.TestCase;

import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityPrototypes;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.entities.DismountedInfantry;
import com.soartech.simjr.sim.entities.NonEntity;
import com.soartech.simjr.sim.entities.Vehicle;

public class EntityFilterTest extends TestCase
{
    
    public void testBasicFiltering() {    
        Vehicle source = new Vehicle("source", EntityPrototypes.NULL);
        EntityFilter filter = new EntityFilter(source);
        
        Vehicle vehicle = new Vehicle("test-vehicle",EntityPrototypes.NULL);
        DismountedInfantry dismount = new DismountedInfantry("test-di", EntityPrototypes.NULL);
        Entity nonEntity = new NonEntity("test-entity", EntityPrototypes.NULL);
        
        EntityTools.setVisible(source, true);
        EntityTools.setVisible(vehicle, true);
        EntityTools.setVisible(dismount, true);
        EntityTools.setVisible(nonEntity, true);
        
        assertFalse(filter.isEntityOfInterest(source));
        assertTrue(filter.isEntityOfInterest(vehicle));
        assertTrue(filter.isEntityOfInterest(dismount));
        assertFalse(filter.isEntityOfInterest(nonEntity));
        
        EntityTools.setVisible(source, false);
        EntityTools.setVisible(vehicle, false);
        EntityTools.setVisible(dismount, false);
        EntityTools.setVisible(nonEntity, false);
        
        assertFalse(filter.isEntityOfInterest(source));
        assertFalse(filter.isEntityOfInterest(vehicle));
        assertFalse(filter.isEntityOfInterest(dismount));
        assertFalse(filter.isEntityOfInterest(nonEntity));        
    }
    
}
