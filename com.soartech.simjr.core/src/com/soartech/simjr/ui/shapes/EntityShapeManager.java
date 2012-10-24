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
 * Created on May 22, 2007
 */
package com.soartech.simjr.ui.shapes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.Shape;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.swing.SwingPrimitiveRendererFactory;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.SimulationListenerAdapter;
import com.soartech.simjr.ui.SimulationImages;

/**
 * Manages the display of shapes for entities in the system on a particular
 * PVD.
 * 
 * @author ray
 */
public class EntityShapeManager
{
    private static final Logger logger = Logger.getLogger(EntityShapeManager.class);
    
    private static final String HIGHLIGHT_ID = "simjr.pvd.highlight";

    private Simulation simulation;
    private ShapeSystem shapeSystem;
    private SwingPrimitiveRendererFactory shapeFactory;
    private Listener listener = new Listener();
    private TimedShapeManager timedShapes;
    private Map<Entity, EntityShape> shapes = new HashMap<Entity, EntityShape>();
    private List<String> selectionIds = new ArrayList<String>();
    private Set<String> decorationIds = new HashSet<String>();
    
    // This has to be a list to get Z-order to behave consistently for entities in
    // the same layer
    private List<Entity> addedEntities = Collections.synchronizedList(new ArrayList<Entity>());
    private Set<Entity> removedEntities = Collections.synchronizedSet(new HashSet<Entity>());
    
    static final ImageIcon explosion = SimulationImages.loadImageFromJar("simjr/images/shapes/explosion.gif");
    
    private static final Map<Object, EntityShapeFactory> factory = new HashMap<Object, EntityShapeFactory>();
    static
    {
        factory.put(AreaShape.NAME, AreaShape.FACTORY);
        factory.put(DismountedInfantryShape.NAME, DismountedInfantryShape.FACTORY);
        factory.put(MissileShape.NAME, MissileShape.FACTORY);
        factory.put(TankShape.NAME, TankShape.FACTORY);
        factory.put(TruckShape.NAME, TruckShape.FACTORY);
        factory.put(WaypointShape.NAME, WaypointShape.FACTORY);
        factory.put(CircularRegionShape.NAME, CircularRegionShape.FACTORY);
        factory.put(RouteShape.NAME, RouteShape.FACTORY);
    }
    
    public EntityShapeManager(Simulation simulation, ShapeSystem shapeSystem, 
                              SwingPrimitiveRendererFactory shapeFactory)
    {
        if(simulation == null)
        {
            throw new IllegalArgumentException("simulation must not be null");
        }
        this.simulation = simulation;
        this.shapeSystem = shapeSystem;
        this.shapeFactory = shapeFactory;
        this.timedShapes = new TimedShapeManager(shapeSystem);
        
        initializeLayerOrder();
        
        synchronized (simulation.getLock())
        {
            for(Entity e : simulation.getEntitiesFast())
            {
                listener.onEntityAdded(e);
            }
        }
        
        simulation.addListener(listener);
        
        shapeFactory.addImage("explosion", explosion.getImage());
        shapeFactory.addImage("speechBubble", SimulationImages.SPEECH_BUBBLE.getImage());
    }

    public void dispose()
    {
        simulation.removeListener(listener);
    }
    
    /**
     * @return the shapeSystem
     */
    public ShapeSystem getShapeSystem()
    {
        return shapeSystem;
    }

    /**
     * @return the timedShapes
     */
    public TimedShapeManager getTimedShapes()
    {
        return timedShapes;
    }

    public EntityShape getEntityShape(Entity e)
    {
        return shapes.get(e);
    }
    
    private void initializeLayerOrder()
    {
        shapeSystem.getLayer(EntityConstants.LAYER_AREA).setZorder(0);
        shapeSystem.getLayer(EntityConstants.LAYER_ROUTE).setZorder(10);
        shapeSystem.getLayer(EntityConstants.LAYER_WAYPOINT).setZorder(20);
        shapeSystem.getLayer(EntityConstants.LAYER_GROUND).setZorder(30);
        shapeSystem.getLayer(EntityConstants.LAYER_SHADOWS).setZorder(50);
        shapeSystem.getLayer(EntityConstants.LAYER_AIR).setZorder(60);
        shapeSystem.getLayer(EntityConstants.LAYER_SELECTION).setZorder(70);
        shapeSystem.getLayer(EntityConstants.LAYER_LABELS).setZorder(80);
    }
    
    public void update()
    {
        String selectionRemoved = "";
        synchronized(removedEntities)
        {
            for(Entity e : removedEntities)
            {
                entityRemoved(e);
                for(String selectionId : selectionIds)
                {
                    //When we remove something that is selected we should remove the selection shape as well!
                    if(selectionId.equals("selection." + e.getName()))
                    {
                        selectionRemoved = e.getName();
                        shapeSystem.removeShape(selectionId);
                    }
                }
            }
            removedEntities.clear();
        }
        synchronized(addedEntities)
        {
            for(Entity e : addedEntities)
            {
                entityAdded(e);
                //If we re-add something that has just been removed we should assume that the shape has changed, but it is still selected
                if(e.getName().equals(selectionRemoved))
                {
                    createSelection(e);
                }
            }
            addedEntities.clear();
        }
        
        // This assumes that the sim lock is held when update() is called
        for(Entity e : simulation.getEntitiesFast())
        {
            updateEntity(e);
        }
        
        timedShapes.update(simulation.getTime());    
    }
    
    public void updateSelection(List<Entity> entities)
    {
        for(String selectionId : selectionIds)
        {
            shapeSystem.removeShape(selectionId);
        }
        selectionIds.clear();
        
        for(Entity e : entities)
        {
            if(e != null && EntityTools.isVisible(e))
            {
                createSelection(e);
            }
        }
    }
    
    private Shape createSelection(Entity selected)
    {
        final String id = "selection." + selected.getName();
        
        final EntityShapeFactory factory = getShapeFactory(selected);
        if(factory == null || factory.equals(NullShapeFactory.FACTORY))
        {
            return null;
        }
        
        factory.initialize(this.shapeFactory);
        final Shape selection = factory.createSelection(id, selected);
        shapeSystem.addShape(selection);

        selectionIds.add(id);
        return selection;
    }
    
    /**
     * Set the primary highlight shape to be centered on the specified entity
     * and to use the default highlight color.
     * 
     * @param entity
     */
    public void highlightEntity(Entity entity)
    {
        highlightEntity(entity, null);
        
    }
    
    /**
     * Set the primary highlight shape to be centered on the specified entity
     * and to use the specified highlight color.
     * 
     * @param entity
     * @param color
     */
    public void highlightEntity(Entity entity, Color color)
    {
        shapeSystem.removeShape(HIGHLIGHT_ID);
        if(entity != null && EntityTools.isVisible(entity))
        {
            createHighlight(entity, HIGHLIGHT_ID, color);
        }
    }

    private Shape createHighlight(Entity selected, String highlightId, Color color)
    {
        final EntityShapeFactory factory = getShapeFactory(selected);
        if(factory == null || factory.equals(NullShapeFactory.FACTORY))
        {
            return null;
        }
        
        factory.initialize(this.shapeFactory);
        
        final Shape selection = factory.createSelection(highlightId, selected);
        ShapeStyle style = selection.getStyle();
        
        style.setOpacity(selection.getStyle().getOpacity() / 2.0f);
        if (color != null)
        {
            style.setFillColor(color);
            style.setLineColor(color.darker());
        }
        shapeSystem.addShape(selection);

        return selection;
    }
    
    public void addHighlightDecoration(Entity e, String suffix, Color color)
    {
        Shape highlight = createHighlight(e, HIGHLIGHT_ID + "." + suffix, color);
        addDecoration(e, highlight);
    }
    
    public void addDecoration(Entity e, Shape shape)
    {
        shape.setPosition(new Position(shapes.get(e).getPrimaryDisplayShape()));
        String id = shape.getName();
        if (shapeSystem.getShape(id) != null)
        {
            shapeSystem.removeShape(id);
        }
        shapeSystem.addShape(shape);
        decorationIds.add(id);
    }
    
    public void clearDecorations()
    {
        for (String decorationId : decorationIds)
        {
            shapeSystem.removeShape(decorationId);
        }
        decorationIds.clear();
    }
    
    public List<Entity> getEntitiesAtScreenPoint(double x, double y, double tolerance)
    {
        List<Entity> r = new ArrayList<Entity>();
        List<Entity> routes = new ArrayList<Entity>();
        List<Entity> areas = new ArrayList<Entity>();
        for(EntityShape es : shapes.values())
        {
            if(es.hitTest(x, y, tolerance))
            {
                final Entity e = es.getEntity();
                final EntityPrototype proto = e.getPrototype();
                if(proto.hasSubcategory("route"))
                {
                    routes.add(e);
                }
                else if(proto.hasSubcategory("area"))
                {
                    areas.add(e);
                }
                else
                {
                    r.add(e);
                }
            }
        }
        r.addAll(routes);
        r.addAll(areas);
        return r;
    }
    
    private void updateEntity(Entity e)
    {
        EntityShape shape = shapes.get(e);
        if(shape == null)
        {
            return;
        }
        
        shape.update();
    }
    
    private EntityShapeFactory getShapeFactory(Entity e)
    {
        Object shapeName = e.getProperty(EntityConstants.PROPERTY_SHAPE);
        
        EntityShapeFactory f = null;
        if(shapeName instanceof EntityShapeFactory)
        {
            f = (EntityShapeFactory) shapeName;
        }
        else
        {
            f = factory.get(shapeName);
            if(f == null)
            {
                logger.error("Could not create shape for entity '" + e + "' with shape property " + shapeName);
                return null;
            }
        }
        return f;
    }
    
    private void entityAdded(Entity e)
    {
        final EntityShapeFactory f = getShapeFactory(e);
        if(f == null)
        {
            return;
        }
        
        f.initialize(shapeFactory);
        final EntityShape shape = f.create(e, shapeSystem);
        if(shape == null)
        {
            logger.error("Shape factory '" + f + "' returned null shape for '" + e + "'");
            return;
        }
        
        shapes.put(e, shape);
    }

    private void entityRemoved(Entity e)
    {
        EntityShape shape = shapes.remove(e);
        if(shape != null)
        {
            shape.remove();
        }
    }
    
    private class Listener extends SimulationListenerAdapter
    {
        /* (non-Javadoc)
         * @see com.soartech.simjr.SimulationAdapter#onEntityAdded(com.soartech.simjr.Entity)
         */
        @Override
        public void onEntityAdded(Entity e)
        {
            addedEntities.add(e);
        }

        /* (non-Javadoc)
         * @see com.soartech.simjr.SimulationAdapter#onEntityRemoved(com.soartech.simjr.Entity)
         */
        @Override
        public void onEntityRemoved(Entity e)
        {
            removedEntities.add(e);
        }
    }
    
}
