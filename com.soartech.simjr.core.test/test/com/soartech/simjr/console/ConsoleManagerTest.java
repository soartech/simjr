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
 * Created on May 15, 2008
 */
package com.soartech.simjr.console;

import com.soartech.simjr.services.DefaultServiceManager;
import com.soartech.simjr.services.ServiceManager;

import junit.framework.TestCase;

/**
 * @author ray
 */
public class ConsoleManagerTest extends TestCase
{
    ServiceManager services;
    
    private static class TestParticipant implements ConsoleParticipant
    {
        private String name;
        
        public TestParticipant(String name)
        {
            super();
            this.name = name;
        }

        public String executeCommand(String command)
        {
            return null;
        }

        public String getName()
        {
            return name;
        }
        
    }
    
    private static class Listener implements ConsoleManagerListener
    {
        int changeCount = 0;
        public void onChanged(ConsoleManager manager)
        {
            changeCount++;
        }
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        services = new DefaultServiceManager();
        services.addService(new ConsoleManager());
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testAddParticipant()
    {
        ConsoleManager cm = services.findService(ConsoleManager.class);
        assertNotNull(cm);
        
        Listener listener = new Listener();
        cm.addListener(listener);
        assertEquals(0, listener.changeCount);
        
        cm.addParticipant(new TestParticipant("C"));
        assertEquals(1, listener.changeCount);
        assertEquals(1, cm.getParticipants().size());
        
        cm.addParticipant(new TestParticipant("A"));
        assertEquals(2, listener.changeCount);
        assertEquals(2, cm.getParticipants().size());
        assertEquals("A", cm.getParticipants().get(0).getName());
        assertEquals("C", cm.getParticipants().get(1).getName());
        
        cm.addParticipant(new TestParticipant("B"));
        assertEquals(3, listener.changeCount);
        assertEquals(3, cm.getParticipants().size());
        assertEquals("A", cm.getParticipants().get(0).getName());
        assertEquals("B", cm.getParticipants().get(1).getName());
        assertEquals("C", cm.getParticipants().get(2).getName());
    }
    
    public void testRemoveParticipant()
    {
        ConsoleManager cm = services.findService(ConsoleManager.class);
        assertNotNull(cm);
        
        Listener listener = new Listener();
        cm.addListener(listener);
        assertEquals(0, listener.changeCount);
        
        TestParticipant b;
        cm.addParticipant(new TestParticipant("C"));
        cm.addParticipant(b = new TestParticipant("B"));
        cm.addParticipant(new TestParticipant("A"));
        assertEquals(3, listener.changeCount);
        assertEquals(3, cm.getParticipants().size());
        assertEquals("A", cm.getParticipants().get(0).getName());
        assertEquals("B", cm.getParticipants().get(1).getName());
        assertEquals("C", cm.getParticipants().get(2).getName());
        
        cm.removeParticipant(b);
        assertEquals(4, listener.changeCount);
        assertEquals(2, cm.getParticipants().size());
        assertEquals("A", cm.getParticipants().get(0).getName());
        assertEquals("C", cm.getParticipants().get(1).getName());
    }
}
