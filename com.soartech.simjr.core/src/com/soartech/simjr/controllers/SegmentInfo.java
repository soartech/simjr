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

import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.Simulation;

/**
 * Information about a particular segment used by {@link SegmentFollower}.
 * A segment consists of a speed and a target waypoint. SegmentInfo object
 * are meant to used in a linked-list. Thus the start waypoint of the next
 * segment is always the end waypoint of the previous segment. 
 * 
 * @author ray
 */
public class SegmentInfo
{
    private final String waypoint;
    private final SpeedProvider speed;
    private final DelayProvider delay;
    private SegmentInfo next;
    
    /**
     * An interface that provides the current speed for a segment
     * 
     * @author ray
     */
    public interface SpeedProvider
    {
        /**
         * @return the current speed for the segment
         */
        double getSpeed();
    }
    
    private static class ConstantSpeedProvider implements SpeedProvider
    {
        private final double speed;
        
        public ConstantSpeedProvider(double speed)
        {
            this.speed = speed;
        }

        /* (non-Javadoc)
         * @see com.soartech.simjr.controllers.SegmentInfo.SpeedProvider#getSpeed()
         */
        public double getSpeed()
        {
            return speed;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return speed + " m/s";
        }
    }
    
    /**
     * An interface that provides the current delay for a segment
     * 
     * @author rjones
     */
    public interface DelayProvider
    {
        /**
         * @return the current delay for the segment
         */
        double getDelay();
    }
    
    private static class ConstantDelayProvider implements DelayProvider
    {
        private final double delay;
        
        public ConstantDelayProvider(double delay)
        {
            this.delay = delay;
        }

        /* (non-Javadoc)
         * @see com.soartech.simjr.controllers.SegmentInfo.DelayProvider#getDelay()
         */
        public double getDelay()
        {
            return delay;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return delay + " seconds";
        }
    }
    
    /**
     * @param waypoint the target waypoint of this segment
     * @param speed the speed to use getting to the target waypoint.
     * @param next the next segment or {@code null}
     */
    public SegmentInfo(String waypoint, double speed, SegmentInfo next)
    {
        this(waypoint, new ConstantSpeedProvider(speed), new ConstantDelayProvider(0.0), next);
    }

    /**
     * @param waypoint the target waypoint of this segment
     * @param speed the speed to use getting to the target waypoint.
     * @param delay the delay to use before proceeding to the segment's waypoint.
     * @param next the next segment or {@code null}
     */
    public SegmentInfo(String waypoint, double speed, double delay, SegmentInfo next)
    {
        this(waypoint, new ConstantSpeedProvider(speed), new ConstantDelayProvider(delay), next);
    }

    /**
     * @param waypoint the target waypoint of this segment
     * @param speed the speed provider used to get the current speed of the segment
     * @param next the next segment or {@code null}
     */
    public SegmentInfo(String waypoint, SpeedProvider speed, SegmentInfo next)
    {
        this(waypoint, speed, new ConstantDelayProvider(0.0), next);
    }

    /**
     * @param waypoint the target waypoint of this segment
     * @param speed the speed provider used to get the current speed of the segment
     * @param delay the delay provider used to get the delay time at the start of the segment
     * @param next the next segment or {@code null}
     */
    public SegmentInfo(String waypoint, SpeedProvider speed, DelayProvider delay, SegmentInfo next)
    {
        if(waypoint == null)
        {
            throw new IllegalArgumentException("waypoint cannot be null");
        }
        if(speed == null)
        {
            throw new IllegalArgumentException("speed cannot be null");
        }
        if(delay == null)
        {
            throw new IllegalArgumentException("delay cannot be null");
        }
        this.waypoint = waypoint;
        this.speed = speed;
        this.delay = delay;
        this.next = next;
    }
    
    /**
     * @return the name of the target waypoint
     */
    public String getWaypoint()
    {
        return waypoint;
    }
    
    public Entity getWaypoint(Simulation sim)
    {
        final Entity entity = sim.getEntity(waypoint);
        if(entity == null)
        {
            throw new IllegalStateException("No entity named '" + waypoint + "' found");
        }
        return entity;
    }

    /**
     * @return the current speed used on the segment
     */
    public double getSpeed()
    {
        return speed.getSpeed();
    }
    
    public SpeedProvider getSpeedProvider()
    {
        return speed;
    }
    
    /**
     * @return the current delay used on the segment
     */
    public double getDelay()
    {
        return delay.getDelay();
    }
    
    public DelayProvider getDelayProvider()
    {
        return delay;
    }
    
    public SegmentInfo getNext()
    {
        return next;
    }
    
    protected void setNext(SegmentInfo next)
    {
        this.next = next;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "(" + waypoint + ", " + speed + ", " + next + ")";
    }
    
}
