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

import javax.swing.undo.UndoableEdit;

import junit.framework.TestCase;

public class TerrainImageElementTest extends TestCase
{
    public void testHasImageDefaultsToFalse()
    {
        final Model model = new Model();
        final TerrainElement terrain = model.getTerrain();
        
        assertFalse(terrain.getImage().hasImage());
    }
    
    public void testHasImageBecomesTrueWhenImageIsSet()
    {
        final Model model = new Model();
        final TerrainElement terrain = model.getTerrain();
        
        final UndoableEdit edit = terrain.getImage().setImageHref("test");
        assertTrue(terrain.getImage().hasImage());
        assertEquals("test", terrain.getImage().getImageHref());
        assertNotNull(edit);
        edit.undo();
        assertFalse(terrain.getImage().hasImage());
        
        edit.redo();
        assertTrue(terrain.getImage().hasImage());
        assertEquals("test", terrain.getImage().getImageHref());
    }
    
    public void testClearImage()
    {
        final Model model = new Model();
        final TerrainElement terrain = model.getTerrain();
        
        terrain.getImage().setImageHref("test");
        UndoableEdit edit = terrain.getImage().clearImage();
        assertNotNull(edit);
        assertFalse(terrain.getImage().hasImage());
        
        edit.undo();
        assertTrue(terrain.getImage().hasImage());
        assertEquals("test", terrain.getImage().getImageHref());

        edit.redo();
        assertFalse(terrain.getImage().hasImage());
    }
}
