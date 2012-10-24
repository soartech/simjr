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
 * Created on Dec 11, 2009
 */
package com.soartech.simjr.web.sim;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.soartech.simjr.radios.RadioHistory;
import com.soartech.simjr.radios.RadioMessage;
import com.soartech.simjr.sim.Simulation;

/**
 * @author ray
 */
public class MessagesResource
{
    private final RootResource root;
    
    public MessagesResource(RootResource simulationResource)
    {
        this.root = simulationResource;
    }

    @GET
    public Response index(@QueryParam("since") @DefaultValue("0.0") double since) throws JSONException
    {
        final JSONArray result = new JSONArray();
        final Simulation sim = root.getSimulation();
        
        synchronized(sim.getLock())
        {
            final RadioHistory history = RadioHistory.findService(root.getServices());
            if(history != null)
            {
                for(RadioMessage message : history.getMessages())
                {
                    if(message.getTime() >= since)
                    {
                        result.put(getMessageJson(message));
                    }
                }
            }
        }
        
        return Response.ok().entity(result.toString(2)).type(MediaType.TEXT_PLAIN_TYPE).build();
    }
        
    private JSONObject getMessageJson(RadioMessage message) throws JSONException
    {
        final JSONObject json = new JSONObject();
        
        json.put("time", message.getTime());
        json.put("content", message.getContent());
        json.put("frequency", message.getFrequency());
        json.put("source", message.getSource());
        json.put("target", message.getTarget());
        
        return json;
    }

}
