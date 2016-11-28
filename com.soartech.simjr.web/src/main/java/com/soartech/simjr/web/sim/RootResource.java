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
 * Created on Dec 6, 2009
 */
package com.soartech.simjr.web.sim;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.sim.ScalableTickPolicy;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.SimulationTickPolicy;

/**
 * @author ray
 */
@Path("/")
public class RootResource
{
    private final ServiceManager services;
    
    public RootResource(ServiceManager services)
    {
        this.services = services;
    }
    
    
    /**
     * @return the services
     */
    public ServiceManager getServices()
    {
        return services;
    }

    public Simulation getSimulation()
    {
        return Simulation.findService(services);
    }
    
    @Path("entities")
    public EntitiesResource entities()
    {
        return new EntitiesResource(this);
    }
    
    @Path("commands")
    public CommandsResource commands()
    {
        return new CommandsResource(this);
    }
    
    @Path("messages")
    public MessagesResource messages()
    {
        return new MessagesResource(this);
    }
    
    @GET
    public Response index() throws JSONException
    {
        final Simulation sim = getSimulation();
        
        final JSONObject result = new JSONObject();
        synchronized(sim.getLock())
        {
            result.put("time", sim.getTime());
            result.put("paused", sim.isPaused());
            result.put("entities", sim.getEntitiesFast().size());
            
            final SimulationTickPolicy tickPolicy = Adaptables.adapt(sim, SimulationTickPolicy.class);
            if(tickPolicy != null)
            {
                result.put("tickPeriod", tickPolicy.getTickPeriod());
                if(tickPolicy instanceof ScalableTickPolicy)
                {
                    result.put("timeFactor", ((ScalableTickPolicy) tickPolicy).getTimeFactor());
                }
            }
        }
        
        return Response.ok().entity(result.toString(2)).type(MediaType.TEXT_PLAIN_TYPE).build();
    }
}
