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
 * Created on Mar 27, 2009
 */
package com.soartech.simjr.scenario;

import javax.swing.undo.UndoableEdit;

import com.soartech.simjr.scenario.EntityElement;
import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.services.DefaultServiceManager;
import com.soartech.simjr.sim.EntityConstants;

import junit.framework.TestCase;

public class EntityElementTest extends TestCase
{

    public void testGetAndSetName()
    {
        final Model model = new Model(new DefaultServiceManager());
        final EntityElement e = EntityElement.attach(model, EntityElement.build(model, "firstName", "prototype"));
        assertEquals("firstName", e.getName());
        e.setName("finalName");
        assertEquals("finalName", e.getName());
    }
    
    public void testGetAndSetPrototype()
    {
        final Model model = new Model(new DefaultServiceManager());
        final EntityElement e = EntityElement.attach(model, EntityElement.build(model, "firstName", "prototype"));
        assertEquals("prototype", e.getPrototype());
        e.setPrototype("finalPrototype");
        assertEquals("finalPrototype", e.getPrototype());
    }
    
    public void testGetLocation()
    {
        final Model model = new Model(new DefaultServiceManager());
        final EntityElement e = EntityElement.attach(model, EntityElement.build(model, "firstName", "prototype"));
        assertNotNull(e.getLocation());
    }
    
    public void testGetAndSetForce()
    {
        final Model model = new Model(new DefaultServiceManager());
        final EntityElement e = EntityElement.attach(model, EntityElement.build(model, "firstName", "prototype"));
        assertEquals(EntityConstants.FORCE_FRIENDLY, e.getForce());
        final UndoableEdit edit = e.setForce(EntityConstants.FORCE_OPPOSING);
        assertNotNull(edit);
        assertEquals(EntityConstants.FORCE_OPPOSING, e.getForce());
        edit.undo();
        assertEquals(EntityConstants.FORCE_FRIENDLY, e.getForce());
        edit.redo();
        assertEquals(EntityConstants.FORCE_OPPOSING, e.getForce());
    }
    
    public void testGetAndSetVisible()
    {
        final Model model = new Model(new DefaultServiceManager());
        final EntityElement e = EntityElement.attach(model, EntityElement.build(model, "firstName", "prototype"));
        assertTrue(e.isVisible());
        final UndoableEdit edit = e.setVisible(false);
        assertNotNull(edit);
        assertFalse(e.isVisible());
        edit.undo();
        assertTrue(e.isVisible());
        edit.redo();
        assertFalse(e.isVisible());
    }
}
