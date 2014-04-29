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

import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import com.soartech.simjr.scenario.Model;
import com.soartech.simjr.scenario.ModelException;
import com.soartech.simjr.ui.SimulationImages;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.pvd.PlanViewDisplayProvider;

/**
 * @author ray
 */
public class OpenAction extends AbstractEditorAction
{
    private static final Logger logger = Logger.getLogger(OpenAction.class);
    private static final long serialVersionUID = 1L;
    
    /**
     * @param manager
     */
    public OpenAction(ActionManager manager)
    {
        super(manager, "Open...", SimulationImages.OPEN);
        
        setAcceleratorKey(KeyStroke.getKeyStroke("ctrl O"));
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update()
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if(!getApplication().saveIfModelIsChanged())
        {
            return;
        }
        
        try
        {
            final File selectedFile = getApplication().selectFile(Model.DEFAULT_EXTENSION, "Sim Jr scenarios");
            if(selectedFile == null)
            {
                return;
            }
            
            getModel().load(selectedFile);
            final PlanViewDisplayProvider pvdPro = findService(PlanViewDisplayProvider.class);
            if(pvdPro != null && pvdPro.getActivePlanViewDisplay() != null)
            {
                pvdPro.getActivePlanViewDisplay().showAll();
            }
        }
        catch (ModelException e1)
        {
            getApplication().showError("Error loading file", e1);
            logger.error(e1);
        }
    }

}
