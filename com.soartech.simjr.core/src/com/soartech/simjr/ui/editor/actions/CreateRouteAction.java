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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.undo.CompoundEdit;

import org.apache.log4j.Logger;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.simjr.scenario.EntityElementList;
import com.soartech.simjr.scenario.edits.NewEntityEdit;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.editor.UndoService;
import com.soartech.simjr.ui.pvd.PlanViewDisplay;
import com.soartech.simjr.ui.pvd.PlanViewDisplayProvider;

/**
 * @author ray
 */
public class CreateRouteAction extends AbstractEditorAction
{
    private static final Logger logger = Logger.getLogger(NewEntityAction.class);
    private static final long serialVersionUID = 1L;
    
    protected Geodetic.Point initialPosition;
    
    private static final int width= 100, height = 35;

    private Simulation sim;
    private PlanViewDisplay pvd;
    
    private JButton doneButton = new JButton("Done");
    
    private List<NewEntityEdit> waypointEdits;
    
    private NewEntityEdit currentGeometryEdit;
    
    private ComponentAdapter resizeListener = new ComponentAdapter() {
        public void componentResized(ComponentEvent evt) {
            logger.info("PVD resized: " + evt);
            doneButton.setBounds(pvd.getWidth()/2 - width/2, 10, width, height);
        }
    };
    
    private MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) 
        {
            if(SwingUtilities.isLeftMouseButton(e)) 
            {
                if(e.getClickCount() == 2) 
                {
                    onComplete();
                }
                else 
                {
                    final Vector3 meters = pvd.getTransformer().screenToMeters((double) e.getX(), (double) e.getY());
                    final Geodetic.Point lla = sim.getTerrain().toGeodetic(meters);
                    createWaypoint(lla);
                    if(currentGeometryEdit != null) {
                        currentGeometryEdit.undo();
                    }
                    currentGeometryEdit = createRoute();
                }
            }
            else if(SwingUtilities.isRightMouseButton(e))
            {
                if(!waypointEdits.isEmpty()) {
                    if(currentGeometryEdit != null) {
                        currentGeometryEdit.undo();
                    }
                    NewEntityEdit last = waypointEdits.remove(waypointEdits.size()-1);
                    last.undo();
                    currentGeometryEdit = createRoute();
                }
            }
        }
    };
    
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
        
        waypointEdits = new ArrayList<NewEntityEdit>();
        currentGeometryEdit = null;
    }
    
    /**
     * Called when the user is done graphically creating points.
     */
    private void onComplete()
    {
        logger.info("CreateRouteAction complete");
        
        final CompoundEdit compoundEdit = new CompoundEdit();
        for(NewEntityEdit edit: waypointEdits) {
            compoundEdit.addEdit(edit);
        }
        
        if(currentGeometryEdit != null) {
            compoundEdit.addEdit(currentGeometryEdit);
            compoundEdit.end();
            findService(UndoService.class).addEdit(compoundEdit);
        }
        else { //user didn't create valid geometry, undo it all
            compoundEdit.end();
            compoundEdit.undo();
        }
        
        pvd.remove(doneButton);
        pvd.removeComponentListener(resizeListener);
        pvd.removeMouseListener(mouseAdapter);
        pvd.setContextMenuEnabled(true);
        
        CreateRouteAction.this.setEnabled(true);
        
        pvd.repaint();
    }
    
    /**
     * Creates a waypoint and adds it to the geometry.
     * @param lla
     */
    private void createWaypoint(Geodetic.Point lla)
    {
        //CompoundEdit edit = new CompoundEdit();
        
        final EntityElementList entities = getModel().getEntities();
        NewEntityEdit addEntityEdit = entities.addEntity("waypoint", "waypoint"); 
        //edit.addEdit(addEntityEdit);
        //edit.addEdit(addEntityEdit.getEntity().getLocation().setLocation(Math.toDegrees(lla.latitude), Math.toDegrees(lla.longitude), lla.altitude));
        //edit.end();
        //compoundEdit.addEdit(edit);
        
        addEntityEdit.getEntity().getLocation().setLocation(Math.toDegrees(lla.latitude), Math.toDegrees(lla.longitude), lla.altitude);
        
        waypointEdits.add(addEntityEdit);
    }
    
    /**
     * Creates a route edit from the current waypoints.
     * @return
     */
    private NewEntityEdit createRoute()
    {
        if(waypointEdits.size() < 2) {
            return null;
        }
        
        final NewEntityEdit edit = getModel().getEntities().addEntity("route", "route");
        ArrayList<String> points = new ArrayList<String>();
        for(NewEntityEdit wpEdit: waypointEdits) {
            points.add(wpEdit.getEntity().getName());
        }
        edit.getEntity().getPoints().setPoints(points);
        
        return edit;
    }

    @Override
    public void update() {  }
}
