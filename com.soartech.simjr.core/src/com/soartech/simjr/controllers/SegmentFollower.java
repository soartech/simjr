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
 * Created on Sep 21, 2009
 */
package com.soartech.simjr.controllers;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.soartech.math.Vector3;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityController;
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.entities.AbstractPolygon;

/**
 * A controller that directs an entity along a series of segments.
 *
 * <p>A segment is defined as a pair of waypoints. Each segment has an
 * associated speed. When the follower is assigned a list of segments to
 * follow with the {@link #setSegments(SegmentInfo)} method, the follower
 * automatically constructs a new route entity in the simulation so that
 * it will be displayed.
 *
 * @author ray
 */
public class SegmentFollower extends AbstractEntityCapability implements
        EntityController
{
    private static final Logger logger = Logger.getLogger(SegmentFollower.class);

    private List<SegmentFollowerListener> listeners = new CopyOnWriteArrayList<SegmentFollowerListener>();
    private SegmentInfo segments = null;
    private SegmentInfo currentSegment = null;
    private Vector3 currentSegmentStart = null;
    private Entity constructedRoute = null;
    private int nameIndex = 0;
    private boolean routeVisible = true;
    private double currentSegmentDelay;

    /**
     * Adapt the given object to a SegmentFollower. This method is a convenience method for scripting.
     *
     * @param o the object to adapt
     * @return the SegmentFollower, or <code>null</code>
     */
    public static SegmentFollower adapt(Object o)
    {
        return Adaptables.adapt(o, SegmentFollower.class);
    }

    /**
     * Add a segment listener
     *
     * @param listener the listener to add
     */
    public void addListener(SegmentFollowerListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Remove a segment listener previously added with {@link #addListener(SegmentFollowerListener)}.
     *
     * @param listener the listener to remove
     */
    public void removeListener(SegmentFollowerListener listener)
    {
        listeners.remove(listener);
    }

    public void setSegments(SegmentInfo segments) throws SimulationException
    {
        this.segments = segments;

        if(getEntity() != null)
        {
            constructRouteForNewSegments();
            startFollowingSegments();
        }
    }

    public SegmentInfo getSegments()
    {
        return segments;
    }

    public SegmentInfo getCurrentSegment()
    {
        return currentSegment;
    }

    public Vector3 getStartOfCurrentSegment()
    {
        return currentSegmentStart;
    }

    public Entity getConstructedRoute()
    {
        return constructedRoute;
    }

    public boolean isRouteVisible()
    {
        return routeVisible;
    }

    public void setRouteVisible(boolean routeVisible)
    {
        this.routeVisible = routeVisible;
    }

    private void startFollowingSegments()
    {
        getEntity().setProperty("segments", segments);
        startFollowingSegment(segments);
    }

    private void startFollowingSegment(SegmentInfo segment)
    {
        currentSegment = segment;
        currentSegmentStart = getEntity().getPosition();
        getEntity().setProperty("segments.current", currentSegment);

        if(currentSegment == null)
        {
            getEntity().setVelocity(Vector3.ZERO);
            logger.debug("'" + getEntity().getName() + "' finished following assigned segments");

            for(SegmentFollowerListener listener : listeners)
            {
                listener.onCompletedAssignedSegments(this);
            }
        } else {
            currentSegmentDelay = currentSegment.getDelay();
        }
    }

    private void removeConstructedRoute()
    {
        if(constructedRoute != null)
        {
            final Simulation sim = getEntity().getSimulation();
            sim.removeEntity(constructedRoute);
            logger.debug("Removed route '" + constructedRoute.getName() + "' for segment follower '"  + getEntity().getName() + "'");
            constructedRoute = null;
        }
    }

    private String generateConstructedRouteName()
    {
        return getEntity().getName() + ":SegmentFollower:route:" + (nameIndex++);
    }

    private boolean hasZeroOrOneSegments()
    {
        return segments == null || segments.getNext() == null;
    }

    private void constructRouteForNewSegments() throws SimulationException
    {
        removeConstructedRoute();

        if(!isRouteVisible())
        {
            return;
        }
        if(hasZeroOrOneSegments())
        {
            return;
        }

        final Simulation simulation = getEntity().getSimulation();
        final EntityPrototype prototype = simulation.getEntityPrototypes().getPrototype("route");
        final Entity routeEntity = prototype.createEntity(generateConstructedRouteName());
        final AbstractPolygon route = Adaptables.adapt(routeEntity, AbstractPolygon.class);

        for(SegmentInfo segment = segments; segment != null; segment = segment.getNext())
        {
            route.addPoint(segment.getWaypoint());
        }

        simulation.addEntity(routeEntity);

        routeEntity.setProperty(EntityConstants.PROPERTY_SHAPE_LINE_COLOR, Color.BLUE);
        routeEntity.setProperty(EntityConstants.PROPERTY_SHAPE_WIDTH_PIXELS, 4);
        routeEntity.setProperty(EntityConstants.PROPERTY_SHAPE_OPACITY, 0.5);
        routeEntity.setProperty(EntityConstants.PROPERTY_SHAPE_LABEL_VISIBLE, false);

        logger.debug("Constructed route '" + routeEntity.getName() + "' for segment follower '"  + getEntity().getName() + "'");
        constructedRoute = routeEntity;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.AbstractEntityCapability#attach(com.soartech.simjr.sim.Entity)
     */
    @Override
    public void attach(Entity entity)
    {
        super.attach(entity);

        try
        {
            constructRouteForNewSegments();
        }
        catch (SimulationException e)
        {
            logger.error(e.getMessage(), e);
            return;
        }

        startFollowingSegments();
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.AbstractEntityCapability#detach()
     */
    @Override
    public void detach()
    {
        removeConstructedRoute();

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
        if(currentSegment == null)
        {
            return;
        }

        final Entity entity = getEntity();
        final Vector3 target = currentSegment.getWaypoint(entity.getSimulation()).getPosition();
        final Vector3 pos = entity.getPosition();
        Vector3 dir = target.subtract(pos).normalized();
        entity.setHeading(Math.atan2(dir.y, dir.x));
       
        currentSegmentDelay -= dt;
        
        if (currentSegmentDelay > 0.0)
        {
            entity.setVelocity(new Vector3(0.0, 0.0, 0.0));
        } else {
            dt = -currentSegmentDelay;
            currentSegmentDelay = 0.0;
            final double speed = currentSegment.getSpeed();
            entity.setVelocity(dir.multiply(speed));

            // If we'll  overshoot the target in the next tick, then consider it
            // achieved and move on to the next.
            if(pos.subtract(target).length() < speed * dt)
            {
                final SegmentInfo completedSegment = currentSegment;
                startFollowingSegment(currentSegment.getNext());

                for(SegmentFollowerListener listener : listeners)
                {
                    listener.onCompletedSegment(this, completedSegment);
                }
            }
        }
    }

}
