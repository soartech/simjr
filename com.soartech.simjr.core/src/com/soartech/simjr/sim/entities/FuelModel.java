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
 * Created on Oct 28, 2007
 */
package com.soartech.simjr.sim.entities;

import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.Tickable;

/**
 * Simple fuel model for vehicles
 * 
 * @author ray
 */
public class FuelModel extends AbstractEntityCapability implements Tickable
{
    private String units;
    private double capacity;
    private double consumptionRate;
    private double level;
    
    public static FuelModel get(Entity e)
    {
        return Adaptables.adapt(e, FuelModel.class);
    }
    
    /**
     * Creates a default fuel model with a capacity of 7000.0 liters and a 
     * consumption rate of 0.0.
     */
    public FuelModel()
    {
        this("liters", 7000.0, 0.0);
    }
    
    public FuelModel(String units, double capacity, double consumptionRate)
    {
        this.units = units;
        this.capacity = capacity;
        this.consumptionRate = consumptionRate;
        this.level = this.capacity;
    }

    /**
     * @return The total fuel capacity of the vehicle
     */
    public double getCapacity()
    {
        return capacity;
    }

    /**
     * @param capacity The total fuel capacity of the vehicle
     */
    public void setCapacity(double capacity)
    {
        this.capacity = capacity;
    }

    /**
     * @return The fuel consumption rate in units per second
     */
    public double getConsumptionRate()
    {
        return consumptionRate;
    }

    /**
     * @param consumptionRate The fuel consumption rate in units per second. A
     *  value of zero is acceptable.
     */
    public void setConsumptionRate(double consumptionRate)
    {
        this.consumptionRate = consumptionRate;
    }

    /**
     * @return The current fuel level
     */
    public double getLevel()
    {
        return level;
    }

    /**
     * @param level The new fuel level. Will be clamped to capacity.
     */
    public void setLevel(double level)
    {
        this.level = Math.max(Math.min(level, capacity), 0.0);
    }

    /**
     * @return The units
     */
    public String getUnits()
    {
        return units;
    }

    /**
     * @param units The units
     */
    public void setUnits(String units)
    {
        this.units = units;
    }
    
    /**
     * @return True if no fuel remains
     */
    public boolean isEmpty()
    {
        return level <= 0.0;
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.AbstractEntityCapability#attach(com.soartech.simjr.sim.Entity)
     */
    @Override
    public void attach(Entity entity)
    {
        super.attach(entity);
        entity.setProperty(EntityConstants.PROPERTY_FUEL, this);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.AbstractEntityCapability#detach()
     */
    @Override
    public void detach()
    {
        getEntity().setProperty(EntityConstants.PROPERTY_FUEL, null);
        super.detach();
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.Tickable#tick(double)
     */
    @Override
    public void tick(double dt)
    {
        if(consumptionRate != 0.0)
        {
            level = Math.max(0.0, level - dt * consumptionRate);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return level + "/" + capacity + " " + units;
    }
}
