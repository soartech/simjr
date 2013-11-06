package com.soartech.simjr.sensors;

import junit.framework.TestCase;

import com.soartech.math.Vector3;
import com.soartech.simjr.sim.EntityPrototypes;
import com.soartech.simjr.sim.entities.Vehicle;

public class ContactTest extends TestCase
{
    
    public void testInitialState() 
    {
        Vehicle entity = new Vehicle("test", EntityPrototypes.NULL);
        
        Contact contact = new Contact(entity);
        
        assertEquals(entity, contact.getEntity());
        assertEquals(ContactState.UNKNOWN, contact.getState());
        assertEquals(Double.NEGATIVE_INFINITY, contact.getExpirationTime());
    }
    
    public void testSetInitialStateVisible()
    {
        Vehicle entity = new Vehicle("test", EntityPrototypes.NULL);
        
        Contact contact = new Contact(entity);
        contact.updateState(ContactState.VISIBLE, 0.);
        
        assertEquals(entity, contact.getEntity());
        assertEquals(ContactState.VISIBLE, contact.getState());
        assertEquals(Double.POSITIVE_INFINITY, contact.getExpirationTime());
    }
    
    public void testSetInitialStateRadar()
    {
        Vehicle entity = new Vehicle("test", EntityPrototypes.NULL);
        
        Contact contact = new Contact(entity);
        contact.updateState(ContactState.RADAR, 0.);
        
        assertEquals(entity, contact.getEntity());
        assertEquals(ContactState.RADAR, contact.getState());
        assertEquals(Double.POSITIVE_INFINITY, contact.getExpirationTime());
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
        assertEquals(Double.POSITIVE_INFINITY, contact.getExpirationTime());  

        contact.updateState(ContactState.RADAR, curtime);
        assertEquals(ContactState.VISIBLE, contact.getState());
        assertEquals(Double.POSITIVE_INFINITY, contact.getExpirationTime());  

        contact.updateState(ContactState.PROJECTED, curtime);
        assertEquals(ContactState.VISIBLE, contact.getState());
        assertEquals(Double.POSITIVE_INFINITY, contact.getExpirationTime());  

        contact.updateState(ContactState.PROJECTED_DISAPPEARING, curtime);
        assertEquals(ContactState.VISIBLE, contact.getState());
        assertEquals(Double.POSITIVE_INFINITY, contact.getExpirationTime());  

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
        assertEquals(Double.POSITIVE_INFINITY, contact.getExpirationTime());

        contact.updateState(ContactState.VISIBLE, 1.0);
        assertEquals(ContactState.VISIBLE, contact.getState());
        assertEquals(Double.POSITIVE_INFINITY, contact.getExpirationTime());
    }
    
    public void testUpdatePosition() {
        Vehicle entity = new Vehicle("test", EntityPrototypes.NULL);
        entity.setVelocity(new Vector3(1.,2.,0.));
        entity.setPosition(new Vector3(1.,0.5,0.));
        
        Contact contact = new Contact(entity);
        contact.updateState(ContactState.PROJECTED, 0.);
        
        // Testing that the last known position and velocity are properly cached
        // the changes below should have no effect on the position calculation
        entity.setVelocity(Vector3.ZERO);
        entity.setPosition(Vector3.ZERO);
        
        contact.updatePosition(1.0);
        
        assertEquals(2., contact.getPosition().x);
        assertEquals(2.5, contact.getPosition().y);
        assertEquals(0., contact.getPosition().z);
        
        assertEquals(1., contact.getVelocity().x);
        assertEquals(2., contact.getVelocity().y);
        assertEquals(0., contact.getVelocity().z);
    }

}
