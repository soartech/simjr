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
 *
 * Created on Sep 22, 2007
 */
package com.soartech.simjr.controllers;

import com.soartech.math.Angles;
import com.soartech.math.Vector3;
import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityTools;

/**
 * A controller that provides an interface for controlling a vehicle, usually
 * a helicopter, as a rotary wing aircraft.  Provides methods for setting
 * desired speed, bearing, heading, and altitude.  This controller may be
 * added directly to a vehicle or may be nested in another controller such as
 * a Soar agent.
 *
 * <p>Why is this not in the helicopter class? A few reasons. First, putting
 * this model in the Helicopter makes it hard to re-use the functionality in
 * other classes. Second, if this is embedded in the Helicopter class, it is
 * difficult to install other controllers (like the virtual joystick) on the
 * helicopter because this code will fight with them.
 *
 * TODO: This controller is just barely adequate, especially when running at
 * faster than 1x speed.
 *
 * @author ray
 */
public class RotaryWingFlightController extends AbstractEntityCapability implements
        FlightController
{
    /**
     * Desired ground speed in m/s
     */
    private double desiredSpeed = 0.0;
    /**
     * Desired bearing in nav radians (0 north)
     */
    private double desiredBearing = 0.0;
    /**
     * Desired altitude in meters
     */
    private double desiredAltitude = 0.0;
    /**
     * Desired altitude rate in m/s
     */
    private double desiredAltitudeRate = 15.0;

    private double desiredHeading = 0.0;

    private double aggressiveness = 0.2;

    /**
     * The speed at which heading should be locked to bearing in m/s
     *
     * The constant value here was adopted from the MAK interface
     * for AutoWingman.
     */
    private static final double HeadingAlignThresholdSpeed = 15.0;

    public RotaryWingFlightController()
    {
    }



    /* (non-Javadoc)
     * @see com.soartech.simjr.controllers.FlightController#getDesiredSpeed()
     */
    public double getDesiredSpeed()
    {
        return desiredSpeed;
    }

    private void setProperty(String name, Object value)
    {
        if(getEntity() != null)
        {
            getEntity().setProperty(name, value);
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.controllers.FlightController#setDesiredSpeed(double)
     */
    public void setDesiredSpeed(double speed)
    {
        this.desiredSpeed = speed;

        setProperty("desired-speed", desiredSpeed);
    }

    /**
     * @param desiredBearing The new desired bearing in radians
     */
    public void setDesiredBearing(double desiredBearing)
    {
        this.desiredBearing = desiredBearing;
        setProperty("desired-bearing", Math.toDegrees(desiredBearing));

        // Once the aircraft reaches a certain speed, the heading
        // should be locked to the bearing to avoid instabilities.
        if (this.desiredSpeed >= HeadingAlignThresholdSpeed)
        {
            setDesiredHeading(desiredBearing);
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.controllers.FlightController#getDesiredHeading()
     */
    public double getDesiredHeading()
    {
        return desiredHeading;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.controllers.FlightController#setDesiredHeading(double)
     */
    public void setDesiredHeading(double desiredHeading)
    {
        this.desiredHeading = desiredHeading;
        setProperty("desired-heading", Math.toDegrees(desiredHeading));
    }


    /* (non-Javadoc)
     * @see com.soartech.simjr.controllers.FlightController#getDesiredAltitude()
     */
    public double getDesiredAltitude()
    {
        return desiredAltitude;
    }

    /**
     * @param altitude
     */
    public void setDesiredAltitude(double altitude)
    {
        this.desiredAltitude = altitude;
        setProperty("desired-altitude", desiredAltitude);

//        logger.debug(getEntity().getName() + ": desired altitude set to " + desiredAltitude +
//                " (rate=" + desiredAltitudeRate + " m/s)");
    }

    public void setDesiredAltitudeRate(double rate)
    {
        this.desiredAltitudeRate = rate;
        setProperty("desired-altitude-rate", desiredAltitudeRate);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.AbstractEntityCapability#attach(com.soartech.simjr.sim.Entity)
     */
    @Override
    public void attach(Entity entity)
    {
        super.attach(entity);
        setProperty("desired-speed", desiredSpeed);
        setProperty("desired-bearing", Math.toDegrees(desiredBearing));
        setProperty("desired-heading", Math.toDegrees(desiredHeading));
        setProperty("desired-altitude", desiredAltitude);
        setProperty("desired-altitude-rate", desiredAltitudeRate);
    }



    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.AbstractEntityCapability#detach()
     */
    @Override
    public void detach()
    {
        setProperty("desired-speed", null);
        setProperty("desired-bearing", null);
        setProperty("desired-heading", null);
        setProperty("desired-altitude", null);
        setProperty("desired-altitude-rate", null);
        super.detach();
    }



    /* (non-Javadoc)
     * @see com.soartech.simjr.EntityController#openDebugger()
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
        double x = Math.sin(desiredBearing);
        double y = Math.cos(desiredBearing);
        Vector3 desiredVelocity = new Vector3(x, y, 0);

        // Scale by speed to get the right ground speed
        desiredVelocity = desiredVelocity.multiply(desiredSpeed);

        final Entity vehicle = getEntity();
        // Calculate desired Z velocity
        double altitudeError = desiredAltitude - EntityTools.getAltitude(vehicle);
        double desiredVelocityZ = altitudeError * dt;

        // Clamp Z velocity to desired altitude rate
        if(desiredVelocityZ > desiredAltitudeRate)
        {
            desiredVelocityZ = desiredAltitudeRate;
        }
        else if(desiredVelocityZ < -desiredAltitudeRate)
        {
            desiredVelocityZ = -desiredAltitudeRate;
        }

        // Create final desired velocity
        desiredVelocity = new Vector3(desiredVelocity.x, desiredVelocity.y, desiredVelocityZ);
        if(desiredVelocity.epsilonEquals(Vector3.ZERO))
        {
            desiredVelocity = Vector3.ZERO;
        }

        // Store in properties so it's displayed in UI.
        vehicle.setProperty("desired-velocity", desiredVelocity);

        // Calculated error between desired velocity and actual velocity
        Vector3 velocityError = desiredVelocity.subtract(vehicle.getVelocity());

        // Move current velocity toward desired velocity
        double vfactor = 1.0 - Math.exp(-aggressiveness * dt);
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

        double hfactor = 1.0 - Math.exp(-aggressiveness * dt);
        double newHeading = currentHeading + hfactor * headingError;

        vehicle.setHeading(Angles.navRadiansToMathRadians(newHeading));
    }

}
