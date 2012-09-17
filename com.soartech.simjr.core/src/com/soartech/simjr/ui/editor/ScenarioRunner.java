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
 * Created on Apr 15, 2009
 */
package com.soartech.simjr.ui.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.log4j.Logger;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.AbstractAdaptable;
import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.services.SimulationService;
import com.soartech.simjr.ui.SimulationApplication;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.util.ProcessStreamConsumer;

/**
 * @author ray
 */
public class ScenarioRunner extends AbstractAdaptable implements SimulationService
{
    private static final Logger logger = Logger.getLogger(ScenarioRunner.class);
    
    private final ServiceManager services;
    private final AtomicReference<Process> process = new AtomicReference<Process>();
    private ByteArrayOutputStream logStream = new ByteArrayOutputStream(8092);
    private PrintStream printStream = new PrintStream(logStream);
    
    public ScenarioRunner(ServiceManager services)
    {
        this.services = services;
        
        // Periodically push output from buffer to log window
        new Timer(50, new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                flushLog();
            }}).start();
    }
    
    public synchronized boolean isRunning()
    {
        return process.get() != null;
    }
    
    public void runScenario(Model model)
    {
        if(process.get() != null)
        {
            throw new IllegalStateException("Already running a scenario");
        }
        if(model.isDirty())
        {
            throw new IllegalStateException("Model is not saved");
        }
        
        final File file = model.getFile();
     
        final ProcessBuilder builder = new ProcessBuilder();
        builder.redirectErrorStream(true);
        builder.directory(new File(SimJrProps.get(SimJrProps.HOME)));
        
        List<String> commands = Arrays.asList(
                // assume java is on the system path
                "java",
                
                // Some memory, just for good measure
                "-Xmx1000m",
                
                // Just re-use our classpath
                "-cp", System.getProperty("java.class.path"),
                
                // simjr.home has to be set so it can find ATE and friends
                "-Dsimjr.home=" + SimJrProps.get(SimJrProps.HOME),
                
                // Set other settings
                
                // Run SimulationApplication.main()
                SimulationApplication.class.getCanonicalName(),
                
                // The model file
                file.getAbsolutePath()
                );
        clearLog();
        printStream.println("\nSim Jr process started with args " + commands + "\n");
        
        builder.command(commands);
        
        try
        {
            process.set(builder.start());
            new ProcessStreamConsumer(process.get(), process.get().getInputStream(), logStream);
            new Thread("ScenarioRunner process monitor") {

                /* (non-Javadoc)
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run()
                {
                    try
                    {
                        handleSimJrExit(process.get().waitFor());
                    }
                    catch (InterruptedException e)
                    {
                        logger.error("Interruped while waiting for Sim Jr process to exit");
                    }
                }
                
            }.start();
        }
        catch (IOException e)
        {
            logger.error(e);
        }
    }
    
    private void handleSimJrExit(int code)
    {
        logger.info("Sim Jr process exited with code " + code);
        printStream.println("\nSim Jr process exited with code " + code);
        process.set(null);
        
        SwingUtilities.invokeLater(new Runnable() {

            public void run()
            {
                services.findService(ActionManager.class).updateActions();
            }});
    }
    
    private void clearLog()
    {
        synchronized(logStream)
        {
            logStream.reset();
            RunPanel runPanel = services.findService(RunPanel.class);
            if(runPanel != null)
            {
                runPanel.clear();
            }
        }
        
    }
    private void flushLog()
    {
        final String output;
        synchronized(logStream)
        {
            output = logStream.toString();
            logStream.reset();
        }
        
        if(output.length() == 0)
        {
            return;
        }
        
        RunPanel runPanel = services.findService(RunPanel.class);
        if(runPanel != null)
        {
            runPanel.append(output);
        }
        else
        {
            System.out.print(output);
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#shutdown()
     */
    public void shutdown() throws SimulationException
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#start(com.soartech.simjr.ProgressMonitor)
     */
    public void start(ProgressMonitor progress) throws SimulationException
    {
    }
}
