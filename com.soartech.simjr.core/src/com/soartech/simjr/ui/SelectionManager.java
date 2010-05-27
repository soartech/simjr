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
 * Created on May 25, 2007
 */
package com.soartech.simjr.ui;

import java.util.ArrayList;
import java.util.List;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.AbstractAdaptable;
import com.soartech.simjr.services.ConstructOnDemand;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.services.SimulationService;

/**
 * @author ray
 */
@ConstructOnDemand
public class SelectionManager extends AbstractAdaptable implements SimulationService
{
    private List<SelectionManagerListener> listeners = new ArrayList<SelectionManagerListener>();
    private List<Object> selection = new ArrayList<Object>();
    
    public static SelectionManager findService(ServiceManager services)
    {
        return services.findService(SelectionManager.class);
    }
    
    /**
     * Constructed on demand by SimulationManager.findService() 
     */
    public SelectionManager()
    {
        
    }
    
    public void addListener(SelectionManagerListener listener)
    {
        listeners.add(listener);
    }
    
    public void removeListener(SelectionManagerListener listener)
    {
        listeners.remove(listener);
    }
    
    public void setSelection(Object source, Object selectedObject)
    {
        selection.clear();
        if(selectedObject != null)
        {
            selection.add(selectedObject);
        }
        fireSelectionChanged(source);
    }
    
    public void setSelection(Object source, List<Object> selection)
    {
        if(selection != null)
        {
            this.selection = new ArrayList<Object>(selection);
        }
        else
        {
            this.selection.clear();
        }
        fireSelectionChanged(source);
    }
    
    public Object getSelectedObject()
    {
        return !selection.isEmpty() ? selection.get(0) : null;
    }
    
    
    public void shutdown() throws SimulationException
    {
    }

    public void start(ProgressMonitor progress) throws SimulationException
    {
    }

    private List<SelectionManagerListener> getSafeListeners()
    {
        return new ArrayList<SelectionManagerListener>(listeners);
    }
    
    private void fireSelectionChanged(Object source)
    {
        for(SelectionManagerListener listener : getSafeListeners())
        {
            listener.selectionChanged(source);
        }
    }
}
