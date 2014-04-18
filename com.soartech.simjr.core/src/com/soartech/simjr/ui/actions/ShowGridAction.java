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
 * Created on Jun 29, 2007
 */
package com.soartech.simjr.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractButton;

import com.soartech.simjr.ui.SimulationMainFrame;
import com.soartech.simjr.ui.pvd.PlanViewDisplay;

/**
 * @author ray
 */
public class ShowGridAction extends AbstractSimulationAction
{
    private static final long serialVersionUID = -1859898154825121773L;

    private AbstractButton menu;
    
    /**
     * 
     * @param actionManager
     * @param menu
     */
    public ShowGridAction(ActionManager actionManager, AbstractButton menu)
    {
        super(actionManager, "Show grid");
        
        this.menu = menu;
        
        this.setToolTip("Toggle display of grid in the active PVD");
        
        this.menu.setAction(this);
    }

    private PlanViewDisplay getPvd()
    {
        SimulationMainFrame mf = findService(SimulationMainFrame.class);
        
        return mf != null ? mf.getActivePlanViewDisplay() : null;
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update()
    {
        PlanViewDisplay pvd = getPvd();
        
        menu.setEnabled(pvd != null);
        menu.setSelected(pvd != null && pvd.getGrid().isVisible());
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0)
    {
        PlanViewDisplay pvd = getPvd();
        if(pvd != null)
        {
            pvd.getGrid().setVisible(menu.isSelected());
        }
    }

}
