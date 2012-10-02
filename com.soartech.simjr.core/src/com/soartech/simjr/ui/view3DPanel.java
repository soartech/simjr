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
 * Created on Aug 10, 2007
 */
package com.soartech.simjr.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

import com.soartech.simjr.console.ConsoleManager;
import com.soartech.simjr.console.ConsoleManagerListener;
import com.soartech.simjr.console.ConsoleParticipant;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.ui.actions.AbstractSimulationAction;
import com.soartech.simjr.ui.editor.ScenarioEditorServiceManager;
import com.soartech.simjr.ui.editor.View3DPanel;
import com.soartech.simjr.util.SwingTools;

/**
 * This is the wrapper to the View3DPanel that allows that panel to interropt with the tab framework
 * 
 * @author Joshua Haley
 */
public class view3DPanel extends DefaultSingleCDockable
{
    private final JPanel view3d;

    
    public view3DPanel(final ServiceManager services)
    {
        super("View3DPanel");
        
        setUpDockable();

        this.view3d = new View3DPanel(services);
        this.add(view3d);
        view3d.setVisible(true);

    }
    
    public view3DPanel(final ScenarioEditorServiceManager app)
    {
        super("View3DPanel");
        
        setUpDockable();

        this.view3d = new View3DPanel(app);
        this.add(view3d);
        view3d.setVisible(true);
    }
    
    private void setUpDockable()
    {
        //DF settings
        setLayout(new BorderLayout());
        setCloseable(true);
        setMinimizable(true);
        setExternalizable(true);
        setMaximizable(true);
        setTitleText("3D View");
        setResizeLocked(true);
        setTitleIcon(SimulationImages.PVD);
    }
    
}
