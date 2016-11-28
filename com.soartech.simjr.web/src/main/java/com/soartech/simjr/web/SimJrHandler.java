package com.soartech.simjr.web;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.web.gson.adapter.EntityAdapter;
import com.soartech.simjr.web.gson.adapter.EntityListAdapter;

public class SimJrHandler extends AbstractHandler
{
    private static final Logger logger = LoggerFactory.getLogger(SimJrHandler.class);
    
    private final Map<String, Handler> handlers;
    
    private static final Type entityListType = new TypeToken<List<Entity>>(){}.getType();
    
    @SuppressWarnings("serial")
    public SimJrHandler(final Simulation simulation)
    {
        final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Entity.class, new EntityAdapter())
            .registerTypeAdapter(entityListType, new EntityListAdapter())
            .create();
        
        final Handler entitiesHandler = new AbstractHandler()
        {
            @Override
            public void handle(String target, Request baseRequest,
                    HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException
            {
                response.getWriter().print(
                        gson.toJson(simulation.getEntities(),
                        entityListType));
            }
        };
        
        handlers = new HashMap<String, Handler>()
        {{
            put("/entities.json", entitiesHandler);
        }};
    }
    
    @Override
    public void handle(String target, Request baseRequest,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException
    {
        logger.error("getting handler for: " + target);
        logger.error(handlers.toString());
        Handler handler = handlers.get(target);
        if (null != handler)
        {
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
            logger.error("handling target: " + target);
            handler.handle(target, baseRequest, request, response);
        }
    }
}
