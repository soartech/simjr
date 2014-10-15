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
 * Created on Apr 6, 2007
 */
package com.soartech.simjr.ui.actions;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.services.SimulationService;
import com.soartech.simjr.ui.SelectionManager;
import com.soartech.simjr.ui.pvd.IPvdView;
import com.soartech.simjr.ui.pvd.PlanViewDisplayProvider;

/**
 * @author ray
 */
public abstract class AbstractSimulationAction extends AbstractAction
{
    private static final long serialVersionUID = 2216173461956282248L;
    
    private ActionManager manager;
    private String submenuId = "";
    
    public AbstractSimulationAction(String label)
    {
        super(label);
    }

    public AbstractSimulationAction(String label, Icon icon)
    {
        super(label, icon);
    }
    
    public AbstractSimulationAction(ActionManager manager, String label)
    {
        super(label);
        this.manager = manager;
        manager.addAction(this);
    }

    public AbstractSimulationAction(ActionManager manager, String label, Icon icon)
    {
        super(label, icon);
        this.manager = manager;
        manager.addAction(this);
    }
    
    public AbstractSimulationAction(ActionManager manager, String label, Class<?> klass, boolean adapt)
    {
        super(label);
        this.manager = manager;
        manager.addObjectAction(this, klass, adapt);
    }

    public AbstractSimulationAction(ActionManager manager, String label, Icon icon, Class<?> klass, boolean adapt)
    {
        super(label, icon);
        this.manager = manager;
        manager.addObjectAction(this, klass, adapt);
    }
    public void setToolTip(String tip)
    {
        this.putValue(SHORT_DESCRIPTION, tip);
    }
    
    public void setAcceleratorKey(KeyStroke key)
    {
        this.putValue(ACCELERATOR_KEY, key);
    }
    
    public void setLabel(String label)
    {
        this.putValue(NAME, label);
    }
    
    public void setIcon(Icon icon)
    {
        putValue(SMALL_ICON, icon);
    }

    public abstract void update();
    
    public String getId()
    {
        return getClass().getCanonicalName();
    }
    
    public ServiceManager getServices()
    {
        return manager != null ? manager.getServices() : null;
    }
    
    public <T extends SimulationService> T findService(Class<T> klass)
    {
        if(manager == null)
        {
            return null;
        }
        return manager.getServices().findService(klass);
    }
    
    public SelectionManager getSelectionManager()
    {
        return findService(SelectionManager.class);
    }
    
    public ActionManager getActionManager()
    {
        return manager;
    }
    
    public void setActionManager(ActionManager manager)
    {
        this.manager = manager;
    }
    
    public String getSubmenuId()
    {
        return submenuId;
    }
    
    public void setSubmenuId(String newSubmenuId)
    {
        this.submenuId = newSubmenuId;
    }
    
    public IPvdView getPvdView()
    {
        PlanViewDisplayProvider mf = findService(PlanViewDisplayProvider.class);
        
        return mf != null ? mf.getActivePlanViewDisplay().getView() : null;
    }
}
