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
 * Created on Mar 28, 2009
 */
package com.soartech.simjr.sim;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.StringReader;
import java.util.List;

import org.apache.log4j.Logger;

import com.soartech.math.Angles;
import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.scenario.EntityElement;
import com.soartech.simjr.scenario.ScriptBlockElement;
import com.soartech.simjr.scenario.TerrainImageElement;
import com.soartech.simjr.scenario.TerrainTypeElement;
import com.soartech.simjr.scenario.model.ModelException;
import com.soartech.simjr.scenario.model.ModelService;
import com.soartech.simjr.scripting.ScriptRunSettings;
import com.soartech.simjr.scripting.ScriptRunner;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.sim.entities.AbstractPolygon;
import com.soartech.simjr.ui.cheatsheets.CheatSheetView;
import com.soartech.simjr.ui.pvd.MapImage;
import com.soartech.simjr.ui.pvd.PlanViewDisplayProvider;

/**
 * @author ray
 */
public class ScenarioLoader
{
    private static final Logger logger = Logger.getLogger(ScenarioLoader.class);
    private final ServiceManager services;
    
    private ModelService model;
    
    public ScenarioLoader(ServiceManager services)
    {
        this.services = services;
    }
    
    /**
     * Load a scenario from a file
     * 
     * @param file the Sim Jr XML scenario file (*.sjx)
     * @param progress
     * @throws SimulationException
     */
    public void loadScenario(File file, ProgressMonitor progress) throws SimulationException
    {
        this.model = services.findService(ModelService.class);
        if(this.model == null)
        {
            this.model = new ModelService(services);
            services.addService(this.model);
        }
        try
        {
            logger.info("Loading scenario from '" + file + "'");
            model.getModel().load(file);
            initializeTerrain(progress);
            runPrePostLoadScript(progress, model.getModel().getPreLoadScript(), "pre");
            loadEntities(progress);
            runPrePostLoadScript(progress, model.getModel().getPostLoadScript(), "post");
            loadCheatSheet();
            logger.info("Finished loading scenario.");
        }
        catch (ModelException e)
        {
            throw new SimulationException(e);
        }
        finally
        {
            this.model = null;
        }
    }
    
    /**
     * Load a scenario from a pre-existing scenario model
     * 
     * @param model the model
     * @param progress 
     * @throws SimulationException
     */
    public void loadScenario(ProgressMonitor progress) throws SimulationException
    {
        this.model = services.findService(ModelService.class);
        try
        {
            logger.info("Loading scenario from '" + model.getModel().getFile() + "'");
            initializeTerrain(progress);
            runPrePostLoadScript(progress, model.getModel().getPreLoadScript(), "pre");
            loadEntities(progress);
            runPrePostLoadScript(progress, model.getModel().getPostLoadScript(), "post");
            loadCheatSheet();
            logger.info("Finished loading scenario.");
        }
        finally
        {
            this.model = null;
        }
    }
    
    private void initializeTerrain(ProgressMonitor progress)
    {
        final Geodetic.Point origin = new Geodetic.Point();
        origin.latitude = Math.toRadians(model.getModel().getTerrain().getOriginLatitude());
        origin.longitude = Math.toRadians(model.getModel().getTerrain().getOriginLongitude());
        final Simulation sim = services.findService(Simulation.class);
        
        final TerrainTypeElement tte = model.getModel().getTerrain().getTerrainType();
        File href = null;
        if (tte != null && tte.hasTerrainType())
        {
        	href = tte.getTerrainTypeFile();
        }
        
        // Set the origin of the terrain (for coordinate conversions)
        // and the filename for the terrain type map.
        DetailedTerrain detailedTerrain = new DetailedTerrain(origin, href);
        sim.setTerrain(detailedTerrain);
        
        loadTerrainImages(sim, detailedTerrain);
    }

    private void loadTerrainImages(Simulation sim, DetailedTerrain detailedTerrain)
    {
        final TerrainImageElement tie = model.getModel().getTerrain().getImage();
        if (!tie.hasImage())
        {
            return;
        }
        
        final File href = tie.getImageFile();
        logger.info("Using map image from '" + href + "'");
        final Vector3 origin = sim.getTerrain().fromGeodetic(tie.getLocation().toRadians());
        detailedTerrain.setCoordinateFrame(origin, tie.getImageMetersPerPixel());
        
        final PlanViewDisplayProvider pvdPro = services.findService(PlanViewDisplayProvider.class);
        if (pvdPro != null && pvdPro.getActivePlanViewDisplay() != null)
        {
        	final MapImage image = new MapImage(href, origin, tie.getImageMetersPerPixel());
            pvdPro.getActivePlanViewDisplay().setMapImage(image);
        }
        
        BufferedImage terrainImage = detailedTerrain.getTerrainImage();
        if (terrainImage == null)
        {
            return;
        }
        
        logger.info("Using terrain image.");
        
        if (pvdPro != null && pvdPro.getActivePlanViewDisplay() != null)
        {
            MapImage mi = pvdPro.getActivePlanViewDisplay().getMapImage();
            mi.setCenterMeters(1, origin);
            mi.setMetersPerPixel(1, tie.getImageMetersPerPixel());
            mi.setImage(1, terrainImage);
            mi.setName(1, "terrain");
        }
    }

    private String getScriptPath(String name)
    {
        return model.getModel().getFile() + "#" + name;
    }
    
    private String getScriptPath(EntityElement element)
    {
        return element.getModel().getFile() + "#" + element.getName();
    }
    
    private void runPrePostLoadScript(ProgressMonitor progress, ScriptBlockElement scriptBlock, String type) throws SimulationException
    {
        progress.subTask("Running scenario " + type + "-load script");
        final ScriptRunner runner = services.findService(ScriptRunner.class);
        // TODO check script type
        final String script = scriptBlock.getText().trim();
        if(script.length() != 0)
        {
            try
            {
                ScriptRunSettings.builder().
                    progress(progress).
                    reader(new StringReader(script)).
                    path(getScriptPath(type)).
                    pushFile(model.getModel().getFile()). // make sure relative file refs work
                    run(runner);
            }
            catch (Exception e)
            {
                throw new SimulationException(e);
            }
        }
    }
    
    private void loadEntities(ProgressMonitor progress) throws SimulationException
    {
        final List<EntityElement> entities = model.getModel().getEntities().getEntities();
        int current = 1;
        for(EntityElement ee : entities)
        {
            progress.subTask(String.format("Loading entity '%s' (%d / %d)", ee.getName(), current, entities.size()));
            loadEntity(ee);
            current++;
        }
    }
    
    private void loadEntity(EntityElement ee) throws SimulationException
    {
        final Simulation sim = services.findService(Simulation.class);
        final EntityPrototypeDatabase db = EntityPrototypeDatabase.findService(services);
        final EntityPrototype prototype = db.getPrototype(ee.getPrototype());
        if(prototype == null)
        {
            throw new SimulationException(String.format("Unknown entity prototype '%s' for entity '%s'", ee.getPrototype(), ee.getName()));
        }
        
        final Entity entity = prototype.createEntity(ee.getName());
        
        // Visible
        EntityTools.setVisible(entity, ee.isVisible());
        
        // Force
        entity.setProperty(EntityConstants.PROPERTY_FORCE, ee.getForce());
        
        // Position
        entity.setPosition(sim.getTerrain().fromGeodetic(ee.getLocation().toRadians()));
        
        // Orientation
        entity.setHeading(Angles.navRadiansToMathRadians(Math.toRadians(ee.getOrientation().getHeading())));
        entity.setPitch(Math.toRadians(ee.getOrientation().getPitch()));
        entity.setRoll(Math.toRadians(ee.getOrientation().getRoll()));
        
        // Points (for routes, areas, etc)
        final AbstractPolygon polygon = Adaptables.adapt(entity, AbstractPolygon.class);
        if(polygon != null)
        {
            polygon.setPointNames(ee.getPoints().getPoints());
        }
        
        //3D data stuff
        entity.setProperty(EntityConstants.PROPERTY_SHAPE_WIDTH_PIXELS, 5); // make
        entity.setProperty(EntityConstants.PROPERTY_MINALTITUDE, ee.getThreeDData().getMinAltitude());
        entity.setProperty(EntityConstants.PROPERTY_MAXALTITUDE, ee.getThreeDData().getMaxAltitude());
        entity.setProperty(EntityConstants.PROPERTY_SHAPE_WIDTH_METERS, ee.getThreeDData().getRouteWidth());
        entity.setProperty(EntityConstants.PROPERTY_3DData, ee.getThreeDData().get3dSupported());
        runEntityInitScript(ee, entity);
        
        sim.addEntity(entity);
    }
    
    private void runEntityInitScript(EntityElement element, Entity entity) throws SimulationException
    {
        final ScriptRunner runner = services.findService(ScriptRunner.class);
        // TODO check script type
        String script = element.getInitScript().getText();
        if(script.length() != 0)
        {
            try
            {
                ScriptRunSettings.builder().
                reader(new StringReader(script)).
                path(getScriptPath(element)).
                pushFile(model.getModel().getFile()). // make sure relative file refs work
                property("self", entity).
                run(runner);
            }
            catch (Exception e)
            {
                throw new SimulationException(e.getMessage(), e);
            }
        }
    }

    private void loadCheatSheet()
    {
        if(model.getModel().getFile() == null)
        {
            return;
        }
        
        final File cheatSheetFile = new File(model.getModel().getFile().getParentFile(), "cheatsheet.html");
        if(cheatSheetFile.exists())
        {
            final CheatSheetView csv = CheatSheetView.findService(services);
            if(csv != null)
            {
                csv.createCheatSheet("Cheat Sheet", cheatSheetFile.getAbsolutePath());
            }
        }
    }
}
