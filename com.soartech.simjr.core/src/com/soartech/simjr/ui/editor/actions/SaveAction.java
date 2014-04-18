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
package com.soartech.simjr.ui.editor.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.scenario.model.ModelException;
import com.soartech.simjr.ui.SimulationImages;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.util.FileTools;
import com.soartech.simjr.util.SwingTools;

/**
 * @author ray
 */
public class SaveAction extends AbstractEditorAction
{
    private static final Logger logger = Logger.getLogger(SaveAction.class);
    private static final long serialVersionUID = 1L;
    
    public static final String SAVE = SaveAction.class.getCanonicalName() + ".save";
    public static final String SAVE_AS = SaveAction.class.getCanonicalName() + ".save_as"; 

    private boolean saveAs;
    
    /**
     * 
     * @param manager
     * @param saveAs True if performing a "Save As...", false if performing a "Save".
     */
    public SaveAction(ActionManager manager, boolean saveAs)
    {
        super(saveAs ? "Save As..." : "Save", SimulationImages.SAVE);
        this.saveAs = saveAs;
        setActionManager(manager);
        manager.addAction(this);
        
        if(!saveAs)
        {
            setAcceleratorKey(KeyStroke.getKeyStroke("ctrl S"));
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#getId()
     */
    @Override
    public String getId()
    {
        return saveAs ? SAVE_AS : SAVE;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update()
    {
        setEnabled(saveAs || getModel().isDirty());
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        File file = getModel().getFile();
        if(file == null || saveAs)
        {
            JFileChooser chooser = new JFileChooser();
            chooser.addChoosableFileFilter(SwingTools.createFileFilter(Model.DEFAULT_EXTENSION, "Sim Jr scenarios"));
            if(chooser.showSaveDialog(getApplication().getFrame()) != JFileChooser.APPROVE_OPTION)
            {
                return;
            }
            file = chooser.getSelectedFile();
        }
        
        try
        {
            getModel().save(FileTools.addDefaultExtension(file, Model.DEFAULT_EXTENSION));
        }
        catch (ModelException e1)
        {
            getApplication().showError("Error saving file", e1);
            logger.error(e1);
        }
    }

}
