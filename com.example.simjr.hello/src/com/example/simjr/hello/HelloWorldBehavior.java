/*
 * (c) 2010  Soar Technology, Inc.
 * 
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
 * Created on Jun 11, 2010
 */
package com.example.simjr.hello;

import com.soartech.simjr.radios.RadioHistory;
import com.soartech.simjr.radios.RadioMessage;
import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.Tickable;

/**
 * @author ray
 */
public class HelloWorldBehavior extends AbstractEntityCapability implements
        Tickable
{
    private RadioHistory radioHistory;
    private double signalCountdown = 0;
    
    public RadioHistory getRadioHistory()
    {
        return radioHistory;
    }

    public void setRadioHistory(RadioHistory radioHistory)
    {
        this.radioHistory = radioHistory;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.Tickable#tick(double)
     */
    @Override
    public void tick(double dt)
    {
        signalCountdown -= dt; // substract time that has passed
        
        if(signalCountdown <= 0.0)
        {
            // Construct a message and add it to the radio history
            final Simulation sim = getEntity().getSimulation();
            final RadioMessage message = new RadioMessage(getEntity().getName(), null, "Hello, World!", sim.getTime(), "");
            radioHistory.addMessage(message);
            
            signalCountdown = 10.0; // wait another ten seconds
        }
    }

}
