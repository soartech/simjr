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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.undo.UndoableEdit;

import net.miginfocom.swing.MigLayout;

import com.soartech.simjr.scenario.MetadataElement;
import com.soartech.simjr.scenario.Model;
import com.soartech.simjr.scenario.ModelChangeEvent;
import com.soartech.simjr.scenario.ModelChangeListener;

/**
 * @author ray
 */
public class OverviewPanel extends JPanel implements ModelChangeListener
{
    private static final long serialVersionUID = 5881221908791785184L;

    private final ScenarioEditorApplication app;
    private final JTextField nameField = new JTextField(50);
    private final JTextArea descArea = new JTextArea();
    
    public OverviewPanel(final ScenarioEditorApplication app)
    {
        super(new MigLayout());
        this.app = app;
        
        
        add(new JLabel("Name"));
        add(nameField, "wrap");
        add(new JLabel("Description"), "top");
        descArea.setRows(4);
        add(new JScrollPane(descArea), "growx, growy");
        
        new EntryCompletionHandler(nameField) {
            @Override
            public boolean verify(JComponent input)
            {
                final UndoableEdit edit = app.getModel().getMeta().setName(nameField.getText().trim());
                app.findService(UndoService.class).addEdit(edit);
                return true;
            }
        };
        
        new EntryCompletionHandler(descArea) {
            @Override
            public boolean verify(JComponent input)
            {
                final UndoableEdit edit = app.getModel().getMeta().setDescription(descArea.getText());
                app.findService(UndoService.class).addEdit(edit);
                return true;
            }
            
        };
        
        app.getModel().addModelChangeListener(this);
        onModelChanged(null);
    }

    public void onModelChanged(ModelChangeEvent e)
    {
        if(e == null || e.property.equals(Model.LOADED) || 
           e.property.equals(MetadataElement.NAME) || e.property.equals(MetadataElement.DESC))
        {
            nameField.setText(app.getModel().getMeta().getName());
            descArea.setText(app.getModel().getMeta().getDescription());
        }
    }
}
