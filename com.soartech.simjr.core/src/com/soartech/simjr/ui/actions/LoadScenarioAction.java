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

import org.apache.log4j.Logger;

import com.soartech.simjr.NullProgressMonitor;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.scenario.model.ModelChangeEvent;
import com.soartech.simjr.scenario.model.ModelService;
import com.soartech.simjr.scripting.ScriptRunner;
import com.soartech.simjr.sim.ScenarioLoader;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.ui.SimulationImages;
import com.soartech.simjr.ui.SimulationMainFrame;
import com.soartech.simjr.util.SwingTools;

/**
 * @author ray
 */
public class LoadScenarioAction extends AbstractSimulationAction
{
    private static final Logger logger = Logger.getLogger(LoadScenarioAction.class);
    private static final long serialVersionUID = 1L;

    public LoadScenarioAction(ActionManager actionManager)
    {
        super(actionManager, "Load Scenario ...", SimulationImages.OPEN);
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
        ScriptRunner runner = findService(ScriptRunner.class);
        if(runner == null)
        {
            return;
        }
        
        File cd = new File(System.getProperty("user.dir"));
        JFileChooser chooser = new JFileChooser(cd);
        chooser.addChoosableFileFilter(SwingTools.createFileFilter("js", "Javascript files"));
        chooser.addChoosableFileFilter(SwingTools.createFileFilter("sjx", "Sim Jr scenarios"));
        
        SimulationMainFrame frame = findService(SimulationMainFrame.class);
        if(chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
        {
            return;
        }
        
        File file = chooser.getSelectedFile();
        if(file.getName().endsWith("sjx"))
        {
            final ScenarioLoader loader = new ScenarioLoader(getServices());
            try
            {
                loader.loadScenario(file, new NullProgressMonitor());
                Model model = findService(ModelService.class).getModel();
                model.fireChange(new ModelChangeEvent(model, model, Model.FILE));
            }
            catch (SimulationException e)
            {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(frame, e.getMessage(), "Error loading scenario", JOptionPane.ERROR_MESSAGE);
            }
        }
        else
        {
            try
            {
                runner.run(new NullProgressMonitor(), file);
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(frame, e.getMessage(), "Error loading scenario", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
