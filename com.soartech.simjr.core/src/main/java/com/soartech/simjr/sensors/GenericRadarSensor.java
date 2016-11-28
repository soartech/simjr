/*
 * Copyright (c) 2012, Soar Technology, Inc.
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
 * Created on July 24, 2012
 */
package com.soartech.simjr.sensors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.entities.EntityVisibleRange;
import com.soartech.simjr.util.ExtendedProperties;

public class GenericRadarSensor extends AbstractSensor implements RadarSensor
{
    private EntityFilter filter;
    private List<Detection> detections = new ArrayList<Detection>();
    
    private double visualRange;
    private double visualAngle;
    
    public GenericRadarSensor(String name, ExtendedProperties props) {
        super(name);
        
        visualRange = props.getDouble(name+".range", 200000.);
        visualAngle = props.getDouble(name+".angle", Math.PI/2.);
    }    
    
    public void setVisualRange(double visualRange)
    {
        this.visualRange = visualRange;
        updateEntityVisibleRange();
    }
    
    public void setVisualAngle(double visualAngle)
    {
        this.visualAngle = visualAngle;
        updateEntityVisibleRange();
    }
    
    private void updateEntityVisibleRange()
    {
        Entity entity = getEntity();
        if ( entity != null )
        {
            EntityVisibleRange evr = new EntityVisibleRange(entity,EntityConstants.PROPERTY_RADAR);
            evr.setVisibleAngle(visualAngle);
            evr.setVisibleRange(visualRange);
            entity.setProperty(EntityConstants.PROPERTY_RADAR, evr);        
        }
    }
    
    @Override
    public void setEntity(Entity entity) 
    {
        super.setEntity(entity);
        filter = new EntityFilter(getEntity());
        updateEntityVisibleRange();
    }

    @Override
    public void tick(double dt)
    {
        detections.clear();
        EntityVisibleRange evr = (EntityVisibleRange) getEntity().getProperty(EntityConstants.PROPERTY_RADAR);
        if ( evr == null ) 
        {
            return;
        }
        
        List<Entity> entities = this.getEntity().getSimulation().getEntitiesFast();
        for ( Entity entity : entities ) 
        {
            if ( filter.isEntityOfInterest(entity) ) 
            {
                if ( evr.isInRange(entity.getPosition()) )
                {
                    detections.add(new Detection(this, entity, DetectionType.RADAR));
                }
            }
        }
    }

    @Override
    public List<Detection> getDetections()
    {
        return Collections.unmodifiableList(detections);
    }
}
