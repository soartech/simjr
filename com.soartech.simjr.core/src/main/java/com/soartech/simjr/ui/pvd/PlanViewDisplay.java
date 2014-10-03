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
package com.soartech.simjr.ui.pvd;

import java.awt.Point;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.shapesystem.CoordinateTransformer;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.ui.ObjectContextMenu;
import com.soartech.simjr.ui.SelectionManager;
import com.soartech.simjr.ui.shapes.EntityShape;

/**
 * @author ray
 */
public class PlanViewDisplay
{
    private static final Logger logger = LoggerFactory.getLogger(PlanViewDisplay.class);
    
    private final Simulation sim;
    private final ServiceManager app;
    
    private final PvdController controller;
    private final PvdView view;
    
    public PlanViewDisplay(ServiceManager app, PlanViewDisplay toCopy)
    {
        this(app, toCopy, null);
    }
    
    public PlanViewDisplay(ServiceManager app, PlanViewDisplay toCopy, PvdController controller)
    {
        this.app = app;
        this.sim = this.app.findService(Simulation.class);
        
        if (controller == null)
        {
            this.controller = new PvdController();
        }
        else
        {
            this.controller = controller;
        }
        
        this.view = new PvdView(app, sim, new Runnable() {
            public void run() {
                // Align the view with the locked entity, if any, just before a redraw.
                synchronized (sim.getLock())
                {
                    Entity lockEntity = PlanViewDisplay.this.controller.getLockEntity();
                    if (lockEntity != null)
                    {
                        Double agl = (Double) lockEntity.getProperty(EntityConstants.PROPERTY_AGL);
                        view.getTransformer().setRotation(-lockEntity.getHeading() + Math.PI/2);
                        view.jumpToPosition(EntityShape.adjustPositionForShadow(lockEntity.getPosition(), agl), false);
                    }
                }
            }
        });
        
        if (toCopy != null)
        {
            view.setMapImage(toCopy.getView().getMapImage());
        }
        
        this.controller.attachToView(this.view, this.sim, this.app);
    }

    public void dispose()
    {
        logger.info("Disposing PVD " + this);
        
        // controller.dispose();
        view.dispose();
    }

//    public PvdController getController()
//    {
//        return controller;
//    }
    
    public PvdView getView()
    {
        return view;
    }
    
    public CoordinateTransformer getTransformer()
    {
        return view.getTransformer();
    }
    
    public Point getContextMenuPoint()
    {
        return controller.getContextMenuPoint();
    }
    
    public void setContextMenu(ObjectContextMenu contextMenu)
    {
        controller.setContextMenu(contextMenu);
    }
    
    public void setContextMenuEnabled(boolean enabled)
    {
        controller.setContextMenuEnabled(enabled);
    }
    
    public Entity getLockEntity()
    {
        return controller.getLockEntity();
    }
    
    public void setLockEntity(Entity lockEntity)
    {
        controller.setLockEntity(lockEntity);
    }
    
    /**
     * @return A list of all the entities that are selected.
     */
    static List<Entity> getSelectedEntities(ServiceManager app)
    {
        return Adaptables.adaptCollection(SelectionManager.findService(app).getSelection(), Entity.class);
    }
    
    /**
     * @return The first selected entity, or <code>null</code> if no entities are selected.
     */
    static Entity getSelectedEntity(ServiceManager app)
    {
        final List<Entity> selection = getSelectedEntities(app);
        return !selection.isEmpty() ? selection.get(0) : null;
    }
}
