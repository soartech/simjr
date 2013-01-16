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
 * Created on Aug 13, 2007
 */
package com.soartech.simjr.sim.entities;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.soartech.math.Vector3;
import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityTools;

/**
 * An object implementing the ability of an entity to contain other
 * entities, such as a troop transport.  This object is accessible
 * with entity.getAdapter(EntityContainerCapability.class).
 * 
 * @author ray
 */
public class EntityContainerCapability  extends AbstractEntityCapability
{
    private static final Logger logger = Logger.getLogger(EntityContainerCapability.class);
    
    private List<Entity> contents = new ArrayList<Entity>();
    private boolean notify = true;
    
    public EntityContainerCapability()
    {
    }
    
    public void add(Entity e)
    {
        synchronized (getLock())
        {
            final Entity container = getEntity();
            if(!contents.contains(e))
            {
                contents.add(e);
                EntityTools.setVisible(e, false);
                e.setProperty(EntityConstants.PROPERTY_CONTAINER, container);
                if(notify)
                {
                    // force property change event
                    container.setProperty(EntityConstants.PROPERTY_CONTAINS, contents);
                }
                logger.info("Loaded '" + e + "' into '" + container + "'");
            }
        }
    }
    
    public void addAll(List<Entity> newContents)
    {
        synchronized (getLock())
        {
            for(Entity e : newContents)
            {
                add(e);
            }
        }
    }
    
    public void remove(Entity e, Vector3 position)
    {
        synchronized (getLock())
        {
            if(contents.remove(e))
            {
                unload(e, position);
                if(notify)
                {
                    // force property change event
                    getEntity().setProperty(EntityConstants.PROPERTY_CONTAINS, contents);
                }
            }
        }
    }
    
    public void removeAll(List<Entity> entities, double radius)
    {
        assert entities != contents;
        
        if(entities.isEmpty())
        {
            return;
        }
        
        synchronized(getLock())
        {
            notify = false;
            final Entity container = getEntity();
            Vector3 containerPos = container.getPosition();
            double dtheta = 2 * Math.PI / entities.size();
            double theta = 0.0;
            for(Entity e : entities)
            {
                double x = radius * Math.cos(theta);
                double y = radius * Math.sin(theta);
                Vector3 p = containerPos.add(new Vector3(x, y, containerPos.z));
                remove(e, p);
                
                theta += dtheta;
            }
            notify = true;
            
            // force property change event
            container.setProperty(EntityConstants.PROPERTY_CONTAINS, contents);
        }
    }
    
    public void removeAll(double radius)
    {
        synchronized (getLock())
        {
            List<Entity> tempContents = new ArrayList<Entity>(contents);
            removeAll(tempContents, radius);
            assert contents.isEmpty();
        }
    }
    
    /**
     * @return the contents
     */
    public List<Entity> getContents()
    {
        synchronized (getLock())
        {
            return new ArrayList<Entity>(contents);
        }
    }
    

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.AbstractEntityCapability#attach(com.soartech.simjr.sim.Entity)
     */
    @Override
    public void attach(Entity entity)
    {
        super.attach(entity);
        entity.setProperty(EntityConstants.PROPERTY_CONTAINS, contents);
    }


    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.AbstractEntityCapability#detach()
     */
    @Override
    public void detach()
    {
       synchronized(getLock())
       {
           removeAll(0.0);
           getEntity().setProperty(EntityConstants.PROPERTY_CONTAINS, null);
           super.detach();
       }
    }

    private Object getLock()
    {
        return getEntity().getSimulation().getLock();
    }
    
    private void unload(Entity e, Vector3 position)
    {
        e.setPosition(position);
        EntityTools.setVisible(e, true);
        e.setProperty(EntityConstants.PROPERTY_CONTAINER, null);
        
        logger.info("Unloaded '" + e + "' from '" + getEntity() + "' at " + position);
    }
}
