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
import java.util.LinkedList;
import java.util.List;

import javax.swing.undo.UndoableEdit;

import org.apache.log4j.Logger;

import com.soartech.simjr.scenario.EntityElement;
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.sim.EntityPrototypeDatabase;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.editor.UndoService;
import com.soartech.simjr.util.SingleSelectDialog;

public class UpdateFollowAction extends AbstractEditorAction
{
    private static final Logger logger = Logger.getLogger(UpdateFollowAction.class);
    private static final long serialVersionUID = 1L;
    
    private final EntityElement selected;
    
    public static UpdateFollowAction create(ActionManager manager, EntityElement elt)
    {
        logger.debug("Creating UpdateFollowAction for entity: " + elt);
        String followTarget = elt.getCapabilities().getFollowTarget();
        logger.debug("Current follow target: " + followTarget);
        if(followTarget == null || followTarget.isEmpty())
        {
            return new UpdateFollowAction(manager, "Set as Follower", elt);
        }
        else
        {
            return new UpdateFollowAction(manager, "Remove as Follower of '" + followTarget + "'", elt);
        }
    }
    
    /**
     * @param manager
     * @param label
     * @param icon
     */
    private UpdateFollowAction(ActionManager manager, String label, EntityElement elt)
    {
        super(manager, label);
        this.selected = elt;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update() {
        if(getAvailableLeaders().isEmpty()){
            setEnabled(false);
        }
        else {
            setEnabled(true);
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if(selected == null) { 
            return;
        }
        
        String currentFollowTarget = selected.getCapabilities().getFollowTarget();
        if(currentFollowTarget == null || currentFollowTarget.isEmpty())
        {
            //Request desired leader from user            
            final Object selectedLeader = SingleSelectDialog.select(getApplication().getFrame(), 
                    "Select entity to follow", getAvailableLeaders().toArray());
            if(selectedLeader == null || !(selectedLeader instanceof EntityElement)) {
                return;
            }
            
            EntityElement leader = (EntityElement)selectedLeader;
            UndoableEdit edit = selected.getCapabilities().setFollowTarget(leader.getName());
            findService(UndoService.class).addEdit(edit);
        }
        else
        {
            UndoableEdit edit = selected.getCapabilities().removeFollowTarget();
            findService(UndoService.class).addEdit(edit);
        }
    }
    
    private List<EntityElement> getAvailableLeaders()
    {
        final List<EntityElement> availableEntities = new LinkedList<EntityElement>();
        for(EntityElement ee: getModel().getEntities().getEntities())
        {
            if(selected == ee) {
                continue;
            }
            
            final EntityPrototypeDatabase db = EntityPrototypeDatabase.findService(getApplication());
            final EntityPrototype prototype = db.getPrototype(ee.getPrototype());
            if(!canBeFollowed(prototype))
            {
                continue;
            }
            availableEntities.add(ee);
        }
        return availableEntities;
    }
    
    //TODO: Where should this logic live?
    public static boolean canBeFollower(EntityPrototype prototype) 
    {
        if (prototype.isAbstract() || 
            prototype.hasSubcategory("flyout") || 
            prototype.hasSubcategory("control"))
        {
            return false;
        }
        return true;
    }
    
    public static boolean canBeFollowed(EntityPrototype prototype)
    {
        //These are the same, for now
        return canBeFollower(prototype);
    }
}
