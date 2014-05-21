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
 * Created on Oct 26, 2009
 */
package com.soartech.simjr.ui.actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import com.soartech.simjr.ui.MapImageOpacityController;
import com.soartech.simjr.ui.SimulationMainFrame;
import com.soartech.simjr.ui.pvd.MapImage;
import com.soartech.simjr.ui.pvd.PlanViewDisplay;
import com.soartech.simjr.ui.pvd.PlanViewDisplayProvider;

/**
 * @author ray
 */
public class AdjustMapOpacityAction extends AbstractSimulationAction
{
    private static final long serialVersionUID = -659487553362689218L;

    public AdjustMapOpacityAction(ActionManager actionManager)
    {
        super(actionManager, "Adjust map image opacity");
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update()
    {
        final PlanViewDisplayProvider prov = findService(PlanViewDisplayProvider.class);
        setEnabled(prov != null && prov.getActivePlanViewDisplay() != null &&
                  prov.getActivePlanViewDisplay().getMapImage() != null);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0)
    {
        final PlanViewDisplayProvider prov = findService(PlanViewDisplayProvider.class);
        if(prov == null)
        {
            return;
        }
        final PlanViewDisplay pvd = prov.getActivePlanViewDisplay();
        if(pvd == null)
        {
            return;
        }
        final MapImage map = pvd.getMapImage();
        if(map == null)
        {
            return;
        }
        final Rectangle rect = pvd.getBounds();
        
        JFrame frame = getServices().findService(SimulationMainFrame.class);
        MapImageOpacityController.showPopupEditor(frame, pvd, new Point((int) rect.getCenterX(), (int) rect.getCenterY()) , map);
    }

}
