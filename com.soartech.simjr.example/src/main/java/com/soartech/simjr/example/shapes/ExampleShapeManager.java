/**
 * 
 */
package com.soartech.simjr.example.shapes;

import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.AbstractAdaptable;
import com.soartech.simjr.services.ConstructOnDemand;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.services.SimulationService;
import com.soartech.simjr.sim.EntityPrototypeDatabase;
import com.soartech.simjr.sim.Simulation;

/**
 * Example SimulationService that loads custom shapes.
 * 
 * @author aron
 *
 */
@ConstructOnDemand
public class ExampleShapeManager extends AbstractAdaptable implements SimulationService
{
    // Constants
    private static final Logger logger = LoggerFactory.getLogger(ExampleShapeManager.class);
    
    // Services
    private final ServiceManager services;
    private Simulation simulation;
    
    /**
     * Helper function to support scripting. 
     * 
     * Call this instead of the constructor, as it will instantiate the class only if necessary.
     * 
     * The @ConstructOnDemand annotation is required for this functionality as noted by findService().
     * 
     * @param services
     * @return
     */
    public static ExampleShapeManager findService(ServiceManager services)
    {
        return services.findService(ExampleShapeManager.class);
    }
    
    /**
     * Initializes the services used by this object
     * 
     * @param services
     * @throws IOException
     */
    public ExampleShapeManager(ServiceManager services) throws IOException {
        this.services = services;
    }
    
    @Override
    public void start(ProgressMonitor progress) throws SimulationException
    {
        logger.info("Starting Example Shape Service");
        simulation = Simulation.findService(services);
        
        //load the custom entities
        EntityPrototypeDatabase protoDb = services.findService(EntityPrototypeDatabase.class);
        final String examplePrototypes = "/simjr.example.entityprototypes.yaml";
        logger.info("Loading editor entity prototypes from resource: " + examplePrototypes);
        protoDb.load(new InputStreamReader(getClass().getResourceAsStream(examplePrototypes)), getClass().getClassLoader());
        
        //load the custom entities
        final String exampleUiPrototypes = "/simjr.example.ui.entityprototypes.yaml";
        logger.info("Loading editor entity UI prototypes from resource: " + exampleUiPrototypes);
        protoDb.loadFragments(new InputStreamReader(getClass().getResourceAsStream(exampleUiPrototypes)), getClass().getClassLoader());
    }

    @Override
    public void shutdown() throws SimulationException
    {
        // TODO Auto-generated method stub
        
    }

}
