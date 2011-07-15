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
import com.soartech.simjr.scenario.Model;
import com.soartech.simjr.scenario.ModelException;
import com.soartech.simjr.scenario.ScriptBlockElement;
import com.soartech.simjr.scenario.TerrainImageElement;
import com.soartech.simjr.scenario.TerrainTypeElement;
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
    
    private Model model;
    
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
        this.model = new Model();
        try
        {
            logger.info("Loading scenario from '" + file + "'");
            model.load(file);
            initializeTerrain(progress);
            runPrePostLoadScript(progress, model.getPreLoadScript(), "pre");
            loadEntities(progress);
            runPrePostLoadScript(progress, model.getPostLoadScript(), "post");
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
    public void loadScenario(Model model, ProgressMonitor progress) throws SimulationException
    {
        this.model = model;
        try
        {
            logger.info("Loading scenario from '" + model.getFile() + "'");
            initializeTerrain(progress);
            runPrePostLoadScript(progress, model.getPreLoadScript(), "pre");
            loadEntities(progress);
            runPrePostLoadScript(progress, model.getPostLoadScript(), "post");
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
        origin.latitude = Math.toRadians(model.getTerrain().getOriginLatitude());
        origin.longitude = Math.toRadians(model.getTerrain().getOriginLongitude());
        final Simulation sim = services.findService(Simulation.class);
        
        final TerrainTypeElement tte = model.getTerrain().getTerrainType();
        File href = null;
        if (tte != null && tte.hasTerrainType())
        {
        	href = tte.getTerrainTypeFile();
        }
        
        // Set the origin of the terrain (for coordinate conversions)
        // and the filename for the terrain type map.
        DetailedTerrain detailedTerrain = new DetailedTerrain(origin, href);
        sim.setTerrain(detailedTerrain);
        
        loadTerrainImage(sim, detailedTerrain);
    }

    private void loadTerrainImage(Simulation sim, DetailedTerrain detailedTerrain)
    {
        final TerrainImageElement tie = model.getTerrain().getImage();
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
    }

    private String getScriptPath(String name)
    {
        return model.getFile() + "#" + name;
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
                    pushFile(model.getFile()). // make sure relative file refs work
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
        final List<EntityElement> entities = model.getEntities().getEntities();
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
        entity.setOrientation(Angles.navRadiansToMathRadians(Math.toRadians(ee.getOrientation().getHeading())));
        
        // Points (for routes, areas, etc)
        final AbstractPolygon polygon = Adaptables.adapt(entity, AbstractPolygon.class);
        if(polygon != null)
        {
            polygon.setPointNames(ee.getPoints().getPoints());
        }
        
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
                pushFile(model.getFile()). // make sure relative file refs work
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
        if(model.getFile() == null)
        {
            return;
        }
        
        final File cheatSheetFile = new File(model.getFile().getParentFile(), "cheatsheet.html");
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
