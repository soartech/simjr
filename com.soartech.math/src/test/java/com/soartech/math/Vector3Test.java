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
 * Created on Apr 22, 2010
 */
package com.soartech.math;

import org.junit.Test;

import static org.junit.Assert.*;

public class Vector3Test
{
    @Test
    public void testProjectVectorOnPlane()
    {
        Vector3 normal = Vector3.Z_UNIT;
        
        checkProjection(new Vector3(1, 0, 0), normal, new Vector3(1, 0, 0));
        checkProjection(new Vector3(1, 0, 1), normal, new Vector3(1, 0, 0));
        checkProjection(new Vector3(1, 1, 0), normal, new Vector3(1, 1, 0));
        checkProjection(new Vector3(1, 1, 1), normal, new Vector3(1, 1, 0));
        checkProjection(new Vector3(1, -1, 1), normal, new Vector3(1, -1, 0));
        checkProjection(new Vector3(1, -1, -1), normal, new Vector3(1, -1, 0));
    }

    private void checkProjection(Vector3 in, Vector3 normal, Vector3 expected)
    {
        Vector3 out = in.projectOntoPlane(normal);
        assertEquals(expected, out);
        
    }
    
    @Test
    public void testParseVector()
    {
        assertEquals(Vector3.ZERO, Vector3.parseVector("(0 ,0, 0)  "));
        assertEquals(new Vector3(1, 2, 3), Vector3.parseVector("   (1.0 ,2, 3)"));
        assertEquals(new Vector3(1, 2, 3), Vector3.parseVector("1.0 ,2, 3    "));
        
        vectorParseFail("");
        vectorParseFail("(1, 2, 3");
        vectorParseFail("   1, 2, 3)");
        vectorParseFail("1, 2, 3, 4");
        vectorParseFail("1, 2");
        vectorParseFail("2");
        vectorParseFail("(x, 2, 3)");
        vectorParseFail("(1, y, 3)");
        vectorParseFail("(1, 2, z)");
    }
    
    private void vectorParseFail(String s)
    {
        try
        {
            Vector3.parseVector(s);
            fail("Expected vector parse to fail");
        }
        catch(IllegalArgumentException e)
        {
        }
    }

}
