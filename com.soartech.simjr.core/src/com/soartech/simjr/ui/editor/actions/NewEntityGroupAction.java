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

import javax.swing.JOptionPane;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import com.soartech.math.geotrans.Geodetic;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.scenario.EntityElement;
import com.soartech.simjr.scenario.EntityElementList;
import com.soartech.simjr.scenario.edits.NewEntityEdit;
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.sim.EntityPrototypesFilter;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.editor.UndoService;
import com.soartech.simjr.util.SingleSelectDialog;

/**
 * @author ray
 */
public class NewEntityGroupAction extends NewEntityAction
{
    private static final long serialVersionUID = 1L;
    
    /**
     * @param manager
     * @param label
     * @param icon
     */
    public NewEntityGroupAction(ActionManager manager, String label, String prototype, String keyStroke)
    {
        super(manager, label, prototype, keyStroke);
    }

    public NewEntityGroupAction(ActionManager manager, String label, String prototype, Geodetic.Point position)
    {
        super(manager, label, prototype, position);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        final double spread = 0.0005;
        double originLat;
        double originLon;
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
        
        final CompoundEdit compound = new CompoundEdit();
        final List<EntityPrototype> prototypes = EntityPrototypesFilter.getUserPrototypes(getApplication());
        
        //Get the currently selected entity for setting default values
        final EntityElement selected = Adaptables.adapt(getSelectionManager().getSelectedObject(), EntityElement.class);
        EntityPrototype currentSelectedPrototype = null;
        if(selected != null) {
            for(EntityPrototype p: prototypes) {
                if(p.getId() == selected.getPrototype()) {
                    currentSelectedPrototype = p;
                    break;
                }
            }
        }
        
        //If we have a selected entity, start flight group offset
        if(selected != null) {
            originLat -= spread;
            originLon -= spread;
        }
        
        //Request desired prototype from user 
        final Object selectedPrototype = SingleSelectDialog.select(getApplication().getFrame(), 
                "Select entity prototype for new group", prototypes.toArray(),
                currentSelectedPrototype != null ? currentSelectedPrototype : (!prototypes.isEmpty() ? prototypes.get(0) : null));
        if(selectedPrototype == null || !(selectedPrototype instanceof EntityPrototype)) {
            return;
        }
        
        //Request size of flight group from user
        final Object flightGroupSize = SingleSelectDialog.select(getApplication().getFrame(), 
                "Select size of flight group", new Object[] { 2, 3, 4, 5 }, 4);
        if(flightGroupSize == null || !(flightGroupSize instanceof Integer)) {
            return;
        }
        
        //Request name of flight group from user
        final String flightGroupName = JOptionPane.showInputDialog(getApplication().getFrame(), 
                "Enter name of flight group", 
                selected != null ? selected.getName() : "viper");
        if(flightGroupName == null) {
            return;
        }
        
        final String prototypeId = ((EntityPrototype)selectedPrototype).getId();
        final EntityElementList entities = getModel().getEntities();
        for(int i = 0; i < (Integer)flightGroupSize; i++) 
        {
            final NewEntityEdit edit = entities.addEntity((String)flightGroupName + (i + 1), prototypeId);
            compound.addEdit(edit);
            final UndoableEdit locEdit = edit.getEntity().getLocation().setLocation(originLat - i*spread, originLon - i*spread, 0.0);
            if(locEdit != null) {
                compound.addEdit(locEdit);
            }
        }
        
        compound.end();
        findService(UndoService.class).addEdit(compound);
    }
}
