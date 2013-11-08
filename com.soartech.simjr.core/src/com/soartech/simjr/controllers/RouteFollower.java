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
 * Created on Jul 6, 2007
 */
package com.soartech.simjr.controllers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.soartech.math.Vector3;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityController;
import com.soartech.simjr.sim.EntityPropertyListener;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.entities.AbstractPolygon;
import com.soartech.simjr.sim.entities.DamageStatus;

/**
 * A simple controller that moves an entity along a route. The speed and
 * route to follow are controlled by setters on the object, or with the
 * "routeFollower.route" and "routeFollower.speed" properties on the
 * attached entity.
 *
 * @author ray
 */
public class RouteFollower extends AbstractEntityCapability implements EntityController
{
    private static final Logger logger = Logger.getLogger(RouteFollower.class);

    private static final String ROUTE_PROPERTY = "routeFollower.route";
    private static final String SPEED_PROPERTY = "routeFollower.speed";

    private double speed = 10.0;
    private Object route;
    private boolean pushingProps = false;

    private int targetPoint = 0;
    private List<RouteFollowerListener> listeners = new CopyOnWriteArrayList<RouteFollowerListener>();

    private final EntityPropertyListener propListener = new EntityPropertyListener()
    {

        @Override
        public void onPropertyChanged(Entity entity, String propertyName)
        {
            if(ROUTE_PROPERTY.equals(propertyName) || SPEED_PROPERTY.equals(propertyName))
            {
                pullProps();
            }
        }
    };

    /**
     */
    public RouteFollower()
    {
    }

    /**
     * Add a listener to this controller
     *
     * @param listener The listener to add
     */
    public void addListener(RouteFollowerListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Remove a listener from this controller
     *
     * @param listener The listener to remove
     */
    public void removeListener(RouteFollowerListener listener)
    {
        listeners.remove(listener);
    }

    public void setSpeed(double speed)
    {
        this.speed = speed;
        pushProps();
    }

    public double getSpeed()
    {
        return speed;
    }

    public void setRoute(Object route)
    {
        this.route = route;
        pushProps();
    }

    public Object getRoute()
    {
        return route;
    }

    private AbstractPolygon getRoutePolygon()
    {
        if(route == null)
        {
            return null;
        }

        AbstractPolygon routePoly = Adaptables.adapt(route, AbstractPolygon.class);
        if(routePoly != null)
        {
            return routePoly;
        }

        Entity e = getEntity().getSimulation().getEntity(route.toString());
        routePoly = Adaptables.adapt(e, AbstractPolygon.class);
        if(routePoly != null)
        {
            return routePoly;
        }
        return null;
    }

    private Entity getTargetEntity(AbstractPolygon route)
    {
        List<Entity> points = route.getPoints();
        if(points.isEmpty())
        {
            return null;
        }
        targetPoint = Math.min(targetPoint, points.size() - 1);

        return points.get(targetPoint);
    }

    private Vector3 getTargetPoint(AbstractPolygon route)
    {
        Entity target = getTargetEntity(route);
        if(target == null)
        {
            return Vector3.ZERO;
        }

        Double enforceAgl = EntityTools.getEnforcedAboveGroundLevel(getEntity());
        if(enforceAgl != null)
        {
            return getEntity().getSimulation().getTerrain().clampPointToGround(target.getPosition(), enforceAgl);
        }
        else
        {
            return target.getPosition();
        }
    }

    private void pullProps()
    {
        if(pushingProps)
        {
            return;
        }
        final Entity entity = getEntity();
        final Object route = entity.getProperty(ROUTE_PROPERTY);
        if(route != null)
        {
            logger.info(entity + ": using " + ROUTE_PROPERTY + " property value '" + route + "'");
            this.route = route;
        }
        final Double speed = (Double) entity.getProperty(SPEED_PROPERTY);
        if(speed != null)
        {
            logger.info(entity + ": using " + SPEED_PROPERTY + " property value '" + speed + "'");
            this.speed = speed;
        }
    }

    private void pushProps()
    {
        final Entity e = getEntity();
        if(e != null)
        {
            pushingProps = true;
            e.setProperty(ROUTE_PROPERTY, route);
            e.setProperty(SPEED_PROPERTY, speed);
            pushingProps = false;
        }
    }
    private void removeProps()
    {
        final Entity e = getEntity();
        if(e != null)
        {
            pushingProps = true;
            e.setProperty(ROUTE_PROPERTY, null);
            e.setProperty(SPEED_PROPERTY, null);
            pushingProps = false;
        }
    }

    @Override
    public void attach(Entity entity)
    {
        super.attach(entity);
        pullProps();
        pushProps();
        entity.addPropertyListener(propListener);
    }

    @Override
    public void detach()
    {
        getEntity().removePropertyListener(propListener);
        removeProps();
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
        final Entity entity = getEntity();
        Map<String, Object> props = entity.getProperties();
        if(DamageStatus.destroyed == EntityTools.getDamage(props))
        {
            return;
        }

        AbstractPolygon route = getRoutePolygon();
        if(route == null)
        {
            logger.error(entity.getName() + ": No route specified for route follower. Route = " + this.route);
            return;
        }

        Vector3 pos = entity.getPosition();
        Vector3 target = getTargetPoint(route);

        double speed = getSpeed();

        Vector3 dir = target.subtract(pos).normalized();
        entity.setVelocity(dir.multiply(speed));
        entity.setHeading(Math.atan2(dir.y, dir.x));

        // If we'll  overshoot the target in the next tick, then consider it
        // achieved and move on to the next.
        if(pos.subtract(target).length() < speed * dt)
        {
            Entity targetEntity = getTargetEntity(route);
            if(targetEntity != null)
            {
                for(RouteFollowerListener listener : listeners)
                {
                    listener.onWaypointAchieved(this, targetEntity);
                }
            }
            targetPoint = (targetPoint + 1) % route.getPointNames().size();
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "RouteFollower";
    }


}
