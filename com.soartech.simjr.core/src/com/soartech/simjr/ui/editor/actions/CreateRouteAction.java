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
import java.util.ArrayList;
import java.util.List;

import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import com.soartech.math.geotrans.Geodetic;
import com.soartech.simjr.scenario.EntityElement;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.pvd.ActiveEditorPanel;
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
    
    /**
     * @param manager
     * @param label
     * @param icon
     */
    public CreateRouteAction(ActionManager manager, String label, String keyStroke)
    {
        super(manager, label);
        logger.debug("Creating CreateRouteAction");
        
        if(keyStroke != null)
        {
            setAcceleratorKey(KeyStroke.getKeyStroke(keyStroke));
        }
    }

    public CreateRouteAction(ActionManager manager, String label, Geodetic.Point position)
    {
        super(manager, label);
        logger.debug("Creating CreateRouteAction");
        
        this.initialPosition = position;
    }
    
    public void setInitialPosition(Geodetic.Point position)
    {
        this.initialPosition = position;
    }
    

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update()
    {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        logger.info("CreateRouteAction - actionPerformed");
        
        //this.getActionManager().getAction(this.getId()).setEnabled(false);
        setEnabled(false);
        
        PlanViewDisplayProvider pvdProvider = getServices().findService(PlanViewDisplayProvider.class);
        final PlanViewDisplay pvd = pvdProvider.getActivePlanViewDisplay();
        
        final ActiveEditorPanel aep = new ActiveEditorPanel(pvd);
        aep.setOnCompleteListener(new ActiveEditorPanel.OnCompleteListener() {
            @Override
            public void onComplete() {
                CreateRouteAction.this.setEnabled(true);
            }
        });
        
        /*
        final double originLat;
        final double originLon;
        if(initialPosition == null)
        {
            originLat = getModel().getTerrain().getOriginLatitude();
            originLon = getModel().getTerrain().getOriginLongitude();
        }
        else
        {
            originLat = Math.toDegrees(initialPosition.latitude);
            originLon = Math.toDegrees(initialPosition.longitude);
        }
        
        final EntityElementList entities = getModel().getEntities();
        // all of this is within a compound edit
        final CompoundEdit compound = new CompoundEdit();
        final int requiredPoints = 2;
        final Object[] waypoints = getWaypoints();
        final Object selected = Adaptables.adapt(getSelectionManager().getSelectedObject(), EntityElement.class);
        final Object[] pointElements = MultiSelectDialog.select(getApplication().getFrame(), 
                "Select at least " + requiredPoints + " points for new route", waypoints,
                selected != null ? new Object[] { selected } : new Object[] {});
        if(pointElements == null)
        {
            return;
        }
        if(pointElements.length < requiredPoints)
        {
            JOptionPane.showMessageDialog(getApplication().getFrame(), "Please select at least " + requiredPoints + " points");
            return;
        }
        
        // Create two waypoints a little bit apart
        final List<String> points = new ArrayList<String>();
        for(Object o : pointElements)
        {
            points.add(((EntityElement) o).getName());
        }
        
        // Create the route object and add the two points to it
        final NewEntityEdit edit = entities.addEntity(points.get(0) + "-" + points.get(points.size() - 1), "route");
        compound.addEdit(edit);
        edit.getEntity().getPoints().setPoints(points);
        
        compound.end();
        findService(UndoService.class).addEdit(compound);
        */
    }

    private Object[] getWaypoints()
    {
        final List<EntityElement> waypoints = new ArrayList<EntityElement>();
        for(EntityElement e : getModel().getEntities().getEntities())
        {
            if("waypoint".equals(e.getPrototype()))
            {
                waypoints.add(e);
            }
        }
        return waypoints.toArray();
    }
}
