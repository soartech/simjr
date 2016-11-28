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
 * Created on Dec 7, 2009
 */
package com.soartech.simjr.web.sim;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityTools;

/**
 * @author ray
 */
public class PropertiesResource
{
    private final Entity entity;

    /**
     * @param entity
     */
    public PropertiesResource(Entity entity)
    {
        this.entity = entity;
    }
    
    @GET
    public Response index() throws JSONException
    {
        if(entity == null)
        {
            return Response.status(Status.NOT_FOUND).build();
        }
        synchronized(entity.getSimulation())
        {
            return Response.ok().entity(getPropertiesJson().toString(2)).type(MediaType.TEXT_PLAIN_TYPE).build();
        }
    }

    private JSONObject getPropertiesJson() throws JSONException
    {
        final Map<String, Object> properties = entity.getProperties();
        final JSONObject result = new JSONObject();
        for(Map.Entry<String, Object> entry : properties.entrySet())
        {
            // Use EntityTools.getProperty() to ensure that lazy properties are
            // handled correctly.
            final String name = entry.getKey();
            final Object value = EntityTools.getProperty(properties, name, null);
            result.put(name, value);
        }
        return result;
    }
  
}
