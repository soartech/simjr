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

public class TestLocalCartesian extends TestCase
{
    private LocalCartesian c;
    protected void setUp() throws Exception
    {
        super.setUp();
        
        c = new LocalCartesian(GeoTransConstants.WGS84_SEMI_MAJOR_AXIS, GeoTransConstants.WGS84_FLATTENING, 0, 0, 0, 0);
    }
    
    public void testLocalCartesian0()
    {
        Vector3 out = c.fromGeocentric(0, 50000, 0);
        assertEquals(50000.0, out.x);
        //assertEquals(21385.0, out.y);
        assertEquals(0.0, out.y);
        assertEquals(-GeoTransConstants.WGS84_SEMI_MAJOR_AXIS, out.z);
    }
    
    public void testLocalCartesian1()
    {
        Vector3 out = c.fromGeodetic(Math.toRadians(45), Math.toRadians(-75), 700);
        assertEquals(-4364136.0, out.x, 1.0);
        assertEquals(4487843.0, out.y, 1.0);
        assertEquals(-5208770.0, out.z, 1.0);
    }

    public void testLocalCartesian2()
    {
        Vector3 out = c.toGeocentric(50000, 21385, -6367454);
        assertEquals(10683, out.x, 1.0);
        assertEquals(50000, out.y, 1.0);
        assertEquals(21385, out.z, 1.0);
    }
    
    public void testLocalCartesian3()
    {
        Geodetic.Point out = c.toGeodetic(-4364136, 4487843, -5208770);
        
        assertEquals(45, Math.toDegrees(out.latitude), 0.01);
        assertEquals(-75, Math.toDegrees(out.longitude), 0.01);
        assertEquals(700, out.altitude, 0.1);
    }
    
    public void testIncorrectHeightCalculation()
    {
        LocalCartesian lc = new LocalCartesian(GeoTransConstants.WGS84_SEMI_MAJOR_AXIS, 
                GeoTransConstants.WGS84_FLATTENING,
                Math.toRadians(42),
                Math.toRadians(-83),
                0,
                0);
        
        Geodetic.Point out = lc.toGeodetic(0, 500000, 0);
        assertEquals(out.altitude, 19606, 1.0);
        
    }
}
