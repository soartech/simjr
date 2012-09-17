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

import junit.framework.TestCase;

import org.jdom.Element;
import org.jdom.xpath.XPath;

import com.soartech.simjr.scenario.model.Model;

public class LocationElementTest extends TestCase
{
    public void testXPath() throws Exception
    {
        Element e = new Element("hello");
        e.setAttribute("foo", "bar");
        Object o = XPath.selectSingleNode(e, "@foo");
        assertNotNull(o);
    }

    public void testGetAndSetLatitude()
    {
        Model model = new Model();
        EntityElement entity = EntityElement.attach(model, EntityElement.build(model, "testGetAndSetLatitude", "any"));
        LocationElement loc = entity.getLocation();
        assertEquals(0.0, loc.getLatitude(), 0.0001);
        loc.setLatitude(45.0);
        assertEquals(45.0, loc.getLatitude(), 0.0001);
    }
    
    public void testGetAndSetLongitude()
    {
        Model model = new Model();
        EntityElement entity = EntityElement.attach(model, EntityElement.build(model, "testGetAndSetLongitude", "any"));
        LocationElement loc = entity.getLocation();
        assertEquals(0.0, loc.getLongitude(), 0.0001);
        loc.setLongitude(45.0);
        assertEquals(45.0, loc.getLongitude(), 0.0001);
    }
    public void testGetAndSetAltitude()
    {
        Model model = new Model();
        EntityElement entity = EntityElement.attach(model, EntityElement.build(model, "testGetAndSetAltitude", "any"));
        LocationElement loc = entity.getLocation();
        assertEquals(0.0, loc.getAltitude(), 0.0001);
        loc.setAltitude(45.0);
        assertEquals(45.0, loc.getAltitude(), 0.0001);
    }
}
