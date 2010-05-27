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
package com.soartech.simjr.util;

import java.io.File;
import java.io.FileFilter;

import junit.framework.TestCase;

public class FileToolsTest extends TestCase
{
    public void testGetExtension()
    {
        assertEquals("foo", FileTools.getExtension("test.foo"));
    }
    
    public void testGetExtensionReturnsEmptyStringWhenNoExtensionIsPresent()
    {
        assertEquals("", FileTools.getExtension("test"));
    }
    
    public void testGetExtensionIgnoresDotsInDirectories()
    {
        assertEquals("", FileTools.getExtension("/some.path/test"));
        assertEquals("", FileTools.getExtension("some.path\\test"));
    }
    
    public void testAddDefaultExtensionAddsExtensionWhenMissing()
    {
        assertEquals("test.foo", FileTools.addDefaultExtension(new File("root/test"), "foo").getName());
    }
    
    public void testAddDefaultExtensionAddsNothingWhenExtensionIsPresent()
    {
        assertEquals("test.bar", FileTools.addDefaultExtension(new File("root/test.bar"), "foo").getName());
    }
    
    public void testAsDirectoryReturnsInputIfItIsADirectory()
    {
        final File cd = SystemTools.getCurrentDirectory();
        assertTrue("The current directory doesn't exist!?!?", cd.exists());
        assertSame(cd, FileTools.asDirectory(cd));
    }
    
    public void testASDirectoryReturnParentIfItIsAFile()
    {
        final File cd = SystemTools.getCurrentDirectory();
        assertTrue("The current directory doesn't exist!?!?", cd.exists());

        final File[] files = cd.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname)
            {
                return pathname.isFile();
            }});
        assertTrue("No files in current directory to test with: " + cd, files.length > 0);
        assertEquals(cd, FileTools.asDirectory(files[0]));
    }
}
