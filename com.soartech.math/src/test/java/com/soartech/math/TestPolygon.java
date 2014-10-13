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
 * Created on Jun 18, 2006
 */
package com.soartech.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestPolygon
{
    @Test
    public void testCreateConvexHull()
    {
        Vector3 a = new Vector3(1, 1, 0);
        Vector3 b = new Vector3(-1, -1, 0);
        Vector3 c = new Vector3(1, -1, 0);
        Vector3 d = new Vector3(-1, 1, 0);
        Vector3 zero = Vector3.ZERO;
        
        Polygon p = Polygon.createConvexHull(Arrays.asList(a, b, c, d, zero));
        assertEquals(4, p.getPoints().size());
        assertTrue(p.getPoints().contains(a));
        assertTrue(p.getPoints().contains(b));
        assertTrue(p.getPoints().contains(c));
        assertTrue(p.getPoints().contains(d));
        assertFalse(p.getPoints().contains(zero));
        
    }
    
    @Test
    public void testCreateMoreComplicatedConvexHull()
    {
        List<Vector3> points = new ArrayList<Vector3>();
        for(int i = 0; i < 7; ++i)
        {
            double t = i * (2 * Math.PI / 7);
            points.add(new Vector3(Math.cos(t), Math.sin(t), 0));
            points.add(new Vector3(Math.cos(t) / 2, Math.sin(t) / 3, 0));
        }
        points.add(Vector3.ZERO);
        
        Polygon p = Polygon.createConvexHull(points);
        assertEquals(7, p.getPoints().size());
        
        for(double t = 0.0; t < 2 * Math.PI; t += 0.2 )
        {
            Vector3 in = new Vector3(Math.cos(t) / 2, Math.sin(t) / 3, 0);
            assertTrue("Expected polygon to contain " + in, p.contains(in));
            
            Vector3 out = new Vector3(Math.cos(t) * 2, Math.sin(t) * 2, 0);
            assertFalse("Expected polygon not to contain " + out, p.contains(out));
        }
        
    }
    
    @Test
    public void testCreateFromGenerateConvexHull()
    {
        Vector3 a = new Vector3(1, 1, 0);
        
        Polygon p = Polygon.createConvexHull(Arrays.asList(a, a, a, a, a));
        assertEquals(1, p.getPoints().size());
        assertTrue(p.getPoints().contains(a));
    }
    
    @Test
    public void testCreateFromLineSegment()
    {
        Vector3 a = new Vector3(1, 1, 0);
        Vector3 b = new Vector3(-1, -1, 0);
        
        Polygon p = Polygon.createConvexHull(Arrays.asList(a, b, a, b, b, a));
        assertEquals(2, p.getPoints().size());
        assertTrue(p.getPoints().contains(a));
        assertTrue(p.getPoints().contains(b));
        
    }
    
    @Test
    public void testContains()
    {
        Vector3 a = new Vector3(1, 1, 0);
        Vector3 b = new Vector3(-1, -1, 0);
        Vector3 c = new Vector3(1, -1, 0);
        Vector3 d = new Vector3(-1, 1, 0);
        Vector3 zero = Vector3.ZERO;
        
        Polygon p = Polygon.createConvexHull(Arrays.asList(a, b, c, d, zero));

        double delta = 0.1;
        for(double x = -10.; x < 10.0; x += delta)
        {
            for(double y = -10; y < 10.0; y += delta)
            {
                Vector3 v = new Vector3(x, y, 0);
                if(x < -1 || x > 1 || y < -1 || y > 1)
                {
                    assertFalse("Expected " + v + " not to be contained", p.contains(v));
                }
                else
                {
                    assertTrue("Expected " + v + " to be contained", p.contains(v));
                }
            }
            
        }
    }
    
    @Test
    public void testCentroid()
    {
        Vector3 a = new Vector3(2, 1, 0);
        Vector3 b = new Vector3(-1, -1, 0);
        Vector3 c = new Vector3(1, -1, 0);
        Vector3 d = new Vector3(-1, 4, 2);
        Vector3 zero = Vector3.ZERO;
        
        Polygon p = Polygon.createConvexHull(Arrays.asList(a, b, c, d, zero));
        
        Vector3 cent = p.getCentroid();
        
        System.out.println("Centroid: " + cent);
        assertTrue(0.24 < cent.x && cent.x < 0.26);
        assertTrue(0.74 < cent.y && cent.y < 0.76);
        
        Vector3[] bb = p.getBoundingBox();
        
        System.out.println("Min: " + bb[0]);
        System.out.println("Max: " + bb[1]);
        
        assertTrue(bb[0].x == -1);
        assertTrue(bb[0].y == -1);
        assertTrue(bb[0].z == 0);
        
        assertTrue(bb[1].x == 2);
        assertTrue(bb[1].y == 4);
        assertTrue(bb[1].z == 2);
    }
}
