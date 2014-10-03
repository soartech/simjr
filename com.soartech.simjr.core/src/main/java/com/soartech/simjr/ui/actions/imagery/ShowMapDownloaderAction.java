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
package com.soartech.simjr.ui.actions.imagery;

import java.awt.event.ActionEvent;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.soartech.simjr.ui.actions.AbstractSimulationAction;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.pvd.PlanViewDisplay;
import com.soartech.simjr.ui.pvd.PlanViewDisplayProvider;
import com.soartech.simjr.ui.pvd.PvdView;
import com.soartech.simjr.ui.pvd.imagery.MapImageryDownloader;

/**
 * Shows / hides the map imagery control panel. 
 */
public class ShowMapDownloaderAction extends AbstractSimulationAction
{
    private static final long serialVersionUID = 1L;
    
    //Is the UI control being shown?
    private boolean active = false;
    
    public ShowMapDownloaderAction(ActionManager actionManager)
    {
        super(actionManager, "Download Imagery");
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update()
    {
        PvdView pvd = getPvdView();
        setEnabled(pvd != null && 
                   pvd.getMapTileRenderer() != null && 
                   //pvd.getMapTileRenderer().getTileSource() != null &&
                   !active);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent a)
    {
        final PvdView pvd = getPvdView();
        if(pvd != null) 
        {
            MapImageryDownloader ui = new MapImageryDownloader(pvd);
            ui.addAncestorListener(new AncestorListener () {
                @Override
                public void ancestorAdded(AncestorEvent e) { 
                    active = true;
                    update();
                }
                @Override
                public void ancestorMoved(AncestorEvent e) { }
                @Override
                public void ancestorRemoved(AncestorEvent e) {
                    active = false;
                    update();
                }
            });
            active = true;
            update();
        }
    }
}
