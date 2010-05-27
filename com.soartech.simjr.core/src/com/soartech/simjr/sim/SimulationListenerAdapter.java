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
 * Created on May 22, 2007
 */
package com.soartech.simjr.sim;

/**
 * Empty convenience implementation of {@link SimulationListener}
 * interface.
 * 
 * @author ray
 */
public class SimulationListenerAdapter implements SimulationListener
{
    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.SimulationListener#onTimeSet(double)
     */
    public void onTimeSet(double oldTime)
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationListener#onEntityAdded(com.soartech.simjr.Entity)
     */
    public void onEntityAdded(Entity e)
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationListener#onEntityRemoved(com.soartech.simjr.Entity)
     */
    public void onEntityRemoved(Entity e)
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationListener#onPause()
     */
    public void onPause()
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationListener#onStart()
     */
    public void onStart()
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationListener#onTick(double)
     */
    public void onTick(double dt)
    {
    }

    
    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationListener#onDetonation(com.soartech.simjr.Detonation)
     */
    public void onDetonation(Detonation detonation)
    {
    }

}
