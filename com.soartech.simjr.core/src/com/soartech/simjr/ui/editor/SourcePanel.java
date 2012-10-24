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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import net.miginfocom.swing.MigLayout;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.scenario.model.ModelChangeEvent;
import com.soartech.simjr.scenario.model.ModelChangeListener;
import com.soartech.simjr.scenario.model.ModelException;
import com.soartech.simjr.ui.SimulationImages;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.editor.actions.SaveAction;
import com.soartech.simjr.util.SwingTools;

/**
 * XML display of model source code. Note that JSyntaxPane is pretty slow and
 * appears to maybe have a memory leak so some special precautions are taken in the 
 * code to ensure that it's only updated when it's the active tab.
 * 
 * Modified to support the dockable framework  ~ Joshua Haley
 * Modified to support basic editing and saving of the scenario text. Currently people
 * will resort to javascript hacking for scenario development and this gives them
 * the option of doing that while still taking advantage of the graphical scenario
 * tools available to them.  JHaley
 * 
 * @author ray, haley
 */
public class SourcePanel extends DefaultSingleCDockable implements ModelChangeListener, ActionListener, DocumentListener
{
    private static final long serialVersionUID = 7341341823156862606L;

    static
    {
        jsyntaxpane.DefaultSyntaxKit.initKit();
    }
    
    private final ScenarioEditorServiceManager app;
    private final JEditorPane textArea = new JEditorPane();
    private final JCheckBox editable = new JCheckBox();
    private final JButton saveButton = new JButton("Save");
    private final JButton revert = new JButton("Revert");
    
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
        JPanel optionsPane = new JPanel(new MigLayout());
        optionsPane.add(new JLabel("Allow Editing:"));
        optionsPane.add(editable);
        optionsPane.add(this.saveButton);
        optionsPane.add(this.revert);
        revert.addActionListener(this);
        revert.setEnabled(false);
        saveButton.addActionListener(this);
        saveButton.setEnabled(false);
        editable.addActionListener(this);
        add(optionsPane, BorderLayout.NORTH);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setContentType("text/xml");
        
        
        app.getModel().addModelChangeListener(this);
        textArea.setText(app.getModel().toString());
        textArea.getDocument().addDocumentListener(this);
    }

    /**
     * Warn the user that this is a dangerous operation as there is no scenario error checking
     * We then will select a file if necessary and then save the model as text.  Finally the 
     * model is reloaded.
     */
    private void handleSaveButtonPress()
    {
        int userResponse = JOptionPane.showConfirmDialog(app.getFrame(), "Warning: Saving source directly with any errors can cause instabilities and crashes.\n" + 
                                                                "It is recommended to use the graphical editing techniques only.\n" +
                                                                "Would you like to continue?", "Warning", JOptionPane.YES_NO_OPTION);
        if(userResponse == JOptionPane.NO_OPTION)
            return;
        File file = app.getModel().getFile();
        
        //Select a file if one doesn't currently exist
        if(file == null)
        {
            JFileChooser chooser = new JFileChooser();
            chooser.addChoosableFileFilter(SwingTools.createFileFilter(Model.DEFAULT_EXTENSION, "Sim Jr scenarios"));
            if(chooser.showSaveDialog(app.getFrame()) != JFileChooser.APPROVE_OPTION)
            {
                return;
            }
            file = chooser.getSelectedFile();
        }
        
        //Write the textArea's contents to disk
        try
        {
            PrintWriter fout = new PrintWriter(file);
            fout.print(textArea.getDocument().getText(0, textArea.getDocument().getLength()));
            fout.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (BadLocationException e)
        {
            // Sould not occure as we are starting at the textArea beginning and going to it's length....
        }
        
        //Reload the model
        try
        {
            app.getModel().load(file);
        }
        catch (ModelException e)
        {
            e.printStackTrace();
        }
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.editor.model.ModelChangeListener#onModelChanged(com.soartech.simjr.ui.editor.model.ModelChangeEvent)
     */
    public void onModelChanged(ModelChangeEvent e)
    {
        textArea.setText(app.getModel().toString());
        saveButton.setEnabled(false);
        revert.setEnabled(false);
    }

    
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource().equals(editable))
        {
            textArea.setEditable(editable.isSelected());
        }
        if(e.getSource().equals(saveButton))
        {
            handleSaveButtonPress();
        }
        if(e.getSource().equals(revert))
        {
            textArea.setText(app.getModel().toString());
            saveButton.setEnabled(false);
            revert.setEnabled(false);
        }
    }

    @Override
    public void changedUpdate(DocumentEvent arg0)
    {
        saveButton.setEnabled(true);
        revert.setEnabled(true);
    }

    @Override
    public void insertUpdate(DocumentEvent arg0)
    {
        saveButton.setEnabled(true);
        revert.setEnabled(true);
    }

    @Override
    public void removeUpdate(DocumentEvent arg0)
    {
        saveButton.setEnabled(true);
        revert.setEnabled(true);
    }
}
