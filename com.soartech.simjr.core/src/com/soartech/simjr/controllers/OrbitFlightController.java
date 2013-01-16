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
 * Created on Mar 3, 2009
 */
package com.soartech.simjr.controllers;

import org.apache.log4j.Logger;

import com.soartech.math.Angles;
import com.soartech.math.Vector3;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityController;

/**
 * @author ray
 */
public class OrbitFlightController extends AbstractEntityCapability implements
        EntityController
{
    private static final Logger logger = Logger.getLogger(OrbitFlightController.class);

    /**
     * The entity to circle, or <code>null</code> if <code>centerPoint</code> is set
     */
    private Entity centerEntity;

    /**
     * The point to circle, or <code>null</code> if <code>centerEntity</code> is set
     */
    private Vector3 centerPoint = Vector3.ZERO;

    private double altitude;
    private double speed = 1.0;
    private double radius;

    /**
     * Construct a new orbit flight controller. The controller must still be added
     * to the entity with a call to {@link Entity#addCapability(com.soartech.simjr.sim.EntityCapability)}.
     * The entity must has a {@link FlightController} capability installed.
     */
    public OrbitFlightController()
    {
    }

    /**
     * @return the center of the orbit, either a Vector3, or an {@link Entity}
     */
    public Object getCenter()
    {
        return centerEntity != null ? centerEntity : null;
    }

    /**
     * Set the controller to orbit a particular entity, which may be moving.
     *
     * @param centerEntity the entity to orbit
     */
    public void setCenterEntity(Entity centerEntity)
    {
        this.centerPoint = null;
        this.centerEntity = centerEntity;
    }

    /**
     * Set the controller to orbit a particular point.
     *
     * @param centerPoint the point to orbit
     */
    public void setCenterPoint(Vector3 centerPoint)
    {
        this.centerEntity = null;
        this.centerPoint = centerPoint;
    }
    /**
     * @return the orbit altitude
     */
    public double getAltitude()
    {
        return altitude;
    }


    /**
     * Set the altitude of the orbit
     * @param altitude the altitude of the orbit
     */
    public void setAltitude(double altitude)
    {
        this.altitude = altitude;
    }


    /**
     * @return the orbit speed in m/s
     */
    public double getSpeed()
    {
        return speed;
    }


    /**
     * @param speed the orbit speed in m/s
     */
    public void setSpeed(double speed)
    {
        this.speed = speed;
    }


    /**
     * @return the radius of the orbit in meters
     */
    public double getRadius()
    {
        return radius;
    }


    /**
     * @param radius the radius of the orbit in meters
     */
    public void setRadius(double radius)
    {
        this.radius = radius;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.Tickable#tick(double)
     */
    @Override
    public void tick(double dt)
    {
        if(centerEntity == null && centerPoint == null)
        {
            return;
        }

        // Figure out the point to orbit (entity or fixed point)
        final Vector3 point = centerEntity != null ? centerEntity.getPosition() : centerPoint;

        final Entity entity = getEntity();
        // Get the entity's flight controller so we can tell it where to go
        final FlightController fc = Adaptables.adapt(entity, FlightController.class);
        if(fc == null)
        {
            logger.warn("Entity '" + entity.getName() + "' has no FlightController installed.");
            return;
        }

        Vector3 pos = entity.getPosition();
        Vector3 delta = pos.subtract(point);

//        sp "fly-circle*elaborate*relative-radius-error*inside
//           [match-goal <g> fly-circle]
//           (<g> ^waypoint-computer <wc>
//                ^circle-radius <desired-rad>)
//           (<wc> ^relative-geometry.lateral-range.value {<rad> <= <desired-rad>})
//        -->
//           ## from -1 to 0... 0 at correct radius
//           (<g> ^relative-radius-error (- (/ <rad> <desired-rad>) 1.0))
//        "
//
//      sp "fly-circle*elaborate*relative-radius-error*outside
//      [match-goal <g> fly-circle]
//      (<g> ^waypoint-computer <wc>
//           ^circle-radius <desired-rad>)
//      (<wc> ^relative-geometry.lateral-range.value {<rad> > <desired-rad>})
//   -->
//      ## from 0 to 1... 0 at correct radius
//      (<g> ^relative-radius-error (- 1.0 (/ <desired-rad> <rad>)))
//   "

        double currentRadius = Vector3.getLateralDistance(pos, point);
        double desiredRadius = getRadius();
        double relativeRadiusError = 0.0;
        double currentBearing = Angles.mathRadiansToNavRadians(entity.getHeading());
        double currentRelativeBearing = (Math.PI + Angles.getBearing(delta)) - currentBearing;
        int oriSign = -1; // use +1 for ccw, -1 for clockwise

        if (currentRadius <= desiredRadius)
        {
            // from -1 to 0... 0 at correct radius
            relativeRadiusError = currentRadius / desiredRadius - 1.0;
        }
        else
        {
            // from 0 to 1... 0 at correct radius
            relativeRadiusError = 1.0 - desiredRadius / currentRadius;
        }

//        sp "fly-circle*elaborate*desired-bearing
//           [match-goal <g> fly-circle]
//           (<g> ^waypoint-computer.relative-geometry <rg>
//                ^relative-radius-error {<rre> > -0.8}
//                ^orientation-sign <ori-sgn>
//                ^desired-flight-profile <dfp>)
//           (<rg> ^magnetic-bearing.value <mb>)
//        -->
//           (<dfp> ^desired-bearing (+ <mb> (* <ori-sgn> -90) (* <ori-sgn> 90 <rre>))
//                  ^desired-heading (+ <mb> (* <ori-sgn> -90) (* <ori-sgn> 90 <rre>)))
//        "

        fc.setDesiredAltitude(altitude);
        fc.setDesiredSpeed(speed);

        if (relativeRadiusError > -0.8)
        {
            double desiredHeading = currentBearing + currentRelativeBearing - oriSign * Math.PI/2.0 * (relativeRadiusError - 1.0);
            fc.setDesiredHeading(desiredHeading);
            if (fc instanceof RotaryWingFlightController)
            {
                ((RotaryWingFlightController)fc).setDesiredBearing(desiredHeading);
            }
        }
        // otherwise we're very close to the center point, so fly
        // straight until we're farther away.
    }
}

