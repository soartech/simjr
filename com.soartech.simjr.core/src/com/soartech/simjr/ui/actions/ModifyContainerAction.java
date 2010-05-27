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
 * Created on Aug 13, 2007
 */
package com.soartech.simjr.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.entities.EntityContainerCapability;

/**
 * @author ray
 */
public abstract class ModifyContainerAction extends AbstractSimulationAction
{
    private static final long serialVersionUID = -6526968375781400702L;
    
    private boolean add;
    
    public ModifyContainerAction(ActionManager actionManager, boolean add)
    {
        super(actionManager,
              add ? "Load entity into selection" : "Unload entities from selection");
        
        this.add = add;
    }

    private EntityContainerCapability getContainerCap()
    {
        return Adaptables.adapt(getSelectionManager().getSelectedObject(), 
                                EntityContainerCapability.class);
    }
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update()
    {
        setEnabled(getContainerCap() != null);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0)
    {
        EntityContainerCapability cap = getContainerCap();
        
        if(cap == null)
        {
            return;
        }
        
        if(add)
        {
            Entity e = (Entity) JOptionPane.showInputDialog(null, 
                    "Choose an entity to load into " + cap.getEntity(), 
                    "Entity Selection",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    cap.getEntity().getSimulation().getEntities().toArray(),
                    null);
            
            if(e == null)
            {
                return;
            }
            
            cap.add(e);
        }
        else
        {
            cap.removeAll(5.0);
        }
    }

}
