package com.soartech.simjr.controllers;

import com.soartech.math.Vector3;
import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityController;

/**
 * This is used to allow one entity to follow another entity with
 * given offsets.
 * 
 * @author ACNickels
 */
public class EntityFollower extends AbstractEntityCapability implements
        EntityController
{
    //This is how far off of due behind, the wing man will follow
    private static final double FOLLOW_OFFSET_ANGLE = Math.PI / 8; //45 degrees
    //The separation to maintain between the two craft
    private static final double FOLLOW_DISTANCE = 100.0;
    //This is a multiplier on the current speed, so that the wing man will keep up with the leader
    private static final double CATCH_UP_RATE = 1.07;
    //This is the minimum speed the wing man will move while he is not in formation
    private static final double REPOSITION_SPEED = 10;
    
    //Entity to follow
    private Entity toFollow = null;
    private String nameToFollow = null;
    
    private double previousDT = Double.MIN_VALUE;//Not 0
    
    public EntityFollower(String nameToFollow){
        this.nameToFollow = nameToFollow;
    }
    
    public void setFollowedName(String toFollow){
        nameToFollow = toFollow;
        toFollow = null;
    }
    
    public void setFollow(Entity toFollow){
        this.toFollow = toFollow;
        this.nameToFollow = toFollow.getName();
    }
    
    @Override
    public void tick(double dt)
    {
        //If we don't know who to follow
        if(toFollow == null){
            //Find them
            toFollow = this.getEntity().getSimulation().getEntity(nameToFollow);
            //If we still don't know, bail out
            if(toFollow == null){
                return;
            }
        }
        Entity self = getEntity();
        Vector3 selfPosition = self.getPosition();
        
        //Heading and position of the lead craft
        final Vector3 followPosition = toFollow.getPosition();
        final double followHeading = toFollow.getHeading();
        //Direction from the lead craft to put the point the follower is aiming for
        final double followPointHeading = (followHeading + Math.PI + FOLLOW_OFFSET_ANGLE) % (2 * Math.PI);
        //Point that the follower is aiming for
        final double newX = followPosition.x + (FOLLOW_DISTANCE * Math.cos(followPointHeading));
        final double newY = followPosition.y + (FOLLOW_DISTANCE * Math.sin(followPointHeading));
        final Vector3 target = new Vector3(newX, newY, followPosition.z);
        
        final double speed = Math.max((toFollow.getVelocity().length() / dt), 10) * CATCH_UP_RATE;
        
        //IF were within a tick of the target position, just go there and match the heading
        if(target.subtract(selfPosition).length() < speed * dt){
            self.setPosition(target);
            self.setHeading(followHeading);
            //Zero out the velocity, or the wing man will move again after this returns
            self.setVelocity(Vector3.ZERO);
        }else{
            //Point the craft towards the target, and give it speed
            final Vector3 dir = target.subtract(selfPosition).normalized();
            self.setVelocity(dir.multiply(speed * dt));
            self.setHeading(Math.atan2(dir.y, dir.x));
        }
        previousDT = dt;
    }

    @Override
    public void openDebugger(){
        //Theres no debugger here
    }
}
