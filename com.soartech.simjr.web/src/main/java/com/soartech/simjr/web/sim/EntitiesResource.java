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
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.entities.AbstractPolygon;
import com.soartech.simjr.sim.entities.MilStd2525Provider;

/**
 * @author ray
 */
public class EntitiesResource
{
    //private static final Logger logger = Logger.getLogger(EntitiesResource.class);
    
    private final RootResource root;
    
    public EntitiesResource(RootResource simulationResource)
    {
        this.root = simulationResource;
    }

    @GET
    public Response index() throws JSONException
    {
        final JSONArray result = new JSONArray();
        final Simulation sim = root.getSimulation();
        
        synchronized(sim.getLock())
        {
            for(Entity e : sim.getEntitiesFast())
            {
                result.put(getEntityJson(e));
            }
        }
        
        return Response.ok().entity(result.toString(2)).type(MediaType.TEXT_PLAIN_TYPE).build();
    }
    
    @GET
    @Path("{name}")
    public Response show(@PathParam("name") String name) throws JSONException
    {
        final Simulation sim = root.getSimulation();
        
        synchronized(sim.getLock())
        {
            final Entity e = sim.getEntity(name);
            if(e == null)
            {
                return Response.status(Status.NOT_FOUND).entity("No entity with name '" + name + "'").build();
            }
            return Response.ok().entity(getEntityJson(e).toString(2)).type(MediaType.TEXT_PLAIN_TYPE).build();
        }
    }
    
    @Path("{name}/properties")
    public PropertiesResource indexProperties(@PathParam("name") String name) throws JSONException
    {
        final Simulation sim = root.getSimulation();
        
        synchronized(sim.getLock())
        {
            return new PropertiesResource(sim.getEntity(name));
        }
        
    }

    private JSONObject getEntityJson(Entity e) throws JSONException
    {
        final JSONObject json = new JSONObject();
        
        json.put("name", e.getName());
        json.put("prototype", e.getPrototype().getSubcategory());
        json.put("visible", EntityTools.isVisible(e));
        json.put("force", e.getProperty(EntityConstants.PROPERTY_FORCE));
        json.put("ms2525", e.getProperty(MilStd2525Provider.PROPERTY));
        json.put("showLabel", e.getProperty(EntityConstants.PROPERTY_SHAPE_LABEL_VISIBLE));
        json.put("allowOnClick", e.getProperty(EntityConstants.PROPERTY_WEB_DISABLE_ONCLICK));
        
        addLocationJson(e, json);
        
        addPolygonJson(e, json);
        
        return json;
    }

    private void addPolygonJson(Entity e, final JSONObject json)
            throws JSONException
    {
        final AbstractPolygon polygon = Adaptables.adapt(e, AbstractPolygon.class);
        if(polygon != null)
        {
            final JSONObject jsonPoly = new JSONObject();
            jsonPoly.put("closed", polygon.isClosed());
            final JSONArray points = new JSONArray();
            for(Entity pointEntity : polygon.getPoints())
            {
                points.put(getLocationJson(pointEntity));
            }
            jsonPoly.put("points", points);
            
            json.put("polygon", jsonPoly);
        }
    }

    private void addLocationJson(Entity e, final JSONObject json)
            throws JSONException
    {
        if(e.hasPosition())
        {
            final JSONObject location = getLocationJson(e);
            
            json.put("location", location);
            
            json.put("heading", e.getProperty(EntityConstants.PROPERTY_HEADING));
            json.put("speed", e.getProperty(EntityConstants.PROPERTY_SPEED));
        }
    }

    private JSONObject getLocationJson(Entity e) throws JSONException
    {
        final JSONObject location = new JSONObject();
        location.put("latitude", e.getProperty(EntityConstants.PROPERTY_LATITUDE));
        location.put("longitude", e.getProperty(EntityConstants.PROPERTY_LONGITUDE));
        location.put("altitude", e.getProperty(EntityConstants.PROPERTY_ALTITUDE));
        return location;
    }
}
