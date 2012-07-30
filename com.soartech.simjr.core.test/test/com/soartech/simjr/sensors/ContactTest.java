package com.soartech.simjr.sensors;

import com.soartech.simjr.sim.EntityPrototypes;
import com.soartech.simjr.sim.entities.Vehicle;

import junit.framework.TestCase;

public class ContactTest extends TestCase
{
    
    public void testInitialState() 
    {
        Vehicle entity = new Vehicle("test", EntityPrototypes.NULL);
        
        Contact contact = new Contact(entity);
        
        assertEquals(entity, contact.getEntity());
        assertEquals(ContactState.UNKNOWN, contact.getState());
        assertEquals(Double.MIN_VALUE, contact.getExpirationTime());
    }
    
    public void testSetInitialStateVisible()
    {
        Vehicle entity = new Vehicle("test", EntityPrototypes.NULL);
        
        Contact contact = new Contact(entity);
        contact.updateState(ContactState.VISIBLE, 0.);
        
        assertEquals(entity, contact.getEntity());
        assertEquals(ContactState.VISIBLE, contact.getState());
        assertEquals(Double.MAX_VALUE, contact.getExpirationTime());
    }
    
    public void testSetInitialStateRadar()
    {
        Vehicle entity = new Vehicle("test", EntityPrototypes.NULL);
        
        Contact contact = new Contact(entity);
        contact.updateState(ContactState.RADAR, 0.);
        
        assertEquals(entity, contact.getEntity());
        assertEquals(ContactState.RADAR, contact.getState());
        assertEquals(Double.MAX_VALUE, contact.getExpirationTime());
    }
    
    public void testSetInitialStateProjected()
    {
        Vehicle entity = new Vehicle("test", EntityPrototypes.NULL);
        
        Contact contact = new Contact(entity);
        contact.updateState(ContactState.PROJECTED, 0.);
        
        assertEquals(entity, contact.getEntity());
        assertEquals(ContactState.PROJECTED, contact.getState());
        assertEquals(Contact.projectionDuration, contact.getExpirationTime());        
    }
    
    public void testSetInitialStateProjectedDisappearing()
    {
        Vehicle entity = new Vehicle("test", EntityPrototypes.NULL);
        
        Contact contact = new Contact(entity);
        contact.updateState(ContactState.PROJECTED_DISAPPEARING, 0.);
        
        assertEquals(entity, contact.getEntity());
        assertEquals(ContactState.PROJECTED_DISAPPEARING, contact.getState());
        assertEquals(Contact.disappearingDuration, contact.getExpirationTime());        
    }
    
    public void testStatePriorities() 
    {
        Vehicle entity = new Vehicle("test", EntityPrototypes.NULL);
        
        Contact contact = new Contact(entity);

        // Updating in decreasing priority order (should always stay visible)
        double curtime = 0.0;
        contact.updateState(ContactState.VISIBLE, curtime);
        assertEquals(ContactState.VISIBLE, contact.getState());
        assertEquals(Double.MAX_VALUE, contact.getExpirationTime());  

        contact.updateState(ContactState.RADAR, curtime);
        assertEquals(ContactState.VISIBLE, contact.getState());
        assertEquals(Double.MAX_VALUE, contact.getExpirationTime());  

        contact.updateState(ContactState.PROJECTED, curtime);
        assertEquals(ContactState.VISIBLE, contact.getState());
        assertEquals(Double.MAX_VALUE, contact.getExpirationTime());  

        contact.updateState(ContactState.PROJECTED_DISAPPEARING, curtime);
        assertEquals(ContactState.VISIBLE, contact.getState());
        assertEquals(Double.MAX_VALUE, contact.getExpirationTime());  

        // Now increasing the time and updating in increasing priority order (should change every time)
        curtime = 1.0;
        contact.updateState(ContactState.PROJECTED_DISAPPEARING, curtime);
        assertEquals(ContactState.PROJECTED_DISAPPEARING, contact.getState());
        assertEquals(curtime + Contact.disappearingDuration, contact.getExpirationTime());  

        contact.updateState(ContactState.PROJECTED, 1.0);
        assertEquals(ContactState.PROJECTED, contact.getState());
        assertEquals(curtime + Contact.projectionDuration, contact.getExpirationTime());
        
        contact.updateState(ContactState.RADAR, 1.0);
        assertEquals(ContactState.RADAR, contact.getState());
        assertEquals(Double.MAX_VALUE, contact.getExpirationTime());

        contact.updateState(ContactState.VISIBLE, 1.0);
        assertEquals(ContactState.VISIBLE, contact.getState());
        assertEquals(Double.MAX_VALUE, contact.getExpirationTime());
    }
    
    public void testUpdatePosition() {
        fail("Not yet implemented!");        
    }

}
