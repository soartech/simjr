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
 * Created on Nov 12, 2008
 */
package com.soartech.simjr.ui.pvd;

import java.util.ArrayList;
import java.util.List;

import com.soartech.shapesystem.ShapeSystem;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.ui.actions.ActionManager;

public class DistanceToolManager
{
    private final ServiceManager services;
    private final ShapeSystem shapeSystem;
    private final List<DistanceToolShape> distanceTools = new ArrayList<DistanceToolShape>();
   
    
    /**
     * @param services
     * @param shapeSystem
     */
    public DistanceToolManager(ServiceManager services, ShapeSystem shapeSystem)
    {
        this.services = services;
        this.shapeSystem = shapeSystem;
    }

    public void addDistanceTool(Entity start, Entity end)
    {
        DistanceToolShape shape = new DistanceToolShape(start, end);
        
        distanceTools.add(shape);
        shapeSystem.addShape(shape);
        ActionManager.update(services);
    }
    
    public void clearDistanceTools()
    {
        for(DistanceToolShape shape : distanceTools)
        {
            shapeSystem.removeShape(shape.getName());
        }
        distanceTools.clear();
        ActionManager.update(services);
    }
    
    public boolean hasDistanceTools()
    {
        return !distanceTools.isEmpty();
    }
    

}
