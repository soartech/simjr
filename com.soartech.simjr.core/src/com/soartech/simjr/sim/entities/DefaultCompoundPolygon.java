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
 * Created on Mar 26, 2009
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
import com.soartech.simjr.sim.EntityPrototype;

/**
 * The default implementation of AbstractPolygon used for routes and areas.
 * Whether the polygon is closed or not is determined by the "polygon.closed"
 * property in its prototype. If the property is missing, it defaults to false.
 * 
 * @author ray
 */
public class DefaultCompoundPolygon extends AbstractEntityCapability implements EntityPositionProvider
{
    private final String defaultName;
    protected List<String> polygons = Collections.synchronizedList(new ArrayList<String>());

    public static final Comparator<DefaultCompoundPolygon> NAME_COMPARATOR = new Comparator<DefaultCompoundPolygon>() {

        public int compare(DefaultCompoundPolygon o1, DefaultCompoundPolygon o2)
        {
            return o1.getName().compareTo(o2.getName());
        }};
        
    /**
     * Adapt the given object to a polygon. This method is a convenience method for scripting.
     * 
     * @param o the object to adapt
     * @return the polygon, or <code>null</code>
     */
    public static DefaultCompoundPolygon adapt(Object o)
    {
        return Adaptables.adapt(o, DefaultCompoundPolygon.class);
    }

    /**
     * Default constructor expected by {@link EntityPrototype#createEntity(String)}
     */
    public DefaultCompoundPolygon()
    {
        this.defaultName = "";
    }
    
    /**
     * Construct an empty polygon
     * 
     * @param name The name of the polygon
     */
    public DefaultCompoundPolygon(String defaultName)
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
        getEntity().setProperty(EntityConstants.PROPERTY_POLYGONS, polygons);
    }

    @Override
    public void detach()
    {
        getEntity().setProperty(EntityConstants.PROPERTY_POLYGONS, null);
        super.detach();
    }

    public void tick(double dt)
    {
        // Do nothing
    }
    
    /**
     * Add the given polygon to the list of polygons. A property change event
     * for PROPERTY_POLYGONS is fired.
     * 
     * @param polygonName The name of an entity
     */
    public void addPolygon(String polygonName)
    {
        polygons.add(polygonName);
        getEntity().firePropertyChanged(EntityConstants.PROPERTY_POLYGONS);
    }
    
    /**
     * Remove the first instance of the given polygon from the list of polygons. A
     * property change event for PROPERTY_POLYGONS is fired.
     * 
     * @param polygonName The name of the polygon to remove
     * @return True if the point was removed, false otherwise.
     */
    public boolean removePolygon(String polygonName)
    {
        boolean r = polygons.remove(polygonName);
        if(r)
        {
            getEntity().firePropertyChanged(EntityConstants.PROPERTY_POLYGONS);
        }
        return r;
    }
    
    /**
     * @return List of polygon names in this compound polygon
     */
    public List<String> getPolygonNames()
    {
        synchronized(this.polygons)
        {
            return new ArrayList<String>(polygons);
        }
    }
    
    /**
     * Set the polygons in this compound polygon.  A property change event for 
     * PROPERTY_POLYGONS is fired.
     * 
     * @param polygons New list of polygons
     */
    public void setPolygonNames(List<String> polygons)
    {
        synchronized (this.polygons)
        {
            this.polygons.clear();
            this.polygons.addAll(polygons);
            getEntity().firePropertyChanged(EntityConstants.PROPERTY_POLYGONS);
        }
    }
    
    public List<Entity> getPolygons()
    {
        List<Entity> r = new ArrayList<Entity>();
        for(String p : getPolygonNames())
        {
            Entity e = getEntity().getSimulation().getEntity(p);
            if(e != null)
            {
                r.add(e);
            }
        }
        return r;
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.EntityPositionProvider#getPosition()
     */
    public Vector3 getPosition()
    {
        double x = 0.0, y = 0.0;
        int nn = 0;
        List<Entity> pts = getPolygons();
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
        List<Entity> pts = getPolygons();
        return (pts != null && pts.size() > 0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "compound polygon: " + getPolygonNames();
    }
}
