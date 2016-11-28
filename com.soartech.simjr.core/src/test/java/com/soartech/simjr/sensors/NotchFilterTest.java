package com.soartech.simjr.sensors;

import junit.framework.TestCase;

import com.soartech.math.Vector3;
import com.soartech.simjr.sim.EntityPrototypes;
import com.soartech.simjr.sim.entities.Vehicle;

public class NotchFilterTest extends TestCase
{

    public void testNotchFilter() {
        Vehicle source = new Vehicle("source", EntityPrototypes.NULL);
        source.setPosition(new Vector3(0,0,0));
        
        NotchFilter notchFilter = new NotchFilter(source);
        
        // Perpendicular velocity vectors
        assertTrue( notchFilter.inNotch(new Vector3(1,0,0), new Vector3(0,100,0)) );
        assertTrue( notchFilter.inNotch(new Vector3(1,0,0), new Vector3(0,-100,0)) );
        assertTrue( notchFilter.inNotch(new Vector3(1,0,0), new Vector3(0,0,100)) );
        assertTrue( notchFilter.inNotch(new Vector3(1,0,0), new Vector3(0,0,-100)) );
        assertTrue( notchFilter.inNotch(new Vector3(1,0,0), new Vector3(0,100,-100)) );
        
        // No velocity
        assertTrue( notchFilter.inNotch(new Vector3(1,0,0), new Vector3(0,0,0)) );
        
        // This should not be in the notch
        assertFalse( notchFilter.inNotch(new Vector3(1,0,0), new Vector3(100,100,100)) );
    }
}
