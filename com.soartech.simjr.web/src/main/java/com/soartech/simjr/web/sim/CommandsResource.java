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
 * Created on Dec 10, 2009
 */
package com.soartech.simjr.web.sim;

import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.ScalableTickPolicy;

/**
 * @author ray
 */
public class CommandsResource
{
    private static final Logger logger = Logger.getLogger(CommandsResource.class);
    
    private final RootResource root;
    
    public CommandsResource(RootResource simulationResource)
    {
        this.root = simulationResource;
    }

    @POST
    public Response postCommand(String command) throws JSONException
    {
        final JSONObject parsedCommand = new JSONObject(command);
        
        final String action = parsedCommand.getString("action");
        if("run".equals(action))
        {
            root.getSimulation().setPaused(false);
            return root.index();
        }
        else if("pause".equals(action))
        {
            root.getSimulation().setPaused(true);
            return root.index();
        }
        else if("setTimeFactor".equals(action))
        {
            final ScalableTickPolicy tickPolicy = Adaptables.adapt(root.getSimulation(), ScalableTickPolicy.class);
            if(tickPolicy == null)
            {
                logger.error("Simulation does not support time factor scaling");
                parsedCommand.put("error", "Simulation does not support time factor scaling");
                return Response.status(Status.BAD_REQUEST).
                                entity(parsedCommand.toString(2)).
                                type(MediaType.TEXT_PLAIN).build();
            }
            
            final double factor = parsedCommand.getDouble("value");
            logger.info("Setting simulation time factor to " + factor);
            tickPolicy.setTimeFactor(factor);
            return root.index();
        }
        else
        {
            parsedCommand.put("error", "Unsupported action in command '" + command + "'");
            logger.error("Unsupported action in command '" + command + "'");
            return Response.status(Status.BAD_REQUEST).
                            entity(parsedCommand.toString(2)).
                            type(MediaType.TEXT_PLAIN).build();
        }
    }
}
