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
 * Created on Mar 29, 2009
 */
package com.soartech.simjr.ui.editor;

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.services.ConstructOnDemand;
import com.soartech.simjr.services.SimulationService;

/**
 * Service-y version of UndoManager so it can be queried through the service
 * manager.
 * 
 * @author ray
 */
@ConstructOnDemand
public class UndoService extends UndoManager implements SimulationService
{
    private static final long serialVersionUID = -2022621388848395902L;

    /**
     * Constructed on demand by SimulationManager.findService() 
     */
    public UndoService()
    {
    }
    
    /* (non-Javadoc)
     * @see javax.swing.undo.UndoManager#addEdit(javax.swing.undo.UndoableEdit)
     */
    @Override
    public synchronized boolean addEdit(UndoableEdit anEdit)
    {
        // Overload to filter null edits
        return anEdit != null ? super.addEdit(anEdit) : false;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#shutdown()
     */
    public void shutdown() throws SimulationException
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#start(com.soartech.simjr.ProgressMonitor)
     */
    public void start(ProgressMonitor progress) throws SimulationException
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.adaptables.Adaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class klass)
    {
        return Adaptables.adaptUnchecked(this, klass, false);
    }

}
