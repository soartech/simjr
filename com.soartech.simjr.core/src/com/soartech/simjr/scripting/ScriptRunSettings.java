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

import java.io.File;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

import com.soartech.simjr.NullProgressMonitor;
import com.soartech.simjr.ProgressMonitor;

/**
 * A builder for setting up a script run.
 * 
 * @author ray
 */
public class ScriptRunSettings
{
    private Reader reader;
    private ProgressMonitor progress = new NullProgressMonitor();
    private String path = "unknown";
    private Map<Object, Object> properties = new LinkedHashMap<Object, Object>();
    private File pushFile;
    
    public static ScriptRunSettings builder() { return new ScriptRunSettings(); }
    
    private ScriptRunSettings()
    {
        
    }
    
    /**
     * The reader to execute. This value <b>must</b> be set.
     * 
     * @param reader the reader
     * @return this
     */
    public ScriptRunSettings reader(Reader reader)
    {
        this.reader = reader;
        return this;
    }
    
    public Reader reader() { return reader; }
    
    /**
     * Set the progress monitor to use. If {@code null} or not set
     * defaults to {@link NullProgressMonitor}
     * 
     * @param progress the progress monitor to use
     * @return this
     */
    public ScriptRunSettings progress(ProgressMonitor progress)
    {
        this.progress = progress;
        return this;
    }
    
    public ProgressMonitor progress() { return progress; }
    
    /**
     * Set the script path. If not set, defaults to "unknown".
     * 
     * @param path the script path
     * @return this
     */
    public ScriptRunSettings path(String path)
    {
        this.path = path;
        return this;
    }
    
    public String path() { return path; }
    
    /**
     * Set an interpreter property, i.e. global variable
     * 
     * @param name the name of the property (uses {@code toString()})
     * @param value the value
     * @return this
     */
    public ScriptRunSettings property(Object name, Object value)
    {
        this.properties.put(name, value);
        return this;
    }
    
    public Map<Object, Object> properties() { return properties; }
    
    /**
     * Set the "current file" to push onto the file stack before executing
     * the script.
     * 
     * @param file the file to push
     * @return this
     */
    public ScriptRunSettings pushFile(File file)
    {
        this.pushFile = file;
        return this;
    }
    
    public File pushFile() { return pushFile; }
    
    /**
     * Run the given runner with the current settings.
     * 
     * @param runner the script runner to use
     * @return the script result
     * @throws Exception if an error occurs
     */
    public Object run(ScriptRunner runner) throws Exception
    {
        return runner.run(this);
    }
}
