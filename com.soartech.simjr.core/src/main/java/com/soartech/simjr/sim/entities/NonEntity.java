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
 * Created on Aug 2, 2007
 */
package com.soartech.simjr.sim.entities;

import java.util.Map;

import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPrototype;

/**
 * A NonEntity is essentially a placeholder entity use to represent
 * some external object, like a disembodied Soar agent in the
 * simulation.
 * 
 * <p>Be sure to set the category property.
 * 
 * @author ray
 */
public class NonEntity extends AbstractEntity
{
    /**
     * Construct a new non-entity
     * 
     * @param name The name of the entity
     */
    public NonEntity(String name, EntityPrototype prototype)
    {
        super(name, prototype);
        
        setProperty(EntityConstants.PROPERTY_VISIBLE, false);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.entities.AbstractEntity#hasPosition()
     */
    @Override
    public boolean hasPosition()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.entities.AbstractEntity#updateProperties(java.util.Map)
     */
    @Override
    protected void updateProperties(Map<String, Object> properties)
    {
        // Override and do nothing. This eliminates the position, velocity,
        // etc properties added by the base class.
    }

}
