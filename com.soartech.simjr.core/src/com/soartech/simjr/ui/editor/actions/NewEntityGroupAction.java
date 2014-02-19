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
import javax.swing.KeyStroke;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import org.apache.log4j.Logger;

import com.soartech.math.geotrans.Geodetic;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.scenario.EntityElement;
import com.soartech.simjr.scenario.EntityElementList;
import com.soartech.simjr.scenario.edits.NewEntityEdit;
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.sim.EntityPrototypeDatabase;
import com.soartech.simjr.sim.FilterableEntityPrototypes;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.editor.UndoService;
import com.soartech.simjr.util.SingleSelectDialog;

/**
 * Creates a group of entities in a diagonal. 
 */
public class NewEntityGroupAction extends AbstractEditorAction
{
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(NewEntityGroupAction.class);
    
    protected static final double SPREAD = 0.0005;
    
    protected Geodetic.Point initialPosition;
    
    /**
     * @param manager
     * @param label
     * @param icon
     */
    public NewEntityGroupAction(ActionManager manager, String label, String keyStroke)
    {
        super(manager, label);
        
        if(keyStroke != null) {
            setAcceleratorKey(KeyStroke.getKeyStroke(keyStroke));
        }
    }

    public NewEntityGroupAction(ActionManager manager, String label, Geodetic.Point position)
    {
        super(manager, label);
        
        initialPosition = position;
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update() { }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        EntityPrototype selectedPrototype = promptUserForPrototype();
        if(selectedPrototype == null) {
            return;
        }
        
        final int flightGroupSize = promptUserForGroupSize();
        if(flightGroupSize == 0) {
            return;
        }
        
        String flightGroupName = promptUserForGroupName();
        if(flightGroupName == null) {
            return;
        }
        
        final Geodetic.Point location = getInitialPosition();
        logger.debug("Creating entity group at: " + location);
        
        //Construct the edits
        final CompoundEdit compound = new CompoundEdit();
        final String prototypeId = ((EntityPrototype)selectedPrototype).getId();
        final EntityElementList entities = getModel().getEntities();
        for(int i = 0; i < flightGroupSize; i++) 
        {
            final NewEntityEdit edit = entities.addEntity((String)flightGroupName + (i + 1), prototypeId);
            compound.addEdit(edit);
            final UndoableEdit locEdit = edit.getEntity().getLocation().setLocation(location.latitude - i*SPREAD, location.longitude - i*SPREAD, 0.0);
            if(locEdit != null) {
                compound.addEdit(locEdit);
            }
        }
        
        compound.end();
        findService(UndoService.class).addEdit(compound);
    }
    
    /**
     * Gets the selected EntityElement, if any
     * @return
     */
    protected EntityElement getSelectedEntity()
    {
        return Adaptables.adapt(getSelectionManager().getSelectedObject(), EntityElement.class);
    }
    
    /**
     * Get the initial position for the created entities.
     * @return
     */
    protected Geodetic.Point getInitialPosition() 
    {
        double lat = getModel().getTerrain().getOriginLatitude();
        double lon = getModel().getTerrain().getOriginLongitude();
        if(initialPosition != null)
        {
            lat = Math.toDegrees(initialPosition.latitude);
            lon = Math.toDegrees(initialPosition.longitude);
            
            //If we have a selected entity, start flight group offset
            if(getSelectedEntity() != null) {
                lat -= SPREAD;
                lon -= SPREAD;
            }
        }
        return new Geodetic.Point(lat, lon, 0);
    }
    
    /**
     * Determine the EntityPrototype of the selected entity, if any.
     * @return
     */
    protected EntityPrototype getCurrentlySelectedPrototype()
    {
        final EntityPrototypeDatabase db = EntityPrototypeDatabase.findService(getApplication());
        final List<EntityPrototype> prototypes = db.getPrototypes();
        EntityElement selected = getSelectedEntity();
        if(selected != null) {
            for(EntityPrototype p: prototypes) {
                if(p.getId() == selected.getPrototype()) {
                    return p;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the list of prototypes available for the user to create. 
     * @return
     */
    protected List<EntityPrototype> getAvailablePrototypes()
    {
        return FilterableEntityPrototypes.getUserPrototypes(getApplication()).getPrototypes();
    }
    
    /**
     * Prompts the user to select a prototype.
     * @return
     */
    protected EntityPrototype promptUserForPrototype()
    {
        EntityPrototype currentSelectedPrototype = getCurrentlySelectedPrototype();
        final List<EntityPrototype> prototypes = getAvailablePrototypes();
        final Object selectedPrototype = SingleSelectDialog.select(getApplication().getFrame(), 
                "Select entity prototype for new group", prototypes.toArray(),
                currentSelectedPrototype != null ? currentSelectedPrototype : (!prototypes.isEmpty() ? prototypes.get(0) : null));
        if(!(selectedPrototype instanceof EntityPrototype)) {
            return null;
        }
        return (EntityPrototype) selectedPrototype;
    }
    
    /**
     * Prompts the user to select a entity group size.
     */
    protected int promptUserForGroupSize()
    {
        //Request size of flight group from user
        final Object groupSize = SingleSelectDialog.select(getApplication().getFrame(), 
                "Select size of group", new Object[] { 2, 3, 4, 5 }, 4);
        if(groupSize == null || !(groupSize instanceof Integer)) {
            return 0;
        }
        
        return (Integer) groupSize;
    }
    
    /**
     * Prompt the user to enter an entity group name.
     */
    protected String promptUserForGroupName()
    {
        //Request name of flight group from user
        final EntityElement selected = Adaptables.adapt(getSelectionManager().getSelectedObject(), EntityElement.class);
        final String flightGroupName = JOptionPane.showInputDialog(getApplication().getFrame(), 
                "Enter name of flight group", 
                selected != null ? selected.getName() : "viper");
        return flightGroupName;
    }

}
