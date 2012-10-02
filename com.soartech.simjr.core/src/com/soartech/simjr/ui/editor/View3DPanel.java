
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
 * Created on June 27, 2011
 */
package com.soartech.simjr.ui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.HashMap;

import javax.swing.JPanel;

import com.soartech.simjr.scenario.EntityElement;
import com.soartech.simjr.scenario.EntityElementList;
import com.soartech.simjr.scenario.LocationElement;
import com.soartech.simjr.scenario.model.*;
import com.soartech.simjr.scenario.PointElementList;
import com.soartech.simjr.scenario.TerrainElement;
import com.soartech.simjr.scenario.TerrainImageElement;
import com.soartech.simjr.scenario.ThreeDDataElement;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.sim.Detonation;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.SimulationListener;

import de.jreality.jogl.Viewer;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.CommonAttributes;
import de.jreality.toolsystem.ToolSystem;
import de.jreality.util.CameraUtility;
import de.jreality.util.RenderTrigger;

/**
 * @author Dan Silverglate
 */
public class View3DPanel extends JPanel implements ModelChangeListener, SimulationListener
{
    private static final long serialVersionUID = -4534167209676146675L;
    private static final String EDITOR_ENTITY_PROP = MapPanel.class.getCanonicalName() + ".editorEntity";
    private final Model model;
    private final Simulation sim;
    private final SceneGraphComponent constructs;
    private final HashMap<EntityElement, AbstractConstruct> map = new HashMap<EntityElement, AbstractConstruct>();
    private final Grid grid;
    private final ImagePoly imagePoly;
    private ServiceManager services;
    Viewer viewer;
    
    
    public View3DPanel(ServiceManager services)
    {
        super(new BorderLayout());

        this.model =  services.findService(ModelService.class).getModel();
        model.addModelChangeListener(this);
        this.services = services;
        this.sim = services.findService(Simulation.class);
        sim.addListener(this);

        SceneGraphComponent rootNode = new SceneGraphComponent("root");
        SceneGraphComponent cameraNode = new SceneGraphComponent("camera");
        constructs = new SceneGraphComponent("constructs");
        SceneGraphComponent lightNode = new SceneGraphComponent("light");

        rootNode.addChild(constructs);
        rootNode.addChild(cameraNode);

        grid = new Grid(1000, 1000, 100);
        constructs.addChild(grid);
        imagePoly = new ImagePoly();
        constructs.addChild(imagePoly);
        cameraNode.addChild(lightNode);
        
        Light dl = new DirectionalLight();
        lightNode.setLight(dl);
   

        MatrixBuilder.euclidean().translate(0,500,500).rotateY(0).rotateX(-Math.PI*0.25).assignTo(cameraNode);

        Appearance rootApp = new Appearance();
        rootApp.setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(.9f, .9f, .9f));
        rootApp.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(.5f, .5f, .5f));
        rootNode.setAppearance(rootApp);
            
        Camera camera = new Camera();
        camera.setNear(1);
        camera.setFar(100000);
        cameraNode.setCamera(camera);
        SceneGraphPath camPath = new SceneGraphPath(rootNode, cameraNode);
        camPath.push(camera);
        
        Viewer viewer = new Viewer();
        
        viewer.setSceneRoot(rootNode);
        viewer.setCameraPath(camPath);
        ToolSystem toolSystem = ToolSystem.toolSystemForViewer(viewer);
        toolSystem.initializeSceneTools();
        
        add((Component)viewer.getViewingComponent(), BorderLayout.CENTER);
        
        SimNavigationTool t = new SimNavigationTool();
        t.setGain(400);
        t.setGravitEnabled(false);
        t.setMinHeight(10);
        t.setRunFactor(4);
        cameraNode.addTool(t);
        
        RenderTrigger rt = new RenderTrigger();
        rt.addSceneGraphComponent(rootNode);
        rt.addViewer(viewer);
    }
    
    
    public View3DPanel(ScenarioEditorServiceManager app)
    {
        super(new BorderLayout());

        this.model = app.getModel();
        app.getModel().addModelChangeListener(this);

        this.sim = app.findService(Simulation.class);
        sim.addListener(this);

        SceneGraphComponent rootNode = new SceneGraphComponent("root");
        SceneGraphComponent cameraNode = new SceneGraphComponent("camera");
        constructs = new SceneGraphComponent("constructs");
        SceneGraphComponent lightNode = new SceneGraphComponent("light");

        rootNode.addChild(constructs);
        rootNode.addChild(cameraNode);

        grid = new Grid(50, 50, 100);
        constructs.addChild(grid);
        imagePoly = new ImagePoly();
        constructs.addChild(imagePoly);
        cameraNode.addChild(lightNode);

        Light dl = new DirectionalLight();
        lightNode.setLight(dl);


        MatrixBuilder.euclidean().translate(0,500,500).rotateY(0).rotateX(-Math.PI*0.25).assignTo(cameraNode);

        Appearance rootApp = new Appearance();
        rootApp.setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(.9f, .9f, .9f));
        rootApp.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(.5f, .5f, .5f));
        rootNode.setAppearance(rootApp);

        Camera camera = new Camera();
        camera.setNear(1);
        camera.setFar(100000);
        cameraNode.setCamera(camera);
        SceneGraphPath camPath = new SceneGraphPath(rootNode, cameraNode);
        camPath.push(camera);

        viewer = new Viewer();

        viewer.setSceneRoot(rootNode);
        viewer.setCameraPath(camPath);
        ToolSystem toolSystem = ToolSystem.toolSystemForViewer(viewer);
        toolSystem.initializeSceneTools();

        add((Component)viewer.getViewingComponent(), BorderLayout.CENTER);

        SimNavigationTool t = new SimNavigationTool();
        t.setGain(400);
        t.setGravitEnabled(false);
        t.setMinHeight(10);
        t.setRunFactor(4);
        cameraNode.addTool(t);

        RenderTrigger rt = new RenderTrigger();
        rt.addSceneGraphComponent(rootNode);
        rt.addViewer(viewer);
    }

    private Entity getSimEntity(EntityElement ee)
    {
        /*The previous way of getting the entity given entityelement by looking for a property does not interop with
         *  the tigerboard editor.  We can assume that entity names SHOULD have unique names.
         */
        for (Entity e : sim.getEntities())
        {
            //if(ee == e.getProperty(EDITOR_ENTITY_PROP))
            if (ee.getName().equals(e.getName()))
            {
                return e;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.scenario.ModelChangeListener#onModelChanged(com.soartech.simjr.scenario.ModelChangeEvent)
     */
    public void onModelChanged(ModelChangeEvent e)
    {
        //System.out.println("onModelChanged("+e.property+") source("+e.source.getClass().getName()+") sourceID("+e.source.hashCode()+")");
        //System.out.println("onModelChanged["+EDITOR_ENTITY_PROP);
        //System.out.println("  model("+e.model.getEntities().getEntities().size()+")");
        //System.out.println("  source("+e.source.getClass().getName()+")");
        //System.out.println("  property("+e.property+")");
        //if (e.source instanceof ModelElement)
        //{
        //    ModelElement me = (ModelElement)e.source;
        //    System.out.println("  modelElement("+me+")");
        //}
        //System.out.println("]");
        EntityElement ee = (e.source instanceof EntityElement) ? (EntityElement)e.source : null;

        // file loaded
        if (e.property.equals(Model.LOADED) || e.property.equals(Model.FILE))
        {
            rebuildScene();
        }

        // new entity add by user
        else if (e.property.equals(EntityElementList.ENTITY_ADDED))
        {
            add3DConstruct(ee, null);
        }

        // entity removed by user
        else if (e.property.equals(EntityElementList.ENTITY_REMOVED))
        {
            AbstractConstruct construct = map.get(ee);
            if (construct != null)
            {
                constructs.removeChild(construct);
                map.remove(ee);
            }
        }

        // update to some entity's list of points
        else if (e.property.equals(PointElementList.POINTS))
        {
            PointElementList points = (PointElementList)e.source;
            ModelElement parent = points.getParent();
            if (parent != null)
            {
                updateConstruct((EntityElement)parent);
            }
        }

        // update to the location of an entity
        else if(e.property.equals(LocationElement.LOCATION))
        {
            // what kind of entity has a new location
            ModelElement parent = ((ModelElement)e.source).getParent();

            //System.out.println("location("+parent+")");

            // location of an entity has changed
            if (parent instanceof EntityElement)
            {
                Entity entity = getSimEntity((EntityElement)parent);
                if (entity != null)
                {
                    // if the location belongs to a waypoint
                    if (entity.getPrototype().getCategory().equals("waypoint"))
                    {
                        //see if any of the constructs reference this waypoint
                        for (AbstractConstruct construct : map.values())
                        {
                            construct.testAndUpdateFromEntity(entity);
                        }
                    }
    
                    // if the location belongs to a cylinder
                    else if (entity.getPrototype().getCategory().equals("cylinder"))
                    {
                        updateConstruct((EntityElement)parent);
                    }
                }
            }

            // location of the terrain image has changed
            else if (parent instanceof TerrainImageElement)
            {
                TerrainImageEntity tie = (TerrainImageEntity)sim.getEntity("@@@___TERRAIN___@@@");
                imagePoly.setPosition(tie.getPosition());
            }
        }

        // handle terrain
        else if (e.property.equals(TerrainImageElement.HREF))
        {
            File f = ((TerrainImageElement)e.source).getImageFile();
            imagePoly.setImageFile(f.getPath());
        }

        // remove terrain image
        else if (e.property.equals(TerrainImageElement.REMOVED))
        {
            //grid.setImageFile(null);
            imagePoly.setImageFile(null);
        }

        else if (e.property.equals(TerrainImageElement.METERS_PER_PIXEL))
        {
            imagePoly.setMetersPerPixel(((TerrainImageElement)e.source).getImageMetersPerPixel());
        }

        // update to an entity's 3D data
        else if (e.property.equals(ThreeDDataElement.THREEDDATA))
        {
            // right now this does not appear to be needed
            // update3DData(e.source);
        }

        // the prototype for an entity is changed entity's
        else if (e.property.equals(EntityElement.PROTOTYPE))
        {
            //updateConstruct(ee);
            //if the prototype is changed, just punt and rebuild the entire scene
            rebuildScene();
        }
        
        else if (e.property.equals(EntityElement.NAME))
        {
            updateConstruct(ee);
        }
    }

    private boolean add3DConstruct(EntityElement ee, Entity entity)
    {
        try {
            String id = (entity == null)? ee.getPrototype(): entity.getPrototype().getCategory();
            AbstractConstruct construct = null;

            //System.out.println("add3DConstruct("+id+")");
            if (id.equals("cylinder")) {
                construct = new Cylinder(sim);
            }
            else if (id.equals("area")) {
                construct = new Area(sim);
            }
            else if (id.equals("route")) {
                construct = new Route(sim);
            }

            if (construct != null)
            {
              //System.out.println("handled("+id+")");
              constructs.addChild(construct);
              map.put(ee, construct);
              if (entity != null)
              {
                  construct.updateFromEntity(entity);
              }
              return true;
            }

            //System.out.println("No 3D Construct for type("+id+")");
        } catch (Exception ex) { ex.printStackTrace(); }

        return false;
    }

    /*private boolean _add3DConstruct(EntityElement ee, Entity entity)
    {
        try {
            String id = (entity == null)? ee.getPrototype(): entity.getPrototype().getId();
            AbstractConstruct construct = null;
            boolean secondPassAllowed = false;

            // allow a second pass trying the prototype parent if the entity is not null
            do
            {
                //System.out.println("add3DConstruct("+id+")");
                if (id.equals("cylinder")) {
                    construct = new Cylinder(sim);
                }
                else if (id.equals("area")) {
                    construct = new Area(sim);
                }
                else if (id.equals("route")) {
                    construct = new Route(sim);
                }

                if (construct != null)
                {
                  //System.out.println("handled("+id+")");
                  constructs.addChild(construct);
                  map.put(ee, construct);
                  if (entity != null) construct.updateFromEntity(entity);
                  return true;
                }

                if (entity != null && entity.getPrototype().getParent() != null)
                {
                    id = entity.getPrototype().getParent().getId();
                    secondPassAllowed = !secondPassAllowed;
                }
            } while (secondPassAllowed);

            //System.out.println("No 3D Construct for type("+id+")");
        } catch (Exception ex) { ex.printStackTrace(); }

        return false;
    }*/

    private void rebuildScene()
    {
        for (AbstractConstruct construct : map.values())
        {
            constructs.removeChild(construct);
        }
        map.clear();
        imagePoly.setImageFile(null);

        TerrainImageEntity imageEntity = (TerrainImageEntity)sim.getEntity("@@@___TERRAIN___@@@");
        if (imageEntity != null)
        {
          imagePoly.setPosition(imageEntity.getPosition());
        }
        TerrainImageElement imageElement = model.getTerrain().getImage();
        imagePoly.setMetersPerPixel(imageElement.getImageMetersPerPixel());
        if (imageElement.hasImage())
        {
            File f = imageElement.getImageFile();
            imagePoly.setImageFile(f.getPath());
        }

        for (Entity entity : sim.getEntities())
        {
            add3DConstruct((EntityElement)entity.getProperty(EDITOR_ENTITY_PROP), entity);
        }
    }

    // this method is not currently used
    public boolean update3DData(Object source)
    {
        ThreeDDataElement data = (ThreeDDataElement)source;
        EntityElement ee = data.getEntity();
        System.out.println("3D entity("+ee+") ("+ee.hashCode()+")");
        Object obj = map.get(ee);
        if (obj != null && obj instanceof AbstractConstruct)
        {
            Entity entity = getSimEntity((EntityElement)ee);
            AbstractConstruct construct = (AbstractConstruct)obj;
            construct.updateFromEntity(entity);
            return true;
        }

        return false;
    }

    public boolean updateConstruct(EntityElement ee)
    {
        AbstractConstruct construct = map.get(ee);
        if (construct != null)
        {
            Entity entity = getSimEntity(ee);
            if (entity != null)
            {
                construct.updateFromEntity(entity);
                return true;
            }
        }

        return false;
    }
    @Override
    public void onTimeSet(double oldTime)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onEntityAdded(Entity e)
    {
        this.rebuildScene();
    }

    @Override
    public void onEntityRemoved(Entity e)
    {
        
    }

    @Override
    public void onPause()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onStart()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTick(double dt)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDetonation(Detonation detonation)
    {
        // TODO Auto-generated method stub
        
    }
/*    public void onTimeSet(double oldTime) { }
    public void onPause() { }
    public void onStart() { }
    public void onTick(double dt) { }
    public void onDetonation(Detonation detonation) { }

    public void onEntityAdded(Entity e)
    {
        System.out.println("onEntityAdded("+e.getPrototype().getId()+", cat("+e.getPrototype().getCategory()+"), sub("+e.getPrototype().getSubcategory()+"), 3D("+e.getProperty(ThreeDDataElement.THREEDDATA)+"))");
    }

    public void onEntityRemoved(Entity e)
    {
        System.out.println("onEntityRemoved("+e.getClass().getName()+")");
    }*/
}

