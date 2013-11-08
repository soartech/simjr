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
 * Created on May 6, 2010
 */
package com.soartech.simjr.controllers;

import java.util.ArrayList;
import java.util.List;

import com.soartech.math.Angles;
import com.soartech.math.Vector3;
import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.Tickable;

/**
 * A simple behavior that causes an entity to avoid one or more points or
 * entities. Should be added to the end of the capability chain (i.e. after
 * any route or segment followers) to ensure that it can affect the movement
 * of the entity.
 *
 * @author ray
 */
public class Avoider extends AbstractEntityCapability implements Tickable
{
    /**
     * Property that is a List<> of Entity, Vector3, or entity names that
     * specifies the points to avoid.
     */
    public static final String POINTS_PROPERTY = "avoider.points";
    public static final String RADIUS_PROPERTY = "avoider.radius";

    private List<Vector3> getPointsToAvoid(Simulation sim)
    {
        final List<Vector3> result = new ArrayList<Vector3>();
        List<?> points = (List<?>) getEntity().getProperty(POINTS_PROPERTY);
        if(points == null)
        {
            points = sim.getEntitiesFast();
        }

        for(Object o : points)
        {
            if(o instanceof Vector3)
            {
                result.add((Vector3) o);
            }
            else if(o instanceof Entity)
            {
                result.add(((Entity) o).getPosition());
            }
            else
            {
                result.add(sim.getEntity(o.toString()).getPosition());
            }
        }
        return result;
    }

    private double getRadius()
    {
        final Number r = (Number) getEntity().getProperty(RADIUS_PROPERTY);
        return r != null ? r.doubleValue() : 10.0;
    }

    private Vector3 rotateXY(Vector3 v, double angle)
    {
        return new Vector3(v.x * Math.cos(angle) - v.y * Math.sin(angle),
                           v.x * Math.sin(angle) + v.y * Math.cos(angle),
                           v.z);
    }
    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.Tickable#tick(double)
     */
    @Override
    public void tick(double dt)
    {
        final Entity e = getEntity();
        final List<Vector3> pointsToAvoid = getPointsToAvoid(e.getSimulation());
        Vector3 newVelocity = e.getVelocity();
        final double radius = getRadius();
        final Vector3 epos = e.getPosition();
        for(Vector3 p : pointsToAvoid)
        {
            final double distance = Vector3.getLateralDistance(epos, p);
            final boolean inFront = newVelocity.dot(p.subtract(epos)) > 0;
            if(inFront && distance < radius)
            {
                final double angle = Math.atan2(p.y - epos.y, p.x - epos.x);
                final double vangle = Math.atan2(newVelocity.y, newVelocity.x);
                if(inFront && Angles.angleDifference(angle, vangle) < Math.toRadians(30.0))
                {
                    newVelocity = rotateXY(newVelocity, Math.toRadians(20));
                }
            }
        }
        e.setVelocity(newVelocity);
        e.setHeading(Math.atan2(newVelocity.y, newVelocity.x));
    }

}
