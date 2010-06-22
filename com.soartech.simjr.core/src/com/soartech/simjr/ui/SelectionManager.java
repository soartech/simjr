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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.soartech.simjr.services.AbstractSimulationService;
import com.soartech.simjr.services.ConstructOnDemand;
import com.soartech.simjr.services.ServiceManager;

/**
 * @author ray
 */
@ConstructOnDemand
public class SelectionManager extends AbstractSimulationService
{
    private List<SelectionManagerListener> listeners = new CopyOnWriteArrayList<SelectionManagerListener>();
    private List<Object> selection = new CopyOnWriteArrayList<Object>();
    
    /**
     * Find the selection manager in the given service manager. This is a 
     * convenience method for scripting.
     * 
     * @param services the service manager
     * @return the selection manager or {@code null} if not found
     */
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
    
    /**
     * Add a listener for selection changes
     * 
     * @param listener the listener
     */
    public void addListener(SelectionManagerListener listener)
    {
        listeners.add(listener);
    }
    
    /**
     * Remove a listener previously registered with {@link #addListener(SelectionManagerListener)}
     * 
     * @param listener the listener
     */
    public void removeListener(SelectionManagerListener listener)
    {
        listeners.remove(listener);
    }
    
    /**
     * Set the current selection to a single object
     *  
     * @param source the source of the selection
     * @param selectedObject the selected object
     */
    public void setSelection(Object source, Object selectedObject)
    {
        setSelection(source, Arrays.asList(selectedObject));
    }
    
    /**
     * Set the current selection to a list of objects.
     * 
     * @param source the source of the selection change
     * @param selection the new list of selected objects
     */
    public void setSelection(Object source, List<Object> selection)
    {
        if(selection != null)
        {
            this.selection = new CopyOnWriteArrayList<Object>(selection);
        }
        else
        {
            this.selection.clear();
        }
        fireSelectionChanged(source);
    }
    
    /**
     * If there's a selection, returns the first item in the selection.
     * 
     * @return the first selected object in the selection list, or {@code null}
     *      if there's no selection.
     */
    public Object getSelectedObject()
    {
        return !selection.isEmpty() ? selection.get(0) : null;
    }
    
    /**
     * @return the current list of selected objects
     */
    public List<Object> getSelection()
    {
        return new ArrayList<Object>(selection);
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
