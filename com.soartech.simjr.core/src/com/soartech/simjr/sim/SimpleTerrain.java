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
import com.soartech.math.geotrans.GeoTransConstants;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.math.geotrans.LocalCartesian;
import com.soartech.math.geotrans.Mgrs;

/**
 * Simple implementation of the {@link Terrain} interface.
 * 
 * @author ray
 */
public class SimpleTerrain implements Terrain
{
    private Geodetic.Point origin;
    private LocalCartesian localCartesian;
    private Mgrs mgrs = new Mgrs();

    public static SimpleTerrain createExampleTerrain()
    {
        Geodetic.Point origin = new Geodetic.Point();
        origin.longitude = Math.toRadians(-83);
        origin.latitude = Math.toRadians(42);
        
        return new SimpleTerrain(origin);
    }
    
    /**
     * @param origin The lat/lon location of (0, 0, 0)
     */
    public SimpleTerrain(Geodetic.Point origin)
    {
        this.origin = origin;
        
        localCartesian = new LocalCartesian(
                GeoTransConstants.WGS84_SEMI_MAJOR_AXIS, 
                GeoTransConstants.WGS84_FLATTENING, 
                origin.latitude, origin.longitude, origin.altitude, 
                0.0);
        
    }

    public Geodetic.Point getOrigin()
    {
        return origin;
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.Terrain#getElevationAtPoint(com.soartech.spatr.math.Vector3)
     */
    public double getElevationAtPoint(Vector3 point)
    {
    	return origin.altitude;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.Terrain#getElevationAtPoint(com.soartech.math.geotrans.Geodetic.Point)
     */
    public double getElevationAtPoint(Geodetic.Point point)
    {
    	return origin.altitude;
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.Terrain#getTerrainTypeAtPoint(com.soartech.math.Vector3)
     */
    public Object getTerrainTypeAtPoint(Vector3 point)
    {
    	return null;
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.Terrain#clampPointToGround(com.soartech.spatr.math.Vector3, double)
     */
    public Vector3 clampPointToGround(Vector3 point, double agl)
    {
        Geodetic.Point lla = toGeodetic(point);
        lla.altitude = 0;
        Vector3 atGround = fromGeodetic(lla);
        return new Vector3(atGround.x, atGround.y, atGround.z + agl);
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.Terrain#getGeodeticPoint(com.soartech.spatr.math.Vector3)
     */
    public Geodetic.Point toGeodetic(Vector3 point)
    {
        return localCartesian.toGeodetic(point.x, point.y, point.z);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.Terrain#fromGeodetic(com.soartech.spatr.geotrans.Geodetic.Point)
     */
    public Vector3 fromGeodetic(Geodetic.Point point)
    {
        return localCartesian.fromGeodetic(point.latitude, point.longitude, point.altitude);
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.Terrain#fromMgrs(java.lang.String)
     */
    public Vector3 fromMgrs(String mgrs)
    {
        try
        {
            Geodetic.Point lla = this.mgrs.toGeodetic(mgrs);
            
            return fromGeodetic(lla);
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            // Index out of bounds is occasionally thrown if the MGRS string
            // is invalid. Convert to illegal argument exception here.
            throw new IllegalArgumentException(e);
        }
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.Terrain#toMgrs(com.soartech.spatr.math.Vector3)
     */
    public String toMgrs(Vector3 point)
    {
        Geodetic.Point lla = localCartesian.toGeodetic(point.x, point.y, point.z);

        return mgrs.fromGeodetic(lla.latitude, lla.longitude, 5);
    }

}
