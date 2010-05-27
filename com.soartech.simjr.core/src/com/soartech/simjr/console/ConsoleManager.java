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
 * Created on May 15, 2008
 */
package com.soartech.simjr.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.AbstractAdaptable;
import com.soartech.simjr.services.ConstructOnDemand;
import com.soartech.simjr.services.SimulationService;

/**
 * Manages a set of consoles in Sim Jr. This allows various agents or other 
 * components to provide a simple command-line interface in Sim Jr. In 
 * particular it allows the console to work with either Soar 7 or Soar 8 agents.
 * 
 * @author ray
 */
@ConstructOnDemand
public class ConsoleManager extends AbstractAdaptable implements SimulationService
{
    private List<ConsoleManagerListener> listeners = new CopyOnWriteArrayList<ConsoleManagerListener>();
    private List<ConsoleParticipant> participants =
        Collections.synchronizedList(new ArrayList<ConsoleParticipant>());
    
    /**
     * Constructed on demand by SimulationManager.findService() 
     */
    public ConsoleManager()
    {
    }
    
    /**
     * Add a listener to the manager. The listener will be notified when the 
     * list of participants changes
     * 
     * @param listener The listener to add
     */
    public void addListener(ConsoleManagerListener listener)
    {
        listeners.add(listener);
    }
    
    /**
     * Remove a listener
     * 
     * @param listener The listener to remove
     */
    public void removeListener(ConsoleManagerListener listener)
    {
        listeners.remove(listener);
    }
    
    /**
     * Add a console to the manager
     * 
     * @param p The console
     */
    public void addParticipant(ConsoleParticipant p)
    {
        participants.add(p);
        fireOnChanged();
    }
    
    /**
     * Remove a console from the manager
     * 
     * @param p The console to remove
     */
    public void removeParticipant(ConsoleParticipant p)
    {
        if(participants.remove(p))
        {
            fireOnChanged();
        }
    }
    
    /**
     * @return A list of consoles, sorted by name
     */
    public List<ConsoleParticipant> getParticipants()
    {
        List<ConsoleParticipant> parts = new ArrayList<ConsoleParticipant>(participants);
        
        Collections.sort(parts, new Comparator<ConsoleParticipant>() {

            public int compare(ConsoleParticipant o1, ConsoleParticipant o2)
            {
                return o1.getName().compareTo(o2.getName());
            }});
        return parts;
    }
    
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#shutdown()
     */
    public void shutdown() throws SimulationException
    {
        listeners.clear();
        participants.clear();
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#start(com.soartech.simjr.ProgressMonitor)
     */
    public void start(ProgressMonitor progress) throws SimulationException
    {
    }

    private void fireOnChanged()
    {
        for(ConsoleManagerListener listener : listeners)
        {
            listener.onChanged(this);
        }
    }

}
