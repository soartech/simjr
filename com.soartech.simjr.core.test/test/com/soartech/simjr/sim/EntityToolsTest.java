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
 * Created on Jan 30, 2010
 */
package com.soartech.simjr.sim;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

public class EntityToolsTest extends TestCase
{
    public void testGetPropertyRetrievesAPropertyThatIsPresent()
    {
        final Map<String, Object> props = new LinkedHashMap<String, Object>();
        props.put("test", "hello");
        assertEquals("hello", EntityTools.getProperty(props, "test", null));
    }
    
    public void testGetPropertyReturnsDefaultValueForMissingValue()
    {
        final Map<String, Object> props = new LinkedHashMap<String, Object>();

        assertNull(EntityTools.getProperty(props, "test", null));
        assertEquals("hello", EntityTools.getProperty(props, "test", "hello"));
    }
    
    public void testGetPropertyHandlesLazyPropertyValues()
    {
        final Map<String, Object> props = new LinkedHashMap<String, Object>();
        props.put("test", new LazyEntityPropertyValue()
        {
            @Override
            public Object getValue()
            {
                return "this is lazy";
            }
        });
        
        assertEquals("this is lazy", EntityTools.getProperty(props, "test", null));
    }
}
