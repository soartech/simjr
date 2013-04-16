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
 * Created on Jun 9, 2006
 */
package com.soartech.math.geotrans;

/**
 * The Class Geodetic.
 * TODO: Unclear what this external class is for since the {@link Point} is the interesting part?
 *
 * @author ray
 */
public class Geodetic
{
    
    /**
     * Represents a geodetic point. (aka lat, lon, altitude)
     */
    public static class Point
    {
        
        /** The latitude in radians. */
        public double latitude;
        
        /** The longitude in radians. */
        public double longitude;
        
        /** The altitude in meters. */
        public double altitude;
        
        /**
         * Instantiates a new point.
         */
        public Point()
        {
        }
        
        /**
         * Creates a copy of an existing point.
         *
         * @param other the other
         */
        public Point(Point other)
        {
            this.latitude = other.latitude;
            this.longitude = other.longitude;
            this.altitude = other.altitude;
        }
        
        public Point(double latitude, double longitude, double altitude)
        {
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = altitude;
        }
    }
    
    /**
     * Instantiates a new {@link Geodetic} instance.
     * TODO: Unclear what this external class is for?
     */
    public Geodetic()
    {
        super();
    }

}
