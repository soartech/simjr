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
 * Created on May 22, 2007
 */
package com.soartech.simjr.sim;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;

/**
 * Simulation terrain interface. The terrain is responsible for providing 
 * elevation information as well as coordinate system conversions.
 * 
 * @author ray
 */
public interface Terrain
{
    /**
     * Convert a point on the terrain to lat/lon
     * 
     * @param point The point to convert
     * @return lat/lon position of the point
     */
    Geodetic.Point toGeodetic(Vector3 point);
    
    /**
     * Convert a lat/lon point to meters
     * 
     * @param point The lat/lon point
     * @return The point in meters
     */
    Vector3 fromGeodetic(Geodetic.Point point);
    
    /**
     * Convert a point on the terrain to MGRS
     * 
     * @param point The point to convert
     * @return MGRS position of the point
     */
    String toMgrs(Vector3 point);
    
    /**
     * Convert an MGRS string to a terrain point in meters
     * 
     * @param mgrs The MGRS string to convert
     * @return The point in meters
     */
    Vector3 fromMgrs(String mgrs);
    
    /**
     * Return the elevation (z) of the terrain at a given point
     *  
     * @param point The point
     * @return The elevation (z) of the terrain
     */
    double getElevationAtPoint(Vector3 point);
    
    /**
     * Return the elevation (z) of the terrain at a given a geodetic point
     *  
     * @param point The point
     * @return The elevation (z) of the terrain
     */
    double getElevationAtPoint(Geodetic.Point point);
    
    /**
     * Return the terrain type at the X/Y position of the given point.
     * 
     * @param point The point
     * @return The terrain type
     */
    Object getTerrainTypeAtPoint(Vector3 point);
    
    /**
     * Preserve the X/Y position of the given point while forcing it to the
     * specified above-ground-level
     * 
     * @param point The point
     * @param agl The desired height from the ground
     * @return New point 
     */
    Vector3 clampPointToGround(Vector3 point, double agl);
    
}
