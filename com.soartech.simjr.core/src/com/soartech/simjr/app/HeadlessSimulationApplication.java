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
 * Created on May 22, 2007
 */
package com.soartech.simjr.app;

import java.io.File;

import org.apache.log4j.Logger;

import com.soartech.math.geotrans.Geodetic;
import com.soartech.math.geotrans.Mgrs;
import com.soartech.simjr.NullProgressMonitor;
import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.scripting.ScriptRunner;
import com.soartech.simjr.services.DefaultServiceManager;
import com.soartech.simjr.sim.ScenarioLoader;
import com.soartech.simjr.sim.SimpleTerrain;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.util.FileTools;

/**
 * Application entry point for running Sim Jr with no UI.
 * 
 * @author ray
 */
public class HeadlessSimulationApplication extends DefaultServiceManager
{
    private static final Logger logger = Logger.getLogger(HeadlessSimulationApplication.class);
                
    private DefaultApplicationStateService appState = new DefaultApplicationStateService();
    
    public HeadlessSimulationApplication()
    {
    }

    public void initialize(ProgressMonitor progress, String[] args)
    {
        progress = NullProgressMonitor.createIfNull(progress);
        
        addService(appState);
        
        progress.subTask("Initializing scripting support ...");
        addService(new ScriptRunner(this));        
        
        createSimulation(progress);
                                
        try
        {
            loadScenario(progress, args);
            
        }
        catch (Throwable e)
        {
            logger.error("Application initialization failure", e);
            try
            {
                shutdownServices();
            }
            catch (SimulationException e1)
            {
                logger.error(e1);
            }
        }
        finally
        {
            appState.setState(ApplicationState.RUNNING);
        }
    }

    private void loadScenario(ProgressMonitor progress, String[] args)
            throws SimulationException, Exception
    {
        progress.subTask("Loading scenario ...");
        final ScriptRunner scriptRunner = findService(ScriptRunner.class);
        final ScenarioLoader loader = new ScenarioLoader(this);
        for(String arg : args)
        {
            final String ext = FileTools.getExtension(arg).toLowerCase();
            if(ext.equals("xml") || ext.equals("sjx"))
            {
                loader.loadScenario(new File(arg), progress);
            }
            else
            {
                scriptRunner.run(progress, new File(arg));
            }
        }
    }

    private void createSimulation(ProgressMonitor progress)
    {
        // Set up the simulation with a default terrain. It can be set later by a scenario script
        // For now, we're doing this here so that the global simulation variable can be set correctly
        // in simjr.common.js. TODO: get rid of the global variable or something.
        progress.subTask("Initializing simulation ...");
        Geodetic.Point origin = new Mgrs().toGeodetic(SimJrProps.get("simjr.simulation.defaultOriginMgrs", "11SMS6025093000"));
        SimpleTerrain terrain = new SimpleTerrain(origin);
        Simulation simulation = new Simulation(terrain);
        addService(simulation);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ServiceManager#shutdownServices()
     */
    public void shutdownServices() throws SimulationException
    {
        super.shutdownServices();
        logger.info("Exiting");
        System.exit(0);
    }
    
    /**
     * @param args
     */
    public static void main(final String[] args)
    {
        main(new HeadlessSimulationApplication(), args);
    }

    public static void main(final HeadlessSimulationApplication app, final String[] args)
    {
        logger.info("Sim Jr (headless) started");
        logger.info("   simjr.home=" + SimJrProps.get(SimJrProps.HOME));
        logger.info("   current directory = " + System.getProperty("user.dir"));
        
        app.initialize(new NullProgressMonitor(), args);
        while(true) {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }
}
