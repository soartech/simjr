package com.soartech.simjr.app;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.math.geotrans.Geodetic;
import com.soartech.math.geotrans.Mgrs;
import com.soartech.simjr.NullProgressMonitor;
import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.scripting.ScriptRunner;
import com.soartech.simjr.services.ConstructOnDemand;
import com.soartech.simjr.services.DefaultServiceManager;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.services.ServiceProvider;
import com.soartech.simjr.services.ServiceProviderLocator;
import com.soartech.simjr.services.SimulationService;
import com.soartech.simjr.sim.ScenarioLoader;
import com.soartech.simjr.sim.SimpleTerrain;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.util.FileTools;

public abstract class AbstractSimulationApplication extends DefaultServiceManager
{
    private static final Logger logger = LoggerFactory.getLogger(AbstractSimulationApplication.class);

    protected DefaultApplicationStateService appState = new DefaultApplicationStateService();
    
    /*
     * initialize
     */
    public void initialize(ProgressMonitor progress, String[] args)
    {
        progress = NullProgressMonitor.createIfNull(progress);
        
        addService(appState);
        
        progress.subTask("Initializing scripting support ...");
        addService(new ScriptRunner(this));
        
        final Simulation simulation = createSimulation(progress);
        
        try
        {
            preInitialize(simulation, progress);
            
            loadPluginServices(ServiceProviderLocator.getProviders()); 
            loadScenario(progress, args);
            
        }
        catch (Throwable e)
        {
            logger.error("Application initialization failure!", e);

            try
            {
                shutdownServices();
            }
            catch (SimulationException e1)
            {
                logger.error("Error shutting down services!", e1);
            }
        }
        finally
        {
            appState.setState(ApplicationState.RUNNING);
        }
    }
    
    /**
     * Called during initialization, before the services and scenario are loaded. 
     * Sort of a misnomer, but lacking a better name. 
     * 
     * Allows UI hooks to be created by inheriting classes.  
     *  
     * @param simulation
     */
    protected void preInitialize(Simulation simulation, ProgressMonitor progress)
    {
    }
    
    /**
     * loadPluginServices
     * 
     * @param providers
     */
    protected void loadPluginServices(Collection<ServiceProvider> providers)
    {
        for (ServiceProvider p : providers)
        {
            Collection<String> servicePaths = p.getServicePaths();
            for (String servicePath : servicePaths)
            {
                try
                {
                    // use reflection to get a class from the name
                    @SuppressWarnings("unchecked")
                    Class<? extends SimulationService> clazz = (Class<? extends SimulationService>) Class.forName(servicePath);

                    // If it's "construct on demand" then skip the "find" section which 
                    // would construct it automatically
                    ConstructOnDemand cod = clazz.getAnnotation(ConstructOnDemand.class);
                    if (cod == null) 
                    {
                        // Checking to see if the service has already been started before starting 
                        // it again. This can happen if the service can be started on demand and is
                        // used in a scenario script started from the command line.
                        //
                        // Note: This will end up directly starting any construct on demand services 
                        // so they are excluded with the previous annotation check
                        SimulationService tmp = findService(clazz);

                        if (tmp == null) 
                        {
                            // TODO: This code is essentially duplicated in the DefaultServiceManager 
                            // so it seems like this could be done in a more elegant way

                            // get the constructor for that class with the given parameters
                            Constructor<?> constructor = clazz.getConstructor(ServiceManager.class);

                            // instantiate the class with the constructor
                            SimulationService ss = (SimulationService) constructor.newInstance(this);

                            // add the newly instantiated service to the simulation and start it
                            addService(ss);
                            ss.start(null);
                        }
                    }
                }
                catch (Exception e)
                {
                    logger.error("Unable to load service from path: " + servicePath, e);
                }
            }
        }
    }
    
    /**
     * loadScenario 
     * 
     * @param progress
     * @param args
     * @throws SimulationException
     * @throws Exception
     */
    protected void loadScenario(ProgressMonitor progress, String[] args)
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
    
    /**
     * createSimulation
     * @param progress
     */
    private Simulation createSimulation(ProgressMonitor progress)
    {
        // Set up the simulation with a default terrain. It can be set later by a scenario script
        // For now, we're doing this here so that the global simulation variable can be set correctly
        // in simjr.common.js. TODO: get rid of the global variable or something.
        progress.subTask("Initializing simulation ...");
        Geodetic.Point origin = new Mgrs().toGeodetic(SimJrProps.get("simjr.simulation.defaultOriginMgrs", "11SMS6025093000"));
        SimpleTerrain terrain = new SimpleTerrain(origin);
        Simulation simulation = new Simulation(terrain);
        // UI Only
        //simulation.addListener(new SimListener());
        addService(simulation);
        return simulation;
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
}
