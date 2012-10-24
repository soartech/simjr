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
 * Created on Apr 9, 2009
 */
package com.soartech.simjr.sim;

import org.apache.log4j.Logger;

import com.soartech.math.Angles;
import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.scenario.EntityElement;
import com.soartech.simjr.scenario.TerrainElement;
import com.soartech.simjr.scenario.TerrainImageElement;
import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.sim.entities.AbstractPolygon;
import com.soartech.simjr.ui.pvd.MapImage;
import com.soartech.simjr.ui.pvd.PlanViewDisplayProvider;


/**
 * @author ray
 */
public class ScenarioExporter
{
    private static final Logger logger = Logger.getLogger(ScenarioExporter.class);
    
    public Model createModel(ServiceManager services)
    {
        final Model model = new Model();
        
        final Simulation sim = Simulation.findService(services);
        if(sim == null)
        {
            throw new IllegalStateException("Could not find simulation");
        }
        
        synchronized (sim.getLock())
        {
            setModelTerrainInfo(model, sim);
            exportMapImage(model, sim, services);
            createModelEntities(model, sim);
        }
        
        return model;
    }


    private void setModelTerrainInfo(Model model, Simulation sim)
    {
        logger.info("Exporting terrain information");
        final TerrainElement te = model.getTerrain();
        final Terrain terrain = sim.getTerrain();
        
        final Geodetic.Point origin = terrain.toGeodetic(Vector3.ZERO);
        te.setOrigin(Math.toDegrees(origin.latitude), Math.toDegrees(origin.longitude));
        
    }
    
    private void exportMapImage(Model model, Simulation sim, ServiceManager services)
    {
        final PlanViewDisplayProvider pvdPro = services.findService(PlanViewDisplayProvider.class);
        if(pvdPro == null || pvdPro.getActivePlanViewDisplay() == null)
        {
            return;
        }
        final MapImage map = pvdPro.getActivePlanViewDisplay().getMapImage();
        if(map == null)
        {
            return;
        }
        
        final TerrainImageElement tie = model.getTerrain().getImage();
        tie.setImageHref(map.getImageFile().getAbsolutePath());
        tie.setImageMetersPerPixel(map.getMetersPerPixel());
        tie.getLocation().setLocation(sim.getTerrain().toGeodetic(map.getCenterMeters()));
    }


    private void createModelEntities(Model model, Simulation sim)
    {
        for(Entity e : sim.getEntitiesFast())
        {
            createModelEntity(model, e);
        }
    }


    private void createModelEntity(Model model, Entity e)
    {
        logger.info("Exporting entity '" + e.getName() + "'");
        
        final EntityElement modelEntity = model.getEntities().addEntity(e.getName(), e.getPrototype().getId()).getEntity();
        
        modelEntity.setVisible(EntityTools.isVisible(e));
        modelEntity.setForce(EntityTools.getForce(e));
        
        // Position
        modelEntity.getLocation().setLocation(e.getSimulation().getTerrain().toGeodetic(e.getPosition()));
        
        // Orientation
        modelEntity.getOrientation().setHeading(Math.toDegrees(Angles.mathRadiansToNavRadians(e.getHeading())));
        modelEntity.getOrientation().setPitch(Math.toDegrees(e.getPitch()));
        modelEntity.getOrientation().setRoll(Math.toDegrees(e.getRoll()));
        
        // Points
        final AbstractPolygon poly = Adaptables.adapt(e, AbstractPolygon.class);
        if(poly != null)
        {
            modelEntity.getPoints().setPoints(poly.getPointNames());
        }
    }
}
