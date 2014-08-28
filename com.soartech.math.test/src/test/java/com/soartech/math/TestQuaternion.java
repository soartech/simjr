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
 * Created on May 22, 2006
 */
package com.soartech.math;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestQuaternion
{

    @Test
    public void testIdentity()
    {
        Vector3 vector = new Vector3(1.0, 2.0, 3.0);
        Vector3 rotated = Quaternion.IDENTITY.rotate(vector);
        assertTrue("Expected " + vector + ", got " + rotated,
                    vector.epsilonEquals(rotated));
    }
    
    @Test
    public void testRotatingDegenerateVectorIsNoop()
    {
        Vector3 rotated = Quaternion.IDENTITY.rotate(Vector3.ZERO);
        
        assertEquals(Vector3.ZERO, rotated);
    }
    
    @Test
    public void testRotatingByIdentityRotationIsNoop()
    {
        Quaternion identity = Quaternion.createRotation(0.0, Vector3.X_UNIT);
        
        Vector3 input = new Vector3(1, 2, 3);
        Vector3 output = identity.rotate(input);
        
        assertSame(input, output);
    }
    
    /*
     * Test method for 'com.soartech.spatr.math.Quaternion.getVector()'
     */
    @Test
    public void testGetVector()
    {
        Quaternion q = new Quaternion(1.0, new Vector3(2.0, 3.0, 4.0));
        
        Vector3 vector = new Vector3(2.0, 3.0, 4.0);
        assertTrue(vector.epsilonEquals(q.v));
    }

    /*
     * Test method for 'com.soartech.spatr.math.Quaternion.rotate(Vector3)'
     */
    @Test
    public void testRotate()
    {
        Quaternion q = Quaternion.createRotation(Math.PI / 2, Vector3.X_UNIT);
        
        Vector3 rotatedX = q.rotate(Vector3.X_UNIT);
        assertTrue(Vector3.X_UNIT.epsilonEquals(rotatedX));
        
        Vector3 rotatedY = q.rotate(Vector3.Y_UNIT);
        assertTrue(Vector3.Z_UNIT.epsilonEquals(rotatedY));
    }

    @Test
    public void testCreateMappingRotation()
    {
        verifyMappingRotation(Vector3.X_UNIT, Vector3.X_UNIT);
        verifyMappingRotation(Vector3.Y_UNIT, Vector3.Y_UNIT);
        verifyMappingRotation(Vector3.Z_UNIT, Vector3.Z_UNIT);
        verifyMappingRotation(Vector3.X_UNIT, Vector3.Y_UNIT);
        verifyMappingRotation(Vector3.X_UNIT, Vector3.X_UNIT.multiply(-1));
        verifyMappingRotation(Vector3.X_UNIT, Vector3.Z_UNIT);
        verifyMappingRotation(Vector3.X_UNIT, new Vector3(1, 1, 0).normalized());
        verifyMappingRotation(Vector3.X_UNIT, new Vector3(1, 1, 1).normalized());
        
    }
    
    private void verifyMappingRotation(Vector3 start, Vector3 end)
    {
        Quaternion q = Quaternion.createMappingRotation(start, end); 
        
        Vector3 rotated = q.rotate(start);
        assertTrue("Expected " + end + ", got " + rotated, end.epsilonEquals(rotated));
        
    }
}
