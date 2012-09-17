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
 * Created on Mar 28, 2009
 */
package com.soartech.simjr.scenario;

import com.soartech.simjr.scenario.EntityElement;
import com.soartech.simjr.scenario.EntityElementList;
import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.scenario.model.ModelChangeEvent;
import com.soartech.simjr.scenario.model.ModelChangeListener;

import junit.framework.TestCase;

/**
 * @author ray
 */
public class EntityElementListTest extends TestCase
{

    public void testAddEntity()
    {
        final Model model = new Model();
        final EntityElementList list = model.getEntities();
        assertTrue(list.getEntities().isEmpty());
        final EntityElement entity = list.addEntity("testAddEntity", "foo").getEntity();
        assertNotNull(entity);
        assertSame(model.getDocument(), entity.getElement().getDocument());
        assertEquals("testAddEntity", entity.getName());
        assertEquals("foo", entity.getPrototype());
        assertEquals(1, list.getEntities().size());
        assertTrue(list.getEntities().contains(entity));
    }

    public void testAddEntityFiresEvent()
    {
        final Model model = new Model();
        final Object source[] = new Object[1];
        final String property[] = new String[1];
        model.addModelChangeListener(new ModelChangeListener() {

            public void onModelChanged(ModelChangeEvent e)
            {
                source[0] = e.source;
                property[0] = e.property;
            }});
        final EntityElementList list = model.getEntities();
        assertTrue(list.getEntities().isEmpty());
        final EntityElement entity = list.addEntity("testAddEntity", "foo").getEntity();
        assertSame(entity, source[0]);
        assertEquals(EntityElementList.ENTITY_ADDED, property[0]);
    }
    
    public void testAddEntityGeneratesUniqueName()
    {
        final Model model = new Model();
        final EntityElementList list = model.getEntities();
        final EntityElement a = list.addEntity("testAddEntityGeneratesUniqueName", "foo").getEntity();
        final EntityElement b = list.addEntity("testAddEntityGeneratesUniqueName", "foo").getEntity();
        assertFalse(a.getName().equals(b.getName()));
    }
    
    public void testRemoveEntity()
    {
        final Model model = new Model();
        final EntityElementList list = model.getEntities();
        assertTrue(list.getEntities().isEmpty());
        final EntityElement entity = list.addEntity("testRemoveEntity", "foo").getEntity();
        assertNotNull(entity);
        list.removeEntity(entity);
        assertTrue(list.getEntities().isEmpty());
        assertNotSame(model.getDocument(), entity.getElement().getDocument());
    }

}
