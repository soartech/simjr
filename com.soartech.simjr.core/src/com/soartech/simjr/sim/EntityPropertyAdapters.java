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
 * Created on Oct 26, 2007
 */
package com.soartech.simjr.sim;

import java.util.HashMap;
import java.util.Map;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;

/**
 * Implementation of property adapters for basic entity properties like position,
 * velocity.  These are the default implementations that are used if they aren't
 * overridden on a particular entity through a call to {@link Entity#addPropertyAdapter(EntityPropertyAdapter)}.
 * 
 * @author ray
 */
public final class EntityPropertyAdapters
{
    private static Map<String, EntityPropertyAdapter> DEFAULT_ADAPTERS = new HashMap<String, EntityPropertyAdapter>();
    static
    {
        add(new MgrsPropertyAdapter());
        add(new AbstractPropertyAdapter(EntityConstants.PROPERTY_VELOCITY) {

            public void setValue(Entity entity, Object newValue)
            {
                entity.setVelocity(Vector3.parseVector(newValue.toString()));
            }});
        add(new AbstractPropertyAdapter(EntityConstants.PROPERTY_POSITION) {

            public void setValue(Entity entity, Object newValue)
            {
                entity.setPosition(Vector3.parseVector(newValue.toString()));
            }});
     
        
    };
    
    private static void add(EntityPropertyAdapter adapter)
    {
        DEFAULT_ADAPTERS.put(adapter.getProperty(), adapter);
    }

    /**
     * Look up a default adapter for the given property name
     * 
     * @param name The property name
     * @return The default adapter for that property, or null if not found
     */
    public static EntityPropertyAdapter getDefaultAdapter(String name)
    {
        return DEFAULT_ADAPTERS.get(name);
    }
    
    public abstract static class AbstractPropertyAdapter implements EntityPropertyAdapter
    {
        private String property;

        public AbstractPropertyAdapter(String property)
        {
            this.property = property;
        }

        /* (non-Javadoc)
         * @see com.soartech.simjr.EntityPropertyAdapter#getProperty()
         */
        public String getProperty()
        {
            return property;
        }
    }
    
    /**
     * Implementation of property adapter that sets entity position from an
     * MGRS string. The entity's current altitude is maintained. Only x and
     * y are changed.
     * 
     * @author ray
     */
    private static class MgrsPropertyAdapter implements EntityPropertyAdapter
    {
        public String getProperty()
        {
            return EntityConstants.PROPERTY_MGRS;
        }

        public void setValue(Entity entity, Object newValue)
        {
            String mgrs = newValue.toString();
            Simulation sim = entity.getSimulation();
            if(!entity.hasPosition() || sim == null)
            {
                return;
            }
            
            Terrain terrain = sim.getTerrain();
            Vector3 newPos = terrain.fromMgrs(mgrs);
            Geodetic.Point newGeoPoint = terrain.toGeodetic(newPos);
            newGeoPoint.altitude = terrain.toGeodetic(entity.getPosition()).altitude;
            
            double newZ = terrain.fromGeodetic(newGeoPoint).z;
            
            entity.setPosition(new Vector3(newPos.x, newPos.y, newZ));
           
        }
    }
}
