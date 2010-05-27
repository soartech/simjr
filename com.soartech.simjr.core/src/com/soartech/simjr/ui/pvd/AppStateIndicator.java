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
 * Created on Mar 26, 2009
 */
package com.soartech.simjr.ui.pvd;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.soartech.simjr.app.ApplicationState;
import com.soartech.simjr.app.ApplicationStateListener;
import com.soartech.simjr.app.ApplicationStateService;
import com.soartech.simjr.ui.SimulationImages;

/**
 * @author ray
 */
public class AppStateIndicator extends ComponentAdapter implements ApplicationStateListener
{
    private final ApplicationStateService appState;
    private final JComponent parent;
    private boolean loadingLabelAdded = false;
    private final ImageIcon loadingImage = SimulationImages.LOADING;
    private final JLabel loadingLabel = new JLabel(loadingImage);

    public AppStateIndicator(ApplicationStateService appState, JComponent parent)
    {
        this.appState = appState;
        this.parent = parent;
        
        this.parent.addComponentListener(this);
        this.appState.addListener(this);
        onApplicationStateChanged(appState);
    }
    
    public ApplicationState getState()
    {
        return this.appState.getState();
    }
    
    public void dispose()
    {
        this.parent.removeComponentListener(this);
        this.appState.removeListener(this);
        SwingUtilities.invokeLater(new Runnable() { public void run() { removeLoadingLabel(); }});
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.app.ApplicationStateListener#onApplicationStateChanged(com.soartech.simjr.app.ApplicationStateService)
     */
    public void onApplicationStateChanged(ApplicationStateService service)
    {
        switch(service.getState())
        {
        case INITIALIZING:
            SwingUtilities.invokeLater(new Runnable() { public void run() { addLoadingLabel(); } });
            break;
        case RUNNING:
            SwingUtilities.invokeLater(new Runnable() { public void run() { removeLoadingLabel(); }});
            break;
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentResized(ComponentEvent e)
    {
        if(!loadingLabelAdded) return;
        
        final int w = loadingImage.getIconWidth();
        final int h = loadingImage.getIconHeight();
        loadingLabel.setBounds((parent.getWidth() - w) / 2, (parent.getHeight() - h) / 2, w, h);
       
    }
    
    private void addLoadingLabel()
    {
        if(loadingLabelAdded) return;
        
        parent.add(loadingLabel);
        loadingLabelAdded = true;
        componentResized(null);
    }
    
    private void removeLoadingLabel()
    {
        if(!loadingLabelAdded) return;
        parent.remove(loadingLabel);
        loadingLabelAdded = false;
    }
}
