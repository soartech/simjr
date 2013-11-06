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

import com.soartech.math.Vector3;

public class TestGeocentric extends TestCase
{
    private Geocentric g;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        g = new Geocentric();
    }
    
    public void testGeodeticToGeocentric()
    {
        verifyGeodeticToGeocentric(42.0, -83.2, 0., 562064, -4713606, 4245604);
        verifyGeodeticToGeocentric(21.0, -43.2, 0., 4342514, -4077892, 2271395);
        verifyGeodeticToGeocentric(60, 21, 0., 2984754, 1145740, 5500477);
        verifyGeodeticToGeocentric(33, -175, 0., -5334099, -466673, 3453959);
        verifyGeodeticToGeocentric(-42.0, -83.2, 0., 562064, -4713606, -4245604);
        verifyGeodeticToGeocentric(-34.9, 138.5, 10000.0, -3928261.0, 3475431.0, -3634495.0);        
    }
    
    private void verifyGeodeticToGeocentric(double lat, double lon, double alt, double x, double y, double z)
    {
        Vector3 out = g.fromGeodetic(Math.toRadians(lat), 
                Math.toRadians(lon), alt);

        assertEquals(x, out.x, 1.0);
        assertEquals(y, out.y, 1.0);
        assertEquals(z, out.z, 1.0);
        
        Geodetic.Point llaout = g.toGeodetic(out.x, out.y, out.z);
        assertEquals(Math.toDegrees(llaout.latitude), lat, .1);
        assertEquals(Math.toDegrees(llaout.longitude), lon, .1);
        assertEquals(llaout.altitude, alt, .1);
        
    }
    
    public void testGeocentricFromGeodeticAngle() {
        
        // Sample from DSTO–TN–0640
        // Aircraft at lat = -34.9 deg lat and 138.5 deg long 
        // heading southeast (135 deg) at pitch 20 degrees and roll 30 degrees
        //
        // should result in:
        //    theta = 47.8 deg
        //    phi = -29.7 deg
        //    psi = -123.0 deg
        Vector3 geocOrientation = Geocentric.fromGeodeticAngle( Math.toRadians(-34.9), 
                                                                Math.toRadians(138.5), 
                                                                Math.toRadians(20.0),  
                                                                Math.toRadians(30.0), 
                                                                Math.toRadians(135.0));
        
        double deg1 = Math.toDegrees(geocOrientation.x);
        double deg2 = Math.toDegrees(geocOrientation.y);
        double deg3 = Math.toDegrees(geocOrientation.z);
        
        assertEquals( 47.8, deg1, 0.1);
        assertEquals(-29.7, deg2, 0.1);
        assertEquals( -123.0, deg3, 0.1);
    }
    
}
