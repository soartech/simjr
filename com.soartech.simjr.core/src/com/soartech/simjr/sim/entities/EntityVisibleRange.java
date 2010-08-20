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
 * Created on Sep 26, 2007
 */
package com.soartech.simjr.sim.entities;

import com.soartech.math.Angles;
import com.soartech.math.Vector3;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;

/**
 * Class representing the visible range of an entity in Sim Jr. This should be
 * added to an entity as the value of the PROPERTY_VISIBLE_RANGE property on 
 * an entity.  Setting the values on this object will cause entity property
 * change events to be fired. Also, the display system will automatically show
 * sensor arcs for entities with this property.  
 * Setting this to -1 will result in an infinite (but not drawn) visible range. 
 * 
 * @author ray
 */
public class EntityVisibleRange
{
    private Entity entity;
    private String property;
    private double visibleRange = 0.0;
    private double visibleAngle = Math.PI / 4.0;
    
    public static EntityVisibleRange get(Entity entity)
    {
        return get(entity, EntityConstants.PROPERTY_VISIBLE_RANGE);
    }
    
    public static EntityVisibleRange get(Entity entity, String property)
    {
        return (EntityVisibleRange) entity.getProperty(property);
    }
        
    public EntityVisibleRange(Entity entity)
    {
        this(entity, EntityConstants.PROPERTY_VISIBLE_RANGE);
    }
    
    public EntityVisibleRange(Entity entity, String property)
    {
        if(entity == null)
        {
            throw new NullPointerException("entity should not be null");
        }
        if(property == null)
        {
            throw new NullPointerException("property should not be null");
        }
        this.entity = entity;
        this.property = property;
    }
    
    /**
     * @return the visibleAngle
     */
    public double getVisibleAngle()
    {
        return visibleAngle;
    }
    
    /**
     * @param visibleAngle The visible angle in radians. A property changed
     *      event will be fired.
     */
    public void setVisibleAngle(double visibleAngle)
    {
        entity.firePropertyChanged(property);
        this.visibleAngle = visibleAngle;
    }
    /**
     * @return the visible range in meters
     */
    public double getVisibleRange()
    {
        return visibleRange;
    }
    /**
     * @param visibleRange the visible range in meters
     */
    public void setVisibleRange(double visibleRange)
    {
        entity.firePropertyChanged(property);
        this.visibleRange = visibleRange;
    }
    
    /**
     * Tests whether a position is within this visible range
     * 
     * @param otherPos The position to test. Z is ignored.
     * @return True if otherPos is within this visible range.
     */
    public boolean isInRange(Vector3 otherPos)
    {
        if(visibleRange < 0) return true;
        
        Vector3 agentPos = entity.getPosition();
        Vector3 displacement = otherPos.subtract(agentPos);
        Vector3 xyDisplacement = new Vector3(displacement.x, displacement.y, 0.0);
        double xyRange = xyDisplacement.length();
        double xyAngle = Math.atan2(xyDisplacement.y, xyDisplacement.x);
        double agentAngle = Angles.boundedAngleRadians(entity.getOrientation());
        
        if(xyRange > visibleRange || Math.abs(Angles.angleDifference(xyAngle, agentAngle)) > 0.5* visibleAngle)
        {
            return false;
        }
        
        return true;
    }
    
    
    /**
     * @return the name of the property this object is associated with
     */
    public String getProperty()
    {
        return property;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("%.0f meters, %.0f degrees", 
                new Object[] { visibleRange, Math.toDegrees(visibleAngle) });
    }

    
    
}
