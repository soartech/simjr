package com.soartech.simjr.sensors;

import com.soartech.math.Vector3;
import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.sim.Simulation;

public class Contact
{
    private final Entity entity;
    private ContactState state = ContactState.UNKNOWN;
    private double expirationTime = Double.NEGATIVE_INFINITY;
    private double lastUpdateTime = Double.NEGATIVE_INFINITY;
    
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
            resetTimeout(currentTime);        
        }
    }

    public void resetTimeout(double currentTime)
    {
        if ( this.state == ContactState.VISIBLE || this.state == ContactState.RADAR )
        {
            this.expirationTime = Double.POSITIVE_INFINITY;
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
            this.expirationTime = Double.NEGATIVE_INFINITY;
        }
    }
    
    public void updatePosition(Simulation simulation, double dt)
    {
        // TODO Auto-generated method stub
    }

    public EntityPrototype getPrototype()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getForceString()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isDismountedInfantry()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public Object getDamageStatus()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isVehicle()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public Vector3 getVelocity()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public double getAboveGroundLevel()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public Vector3 getPosition()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public double getHeading()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getGroundSpeed()
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
