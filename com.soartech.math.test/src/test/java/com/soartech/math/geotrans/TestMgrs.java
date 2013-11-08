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

public class TestMgrs extends TestCase
{
    Mgrs mgrs;

    protected void setUp() throws Exception
    {
        super.setUp();
        
        mgrs = new Mgrs();
    }
    

    public void testFormat()
    {
        assertEquals("0044", Mgrs.format(44, 4));
        assertEquals("65", Mgrs.format(65, 2));
        assertEquals("004032", Mgrs.format(4032, 6));
    }
    
    public void testMgrsToGeodetic()
    {
        verifyMgrsToGeodetic("17TLG3436151711", 42, -83);
        verifyMgrsToGeodetic("44TPM6563951711", 42, 83);
        verifyMgrsToGeodetic("17GLP3436148289", -42, -83);
        verifyMgrsToGeodetic("44GPU6563948289", -42, 83);
        verifyMgrsToGeodetic("17CMM6123617748", -80, -83);
        verifyMgrsToGeodetic("34NCL8914052749", 5, 20);
        verifyMgrsToGeodetic("31NGA2259500000", 0, 5);
        verifyMgrsToGeodetic("02FNK7166660890", -50, -170);
        verifyMgrsToGeodetic("57TWK7881583437", 45, 160);
    }
    
    private void verifyMgrsToGeodetic(String mgrsString, double lat, double lon)
    {
        assertEquals(mgrsString, mgrs.fromGeodetic(Math.toRadians(lat), Math.toRadians(lon), 5));
        
        Geodetic.Point point = mgrs.toGeodetic(mgrsString);
        
        assertEquals(Math.toRadians(lat), point.latitude, 0.01);
        assertEquals(Math.toRadians(lon), point.longitude, 0.01);
      
    }
}
