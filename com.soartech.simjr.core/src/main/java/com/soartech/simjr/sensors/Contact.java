package com.soartech.simjr.sensors;

import com.soartech.math.Vector3;
import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.entities.Person;
import com.soartech.simjr.sim.entities.Vehicle;

public class Contact
{
    private final Entity entity;
    private ContactState state = ContactState.UNKNOWN;
    private double expirationTime = Double.NEGATIVE_INFINITY;
    private double lastUpdateTime = Double.NEGATIVE_INFINITY;
    private Vector3 projectedPosition;
    private Vector3 projectedVelocity;
    
    public static final double projectionDuration = SimJrProps.get("simjr.contacts.projectionDuration", 10.0);
    public static final double disappearingDuration = SimJrProps.get("simjr.contacts.disappearingDuration", 5.0);
    
    public Contact(Entity entity) 
    {
        this.entity = entity;
        projectedVelocity = this.entity.getVelocity();
        projectedPosition = this.entity.getPosition();
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
        projectedVelocity = this.entity.getVelocity();
        projectedPosition = this.entity.getPosition();
        
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
    
    public void updatePosition(double dt)
    {
        // There's roundoff error in force AGL which can cause the entity
        // to move event if velocity is zero, so we do a special check here.
        if (!projectedVelocity.equals(Vector3.ZERO))
        {
            projectedPosition = projectedPosition.add(projectedVelocity.multiply(dt));
        }
    }

    public EntityPrototype getPrototype()
    {
        return this.entity.getPrototype();
    }

    public String getName()
    {
        return this.entity.getName();
    }

    public Object getForceString()
    {
        // TODO: Helo-Soar should be fixed to use "opposing" rather than "red" for this.
        String force = EntityTools.getForce(entity);
        if(force.equals(EntityConstants.FORCE_OPPOSING))
        {
            force = "red";
        }
        return force;
    }

    public boolean isPerson()
    {
        return this.entity instanceof Person;
    }

    public Object getDamageStatus()
    {
        return EntityTools.getDamage(this.entity);
    }

    public boolean isVehicle()
    {
        return this.entity instanceof Vehicle;
    }
    
    public boolean isProjected()
    {
        return (state == ContactState.PROJECTED) || (state == ContactState.PROJECTED_DISAPPEARING);
    }

    public Vector3 getVelocity()
    {
        if ( isProjected() ) 
        {
            return this.projectedVelocity;
        }
        else 
        {
            return this.entity.getVelocity();
        }
    }

    public double getAboveGroundLevel()
    {
        return EntityTools.getAboveGroundLevel(this.entity);
    }

    public Vector3 getPosition()
    {
        if ( isProjected() ) 
        {
            return this.projectedPosition;
        }
        else
        {
            return this.entity.getPosition();
        }
    }

    public double getHeading()
    {
        return this.entity.getHeading();
    }

    public double getGroundSpeed()
    {
        return EntityTools.getGroundSpeed(this.entity);
    }

}
