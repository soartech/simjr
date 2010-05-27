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

import junit.framework.TestCase;

public class TestUniversalTransverseMercator extends TestCase
{
    private UniversalTransverseMercator g;

    protected void setUp() throws Exception
    {
        super.setUp();
        
        g = new UniversalTransverseMercator();
    }
    
    public void testGeodeticToUTM()
    {
        verifyGeodeticToUTM(42.0, -83.2, 17, 'N', 317796, 4652117);
        verifyGeodeticToUTM(21.0, -43.2, 23, 'N', 687095, 2323201);
        verifyGeodeticToUTM(60, 21, 34, 'N', 500000, 6651411);
        verifyGeodeticToUTM(33, -175, 1, 'N', 686847, 3653064);
        verifyGeodeticToUTM(-42.0, -83.2, 17, 'S', 317796, 5347883);
    }
    
    private void verifyGeodeticToUTM(double lat, double lon, int zone, 
            char hemi, double easting, double northing)
    {
        
        UniversalTransverseMercator.Point point = 
            g.fromGeodetic(Math.toRadians(lat), 
                                      Math.toRadians(lon));

        assertEquals(zone, point.Zone);
        assertEquals(hemi, point.Hemisphere);
        assertEquals(easting, point.Easting, 1.0);
        assertEquals(northing, point.Northing, 1.0);
        
        Geodetic.Point llaout = g.toGeodetic(point.Zone, point.Hemisphere, 
                point.Easting, point.Northing);
        assertEquals(Math.toDegrees(llaout.latitude), lat, .1);
        assertEquals(Math.toDegrees(llaout.longitude), lon, .1);
        assertEquals(llaout.altitude, 0, .1);
        
    }

}
