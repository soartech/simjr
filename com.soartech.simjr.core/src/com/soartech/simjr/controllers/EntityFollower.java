package com.soartech.simjr.controllers;

import com.soartech.math.Vector3;
import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityController;

public class EntityFollower extends AbstractEntityCapability implements
        EntityController
{
    private static final double FOLLOW_OFFSET_ANGLE = Math.PI / 4; //45 degrees 
    private static final double FOLLOW_DISTANCE = 75.0;
    private static final double CATCH_UP_RATE = 1.07;
    
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
        
        final Vector3 followPosition = toFollow.getPosition();
        final double followHeading = toFollow.getHeading();
        final double followPointHeading = (followHeading + Math.PI + FOLLOW_OFFSET_ANGLE);// % (2 * Math.PI);
        final double newX = followPosition.x + (FOLLOW_DISTANCE * Math.sin(followPointHeading));
        final double newY = followPosition.y + (FOLLOW_DISTANCE * Math.cos(followPointHeading));
        final Vector3 target = new Vector3(newX, newY, followPosition.z);
        
        final double speed = (toFollow.getVelocity().length() / previousDT) * CATCH_UP_RATE;
        
        //IF were within a tick of the target position, just go there and match the heading
        if(target.subtract(selfPosition).length() < speed * dt){
            self.setPosition(target);
            self.setHeading(followHeading);
            self.setVelocity(Vector3.ZERO);
        }else{
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
