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
 * Created on Jun 20, 2007
 */
package com.soartech.simjr.scripting;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.soartech.simjr.sim.Entity;

/**
 * @author ray
 */
public class ScriptEntityController
{
    private static final Logger logger = Logger.getLogger(ScriptEntityController.class);
    
    private Entity entity;
    private File script;
    private String[] args;
    private Scriptable scope;
    
    public ScriptEntityController(Entity entity, File script) throws Exception
    {
        this(entity, script, new String[] {});
    }
    
    public ScriptEntityController(Entity entity, File script, String[] args) throws Exception
    {
        this.entity = entity;
        this.script = script;
        this.args = args;
        
        Reader reader = null;
        try
        {
            logger.info("Running script '" + script + "'");
            
            reader = new FileReader(this.script);
            Context context = Context.enter();
            scope = new ImporterTopLevel(context);
            
            ScriptableObject.putProperty(scope, "logger", logger);
            ScriptableObject.putProperty(scope, "entity", this.entity);
            ScriptableObject.putProperty(scope, "args", this.args);
            
            context.evaluateReader(scope, reader, script.getPath(), 1, null);
        }
        catch(Exception e)
        {
            logger.error("Error file executing script '" + script + "'", e);
            throw e;
        }
        finally
        {
            Context.exit();
            if(reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                }
            }
        }
    }
    
}
