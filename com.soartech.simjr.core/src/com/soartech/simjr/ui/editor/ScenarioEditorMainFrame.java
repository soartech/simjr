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
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.soartech.simjr.scenario.ModelChangeEvent;
import com.soartech.simjr.scenario.ModelChangeListener;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.actions.AddDistanceToolAction;
import com.soartech.simjr.ui.actions.AdjustMapOpacityAction;
import com.soartech.simjr.ui.actions.ClearDistanceToolsAction;
import com.soartech.simjr.ui.actions.ShowAllAction;
import com.soartech.simjr.ui.editor.actions.NewAction;
import com.soartech.simjr.ui.editor.actions.NewEntityAction;
import com.soartech.simjr.ui.editor.actions.OpenAction;
import com.soartech.simjr.ui.editor.actions.RedoAction;
import com.soartech.simjr.ui.editor.actions.RunAction;
import com.soartech.simjr.ui.editor.actions.SaveAction;
import com.soartech.simjr.ui.editor.actions.UndoAction;

/**
 * @author ray
 */
public class ScenarioEditorMainFrame extends JFrame implements ModelChangeListener
{
    private static final long serialVersionUID = 691070210836482404L;
    private final ScenarioEditorApplication app;
    private JPanel content;
    private JTabbedPane tabs;
    private EditorTab lastActiveTab;
    
    public ScenarioEditorMainFrame(ScenarioEditorApplication scenarioEditorApplication)
    {
        this.app = scenarioEditorApplication;
        
        this.content = new JPanel(new BorderLayout());
        
        tabs = new JTabbedPane();
        tabs.addTab("Map", new MapPanel(app));
        tabs.addTab("Scripts", new ScriptsPanel(app));
        tabs.addTab("Source", new SourcePanel(app));
                
        final RunPanel runPanel = new RunPanel();
        tabs.addTab("Run", runPanel);
        app.addService(runPanel);
        
        content.add(tabs, BorderLayout.CENTER);
        
        add(content, BorderLayout.CENTER);
        
        initMenu();

        tabs.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                final Object tab = tabs.getSelectedComponent();
                if(lastActiveTab != null)
                {
                    lastActiveTab.onTabDeactivated();
                    lastActiveTab = null;
                }
                if(tab instanceof EditorTab)
                {
                    ((EditorTab) tab).onTabActivated();
                    lastActiveTab = (EditorTab) tab;
                }
            }
        });

        this.app.getModel().addModelChangeListener(this);
    }

    public void showPanel(JComponent component)
    {
        if(component != null)
        {
            tabs.setSelectedComponent(component);
        }
    }
    
    private void updateTitle()
    {
        final File file = app.getModel().getFile();
        setTitle((file != null ? file.getAbsolutePath() : "untitled") + (app.getModel().isDirty() ? " *" : ""));
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.editor.model.ModelChangeListener#onModelChanged(com.soartech.simjr.ui.editor.model.ModelChangeEvent)
     */
    public void onModelChanged(ModelChangeEvent e)
    {
        updateTitle();
    }

    private void initMenu()
    {
        final JMenuBar bar = new JMenuBar();
        
        final ActionManager am = app.findService(ActionManager.class);
        final JMenu file = new JMenu("File");
        file.add(am.getAction(NewAction.class.getCanonicalName()));
        file.add(am.getAction(OpenAction.class.getCanonicalName()));
        file.add(am.getAction(SaveAction.SAVE));
        file.add(am.getAction(SaveAction.SAVE_AS));
        bar.add(file);
        
        final JMenu edit = new JMenu("Edit");
        edit.add(am.getAction(UndoAction.class.getCanonicalName()));
        edit.add(am.getAction(RedoAction.class.getCanonicalName()));
        bar.add(edit);
        
        final JMenu view = new JMenu("View");
        view.add(am.getAction(ShowAllAction.class.getCanonicalName()));
        view.add(am.getAction(AddDistanceToolAction.class.getCanonicalName()));
        view.add(am.getAction(ClearDistanceToolsAction.class.getCanonicalName()));
        view.add(am.getAction(AdjustMapOpacityAction.class.getCanonicalName()));
        view.add(new AbstractAction("Refresh") {
            private static final long serialVersionUID = -7408029630861071126L;

            public void actionPerformed(ActionEvent e)
            {
                am.updateActions();
            }});
        bar.add(view);
        
        final JMenu insert = new JMenu("Insert");
        insert.add(new NewEntityAction(am, "New Entity", "any", "ctrl E"));
        insert.add(new NewEntityAction(am, "New Waypoint", "waypoint", (String) null));
        insert.add(new NewEntityAction(am, "New Route", "route", (String) null));
        insert.add(new NewEntityAction(am, "New Area", "area", (String) null));
        bar.add(insert);
        
        final JMenu run = new JMenu("Run");
        run.add(am.getAction(RunAction.class.getCanonicalName()));
        bar.add(run);
        
        setJMenuBar(bar);
    }

}
