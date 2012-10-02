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
import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.scenario.model.ModelChangeEvent;
import com.soartech.simjr.scenario.model.ModelChangeListener;
import com.soartech.simjr.ui.SimulationImages;

/**
 * XML display of model source code. Note that JSyntaxPane is pretty slow and
 * appears to maybe have a memory leak so some special precautions are taken in the 
 * code to ensure that it's only updated when it's the active tab.
 * 
 * Modified to support the dockable framework  ~ Joshua Haley
 * 
 * @author ray
 */
public class SourcePanel extends DefaultSingleCDockable implements ModelChangeListener, EditorTab
{
    private static final long serialVersionUID = 7341341823156862606L;

    static
    {
        jsyntaxpane.DefaultSyntaxKit.initKit();
    }
    
    private final ScenarioEditorServiceManager app;
    private final JEditorPane textArea = new JEditorPane();
    private boolean activated = false;
    
    /**
     * @param app
     */
    public SourcePanel(ScenarioEditorServiceManager app)
    {
        super("SourcePanel");
        
        this.app = app;
        
        //DF settings
        setLayout(new BorderLayout());
        setCloseable(true);
        setMinimizable(true);
        setExternalizable(true);
        setMaximizable(true);
        setTitleText("Source Panel");
        setResizeLocked(true);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setContentType("text/xml");
        
        
        app.getModel().addModelChangeListener(this);
        
        textArea.setText(app.getModel().toString());
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.editor.EditorTab#onTabActivated()
     */
    public void onTabActivated()
    {
        activated = true;
        textArea.setText(app.getModel().toString());
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.editor.EditorTab#onTabDeactivated()
     */
    public void onTabDeactivated()
    {
        activated = false;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.editor.model.ModelChangeListener#onModelChanged(com.soartech.simjr.ui.editor.model.ModelChangeEvent)
     */
    public void onModelChanged(ModelChangeEvent e)
    {
        textArea.setText(app.getModel().toString());

    }
    
}
