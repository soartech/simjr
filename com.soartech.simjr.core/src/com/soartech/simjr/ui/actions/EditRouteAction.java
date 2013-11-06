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
 * Created on Sep 19, 2007
 */
package com.soartech.simjr.ui.actions;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;

import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.entities.AbstractPolygon;
import com.soartech.simjr.ui.RouteEditor;
import com.soartech.simjr.ui.editor.actions.AbstractEditorAction;
import com.soartech.simjr.ui.pvd.PlanViewDisplay;
import com.soartech.simjr.ui.pvd.PlanViewDisplayProvider;

/**
 * @author ray
 */
public class EditRouteAction extends AbstractEditorAction
{
    private static final long serialVersionUID = -9047372417083601728L;
    
    /**
     * @param label
     * @param app
     */
    public EditRouteAction(ActionManager actionManager)
    {
        super(actionManager, "Edit Route/Area", AbstractPolygon.class, true);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update()
    {
        setEnabled(getRoute() != null);
    }
 
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0)
    {
        AbstractPolygon route = getRoute();
        if(route != null)
        {
            final Point location = MouseInfo.getPointerInfo().getLocation();
            final PlanViewDisplayProvider pvdPro = findService(PlanViewDisplayProvider.class);
            final PlanViewDisplay pvd = pvdPro != null ? pvdPro.getActivePlanViewDisplay() : null;
            if(pvd != null)
            {
                RouteEditor.showPopupEditor(getApplication().getFrame(), location, route);
            }
        }
    }

    /**
     * @return The selected route
     */
    private AbstractPolygon getRoute()
    {
        return Adaptables.adapt(getSelectionManager().getSelectedObject(), AbstractPolygon.class);
    }

}
