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
 * Created on Mar 25, 2009
 */
package com.soartech.simjr.sim;

import java.io.StringReader;

import junit.framework.TestCase;

public class EntityPrototypeDatabaseTest extends TestCase
{
    public void testANewDatabaseHasNoEntries() throws Exception
    {
        assertEquals(0, new EntityPrototypeDatabase().getPrototypes().size());
    }
    
    public void testDefaultDatabaseLoadsWithoutErrors() throws Exception
    {
        EntityPrototypeDatabase db = new EntityPrototypeDatabase();
        db.load();
        assertTrue(db.getPrototypes().size() > 0);
    }
    
    public void testLoadingMultipleFiles() throws Exception
    {
        final EntityPrototypeDatabase db = new EntityPrototypeDatabase();
        final String first = "" +
        "prototypes:\n" +
        "- &a\n" +
        "    id: a";
        
        db.load(new StringReader(first), getClass().getClassLoader());
        final EntityPrototype a = db.getPrototype("a");
        assertNotNull(a);
        
        final String second = "" +
        "prototypes:\n" +
        "- &b\n" +
        "    id: b";
        db.load(new StringReader(second), getClass().getClassLoader());
        final EntityPrototype b = db.getPrototype("b");
        assertNotNull(b);
        assertSame(a, db.getPrototype("a"));
        assertNotSame(a, b);
        
        assertEquals(2, db.getPrototypes().size());
    }
    
    public void testLoadingMultipleFilesWithPrototypeCrossReferences() throws Exception
    {
        // Test that a prototype in one document can refer to a prototype (e.g. parent)
        // in a previously loaded document.
        final EntityPrototypeDatabase db = new EntityPrototypeDatabase();
        final String first = "" +
        "prototypes:\n" +
        "-\n" +
        "    id: a";
        
        db.load(new StringReader(first), getClass().getClassLoader());
        final EntityPrototype a = db.getPrototype("a");
        assertNotNull(a);
        
        final String second = "" +
        "prototypes:\n" +
        "- &b\n" +
        "    id: b\n" +
        "    parent: !prototype a";
        db.load(new StringReader(second), getClass().getClassLoader());
        final EntityPrototype b = db.getPrototype("b");
        assertNotNull(b);
        assertSame(a, b.getParent());
        
        assertEquals(2, db.getPrototypes().size());
    }
    
    public void testLoadingPrototypeFragments() throws Exception
    {
        final EntityPrototypeDatabase db = new EntityPrototypeDatabase();
        final String prototype = "" +
        "prototypes:\n" +
        "-\n" +
        "    id: a\n" +
        "    properties:\n" +
        "        test: hello\n" +
        "-\n" +
        "    id: b\n" +
        "    properties:\n" +
        "        test: goodbye";
        
        db.load(new StringReader(prototype), getClass().getClassLoader());
        final String fragments = "" +
        "prototypes:\n" +
        "-\n" +
        "    id: a\n" +
        "    properties:\n" +
        "        more: morea\n" +
        "-\n" +
        "    id: b\n" +
        "    properties:\n" +
        "        more: moreb";
        db.loadFragments(new StringReader(fragments), getClass().getClassLoader());
        
        final EntityPrototype a = db.getPrototype("a");
        assertEquals("morea", a.getProperty("more"));
        assertEquals("hello", a.getProperty("test"));
        
        final EntityPrototype b = db.getPrototype("b");
        assertEquals("moreb", b.getProperty("more"));
        assertEquals("goodbye", b.getProperty("test"));
    }
}
