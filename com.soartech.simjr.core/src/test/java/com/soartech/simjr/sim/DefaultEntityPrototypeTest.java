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
 * Created on Mar 26, 2009
 */
package com.soartech.simjr.sim;

import junit.framework.TestCase;

public class DefaultEntityPrototypeTest extends TestCase
{

    public void testSpecializeSetsParentAutomatically()
    {
        DefaultEntityPrototype parent = new DefaultEntityPrototype();
        parent.setFactory(DefaultEntityPrototypeTest.class);
        
        DefaultEntityPrototype spec = DefaultEntityPrototype.newBuilder().parent(parent).build();
        assertNotSame(spec, parent);
        assertSame(parent, spec.getParent());
    }
    
    public void testSubcategoryDefaultsToId()
    {
        DefaultEntityPrototype parent = DefaultEntityPrototype.newBuilder().id("id").domain("domain").category("cat").build();
        assertEquals(parent.getId(), parent.getSubcategory());
    }
    
    public void testMethodsDelegateToParent()
    {
        DefaultEntityPrototype parent = DefaultEntityPrototype.newBuilder().id("id").domain("domain").category("cat").subcategory("subcat").build();
        parent.setFactory(DefaultEntityPrototypeTest.class);
        
        DefaultEntityPrototype spec = DefaultEntityPrototype.newBuilder().parent(parent).build();
        assertSame(parent, spec.getParent());
        assertNull(spec.getId());
        assertEquals(parent.getDomain(), spec.getDomain());
        assertEquals(parent.getCategory(), spec.getCategory());
        assertEquals(parent.getFactory(), spec.getFactory());
        
        spec.setDomain("domain1");
        assertEquals("domain1", spec.getDomain());
        
        spec.setCategory("cat1");
        assertEquals("cat1", spec.getCategory());
        
        spec.setSubcategory("subcat1");
        assertEquals("subcat1", spec.getSubcategory());
        
        spec.setFactory("factory");
        assertEquals("factory", spec.getFactory());
    }
    
    public void testHasSubcategory()
    {
        DefaultEntityPrototype parent = DefaultEntityPrototype.newBuilder().domain("domain").category("cat").subcategory("subcat").build();
        parent.setFactory(DefaultEntityPrototypeTest.class);
        
        DefaultEntityPrototype spec = DefaultEntityPrototype.newBuilder().parent(parent).build();
        spec.setSubcategory("subcat1");
        assertTrue(spec.hasSubcategory("subcat"));
        assertTrue(spec.hasSubcategory("subcat1"));
        assertFalse(spec.hasSubcategory("another"));
        
    }
}
