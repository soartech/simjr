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

import com.soartech.simjr.sim.EntityPrototype;

/**
 * The default implementation of AbstractPolygon used for routes and areas.
 * Whether the polygon is closed or not is determined by the "polygon.closed"
 * property in its prototype. If the property is missing, it defaults to false.
 * 
 * @author ray
 */
public class DefaultPolygon extends AbstractPolygon
{
    /**
     * Default constructor expected by {@link EntityPrototype#createEntity(String)}
     */
    public DefaultPolygon()
    {
        super("");
    }

    /**
     * Default constructor expected by {@link EntityPrototype#createEntity(String)}
     */
    public DefaultPolygon(String defaultName)
    {
        super(defaultName);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.entities.AbstractPolygon#isClosed()
     */
    @Override
    public boolean isClosed()
    {
        Boolean closed = (Boolean) getEntity().getPrototype().getProperty("polygon.closed");
        return closed != null ? closed.booleanValue() : false;
    }
    
    public int getMinAltitude()
    {
        Integer minProperty = (Integer)getEntity().getProperty("min_altitude");
        //added to set legacy routes default minimumAltitude to zero
        if(minProperty == null)
        {
            this.setMinAltitude(0);
            return 0;
        }
        Integer minAltitude = Integer.valueOf(minProperty);
        return minAltitude.intValue();
    }
    
    public int getMaxAltitude()
    {
        Integer maxProperty = (Integer)getEntity().getProperty("max_altitude");
        //added to set legacy routes default minimumAltitude to zero
        if(maxProperty == null)
        {
            this.setMaxAltitude(0);
            return 0;
        }
        Integer maxAltitude = Integer.valueOf(maxProperty);
        return maxAltitude.intValue();
    }
    
    public int getRouteWidth()
    {
        Integer routeProperty = (Integer)getEntity().getProperty("route_width");
        //added to set legacy routes default minimumAltitude to zero
        if(routeProperty == null)
        {
            this.setRouteWidth(0);
            return 0;
        }
        Integer routeWidth= Integer.valueOf(routeProperty);
        return routeWidth.intValue();
    }
    
    public void setMinAltitude(int minAltitude)
    {
       getEntity().setProperty("min_altitude", new Integer(minAltitude));
  
    }
    public void setMaxAltitude(int maxAltitude)
    {
        getEntity().setProperty("max_altitude", new Integer(maxAltitude));
        
    }
    public void setRouteWidth(int width)
    {
        getEntity().setProperty("route_width", new Integer(width));
       
    }
    
    
}
