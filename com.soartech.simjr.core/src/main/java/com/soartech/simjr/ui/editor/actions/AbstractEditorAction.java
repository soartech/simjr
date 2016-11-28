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

import javax.swing.Icon;

import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.ui.actions.AbstractSimulationAction;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.editor.ScenarioEditorServiceManager;

/**
 * @author ray
 */
public abstract class AbstractEditorAction extends AbstractSimulationAction
{
    private static final long serialVersionUID = 1L;

    /**
     * @param label
     */
    public AbstractEditorAction(String label)
    {
        super(label);
    }

    /**
     * @param label
     * @param icon
     */
    public AbstractEditorAction(String label, Icon icon)
    {
        super(label, icon);
    }

    /**
     * @param manager
     * @param label
     */
    public AbstractEditorAction(ActionManager manager, String label)
    {
        super(manager, label);
    }

    /**
     * @param manager
     * @param label
     * @param icon
     */
    public AbstractEditorAction(ActionManager manager, String label, Icon icon)
    {
        super(manager, label, icon);
    }

    /**
     * @param manager
     * @param label
     * @param klass
     * @param adapt
     */
    public AbstractEditorAction(ActionManager manager, String label,
            Class<?> klass, boolean adapt)
    {
        super(manager, label, klass, adapt);
    }

    /**
     * @param manager
     * @param label
     * @param icon
     * @param klass
     * @param adapt
     */
    public AbstractEditorAction(ActionManager manager, String label, Icon icon,
            Class<?> klass, boolean adapt)
    {
        super(manager, label, icon, klass, adapt);
    }
    
    public ScenarioEditorServiceManager getApplication()
    {
        return getServices().findService(ScenarioEditorServiceManager.class);
    }
    
    public Model getModel()
    {
        return getApplication().getModel();
    }
}
