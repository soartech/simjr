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
 * Created on Jun 19, 2007
 */
package com.soartech.simjr.radios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
public class RadioHistory extends AbstractAdaptable implements SimulationService
{
    private final List<RadioMessage> messages = Collections.synchronizedList(new ArrayList<RadioMessage>());
    private final List<RadioHistoryListener> listeners = new CopyOnWriteArrayList<RadioHistoryListener>();
    
    /**
     * Find the service. Convenience method for scripts.
     * 
     * @param services the service manager
     * @return the service, or {@code null} if not found
     */
    public static RadioHistory findService(ServiceManager services)
    {
        return services.findService(RadioHistory.class);
    }
    
    /**
     * Constructed on demand by SimulationManager.findService() 
     */
    public RadioHistory()
    {
    }
    
    public void addListener(RadioHistoryListener listener)
    {
        listeners.add(listener);
    }
    
    public void removeListener(RadioHistoryListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * @return the messages
     */
    public List<RadioMessage> getMessages()
    {
        synchronized(messages)
        {
            return new ArrayList<RadioMessage>(messages);
        }
    }
    
    public void addMessage(RadioMessage message)
    {
        messages.add(message);
        
        for(RadioHistoryListener listener : listeners)
        {
            listener.onRadioMessage(message);
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.services.SimulationService#shutdown()
     */
    public void shutdown() throws SimulationException
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.services.SimulationService#start(com.soartech.simjr.ProgressMonitor)
     */
    public void start(ProgressMonitor progress) throws SimulationException
    {
    }
    
    
}
