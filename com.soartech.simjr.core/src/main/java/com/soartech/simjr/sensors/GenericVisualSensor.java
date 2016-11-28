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

public class GenericVisualSensor extends AbstractSensor implements VisionSensor
{
    private static final double DEFAULT_VIS_RANGE = 7500.;
    private static final double DEFAULT_VIS_FOV = 2.*Math.PI;
    
    private EntityFilter filter;
    private List<Detection> detections = new ArrayList<Detection>();
    
    private double visualRange = DEFAULT_VIS_RANGE;
    private double visualAngle = DEFAULT_VIS_FOV;
    
    public GenericVisualSensor(String name, ExtendedProperties props) {
        this(name, 
             props.getDouble(name+".range", DEFAULT_VIS_RANGE),
             props.getDouble(name+".angle", DEFAULT_VIS_FOV));
    }    
    
    public GenericVisualSensor(String name)
    {
        this(name, DEFAULT_VIS_RANGE);
    }
    
    public GenericVisualSensor(String name, double range)
    {
        this(name, range, DEFAULT_VIS_FOV);
    }
    
    public GenericVisualSensor(String name, double range, double fieldOfView)
    {
        super(name);
        visualRange = range;
        visualAngle = fieldOfView;
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
            EntityVisibleRange evr = new EntityVisibleRange(entity,EntityConstants.PROPERTY_VISIBLE_RANGE);
            evr.setVisibleAngle(visualAngle);
            evr.setVisibleRange(visualRange);
            entity.setProperty(EntityConstants.PROPERTY_VISIBLE_RANGE, evr);        
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
        EntityVisibleRange evr = EntityVisibleRange.get(this.getEntity());
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
                    detections.add(new Detection(this, entity, DetectionType.VISIBLE));
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
