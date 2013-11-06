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
 * Created on Jun 15, 2006
 */
package com.soartech.math;

import junit.framework.TestCase;

public class TestLineSegmentDistance extends TestCase
{
    public void testDistanceToPoint()
    {
        Vector3 start = new Vector3(1, 1, 0);
        Vector3 end = new Vector3(-1, 1, 0);
        Vector3 dir = end.subtract(start);
        
        double d = LineSegmentDistance.toPoint(start, end, dir, new Vector3(-.55, .55, 0));
        assertEquals(.45, d, 1e-6);
    }
    
    public void testSimpleDistance()
    {
        Vector3 aStart = new Vector3(-1, 0, 1);
        Vector3 aEnd = new Vector3(1, 0, 1);
        Vector3 bStart = new Vector3(0, -1, -1);
        Vector3 bEnd = new Vector3(0, 1, -1);
        
        double d = LineSegmentDistance.calculate(aStart, aEnd, bStart, bEnd);
        assertEquals(2.0, d);
    }
    
    public void testParallelSegments()
    {
        Vector3 aStart = new Vector3(0, 1, 2);
        Vector3 aEnd = new Vector3(0, 1, 2);
        Vector3 bStart = new Vector3(0, -1, -1);
        Vector3 bEnd = new Vector3(0, 1, -1);
        
        double d = LineSegmentDistance.calculate(aStart, aEnd, bStart, bEnd);
        assertEquals(3.0, d);
    }
    
    public void testColinearSegments()
    {
        Vector3 aStart = new Vector3(0, -2, -1);
        Vector3 aEnd = new Vector3(0, 0, -1);
        Vector3 bStart = new Vector3(0, -1, -1);
        Vector3 bEnd = new Vector3(0, 1, -1);
        
        double d = LineSegmentDistance.calculate(aStart, aEnd, bStart, bEnd);
        assertEquals(0.0, d);
    }
    
    public void testIntersectingSegments()
    {
        Vector3 aStart = new Vector3(-2, -2, -2);
        Vector3 aEnd = new Vector3(2, 2, 2);
        Vector3 bStart = new Vector3(2, -2, -2);
        Vector3 bEnd = new Vector3(-2, 2, 2);
                
        double d = LineSegmentDistance.calculate(aStart, aEnd, bStart, bEnd);
        assertEquals(0.0, d, 1e-06);
    }
    
    public void testEndpointsClosest()
    {
        Vector3 aStart = new Vector3(-5, 0, 0);
        Vector3 aEnd = new Vector3(-3, 0, 0);
        Vector3 bStart = new Vector3(3, 0, 0);
        Vector3 bEnd = new Vector3(5, 0, 0);
        
        double d = LineSegmentDistance.calculate(aStart, aEnd, bStart, bEnd);
        assertEquals(aEnd.distance(bStart), d, 1e-06);        
    }
    
    public void testEndpointClosest()
    {
        Vector3 aStart = new Vector3(-1, 0, 0);
        Vector3 aEnd = new Vector3(1, 0, 0);
        Vector3 bStart = new Vector3(0, 3, 0);
        Vector3 bEnd = new Vector3(0, 99, 0);
        
        double d = LineSegmentDistance.calculate(aStart, aEnd, bStart, bEnd);
        assertEquals(3.0, d, 1e-06);        
    }
    
}
