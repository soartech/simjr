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

import javax.swing.KeyStroke;

import com.soartech.simjr.ui.SimulationImages;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.editor.RunPanel;
import com.soartech.simjr.ui.editor.ScenarioRunner;

/**
 * @author ray
 */
public class RunAction extends AbstractEditorAction
{
    //private static final Logger logger = Logger.getLogger(RunAction.class);
    private static final long serialVersionUID = 1L;
    
    /**
     * @param manager the owning action manager
     */
    public RunAction(ActionManager manager)
    {
        super(manager, "Run in Sim Jr", SimulationImages.START);
        
        setAcceleratorKey(KeyStroke.getKeyStroke("F5"));
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update()
    {
        final ScenarioRunner runner = findService(ScenarioRunner.class);
        setEnabled(runner != null && !runner.isRunning() && !getModel().isDirty());
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if(getModel().isDirty())
        {
            return;
        }
        
        final ScenarioRunner runner = findService(ScenarioRunner.class);
        if(runner.isRunning())
        {
            return;
        }
        
        runner.runScenario(getModel());
        
        getActionManager().updateActions();
    }

}
