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
 * Created on Jun 11, 2007
 */
package com.soartech.simjr.ui.pvd;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.undo.CompoundEdit;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXPanel;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.simjr.scenario.EntityElementList;
import com.soartech.simjr.scenario.edits.NewEntityEdit;
import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.sim.Simulation;

/**
 * Encapsulates the functionality of a graphical route/area editor.
 */
public class ActiveEditorPanel extends JXPanel
{
    private static final long serialVersionUID = -1L;
    private static final Logger logger = Logger.getLogger(ActiveEditorPanel.class);
    
    private static final int width= 100, height = 35;

    private Simulation sim;
    private Model model;
    private PlanViewDisplay pvd;
    
    private JButton doneButton = new JButton("Done");
    
    private CompoundEdit compoundEdit = new CompoundEdit();
    
    //Callback for when the edit is completed
    public interface OnCompleteListener {
        void onComplete();
    };
    private OnCompleteListener onCompleteListener;
    public void setOnCompleteListener(OnCompleteListener l) {
        this.onCompleteListener = l;
    }
    
    private ComponentAdapter resizeListener = new ComponentAdapter() {
        public void componentResized(ComponentEvent evt) {
            logger.info("PVD resized: " + evt);
            setBounds(ActiveEditorPanel.this.pvd.getWidth()/2 - width/2, 10, width, height);
        }
    };
    
    private MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            logger.info("Mouse click");
            if(SwingUtilities.isLeftMouseButton(e)) {
                if(e.getClickCount() == 2) {
                    logger.info("Double clicked!");
                    onComplete();
                }
                else {
                    final Vector3 meters = pvd.getTransformer().screenToMeters((double) e.getX(), (double) e.getY());
                    final Geodetic.Point lla = sim.getTerrain().toGeodetic(meters);
                    logger.info("Clicked at latlon: " + lla.latitude + "," + lla.longitude);
                    createWaypoint(lla);
                }
            }
            else if(SwingUtilities.isRightMouseButton(e)) {
                logger.info("Right clicked");
            }
        }
    };
    
    public ActiveEditorPanel(Simulation sim, Model model, PlanViewDisplay pvd)
    {
        super();
        
        this.sim = sim;
        this.model = model;
        this.pvd = pvd;
        
        this.add(doneButton);
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onComplete();
            }
        });
        
        setAlpha(0.7f);
        setBounds(pvd.getWidth()/2 - width/2, 10, width, height);
        
        pvd.addMouseListener(mouseAdapter);
        pvd.addComponentListener(resizeListener);
        pvd.setContextMenuEnabled(false);
        
        this.pvd.add(this);
        this.pvd.repaint();
    }

    private void onComplete()
    {
        pvd.remove(ActiveEditorPanel.this);
        pvd.removeComponentListener(resizeListener);
        pvd.removeMouseListener(mouseAdapter);
        pvd.setContextMenuEnabled(true);
        
        if(onCompleteListener != null) {
            onCompleteListener.onComplete();
        }
    }
    
    private void createWaypoint(Geodetic.Point lla)
    {
        CompoundEdit edit = new CompoundEdit();
        
        final EntityElementList entities = model.getEntities();
        NewEntityEdit addEntityEdit = entities.addEntity("waypoint", "waypoint"); 
        edit.addEdit(addEntityEdit);
        edit.addEdit(addEntityEdit.getEntity().getLocation().setLocation(Math.toDegrees(lla.latitude), Math.toDegrees(lla.longitude), lla.altitude));
        compoundEdit.addEdit(edit);
    }
}
