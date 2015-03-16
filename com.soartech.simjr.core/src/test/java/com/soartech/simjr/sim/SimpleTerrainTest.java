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
 * Created on May 1, 2008
 */
package com.soartech.simjr.sim;

import junit.framework.TestCase;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.math.geotrans.Mgrs;

/**
 * @author ray
 */
public class SimpleTerrainTest extends TestCase
{
    
    public void testOrigin()
    {
        String mgrs = "11SMS6255091250";
        Geodetic.Point origin = new Mgrs().toGeodetic(mgrs);
        SimpleTerrain terrain = new SimpleTerrain(origin);
        
        Geodetic.Point newOrigin = terrain.toGeodetic(Vector3.ZERO);
        assertEquals(origin.latitude, newOrigin.latitude, 0.000001);
        assertEquals(origin.longitude, newOrigin.longitude, 0.000001);
        assertEquals(origin.altitude, newOrigin.altitude, 0.01);
        
        String newMgrs = terrain.toMgrs(Vector3.ZERO);
        assertEquals(mgrs, newMgrs);
    }
    
    public void testTranslation()
    {
        Geodetic.Point origin = new Geodetic.Point(0.5825127242298382, -2.049493620687549, 0.0);
        SimpleTerrain terrain = new SimpleTerrain(origin);
        Geodetic.Point pt = new Geodetic.Point(Math.toRadians(27.96), Math.toRadians(-140.0991), -800);
        Geodetic.Point pt2 = terrain.toGeodetic(terrain.fromGeodetic(pt));

        assertEquals(pt.latitude, pt2.latitude, 0.000001);
        assertEquals(pt.longitude, pt2.longitude, 0.000001);
        assertEquals(pt.altitude, pt2.altitude, 0.01);
    }

}
