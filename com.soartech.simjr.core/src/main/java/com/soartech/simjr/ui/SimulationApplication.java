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
package com.soartech.simjr.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.app.AbstractSimulationApplication;
import com.soartech.simjr.scripting.ScriptRunner;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.SimulationListenerAdapter;
import com.soartech.simjr.ui.actions.ActionManager;

/**
 * @author ray
 */
public class SimulationApplication extends AbstractSimulationApplication
{
    private static final Logger logger = LoggerFactory.getLogger(SimulationApplication.class);

    public final WindowAdapter exitHandler = new WindowAdapter()
    {
        public void windowClosed(WindowEvent arg0)
        {
            try
            {
                shutdownServices();
            }
            catch (SimulationException e)
            {
                logger.error(e.toString());
            }
        }
    };
    
    private ActionManager actionManager;
    
    public SimulationApplication() { }
    
    protected void preInitialize(Simulation simulation, ProgressMonitor progress) 
    {
        addService(actionManager = findService(ActionManager.class));
        
        simulation.addListener(new SimListener());
        loadActions(progress);
        final SimulationMainFrame mainFrame = createMainFrame(progress);
        completeInitialization(mainFrame);
    }

    private void loadActions(ProgressMonitor progress)
    {
        // Install default actions from javascript
        final ScriptRunner scriptRunner = findService(ScriptRunner.class);
        final String actionsJs = SimJrProps.get("simjr.actions.path", "simjr.actions.js");
        try 
        {
            scriptRunner.runResource(progress, getClass().getClassLoader(), actionsJs);
        }
        catch (Exception e) 
        {
            logger.error("Unable to load actions from: " + actionsJs, e);
        }
    }
    

    protected void completeInitialization(SimulationMainFrame mainFrame)
    {
        actionManager.updateActions();
        mainFrame.toFront();
        mainFrame.requestFocus();
    }

    protected SimulationMainFrame createMainFrame(ProgressMonitor progress)
    {
        progress.subTask("Initializing main window ...");
        SimulationMainFrame mainFrame = new SimulationMainFrame(this);
        mainFrame.setDefaultCloseOperation(SimulationMainFrame.DISPOSE_ON_CLOSE);
        mainFrame.addWindowListener(exitHandler);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setIconImage(SimulationImages.SIMJR_ICON.getImage());
        mainFrame.setVisible(true);
        return mainFrame;
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
    
    private class SimListener extends SimulationListenerAdapter
    {
        public void onPause()
        {   
            actionManager.updateActions();
        }

        public void onStart()
        {
            actionManager.updateActions();
        }
    }
 
    private static SplashScreen showSplashScreen()
    {
        SplashScreen splashScreen = null;
        if(SimJrProps.get("simjr.splash.enabled", true))
        {
            try
            {
                final String splashImage = SimJrProps.get("simjr.splash.image", "/simjr/images/simjr-splash.png");
                splashScreen = new SplashScreen(splashImage);
                splashScreen.setAlwaysOnTop(SimJrProps.get("simjr.splash.alwaysOnTop", true));
                splashScreen.splash();
            }
            catch (MalformedURLException e)
            {
                logger.error("Failed to display splash screen: " + e.getMessage());
            }
        }
        return splashScreen;
    }
    
    private static void hideSplashScreen(SplashScreen splashScreen)
    {
        if(splashScreen != null)
        {
            splashScreen.hide();
        }
    }
    
    /**
     * @param args
     */
    public static void main(final String[] args)
    {
        main(new SimulationApplication(), args);
    }

    public static void main(final SimulationApplication app, final String[] args)
    {
        logger.info("Sim Jr started");
        logger.info("   simjr.home=" + SimJrProps.get(SimJrProps.HOME));
        logger.info("   current directory = " + System.getProperty("user.dir"));
        
        SplashScreen splashScreen = showSplashScreen();
        
        app.initialize(splashScreen, args);
        
        hideSplashScreen(splashScreen);
    }
}
