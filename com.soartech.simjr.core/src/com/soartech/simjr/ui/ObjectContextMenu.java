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
 * Created on Sep 20, 2007
 */
package com.soartech.simjr.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.log4j.Logger;

import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.ui.actions.AbstractSimulationAction;
import com.soartech.simjr.ui.actions.ActionManager;

/**
 * A context menu for an arbitrary object. Uses the current selections along 
 * with {@link ActionManager#getActionsForObject(Object)} to populate itself
 * dynamically when shown.
 * 
 * @author ray
 */
public class ObjectContextMenu extends JPopupMenu
{
    private static final Logger logger = Logger.getLogger(ObjectContextMenu.class);    
    private static final long serialVersionUID = -2009485868788433167L;
    
    private ActionManager actionManager;
    private SelectionManager selectionManager;
    
    public ObjectContextMenu(ServiceManager services)
    {
        this.actionManager = services.findService(ActionManager.class);
        this.selectionManager = services.findService(SelectionManager.class);
        
        this.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent e) { }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }
            public void popupMenuWillBecomeVisible(PopupMenuEvent e)
            {
                populate();
            }});
    }
    
    public ActionManager getActionManager()
    {
        return actionManager;
    }
    
    /**
     * Override this method to provide additional actions for the context menu
     * 
     * @return Additional actions to be included in the context menu.
     */
    protected List<Action> getAdditionalActions()
    {
        return new ArrayList<Action>();
    }
    
    /**
     * Override this method to provide additional per-object actions for the context menu
     * @param the selected object
     * @return Additional actions to be included in the context menu
     */
    protected List<AbstractSimulationAction> getAdditionalActionsForObject(Object selected) 
    {
        return new ArrayList<AbstractSimulationAction>();
    }
    
    private void populate()
    {
        removeAll();
        
        if(actionManager == null)
        {
            logger.error("No action manager present.");
            return;
        }
        
        if(selectionManager == null)
        {
            logger.warn("No selection manager present");
            return;
        }
        
        // First add custom actions....
        final boolean hasAdditional = addAdditionalActions();
        
        // Now add actions specific to the selection
        if(selectionManager.getSelectedObject() != null)
        {
            if(!hasAdditional)
            {
                addSeparator();
            }
            List<AbstractSimulationAction> actions = actionManager.getActionsForObject(selectionManager.getSelectedObject());
            
            Collections.sort(actions, new Comparator<AbstractSimulationAction>(){
                public int compare(AbstractSimulationAction o1, AbstractSimulationAction o2)
                {
                    return o1.getValue(Action.NAME).toString().compareToIgnoreCase(o2.getValue(Action.NAME).toString());
                }});
            
            addActions(actions);
            
            List<AbstractSimulationAction> extraActions = getAdditionalActionsForObject(selectionManager.getSelectedObject());
            if(!extraActions.isEmpty()) {
                addSeparator();
                addActions(extraActions);
            }

        }//end if selected != null
    }
    
    /**
     * Adds the given actions to the menu.
     * @param actions The list of actions to add to the menu.
     */
    private void addActions(List<AbstractSimulationAction> actions)
    {
        Map<String, JMenu> submenus = new HashMap<String, JMenu>();
        for(AbstractSimulationAction action : actions)
        {
            if(action.isEnabled())
            {
                //add this action to a submenu in the popup if necessary
                if(!action.getSubmenuId().isEmpty())
                {                        
                    //try to get the submenu based on its id
                    JMenu submenu = submenus.get(action.getSubmenuId());
                    
                    //create a new JMenu object if the submenu doesn't exist in the map
                    if(submenu == null)
                    {
                        submenu = new JMenu();
                        submenu.setText(action.getSubmenuId());
                        
                        //add the new submenu to this JPopupMenu
                        add(submenu);
                        
                        //add the new submenu to the map
                        submenus.put(action.getSubmenuId(), submenu);
                    }
                    
                    //add the action to the submenu now that we know it exists
                    submenu.add(action);
                }
                else //or just add as a regular menu item
                {
                    add(action);
                }
            }
        }//end for actions
    }

    private boolean addAdditionalActions()
    {
        List<Action> addl = getAdditionalActions();
        final boolean hasAdditional = !addl.isEmpty();
        if(!addl.isEmpty())
        {
            for(Action action : addl)
            {
                if(action != null)
                {
                    add(action);
                    if(action instanceof AbstractSimulationAction)
                    {
                        ((AbstractSimulationAction) action).update();
                    }
                }
                else
                {
                    addSeparator();
                }
            }
        }
        return hasAdditional;
    }
}
