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
import java.util.List;

import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import com.soartech.math.geotrans.Geodetic;
import com.soartech.simjr.scenario.EntityElement;
import com.soartech.simjr.scenario.EntityElementList;
import com.soartech.simjr.scenario.edits.NewEntityEdit;
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.sim.FilterableEntityPrototypes;
import com.soartech.simjr.ui.NewFlightGroupDialog;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.editor.UndoService;

/**
 * Creates a group of entities in a diagonal, limited to aircraft. 
 */
public class NewFlightGroupAction extends NewEntityGroupAction
{
    private static final long serialVersionUID = 1L;
    
    /**
     * @param manager
     * @param label
     * @param icon
     */
    public NewFlightGroupAction(ActionManager manager, String label, String keyStroke)
    {
        super(manager, label, keyStroke);
    }

    public NewFlightGroupAction(ActionManager manager, String label, Geodetic.Point position)
    {
        super(manager, label, position);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        
        NewFlightGroupDialog newFlightGroupForm = NewFlightGroupDialog.select(getApplication().getFrame(), 
                getAvailablePrototypes(), null, new Integer[] { 2, 3, 4, 5 }, 4, "viper");
        
        if (newFlightGroupForm.getUserCancelled())
        {
            return;
        }
        
        EntityPrototype selectedPrototype = newFlightGroupForm.getPrototype();
        if(selectedPrototype == null) {
            return;
        }
        
        final int flightGroupSize = newFlightGroupForm.getGroupSize();
        if(flightGroupSize == 0) {
            return;
        }
        
        String flightGroupName = newFlightGroupForm.getGroupName();
        if(flightGroupName == null || flightGroupName.equals("")) {
            return;
        }
        
        final Geodetic.Point location = getInitialPosition();
        
        //Construct the edits
        final CompoundEdit compound = new CompoundEdit();
        final String prototypeId = ((EntityPrototype)selectedPrototype).getId();
        final EntityElementList entities = getModel().getEntities();
        
        EntityElement leader = getSelectedEntity();
        
        for(int i = 0; i < flightGroupSize; i++) 
        {
            final NewEntityEdit edit = entities.addEntity((String)flightGroupName + (i + 1), prototypeId);
            compound.addEdit(edit);
            final UndoableEdit locEdit = edit.getEntity().getLocation().setLocation(location.latitude - i*SPREAD, location.longitude - i*SPREAD, 0.0);
            if(locEdit != null) {
                compound.addEdit(locEdit);
            }
            
            if(i == 0 && leader == null) {
                leader = edit.getEntity();
            }
            else if(leader != null) {
                final UndoableEdit followEdit = edit.getEntity().getCapabilities().setFollowTarget(leader.getName());
                compound.addEdit(followEdit);
            }
        }
        
        compound.end();
        findService(UndoService.class).addEdit(compound);
    }
    
    /**
     * Returns the list of prototypes available for the user to create. 
     * @return
     */
    protected List<EntityPrototype> getAvailablePrototypes()
    {
        return FilterableEntityPrototypes.getUserPrototypes(getApplication()).include("air.*.*").getPrototypes();
    }
    
}
