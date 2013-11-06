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
 * Created on Jun 12, 2007
 */
package com.soartech.simjr.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import com.soartech.simjr.NullProgressMonitor;
import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.AbstractAdaptable;
import com.soartech.simjr.console.ConsoleManager;
import com.soartech.simjr.console.ConsoleParticipant;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.services.SimulationService;
import com.soartech.simjr.util.SystemTools;

/**
 * Simple wrapper around Rhino JavaScript engine. Sets up the engine and keeps 
 * track of bookkeeping like the current directory, etc.
 *   
 * @author ray
 */
public class ScriptRunner extends AbstractAdaptable implements SimulationService, ProgressMonitor
{
    private static final Logger logger = Logger.getLogger(ScriptRunner.class);
    
    private ServiceManager services;
    private Scriptable globalScope;
    private ProgressMonitor currentProgress = new NullProgressMonitor();
    private Stack<File> fileStack = new Stack<File>();
    private ScriptProviderManager providers;
    private Set<String> requiredScripts = Collections.synchronizedSet(new HashSet<String>());
    
    /**
     * Construct a new script engine. Each engine has its own global state that
     * persists across calls to run scripts.
     * 
     * @param services
     */
    public ScriptRunner(ServiceManager services)
    {
        this.services = services;
        
        // If theres's console, install our own console
        final ConsoleManager consoleManager = services.findService(ConsoleManager.class);
        if(consoleManager != null)
        {
            consoleManager.addParticipant(new Console());
        }
        
        this.providers = services.findService(ScriptProviderManager.class);
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#shutdown()
     */
    public void shutdown() throws SimulationException
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#start()
     */
    public void start(ProgressMonitor progress) throws SimulationException
    {
    }
    
    /**
     * Scripts may call this method (on the runner object) to report progress
     * to the system.
     * 
     * <p>This method should only be called from scripts!

     * @param name The name of the subtask, e.g. "Creating agent X ..."
     */
    public void subTask(String name)
    {
        currentProgress.subTask(name);
    }
    
    /**
     * @return The current progress monitor
     */
    public ProgressMonitor getProgressMonitor()
    {
        return currentProgress;
    }
    
    /**
     * Given a relative file, returns an absolute path to the file as if it
     * were relative to the currently executing script. May be called by
     * scripts through the "runner" object to refer to files in a relative
     * way. If the file is already absolute, it is just returned.
     * 
     * <p>This method should only be called from scripts!
     *
     * @param file A relative file path
     * @return Absolute file path
     */
    public File getRelativeFile(String file)
    {
        final File abs = new File(file);
        if(abs.isAbsolute())
        {
            return abs;
        }
        
        final File current = fileStack.peek();
        
        return new File(current.getParent(), file).getAbsoluteFile();
    }
    
    /**
     * Evaluate the given file. If the file is absolute, it is evaluated. 
     * Otherwise, it's absolute location is calculated with 
     * {@link #getRelativeFile(String)}, i.e. it is treated as if it were
     * relative to the location of the currently executing script. 
     * 
     * <p>This method should only be called from scripts!
     * 
     * @param script The script to run
     * @return Result of execution
     * @throws Exception
     */
    public Object evalFile(String file) throws Exception
    {
        final File script = getRelativeFile(file);
        Reader reader = null;
        try
        {
            logger.debug("Evaluating sub script '" + script + "'");
            fileStack.push(script);
            reader = new FileReader(script);
            Context context = Context.enter();
            
            return context.evaluateReader(globalScope, reader, script.getPath(), 1, null);
        }
        catch(Exception e)
        {
            logger.error("Error executing script '" + script + "'", e);
            throw e;
        }
        finally
        {
            fileStack.pop();
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
    
    /**
     * Load a named script using the runner's {@link ScriptProviderManager}. If the
     * script has already been previously loaded, the method returns {@code null}
     * immediately.
     * 
     * <p>This method should only be called from scripts!
     * 
     * @param name the name of the script
     * @return the result of evaluating the script
     * @throws Exception
     * @see ScriptProvider
     */
    public Object requireScript(String name) throws Exception
    {
        synchronized(requiredScripts)
        {
            if(requiredScripts.contains(name))
            {
                return null;
            }
            
            logger.info("Requiring script '" + name + "'");

            final Object result;
            final ScriptProvider provider = providers != null ? providers.getProvider(name) : null;
            if(provider != null)
            {
                final InputStream contents = provider.getContents();
                if(contents == null)
                {
                    throw new FileNotFoundException("Failed to retrieve contents of script '" + provider.getPath() + "'");
                }
                result = evalStream(contents, provider.getPath());
            }
            else
            {
                // TODO For now, fall back to the old way
                result = evalResource("/simjr." + name + ".js");
            }
            
            requiredScripts.add(name);
            
            return result;
        }
    }
    
    private Object evalStream(InputStream stream, String path) throws Exception
    {
        Reader reader = new BufferedReader(new InputStreamReader(stream));
        try
        {
            logger.info("Evaluating resource '" + path + "'");
            fileStack.push(new File(SystemTools.getCurrentDirectory(), "resource"));
            Context context = Context.enter();
            
            return context.evaluateReader(globalScope, reader, path, 1, null);
        }
        catch(Exception e)
        {
            logger.error("Error executing resource '" + path + "'", e);
            throw e;
        }
        finally
        {
            fileStack.pop();
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
    
    private Object evalResource(String path) throws Exception
    {
        InputStream stream = ScriptRunner.class.getResourceAsStream(path);
        if(stream == null)
        {
            throw new IllegalArgumentException("Could not find resource '" + path + "' on class path");
        }
        return evalStream(stream, path);
    }
    
    /**
     * Run a script using the given settings.
     * 
     * @param settings the settings for the run
     * @return the result
     * @throws Exception
     * @see ScriptRunSettings
     */
    public synchronized Object run(ScriptRunSettings settings) throws Exception
    {
        final File pushFile = settings.pushFile();
        if(pushFile != null)
        {
            try
            {
                fileStack.push(pushFile);
                return runReader(settings);                
            }
            finally
            {
                fileStack.pop();
            }
        }
        else
        {
            return runReader(settings);
        }
    }
    
    private synchronized Object runReader(ScriptRunSettings settings) throws Exception
    {
        try
        {
            logger.info("Running script '" + settings.path() + "'");
            currentProgress = NullProgressMonitor.createIfNull(settings.progress());
            Context context = Context.enter();
            initializeGlobalScope(context);
            for(Map.Entry<Object, Object> entry : settings.properties().entrySet())
            {
                final String name = entry.getKey().toString();
                final Object value = entry.getValue();
                // TODO this seems wrong
                ScriptableObject.putProperty(globalScope, name, value);
            }
            
            // Now run the file
            return context.evaluateReader(globalScope, settings.reader(), settings.path(), 1, null);
        }
        finally
        {
            Context.exit();
        }
    }

    /**
     * @param context
     * @throws Exception
     */
    private synchronized void initializeGlobalScope(Context context) throws Exception
    {
        if(globalScope != null)
        {
            return;
        }
        globalScope = new ImporterTopLevel(context);
        
        ScriptableObject.putProperty(globalScope, "logger", logger);
        ScriptableObject.putProperty(globalScope, "services", services);
        ScriptableObject.putProperty(globalScope, "runner", this);
        
        // First auto load common stuff into the interpreter
        evalResource("/simjr.common.js");
    }
    
    /**
     * Run a script stored as a resource on the class path
     * 
     * @param progress Progress monitor
     * @param path The path to the resource
     * @return The return value of the script
     * @throws Exception
     */
    public synchronized Object runResource(ProgressMonitor progress, ClassLoader loader, String path) throws Exception
    {
        final InputStream stream = loader.getResourceAsStream(path);
        if(stream == null)
        {
            throw new IllegalArgumentException("Could not find resource '" + path + "' on class path");
        }
        
        final Reader reader = new BufferedReader(new InputStreamReader(stream));
        try
        {
            return ScriptRunSettings.builder().
                reader(reader).
                progress(progress).
                path(path).
                run(this);
        }
        finally
        {
            reader.close();
        }
    }
    
    public synchronized Object run(ProgressMonitor progress, File script) throws Exception
    {
        Reader reader = new FileReader(script);
        try
        {
            return ScriptRunSettings.builder().
                reader(reader).
                progress(progress).
                path(script.getPath()).
                pushFile(script).
                run(this);
        }
        finally
        {
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
    
    private class Console implements ConsoleParticipant
    {
        /* (non-Javadoc)
         * @see com.soartech.simjr.console.ConsoleParticipant#executeCommand(java.lang.String)
         */
        public String executeCommand(String command)
        {
            try
            {
                final Context context = Context.enter();
                initializeGlobalScope(context);
                Object r = context.evaluateString(globalScope, command, "console", 1, null);
                if(r == null)
                {
                    return "null";
                }
                if(r instanceof NativeJavaObject)
                {
                    r = ((NativeJavaObject) r).unwrap();
                }
                else if(r instanceof Undefined)
                {
                    r = "";
                }
                return r.toString();
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
                return e.getMessage();
            }
            finally
            {
                Context.exit();
            }
        }

        /* (non-Javadoc)
         * @see com.soartech.simjr.console.ConsoleParticipant#getName()
         */
        public String getName()
        {
            return "JavaScript";
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return getName();
        }
    }

}
