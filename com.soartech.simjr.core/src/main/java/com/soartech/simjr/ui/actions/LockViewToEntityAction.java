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
 * Created on Jul 12, 2007
 */
package com.soartech.simjr.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractButton;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.ui.SimulationImages;
import com.soartech.simjr.ui.pvd.PvdView;

/**
 * @author ray
 */
public class LockViewToEntityAction extends AbstractSimulationAction
{
    private static final long serialVersionUID = -6162603068482506334L;

    private AbstractButton button;
    
    /**
     * @param actionManager
     * @param button
     */
    public LockViewToEntityAction(ActionManager actionManager, AbstractButton button)
    {
        super(actionManager, "", SimulationImages.LOCK);
        
        this.button = button;
        
        this.button.setAction(this);
        if(this.button instanceof JMenuItem)
        {
            JMenuItem menu = (JMenuItem) this.button;
            menu.setAccelerator(KeyStroke.getKeyStroke("control E"));
        }
    }

    private Entity getSelection()
    {
        Object s = getSelectionManager().getSelectedObject();
        
        if(!(s instanceof Entity))
        {
            return null;
        }
        
        Entity e = (Entity) s;
        if(!EntityTools.isVisible(e))
        {
            return null;
        }
        
        return e;
    }
    
//    private PlanViewDisplay getPvd()
//    {
//        SimulationMainFrame mf = findService(SimulationMainFrame.class);
//        
//        return mf != null ? mf.getActivePlanViewDisplay() : null;
//    }
    

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update()
    {
        PvdView pvd = getPvdView();
        
        button.setEnabled(pvd != null && (button.isSelected() || getSelection() != null));
        Entity lockEntity = pvd.getLockEntity();
        button.setSelected(lockEntity != null);
        String text = "";
        if(lockEntity != null)
        {
            text = "Unlock PVD (locked on " + lockEntity.getName() + ")";
        }
        else
        {
            text = "Lock PVD on selection";
        }
        setToolTip(text);
        if(button instanceof JMenuItem)
        {
            button.setText(text);
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0)
    {
        PvdView pvd = getPvdView();
        
        if(!button.isSelected())
        {
            pvd.setLockEntity(null);
        }
        else
        {
            Entity e = getSelection();
            if(e == null || pvd == null)
            {
                return;
            }
            
            pvd.setLockEntity(e);
        }
    }

}
