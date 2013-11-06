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

import java.io.InputStream;

/**
 * An implementation of {@link ScriptProvider} that retrieves a script
 * as a resource using a specific class loader.
 * 
 * @author ray
 */
public class ResourceScriptProvider implements ScriptProvider
{
    private final String name;
    private final String path;
    private final ClassLoader loader;
    
    /**
     * Construct a new provider
     * 
     * @param name the name of the provider
     * @param path the path to the resource
     * @param loader the class loader used to retrieve the resource
     */
    public ResourceScriptProvider(String name, String path, ClassLoader loader)
    {
        this.name = name;
        this.path = path;
        this.loader = loader;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.scripting.ScriptProvider#getContents()
     */
    public InputStream getContents()
    {
        return loader.getResourceAsStream(path);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.scripting.ScriptProvider#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.scripting.ScriptProvider#getPath()
     */
    public String getPath()
    {
        return path;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return loader + ":" + name + ":" + path;
    }

    
}
