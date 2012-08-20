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
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JPanel;

import com.soartech.simjr.scenario.EntityElement;
import com.soartech.simjr.scenario.EntityElementList;
import com.soartech.simjr.scenario.LocationElement;
import com.soartech.simjr.scenario.Model;
import com.soartech.simjr.scenario.ModelChangeEvent;
import com.soartech.simjr.scenario.ModelChangeListener;
import com.soartech.simjr.scenario.ModelElement;
import com.soartech.simjr.scenario.PointElementList;
import com.soartech.simjr.sim.Detonation;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.SimulationListener;

import de.jreality.jogl.Viewer;
//import de.jreality.scene.Viewer;
//import de.jreality.soft.DefaultViewer;
//import de.jreality.plugin.JRViewer;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.ClickWheelCameraZoomTool;
import de.jreality.tools.RotateTool;
import de.jreality.tools.ShipNavigationTool;
import de.jreality.tools.ShipRotateTool;
import de.jreality.toolsystem.ToolSystem;
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
    private final HashMap<EntityElement, Object> map = new HashMap<EntityElement, Object>();
    public View3DPanel(ScenarioEditorServiceManager app)    {
        super(new BorderLayout());

        this.model = app.getModel();
        model.addModelChangeListener(this);
        
        this.sim = app.findService(Simulation.class);
        //sim.addListener(this);

        SceneGraphComponent rootNode = new SceneGraphComponent("root");
        SceneGraphComponent cameraNode = new SceneGraphComponent("camera");
        constructs = new SceneGraphComponent("constructs");
        SceneGraphComponent lightNode = new SceneGraphComponent("light");

        rootNode.addChild(constructs);
        rootNode.addChild(cameraNode);
        constructs.addChild(new Grid(50, 50, 100));
        cameraNode.addChild(lightNode);
        
        //double path[][] = new double[][] { {-10, -10}, {-10, 10}, {10, 10}, {10, -10}};
        //constructs.addChild(new Area(path, 10, 20));
        
        Light dl = new DirectionalLight();
        lightNode.setLight(dl);
   
        RotateTool rotateTool = new RotateTool();
        constructs.addTool(rotateTool);

        ClickWheelCameraZoomTool zoomTool = new ClickWheelCameraZoomTool();
        constructs.addTool(zoomTool);
        
        MatrixBuilder.euclidean().translate(0, 250, 1000).assignTo(cameraNode);
        //MatrixBuilder.euclidean().rotate(1.57, 1, 0, 0).assignTo(cameraNode);

        Appearance rootApp = new Appearance();
        rootApp.setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(.9f, .9f, .9f));
        rootApp.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(.5f, .5f, .5f));
        rootNode.setAppearance(rootApp);
            
        Camera camera = new Camera();
        camera.setNear(1);
        camera.setFar(10000);
        cameraNode.setCamera(camera);
        SceneGraphPath camPath = new SceneGraphPath(rootNode, cameraNode);
        camPath.push(camera);
        
        Viewer viewer = new Viewer();
        
        viewer.setSceneRoot(rootNode);
        viewer.setCameraPath(camPath);
        ToolSystem toolSystem = ToolSystem.toolSystemForViewer(viewer);
        toolSystem.initializeSceneTools();
        
        add((Component)viewer.getViewingComponent(), BorderLayout.CENTER);
        
        RenderTrigger rt = new RenderTrigger();
        rt.addSceneGraphComponent(rootNode);
        rt.addViewer(viewer);
    }
    
    private Entity getSimEntity(EntityElement ee)
    {
        for(Entity e : sim.getEntities())
        {
            if(ee == e.getProperty(EDITOR_ENTITY_PROP))
            {
                return e;
            }
        }
        return null;
    }
    
    public void onModelChanged(ModelChangeEvent e)
    {
        System.out.println("onModelChanged["+EDITOR_ENTITY_PROP);
        System.out.println("  model("+e.model.getEntities().getEntities().size()+")");
        System.out.println("  source("+e.source.getClass().getName()+")");
        System.out.println("  property("+e.property+")");
        if (e.source instanceof ModelElement)
        {
            ModelElement me = (ModelElement)e.source;
            System.out.println("  modelElement("+me+")");
        }
        System.out.println("]");
        
        EntityElement ee = (e.source instanceof EntityElement) ? (EntityElement)e.source : null;
        
        if (e.property.equals(EntityElementList.ENTITY_ADDED))
        {
          //if (ee.getPrototype().equals("area")) {
          if (ee.getPrototype().equals("cylinder")) {
              Cylinder cyl = new Cylinder(sim);
              map.put(ee, cyl);
              constructs.addChild(cyl);
          }
          else if (ee.getPrototype().equals("area")) {
              Area area = new Area(sim);
              map.put(ee, area);
              constructs.addChild(area);
          }
          else if (ee.getPrototype().equals("route")) {
              Route route = new Route(sim);
              map.put(ee, route);
              constructs.addChild(route);
          }
        }
        else if (e.property.equals(EntityElementList.ENTITY_REMOVED))
        {
            Object obj = map.get(ee);
            if (obj != null && obj instanceof SceneGraphComponent)
            {
                constructs.removeChild((SceneGraphComponent)obj);
            }
        }
        else if (e.property.equals(PointElementList.POINTS))
        {
            PointElementList points = (PointElementList)e.source;
            ModelElement parent = points.getParent();
            Object obj = map.get(parent);
            
            if (obj != null && obj instanceof AbstractConstruct)
            {
                AbstractConstruct construct = (AbstractConstruct)obj;
                Entity entity = getSimEntity((EntityElement)parent);
                construct.buildFromEntity(entity);
            }
        }
        else if(e.property.equals(LocationElement.LOCATION))
        {
            // need to query all entities affected by the parent element
            ModelElement parent = ((ModelElement)e.source).getParent();
            Entity entity = getSimEntity((EntityElement)parent);
            Iterator<Object> i = map.values().iterator();
            while (i.hasNext())
            {
                Object obj = i.next();
                if (obj instanceof AbstractConstruct)
                {
                    ((AbstractConstruct)obj).testAndUpdateFromEntity(entity);
                }
            }
        }
    }
    
    public void onTimeSet(double oldTime) { }
    public void onPause() { }
    public void onStart() { }
    public void onTick(double dt) { }
    public void onDetonation(Detonation detonation) { }
    
    public void onEntityAdded(Entity e)
    {
        System.out.println("onEntityAdded("+e.getPrototype().getId()+", cat("+e.getPrototype().getCategory()+"), sub("+e.getPrototype().getSubcategory()+"), 3D("+e.getProperty("PROPERTY_3DData")+"))");
        if (true)//e.getProperty("PROPERTY_3DData") != null && e.getProperty("PROPERTY_3DData").equals("true"))
        {
            if (e.getPrototype().getId().equals("area"))
            {
                constructs.addChild(new Area(e, sim));
            }
        }
    }
    
    public void onEntityRemoved(Entity e)
    {
        //System.out.println("onEntityRemoved("+e.getClass().getName()+")");
    }
}
