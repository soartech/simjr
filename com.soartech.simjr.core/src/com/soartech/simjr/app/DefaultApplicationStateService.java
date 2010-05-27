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
 * Created on Mar 26, 2009
 */
package com.soartech.simjr.app;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.AbstractAdaptable;

/**
 * @author ray
 */
public class DefaultApplicationStateService extends AbstractAdaptable implements
        ApplicationStateService
{

    private final List<ApplicationStateListener> listeners = new CopyOnWriteArrayList<ApplicationStateListener>();
    private AtomicReference<ApplicationState> state = new AtomicReference<ApplicationState>(ApplicationState.INITIALIZING);
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.app.ApplicationStateService#addListener(com.soartech.simjr.app.ApplicationStateListener)
     */
    public void addListener(ApplicationStateListener listener)
    {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.app.ApplicationStateService#getState()
     */
    public ApplicationState getState()
    {
        return state.get();
    }
    
    public void setState(ApplicationState state)
    {
        this.state.set(state);
        for(ApplicationStateListener listener : listeners)
        {
            listener.onApplicationStateChanged(this);
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.app.ApplicationStateService#removeListener(com.soartech.simjr.app.ApplicationStateListener)
     */
    public void removeListener(ApplicationStateListener listener)
    {
        listeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#shutdown()
     */
    public void shutdown() throws SimulationException
    {
        setState(ApplicationState.CLOSING);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#start(com.soartech.simjr.ProgressMonitor)
     */
    public void start(ProgressMonitor progress) throws SimulationException
    {
    }
}
