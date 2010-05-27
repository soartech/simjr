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
 * Created on May 30, 2007
 */
package com.soartech.simjr.sim.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.soartech.math.Vector3;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPositionProvider;
import com.soartech.simjr.sim.Simulation;

/**
 * A polygon is an ordered list of points such as a route or an area. The points
 * in a polygon are names of other entities in the system.  Thus, it is possible
 * for a polygon to change shape over time as entities are moved.
 * 
 * @author ray
 */
public abstract class AbstractPolygon extends AbstractEntityCapability implements EntityPositionProvider
{
    private final String defaultName;
    private List<String> points = Collections.synchronizedList(new ArrayList<String>());

    public static final Comparator<AbstractPolygon> NAME_COMPARATOR = new Comparator<AbstractPolygon>() {

        public int compare(AbstractPolygon o1, AbstractPolygon o2)
        {
            return o1.getName().compareTo(o2.getName());
        }};
        
    /**
     * Adapt the given object to a polygon. This method is a convenience method for scripting.
     * 
     * @param o the object to adapt
     * @return the polygon, or <code>null</code>
     */
    public static AbstractPolygon adapt(Object o)
    {
        return Adaptables.adapt(o, AbstractPolygon.class);
    }
    
    /**
     * Searches the sim for all polygons that contain the given named point, 
     * usually the name of an entity.
     * 
     * @param sim The simulation
     * @param pointName The name of the point
     * @return List of polygons containing point as a member.
     */
    public static List<AbstractPolygon> getPolygonsContainingPoint(Simulation sim, String pointName)
    {
        List<AbstractPolygon> r = new ArrayList<AbstractPolygon>();
        for(Entity e : sim.getEntities())
        {
            final AbstractPolygon p = Adaptables.adapt(e, AbstractPolygon.class);
            if(p != null && p.containsPoint(pointName))
            {
                r.add(p);
            }
        }
        return r;
    }
    
    /**
     * Construct an empty polygon
     * 
     * @param name The name of the polygon
     */
    public AbstractPolygon(String defaultName)
    {
        this.defaultName = defaultName;
    }

    /**
     * @return the name of the containing entity, or the default name if not 
     *      attached to an entity.
     */
    public String getName()
    {
        final Entity e = getEntity();
        return e != null ? e.getName() : defaultName;
    }
    
    @Override
    public void attach(Entity entity)
    {
        super.attach(entity);
        getEntity().setProperty(EntityConstants.PROPERTY_POINTS, points);
    }

    @Override
    public void detach()
    {
        getEntity().setProperty(EntityConstants.PROPERTY_POINTS, null);
        super.detach();
    }

    public void tick(double dt)
    {
        // Do nothing
    }

    /**
     * @return the number of points in the polygon
     */
    public int getNumPoints()
    {
        return points.size();
    }
    
    /**
     * Add the given point to the end of the polygon. A property change event
     * for PROPERTY_POINTS is fired.
     * 
     * @param pointName The name of an entity
     */
    public void addPoint(String pointName)
    {
        points.add(pointName);
        getEntity().firePropertyChanged(EntityConstants.PROPERTY_POINTS);
    }
    
    /**
     * Remove the first instance of the given point from the polygon. A
     * property change event for PROPERTY_POINTS is fired.
     * 
     * @param pointName The name of the point to remove
     * @return True if the point was removed, false otherwise.
     */
    public boolean removePoint(String pointName)
    {
        boolean r = points.remove(pointName);
        if(r)
        {
            getEntity().firePropertyChanged(EntityConstants.PROPERTY_POINTS);
        }
        return r;
    }
    
    /**
     * Returns true if the given point is in the polygon's list of points.
     * 
     * @param pointName The name of the point to test
     * @return True if the point is in the polygon's list of points
     */
    public boolean containsPoint(String pointName)
    {
        return points.contains(pointName);
    }
    
    /**
     * @return List of point names in this polygon
     */
    public List<String> getPointNames()
    {
        synchronized(this.points)
        {
            return new ArrayList<String>(points);
        }
    }
    
    /**
     * Set the points in this polygon.  A property change event for 
     * PROPERTY_POINTS is fired.
     * 
     * @param points New list of points
     */
    public void setPointNames(List<String> points)
    {
        synchronized (this.points)
        {
            this.points.clear();
            this.points.addAll(points);
            getEntity().firePropertyChanged(EntityConstants.PROPERTY_POINTS);
        }
    }

    public List<Entity> getPoints()
    {
        List<Entity> r = new ArrayList<Entity>();
        for(String p : getPointNames())
        {
            Entity e = getEntity().getSimulation().getEntity(p);
            if(e != null)
            {
                r.add(e);
            }
        }
        return r;
    }
    
    /**
     * @return true if this polygon is closed
     */
    public abstract boolean isClosed();

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.EntityPositionProvider#getPosition()
     */
    public Vector3 getPosition()
    {
        double x = 0.0, y = 0.0;
        int nn = 0;
        List<Entity> pts = getPoints();
        if (pts != null && pts.size() > 0)
        {
        	for (Entity e : pts)
        	{
        		Vector3 thisPos = e.getPosition();
        		if (thisPos != null)
        		{
        			x += thisPos.x;
        			y += thisPos.y;
        			nn++;
        		}
        	}
        }
        if (nn > 0)
        {
        	return new Vector3(x / nn, y / nn, 0);
        }
        return Vector3.ZERO;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.EntityPositionProvider#hasPosition()
     */
    public boolean hasPosition()
    {
        List<Entity> pts = getPoints();
        return (pts != null && pts.size() > 0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return (isClosed() ? "Closed" : "Open") + " polygon: " + getPointNames();
    }
    
    
}

