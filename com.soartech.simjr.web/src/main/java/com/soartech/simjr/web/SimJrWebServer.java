package com.soartech.simjr.web;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

import com.soartech.simjr.sim.Simulation;

public class SimJrWebServer
{
    public SimJrWebServer(final Simulation simulation)
    {
        Server server = new Server(8080);
        server.setHandler(createHandler(simulation));
        
        try
        {
            server.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private Handler createHandler(final Simulation simulation)
    {
        final Handler staticHandler = new ResourceHandler()
        {{
            setDirectoriesListed(true);
            setBaseResource(Resource.newClassPathResource("www"));
        }};
        
        final Handler apiHandler = new ContextHandler()
        {{
            setContextPath("/api");
            setHandler(new SimJrHandler(simulation));
        }};
        
        return new HandlerList()
        {{
            setHandlers(new Handler[] {
                staticHandler,
                apiHandler
            });
        }};
    }
}
