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
package com.soartech.simjr.ui.editor;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.scenario.model.ModelChangeEvent;
import com.soartech.simjr.scenario.model.ModelChangeListener;
import com.soartech.simjr.ui.SimulationImages;

/**
 * @author ray
 */
public class ScriptsPanel extends DefaultSingleCDockable implements ModelChangeListener
{
    private static final long serialVersionUID = 7341341823156862606L;

    private final Model model;
    private final ScriptEditPanel preLoad;
    private final ScriptEditPanel postLoad;
    
    /**
     * @param app
     */
    public ScriptsPanel(ScenarioEditorServiceManager app)
    {
        super("Scripts Panel");
        

        //DF settings
        setLayout(new BorderLayout());
        setCloseable(true);
        setMinimizable(true);
        setExternalizable(true);
        setMaximizable(true);
        setTitleText("Scripts");
        setResizeLocked(true);
        
        this.model = app.getModel();
        this.preLoad = new ScriptEditPanel(app.findService(UndoService.class), -1);
        this.postLoad = new ScriptEditPanel(app.findService(UndoService.class), -1);
        
        final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerSize(5);
        
        split.setLeftComponent(configureArea("Script executed before scenario is loaded", preLoad));
        split.setRightComponent(configureArea("Script executed after scenario is loaded", postLoad));
        
        split.setDividerSize(5);
        add(split, BorderLayout.CENTER);
        
        model.addModelChangeListener(this);
        onModelChanged(null);
        
        SwingUtilities.invokeLater(new Runnable() {

            public void run()
            {
                split.setDividerLocation(0.5);
            }});
    }

    private JComponent configureArea(String label, ScriptEditPanel area)
    {        
        JPanel panel = new JPanel(new BorderLayout());
//        panel.add(new JLabel(label), BorderLayout.NORTH);
        panel.setBorder(BorderFactory.createTitledBorder(label));
        panel.add(area, BorderLayout.CENTER);
        return panel;
    }

    public void onModelChanged(ModelChangeEvent e)
    {
        if(e == null || e.property.equals(Model.LOADED))
        {
            preLoad.setScript(model.getPreLoadScript());
            postLoad.setScript(model.getPostLoadScript());
        }
    }
}
