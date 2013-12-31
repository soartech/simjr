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
package com.soartech.simjr.ui.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import org.apache.log4j.Logger;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.simjr.scenario.EntityElementList;
import com.soartech.simjr.scenario.edits.NewEntityEdit;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.editor.UndoService;
import com.soartech.simjr.ui.pvd.PlanViewDisplay;
import com.soartech.simjr.ui.pvd.PlanViewDisplayProvider;

/**
 * Begins a geometry creation mode that adds and removes points based on user clicks in the PVD.
 * 
 * TODO: Does saving in the middle of this action cause issues?
 */
public class CreateRouteAction extends AbstractEditorAction
{
    private static final Logger logger = Logger.getLogger(NewEntityAction.class);
    private static final long serialVersionUID = 1L;
    
    protected Geodetic.Point initialPosition;
    
    private static final int width= 100, height = 35;
    private static final double SELECTION_TOLERANCE = 15.0;
    private static final int NUM_POINTS = 2;

    private final Simulation sim;
    private final PlanViewDisplay pvd;
    
    private JButton doneButton = new JButton("Done");
    
    //The edit responsible for creating the new geometry
    private NewEntityEdit newGeometryEdit;
    
    //The individual point edits
    private Stack<UndoableEdit> pointEdits;
    
    //Keep the Done button in the center of the PVD
    private ComponentAdapter resizeListener = new ComponentAdapter() {
        public void componentResized(ComponentEvent evt) {
            logger.info("PVD resized: " + evt);
            doneButton.setBounds(pvd.getWidth()/2 - width/2, 10, width, height);
        }
    };
    
    //Controller
    private MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent evt) 
        {
            if(SwingUtilities.isLeftMouseButton(evt)) 
            {
                if(evt.getClickCount() == 2) 
                {
                    onComplete();
                }
                else 
                {
                    Entity selected = getWaypointAt(evt.getX(), evt.getY());
                    if(selected != null) //add existing wp to route
                    {
                        addExistingPoint(selected.getName());
                    }
                    else //create wp at click
                    {
                        addNewPoint(evt.getX(), evt.getY());
                    }
                }
            }
            else if(SwingUtilities.isRightMouseButton(evt))
            {
                removeLastPoint();
            }
        }
    };
    
    /**
     * Handles adding/removing a point to/from a geometric entity.
     */
    private class AddPointEdit extends AbstractUndoableEdit 
    {
        private static final long serialVersionUID = 1L;
        private final String wpName;
        
        public AddPointEdit(String wpName) {
            this.wpName = wpName;
        }
        
        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            addPoint(wpName);
        }
        
        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            List<String> points = newGeometryEdit.getEntity().getPoints().getPoints();
            int i = points.lastIndexOf(wpName);
            if(i != -1) {
                points.remove(i);
            }
            newGeometryEdit.getEntity().getPoints().setPoints(points);
        }
    }
    
    private void addExistingPoint(String wpName)
    {
        AddPointEdit ape = new AddPointEdit(wpName);
        
        addPoint(wpName);
        pointEdits.add(ape);
    }
    
    private void addNewPoint(int x, int y) 
    {
        final Vector3 meters = pvd.getTransformer().screenToMeters((double) x, (double) y);
        final Geodetic.Point lla = sim.getTerrain().toGeodetic(meters);
        CompoundEdit compoundEdit = new CompoundEdit();
        NewEntityEdit addEntityEdit = createWaypoint(lla);
        compoundEdit.addEdit(addEntityEdit);
        compoundEdit.addEdit(new AddPointEdit(addEntityEdit.getEntity().getName()));
        compoundEdit.end();
        
        addPoint(addEntityEdit.getEntity().getName());
        pointEdits.add(compoundEdit);
    }
    
    private void addPoint(String wpName) 
    {
        List<String> points = newGeometryEdit.getEntity().getPoints().getPoints();
        points.add(wpName);
        newGeometryEdit.getEntity().getPoints().setPoints(points);
        
        updateVisibility();
    }
    
    /**
     * Removes the last waypoint added.
     */
    private void removeLastPoint()
    {
        UndoableEdit lastEdit = pointEdits.pop();
        if(lastEdit != null) {
            lastEdit.undo();
        }
        
        updateVisibility();
    }
    
    /**
     * @param manager
     * @param label
     * @param icon
     */
    public CreateRouteAction(ActionManager manager, String label, String keyStroke, Geodetic.Point position)
    {
        super(manager, label);
        logger.debug("Creating CreateRouteAction");
        
        if(keyStroke != null) {
            setAcceleratorKey(KeyStroke.getKeyStroke(keyStroke));
        }
        
        this.initialPosition = position;
        
        PlanViewDisplayProvider pvdProvider = getServices().findService(PlanViewDisplayProvider.class);
        pvd = pvdProvider.getActivePlanViewDisplay();
        sim = getServices().findService(Simulation.class);
        
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onComplete();
            }
        });
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        logger.info("CreateRouteAction - actionPerformed");
        
        setEnabled(false);
        
        doneButton.setBounds(pvd.getWidth()/2 - width/2, 10, width, height);
        pvd.add(doneButton);
        pvd.addComponentListener(resizeListener);
        pvd.addMouseListener(mouseAdapter);
        pvd.setContextMenuEnabled(false);
        
        pvd.repaint();
        
        pointEdits = new Stack<UndoableEdit>();
        newGeometryEdit = getModel().getEntities().addEntity("route", "route");
        updateVisibility();
    }
    
    /**
     * Called when the user is done adding points to the geometry.
     */
    private void onComplete()
    {
        logger.info("CreateRouteAction complete");
        
        final CompoundEdit compoundEdit = new CompoundEdit();
        compoundEdit.addEdit(newGeometryEdit);
        for(UndoableEdit edit: pointEdits) {
            compoundEdit.addEdit(edit);
        }
        compoundEdit.end();
        
        if(pointEdits.size() >= NUM_POINTS) { //TODO: Generalize
            findService(UndoService.class).addEdit(compoundEdit);
        }
        else {
            compoundEdit.undo();
        }
        
        pvd.remove(doneButton);
        pvd.removeComponentListener(resizeListener);
        pvd.removeMouseListener(mouseAdapter);
        pvd.setContextMenuEnabled(true);
        
        CreateRouteAction.this.setEnabled(true);
        
        pvd.repaint();
    }
    
    private void updateVisibility()
    {
        Entity e = sim.getEntity(newGeometryEdit.getEntity().getName());
        boolean showGeometry = newGeometryEdit.getEntity().getPoints().getPoints().size() >= NUM_POINTS; 
        EntityTools.setVisible(e, showGeometry);
    }
    
    /**
     * Creates a waypoint at the given position.
     * @param lla
     */
    private NewEntityEdit createWaypoint(Geodetic.Point lla)
    {
        final EntityElementList entities = getModel().getEntities();
        NewEntityEdit addEntityEdit = entities.addEntity("waypoint", "waypoint"); 
        
        addEntityEdit.getEntity().getLocation().setLocation(Math.toDegrees(lla.latitude), Math.toDegrees(lla.longitude), lla.altitude);
        return addEntityEdit;
    }
    
    /**
     * Gets the first waypoint found at the given screen coords.
     * @param x
     * @param y
     * @return The waypoint at x,y or null if none found.
     */
    private Entity getWaypointAt(double x, double y)
    {
        final List<Entity> entities = pvd.getShapeAdapter().getEntitiesAtScreenPoint(x, y, SELECTION_TOLERANCE);
        Entity selected = null;
        if(!entities.isEmpty()) {
            for(Entity e: entities) {
                if(e.getPrototype().getCategory().equals("waypoint")) {
                    selected = e;
                    break;
                }
            }
        }
        return selected;
    }

    @Override
    public void update() {  }
}
