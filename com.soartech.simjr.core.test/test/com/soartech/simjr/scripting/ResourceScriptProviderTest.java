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
 * Created on Sep 16, 2009
 */
package com.soartech.simjr.scripting;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import com.soartech.simjr.util.FileTools;


/**
 * @author ray
 */
public class ResourceScriptProviderTest extends TestCase
{
    public void testGetName()
    {
        final ResourceScriptProvider provider = new ResourceScriptProvider("testGetName", "path", getClass().getClassLoader());
        assertEquals("testGetName", provider.getName());
    }
    
    public void testGetPath()
    {
        final ResourceScriptProvider provider = new ResourceScriptProvider("testGetName", "path", getClass().getClassLoader());
        assertEquals("path", provider.getPath());
    }
    
    public void testGetContents() throws Exception
    {
        final ResourceScriptProvider provider = new ResourceScriptProvider("name", 
                "/com/soartech/simjr/scripting/ResourceScriptProviderTest_testGetContents.txt", getClass().getClassLoader());
        final InputStream is = provider.getContents();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        FileTools.copy(is, os);
        final String contents = new String(os.toByteArray());
        assertEquals("The contents of this file are loaded by the provider", contents);
        
    }
}
