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
 * Created on Aug 7, 2007
 */
package com.soartech.simjr.util;

import junit.framework.TestCase;

public class ExtendedPropertiesTest extends TestCase
{
    public void testBasicRetrieval()
    {
        ExtendedProperties props = new ExtendedProperties();
        
        assertTrue(props.isEmpty());
        
        props.setProperty("key", "value");
        
        assertFalse(props.isEmpty());
        assertEquals("value", props.getProperty("key"));
    }
    
    public void testExpansion()
    {
        ExtendedProperties parent = new ExtendedProperties();
        ExtendedProperties child = new ExtendedProperties(parent);
        
        assertTrue(child.isEmpty());
        
        parent.setProperty("parent.x", "A");
        parent.setProperty("parent.y", "B");
        
        assertFalse(parent.isEmpty());
        assertFalse(child.isEmpty());
        
        assertEquals("A", parent.getProperty("parent.x"));
        assertEquals("A", child.getProperty("parent.x"));
        assertEquals("B", parent.getProperty("parent.y"));
        assertEquals("B", child.getProperty("parent.y"));
        
        child.setProperty("parent.x", "C");
        assertEquals("A", parent.getProperty("parent.x"));
        assertEquals("C", child.getProperty("parent.x"));
        
        child.setProperty("child.z", "Z");
        parent.setProperty("parent.z", "${parent.x} ${parent.y}${child.z}");
        
        assertEquals("C BZ", child.getProperty("parent.z"));
    }
}
