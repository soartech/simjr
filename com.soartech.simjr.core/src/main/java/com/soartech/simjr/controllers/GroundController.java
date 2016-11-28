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
 */
package com.soartech.simjr.controllers;

import com.soartech.math.Angles;
import com.soartech.math.Vector3;
import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityController;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.Tickable;

/**
 * A controller that provides an interface for controlling a ground entity.
 * Provides methods for setting desired speed and
 * heading.  This controller may be added directly
 * to a vehicle or may be nested in another controller such as a Soar agent.
 *
 * @author glenn
 */
public class GroundController extends AbstractEntityCapability implements
    EntityController, Tickable
{
    /**
     * Desired ground speed in m/s
     */
    private double desiredSpeed = 0.0;
    /**
     * Desired heading in nav radians (0 north)
     */
    private double desiredHeading = 0.0;
    /**
     * Desired turning rate in radians per second
     */
    private double desiredTurnRate = 0.0;

    public GroundController()
    {
    }

    /**
     * @param speed The new desired speed in m/s.
     */
    public void setDesiredSpeed(double speed)
    {
        this.desiredSpeed = speed;
        if(getEntity() != null)
        {
            getEntity().setProperty("desired-speed", desiredSpeed);
        }
    }

    /**
     * @param desiredHeading The new desired bearing in radians
     */
    public void setDesiredHeading(double desiredHeading)
    {
        this.desiredHeading = desiredHeading;
        if(getEntity() != null)
        {
            getEntity().setProperty("desired-heading", Math.toDegrees(desiredHeading));
        }
    }

    public void setDesiredTurnRate(Double turnRate)
    {
        this.desiredTurnRate = turnRate;
        if(getEntity() != null)
        {
            getEntity().setProperty("desired-turn-rate", Math.toDegrees(desiredTurnRate));
        }
    }

    public double getDesiredHeading() { return desiredHeading; }
    public double getDesiredSpeed() { return desiredSpeed; }



    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.AbstractEntityCapability#attach(com.soartech.simjr.sim.Entity)
     */
    @Override
    public void attach(Entity entity)
    {
        super.attach(entity);
        getEntity().setProperty("desired-speed", desiredSpeed);
        getEntity().setProperty("desired-heading", Math.toDegrees(desiredHeading));
        getEntity().setProperty("desired-turn-rate", Math.toDegrees(desiredTurnRate));
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.AbstractEntityCapability#detach()
     */
    @Override
    public void detach()
    {
        // TODO Clean up properties when removed from entity
        super.detach();
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.EntityController#openDebugger()
     */
    public void openDebugger()
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.Tickable#tick(double)
     */
    @Override
    public void tick(double dt)
    {
        // Convert desired speed, bearing and altitude into a desired velocity

        // Get X/Y part of desired velocity - sin/cos switched to account for 0 North.
        double x = Math.sin(desiredHeading);
        double y = Math.cos(desiredHeading);
        Vector3 desiredVelocity = new Vector3(x, y, 0);

        // Scale by speed to get the right ground speed
        desiredVelocity = desiredVelocity.multiply(desiredSpeed);

        // Create final desired velocity
        desiredVelocity = new Vector3(desiredVelocity.x, desiredVelocity.y, 0.0);
        if(desiredVelocity.epsilonEquals(Vector3.ZERO))
        {
            desiredVelocity = Vector3.ZERO;
        }

        final Entity vehicle = getEntity();

        // Store in properties so it's displayed in UI.
        vehicle.setProperty("desired-velocity", desiredVelocity);

        // Calculated error between desired velocity and actual velocity
        Vector3 velocityError = desiredVelocity.subtract(vehicle.getVelocity());

        // Move current velocity toward desired velocity
        double vfactor = 1.0 - 0.0; // Math.exp(-aggressiveness * dt);
        Vector3 newVelocity = vehicle.getVelocity().add(velocityError.multiply(vfactor));
        if(newVelocity.epsilonEquals(Vector3.ZERO))
        {
            newVelocity = Vector3.ZERO;
        }
        vehicle.setVelocity(newVelocity);

        ///////////////////////////////////////////////////////////////////////
        // Now update the heading...
        double currentHeading = EntityTools.getHeading(vehicle);
        double headingError = desiredHeading - currentHeading;
        while(headingError > Math.PI)
        {
            headingError -= 2 * Math.PI;
        }
        while(headingError < -Math.PI)
        {
            headingError += 2 * Math.PI;
        }

        double hfactor = 1.0 - 0.0; // Math.exp(-aggressiveness * dt);
        double newHeading = currentHeading + hfactor * headingError;

        vehicle.setHeading(Angles.navRadiansToMathRadians(newHeading));
    }

}
