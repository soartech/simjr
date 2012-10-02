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
 * Created on Mar 27, 2009
 */
package com.soartech.simjr.ui.editor;

import java.awt.BorderLayout;
import java.awt.Point;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

import com.soartech.math.Angles;
import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.scenario.EntityElement;
import com.soartech.simjr.scenario.EntityElementList;
import com.soartech.simjr.scenario.LocationElement;
import com.soartech.simjr.scenario.OrientationElement;
import com.soartech.simjr.scenario.PointElementList;
import com.soartech.simjr.scenario.TerrainElement;
import com.soartech.simjr.scenario.TerrainImageElement;
import com.soartech.simjr.scenario.ThreeDDataElement;
import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.scenario.model.ModelChangeEvent;
import com.soartech.simjr.scenario.model.ModelChangeListener;
import com.soartech.simjr.scenario.model.ModelElement;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPropertyListener;
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.sim.EntityPrototypeDatabase;
import com.soartech.simjr.sim.SimpleTerrain;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.Terrain;
import com.soartech.simjr.sim.entities.AbstractPolygon;
import com.soartech.simjr.sim.entities.DefaultEntity;
import com.soartech.simjr.sim.entities.DefaultPolygon;
import com.soartech.simjr.ui.ObjectContextMenu;
import com.soartech.simjr.ui.SelectionManager;
import com.soartech.simjr.ui.SelectionManagerListener;
import com.soartech.simjr.ui.SimulationImages;
import com.soartech.simjr.ui.editor.actions.ClearTerrainImageAction;
import com.soartech.simjr.ui.editor.actions.NewEntityAction;
import com.soartech.simjr.ui.editor.actions.SetTerrainImageAction;
import com.soartech.simjr.ui.pvd.MapImage;
import com.soartech.simjr.ui.pvd.PlanViewDisplay;
import com.soartech.simjr.ui.pvd.PlanViewDisplayProvider;

/**
 * @author ray
 * Modified to support the dockable framework  ~ Joshua Haley
 */
public class MapPanel extends DefaultSingleCDockable implements ModelChangeListener, SelectionManagerListener, PlanViewDisplayProvider, TerrainImageListener
{
    private static final long serialVersionUID = -6829507868179277224L;
    
    private static final String EDITOR_ENTITY_PROP = MapPanel.class.getCanonicalName() + ".editorEntity";
    private final ScenarioEditorServiceManager app;
    private final Simulation sim;
    private final PlanViewDisplay pvd;
    private final Set<Entity> movedEntities = new HashSet<Entity>();
    private final EntityPropertiesPanel propsPanel;

    public MapPanel(ScenarioEditorServiceManager app, EntityPropertiesPanel props)
    {
        
        
        super("MapPanel");
        
        this.propsPanel = props;
        
        //DF settings
        setLayout(new BorderLayout());
        setCloseable(true);
        setMinimizable(true);
        setExternalizable(true);
        setMaximizable(true);
        setTitleText("Map Panels");
        setResizeLocked(true);
        setTitleIcon(SimulationImages.PVD);
        
        this.app = app;
        this.sim = app.findService(Simulation.class);
        this.pvd = new PlanViewDisplay(app, null) {

            private static final long serialVersionUID = 2647338467484833244L;

            @Override
            protected void dragFinished()
            {
                super.dragFinished();
                updateEditorEntityPositionsAfterDrag();
            }
        };
        
        this.pvd.setContextMenu(new ObjectContextMenu(app) {

            private static final long serialVersionUID = -5454693942029564642L;

            @Override
            protected List<Action> getAdditionalActions()
            {
                List<Action> actions = super.getAdditionalActions();
                final Point contextPoint = pvd.getContextMenuPoint();
                final Vector3 meters = pvd.getTransformer().screenToMeters((double)contextPoint.x, (double) contextPoint.y);
                final Geodetic.Point lla = sim.getTerrain().toGeodetic(meters);
                actions.add(new NewEntityAction(getActionManager(), "New Entity", "any", lla));
                actions.add(new NewEntityAction(getActionManager(), "New Waypoint", "waypoint", lla));
                actions.add(new NewEntityAction(getActionManager(), "New Route", "route", lla));
                actions.add(new NewEntityAction(getActionManager(), "New Area", "area", lla));
                actions.add(new NewEntityAction(getActionManager(), "New Circular Region", "cylinder", lla));
                actions.add(null);
                actions.add(new SetTerrainImageAction(getActionManager()));
                actions.add(new ClearTerrainImageAction(getActionManager()));
                return actions;
            }});
        
        SelectionManager.findService(app).addListener(this);
        
        add(pvd);
        

        this.app.addService(this); // So PVD actions can access
        this.app.getModel().addModelChangeListener(this);
        
    }
    


    public PlanViewDisplay getActivePlanViewDisplay()
    {
        return pvd;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.adaptables.Adaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class<?> klass)
    {
        return Adaptables.adaptUnchecked(this, klass, false);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#shutdown()
     */
    public void shutdown() throws SimulationException
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#start(com.soartech.simjr.ProgressMonitor)
     */
    public void start(ProgressMonitor progress) throws SimulationException
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.SelectionManagerListener#selectionChanged(java.lang.Object)
     */
    public void selectionChanged(Object source)
    {
        final Object sel = SelectionManager.findService(app).getSelectedObject(); 
        final EntityElement ee = Adaptables.adapt(sel, EntityElement.class);
        propsPanel.setEntity(ee);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.editor.model.ModelChangeListener#onModelChanged(com.soartech.simjr.ui.editor.model.ModelChangeEvent)
     */
    public void onModelChanged(ModelChangeEvent e)
    {

        if(e.property.equals(EntityElementList.ENTITY_ADDED))
        {
            createSimEntityForNewEditorEntity((EntityElement) e.source);
        }
        else if(e.property.equals(EntityElementList.ENTITY_REMOVED))
        {
            destroySimEntityForRemovedEditorEntity((EntityElement) e.source);
            if(e.source == propsPanel.getEntity())
            {
                propsPanel.setEntity(null);
            }
        }
        else if(e.property.equals(LocationElement.LOCATION))
        {
            final ModelElement me = ((LocationElement) e.source).getParent();
            if(me instanceof EntityElement)
            {
                updateSimEntityPosition((EntityElement) me);
            }
            else if(me instanceof TerrainImageElement)
            {
                updateMapImage((TerrainImageElement) me, e.property);
            }
        }
        else if(e.property.equals(OrientationElement.ORIENTATION))
        {
            final EntityElement ee = ((OrientationElement) e.source).getEntity();
            updateSimEntityOrientation(ee);
        }
        else if(e.property.equals(EntityElement.NAME) || e.property.equals(EntityElement.PROTOTYPE))
        {
            // :( We have to destroy and recreate the sim entity for a rename or type change
            destroySimEntityForRemovedEditorEntity((EntityElement) e.source);
            createSimEntityForNewEditorEntity((EntityElement) e.source);
            updateSimEntityPosition((EntityElement) e.source);
        }
        else if(e.property.equals(EntityElement.FORCE))
        {
            updateSimEntityForce((EntityElement) e.source);
        }
        else if(e.property.equals(TerrainElement.ORIGIN))
        {
            updateSimTerrainOrigin();
        }
        else if(e.property.equals(PointElementList.POINTS))
        {
            updateSimEntityPoints(e);
        }
        else if(e.property.equals(Model.LOADED))
        {
            for(Entity simEntity : sim.getEntities())
            {
                sim.removeEntity(simEntity);
            }
            updateSimTerrainOrigin();
            updateMapImage((TerrainImageElement) e.model.getTerrain().getImage(), e.property);
            for(EntityElement ee : e.model.getEntities().getEntities())
            {
                createSimEntityForNewEditorEntity(ee);
            }
        }
        else if(TerrainImageElement.isProperty(e.property))
        {
            updateMapImage((TerrainImageElement) e.source, e.property);
        }
        else if(e.property.equals(ThreeDDataElement.THREEDDATA))
        {
            final EntityElement ee = ((ThreeDDataElement) e.source).getEntity();
            updateSimEntityThreeDData(ee);
        }
    }
    
    private MapImage ensureMapImageExists(TerrainImageElement tie)
    {
        if(pvd.getMapImage() == null)
        {
            final MapImage mi = new MapImage();
            mi.setImage(tie.getImageFile());
            mi.setMetersPerPixel(tie.getImageMetersPerPixel());
            mi.setCenterMeters(sim.getTerrain().fromGeodetic(tie.getLocation().toRadians()));
            pvd.setMapImage(mi);
            final TerrainImageEntity entity = new TerrainImageEntity("@@@___TERRAIN___@@@", this);
            entity.setPosition(mi.getCenterMeters());
            sim.addEntity(entity);
        }
        return pvd.getMapImage();
    }

    private TerrainImageEntity getTerrainImageEntity(TerrainImageElement tie)
    {
        return (TerrainImageEntity) sim.getEntity("@@@___TERRAIN___@@@");
    }
    
    @Override
    public void terrainImageMoved(TerrainImageEntity tie)
    {
        this.pvd.getMapImage().setCenterMeters(tie.getPosition());
        if(pvd.isDraggingEntity())
        {
            movedEntities.add(tie);
        }
    }
    
    private void updateMapImage(TerrainImageElement source, String property)
    {
        if(property.equals(Model.LOADED) && source.hasImage())
        {
            pvd.setMapImage(null);
            ensureMapImageExists(source);
        }
        else if(property.equals(LocationElement.LOCATION))
        {
            // Find map image entity
            final TerrainImageEntity entity = getTerrainImageEntity(source);
            // Update entities position
            entity.setPositionQuietly(sim.getTerrain().fromGeodetic(source.getLocation().toRadians()));
        }
        else if(property.equals(TerrainImageElement.HREF))
        {
            ensureMapImageExists(source).setImage(new File(source.getImageHref()));
        }
        else if(property.equals(TerrainImageElement.METERS_PER_PIXEL))
        {
            ensureMapImageExists(source).setMetersPerPixel(source.getImageMetersPerPixel());
        }
        else if(property.equals(TerrainImageElement.REMOVED))
        {
            sim.removeEntity(getTerrainImageEntity(source));
            pvd.setMapImage(null);
        }
    }

    private void updateSimEntityPoints(ModelChangeEvent e)
    {
        final PointElementList points = (PointElementList) e.source;
        final Entity simEntity = getSimEntity(points.getParent());
        final AbstractPolygon polygon = Adaptables.adapt(simEntity, AbstractPolygon.class);
        if(polygon != null)
        {
            polygon.setPointNames(points.getPoints());
        }
    }

    private void updateSimTerrainOrigin()
    {
        final TerrainElement te = app.getModel().getTerrain();
        Geodetic.Point origin = new Geodetic.Point();
        origin.latitude = Math.toRadians(te.getOriginLatitude());
        origin.longitude = Math.toRadians(te.getOriginLongitude());
        final Terrain newTerrain = new SimpleTerrain(origin);
        sim.setTerrain(newTerrain);
        
        pvd.showPosition(Vector3.ZERO);
    }
    
    private void updateSimEntityForce(final EntityElement ee)
    {
        final Entity simEntity = getSimEntity(ee);
        simEntity.setProperty(EntityConstants.PROPERTY_FORCE, ee.getForce());
    }

    private void updateSimEntityPosition(final EntityElement ee)
    {
        final Entity simEntity = getSimEntity(ee);
        simEntity.setPosition(sim.getTerrain().fromGeodetic(ee.getLocation().toRadians()));
    }
    
    private void updateSimEntityOrientation(final EntityElement ee)
    {
        final Entity simEntity = getSimEntity(ee);
        simEntity.setHeading(Angles.navRadiansToMathRadians(Math.toRadians(ee.getOrientation().getHeading())));
    }
    
    private void updateSimEntityThreeDData(final EntityElement ee)
    {
        final Entity simEntity = getSimEntity(ee);
        simEntity.setProperty(EntityConstants.PROPERTY_MINALTITUDE, ee.getThreeDData().getMinAltitude());
        simEntity.setProperty(EntityConstants.PROPERTY_MAXALTITUDE, ee.getThreeDData().getMaxAltitude());
        simEntity.setProperty(EntityConstants.PROPERTY_SHAPE_WIDTH_METERS, ee.getThreeDData().getRouteWidth());
        simEntity.setProperty(EntityConstants.PROPERTY_3DData, ee.getThreeDData().get3dSupported());
    }
    
    private void destroySimEntityForRemovedEditorEntity(EntityElement source)
    {
        final Entity simEntity = getSimEntity(source);
        sim.removeEntity(simEntity);
    }

    private void createSimEntityForNewEditorEntity(EntityElement source)
    {
        final EntityPrototypeDatabase db = app.findService(EntityPrototypeDatabase.class);
        final EntityPrototype prototype = db.getPrototype(source.getPrototype());
        final DefaultEntity e = new DefaultEntity(source.getName(), prototype) {

            /* (non-Javadoc)
             * @see com.soartech.simjr.sim.entities.AbstractEntity#setPosition(com.soartech.spatr.math.Vector3)
             */
            @Override
            public void setPosition(Vector3 position)
            {
                super.setPosition(position);
                
                if(pvd.isDraggingEntity())
                {
                    movedEntities.add(this);
                }
            }

            /* (non-Javadoc)
             * @see com.soartech.simjr.sim.entities.AbstractEntity#getAdapter(java.lang.Class)
             */
            @Override
            public Object getAdapter(Class<?> klass)
            {
                // Add adaptation to EntityElement so that a right-click in the PVD
                // will show the right actions...
                if(klass.equals(EntityElement.class))
                {
                    return getProperty(EDITOR_ENTITY_PROP);
                }
                return super.getAdapter(klass);
            }
        };
        
        // Decorate the object with a polygon capability so it can be edited with the sim route editor.
        if(prototype.hasSubcategory("polygon"))
        {
            final DefaultPolygon polygon = new DefaultPolygon();
            e.addCapability(polygon);
            polygon.setPointNames(source.getPoints().getPoints());
        }
        e.setPosition(sim.getTerrain().fromGeodetic(source.getLocation().toRadians()));
        e.setHeading(Angles.navRadiansToMathRadians(Math.toRadians(source.getOrientation().getHeading())));
        e.setProperty(EntityConstants.PROPERTY_FORCE, source.getForce());
        e.setProperty(EDITOR_ENTITY_PROP, source);
        e.setProperty(EntityConstants.PROPERTY_SHAPE_WIDTH_PIXELS, 5); // make routes stand out more in editor
        e.setProperty(EntityConstants.PROPERTY_MINALTITUDE, source.getThreeDData().getMinAltitude());
        e.setProperty(EntityConstants.PROPERTY_MAXALTITUDE, source.getThreeDData().getMaxAltitude());
        e.setProperty(EntityConstants.PROPERTY_SHAPE_WIDTH_METERS, source.getThreeDData().getRouteWidth());
        e.setProperty(EntityConstants.PROPERTY_3DData, source.getThreeDData().get3dSupported());
        e.addPropertyListener(new PolygonPointChangeListener());
        sim.addEntity(e);
    }

    private void updateEditorEntityPositionsAfterDrag()
    {
        final CompoundEdit compound = new CompoundEdit();
        for(Entity e : movedEntities)
        {
            final EntityElement ee = getEditorEntity(e);
            if(ee != null)
            {
                final UndoableEdit edit = ee.getLocation().setLocation(sim.getTerrain().toGeodetic(e.getPosition()));
                compound.addEdit(edit);
            }
            else if(e instanceof TerrainImageEntity)
            {
                final Geodetic.Point lla = sim.getTerrain().toGeodetic(e.getPosition());
                UndoableEdit edit = app.getModel().getTerrain().getImage().getLocation().setLocation(lla);
                app.findService(UndoService.class).addEdit(edit);
            }
        }
        compound.end();
        app.findService(UndoService.class).addEdit(compound);
        
        movedEntities.clear();
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
    
    private EntityElement getEditorEntity(Entity e)
    {
        return (EntityElement) e.getProperty(EDITOR_ENTITY_PROP);
    }
    
    private class PolygonPointChangeListener implements EntityPropertyListener
    {
        private boolean active = false;
        
        public void onPropertyChanged(Entity entity, String propertyName)
        {
            if(active)
            {
                return;
            }
            if(!propertyName.equals(EntityConstants.PROPERTY_POINTS))
            {
                return;
            }
            final AbstractPolygon polygon = Adaptables.adapt(entity, AbstractPolygon.class);
            if(polygon == null)
            {
                return;
            }
            final EntityElement ee = getEditorEntity(entity);
            active = true;
            app.findService(UndoService.class).addEdit(ee.getPoints().setPoints(polygon.getPointNames()));
            active = false;
        }  
    }
}
