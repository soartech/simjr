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
 * Created on Dec 9, 2009
 */
package com.soartech.simjr.sim.entities;

import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityTools;

/**
 * @author ray
 */
public class MilStd2525Provider extends AbstractEntityCapability
{
    public static final String PROPERTY = "ms2525";
    
    /**
     * 
     */
    public MilStd2525Provider()
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.AbstractEntityCapability#attach(com.soartech.simjr.sim.Entity)
     */
    @Override
    public void attach(Entity entity)
    {
        super.attach(entity);
        entity.setProperty(PROPERTY, this);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.AbstractEntityCapability#detach()
     */
    @Override
    public void detach()
    {
        getEntity().setProperty(PROPERTY, null);
        super.detach();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        final Entity e = getEntity();
        final char force = getForceChar(EntityTools.getForce(e));
        if(e == null)
        {
            return String.format("S%cGP------", force);
        }
        final Object templateObject = e.getProperty("ms2525.template");
        final String templateString = templateObject != null ? templateObject.toString() : "S%cGP------";
        return String.format(templateString, force);
    }
    
    private char getForceChar(String force)
    {
        if(EntityConstants.FORCE_FRIENDLY.equals(force))
        {
            return 'F';
        }
        else if(EntityConstants.FORCE_NEUTRAL.equals(force))
        {
            return 'N';
        }
        else if(EntityConstants.FORCE_OPPOSING.equals(force))
        {
            return 'H';
        }
        else
        {
            return 'U';
        }
        
    }
}
