/*
 * Copyright (c) 2011, Soar Technology, Inc.
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
 * Created on September 2, 2011
 */
package com.soartech.simjr.sim.entities;

import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.Tickable;

/**
 * Sets self to invisible if not in range of a beholder.
 * 
 * @author voigtjr
 */
public class VeilCapability extends AbstractEntityCapability implements Tickable
{
    @Override
    public void tick(double dt)
    {
        for (Entity e : getEntity().getSimulation().getEntitiesFast())
        {
            BeholderCapability beholder = Adaptables.adapt(e, BeholderCapability.class);

            if (beholder == null)
            {
                continue;
            }
            
            if (beholder.beholds(getEntity().getPosition()))
            {
                getEntity().setProperty(EntityConstants.PROPERTY_VISIBLE, true);
                return;
            }
        }
        
        getEntity().setProperty(EntityConstants.PROPERTY_VISIBLE, false);
    }
    
    @Override
    public Object getAdapter(Class<?> klass)
    {
        if (klass == VeilCapability.class)
        {
            return this;
        }
        return super.getAdapter(klass);
    }
}
