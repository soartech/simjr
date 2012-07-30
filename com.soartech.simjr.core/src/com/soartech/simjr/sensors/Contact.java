package com.soartech.simjr.sensors;

import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.Simulation;

public class Contact
{
    private final Entity entity;
    private ContactState state = ContactState.UNKNOWN;
    private double expirationTime = Double.MIN_VALUE;
    private double lastUpdateTime = Double.MIN_VALUE;
    
    public static final double projectionDuration = SimJrProps.get("simjr.contacts.projectionDuration", 10.0);
    public static final double disappearingDuration = SimJrProps.get("simjr.contacts.disappearingDuration", 5.0);
    
    public Contact(Entity entity) 
    {
        this.entity = entity;
    }

    public double getExpirationTime()
    {
        return this.expirationTime;
    }

    public ContactState getState()
    {
        return this.state;
    }
    
    public Entity getEntity()
    {
        return this.entity;
    }
    
    public void updateState(ContactState newState, double currentTime)
    {
        ContactState oldState = this.state;
        if ( currentTime <= this.lastUpdateTime ) 
        {
            if ( newState.getPriority() > this.state.getPriority() )
            {
                this.state = newState;
            }
        }
        else
        {
            this.state = newState;
        }
        this.lastUpdateTime = currentTime;
        
        boolean stateChanged = ( oldState != this.state );
        if ( stateChanged ) {
            if ( this.state == ContactState.VISIBLE || this.state == ContactState.RADAR )
            {
                this.expirationTime = Double.MAX_VALUE;
            }
            else if ( this.state == ContactState.PROJECTED )
            {
                this.expirationTime = currentTime + projectionDuration;
            }
            else if ( this.state == ContactState.PROJECTED_DISAPPEARING )
            {
                this.expirationTime = currentTime + disappearingDuration;
            }
            else
            {
                this.expirationTime = Double.MIN_VALUE;
            }        
        }
    }

    public void updatePosition(Simulation simulation, double dt)
    {
        // TODO Auto-generated method stub
    }

}
