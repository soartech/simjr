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

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.scenario.model.ModelException;
import com.soartech.simjr.sim.ScenarioExporter;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.ui.SimulationImages;
import com.soartech.simjr.ui.SimulationMainFrame;
import com.soartech.simjr.util.FileTools;
import com.soartech.simjr.util.SwingTools;

/**
 * @author ray
 */
public class ExportScenarioAction extends AbstractSimulationAction
{
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ExportScenarioAction.class);
    
    public ExportScenarioAction(ActionManager actionManager)
    {
        super(actionManager, "Export Scenario ...", SimulationImages.OPEN);
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update()
    {
        Simulation sim = findService(Simulation.class);
        setEnabled(sim != null && sim.isPaused());
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0)
    {
        final ScenarioExporter exporter = new ScenarioExporter();
        final Model model = exporter.createModel(getServices());
        
        final File cd = new File(System.getProperty("user.dir"));
        JFileChooser chooser = new JFileChooser(cd);
        chooser.addChoosableFileFilter(SwingTools.createFileFilter(Model.DEFAULT_EXTENSION, "Sim Jr scenarios"));
        
        SimulationMainFrame frame = findService(SimulationMainFrame.class);
        if(chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION)
        {
            return;
        }
        
        File file = chooser.getSelectedFile();
        try
        {
            model.save(FileTools.addDefaultExtension(file, Model.DEFAULT_EXTENSION));
        }
        catch (ModelException e)
        {
            logger.error(e.toString());
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }
}
