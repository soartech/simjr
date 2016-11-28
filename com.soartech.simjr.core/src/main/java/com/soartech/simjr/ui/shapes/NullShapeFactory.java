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
 * Created on Sep 26, 2007
 */
package com.soartech.simjr.ui.shapes;

import com.soartech.shapesystem.Shape;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.swing.SwingPrimitiveRendererFactory;
import com.soartech.simjr.sim.Entity;

/**
 * @author ray
 */
public class NullShapeFactory implements EntityShapeFactory
{
    public static final EntityShapeFactory FACTORY = new NullShapeFactory();
    
    private final EntityShapeFactory internal;
    
    public NullShapeFactory()
    {
        this.internal = ImageEntityShape.create("images/shapes/unknown-entity.png");
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.shapes.EntityShapeFactory#initialize(com.soartech.shapesystem.swing.SwingPrimitiveRendererFactory)
     */
    public void initialize(SwingPrimitiveRendererFactory rendererFactory)
    {
        this.internal.initialize(rendererFactory);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.shapes.EntityShapeFactory#create(com.soartech.simjr.Entity, com.soartech.shapesystem.ShapeSystem)
     */
    public EntityShape create(Entity entity, ShapeSystem system)
    {
        return internal.create(entity, system);
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.shapes.EntityShapeFactory#createSelection(java.lang.String, com.soartech.simjr.Entity)
     */
    public Shape createSelection(String id, Entity selected)
    {
        return internal.createSelection(id, selected);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "none";
    }

    @Override
    public SwingPrimitiveRendererFactory getRendererFactory()
    {
        return this.internal.getRendererFactory();
    }

    
}
