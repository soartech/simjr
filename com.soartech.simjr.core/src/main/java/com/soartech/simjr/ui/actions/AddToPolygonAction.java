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
 * Created on Sep 19, 2007
 */
package com.soartech.simjr.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.entities.AbstractPolygon;
import com.soartech.simjr.ui.SimulationMainFrame;

/**
 * @author ray
 */
public class AddToPolygonAction extends AbstractSimulationAction
{
    private static final long serialVersionUID = -4856565292519302309L;
    
    public AddToPolygonAction(ActionManager actionManager)
    {
        super(actionManager, "Add to Route/Area", Entity.class, true);
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update()
    {
        Object o = getSelectionManager().getSelectedObject();
        setEnabled(o instanceof Entity && ((Entity) o).hasPosition());
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0)
    {
        SimulationMainFrame frame = findService(SimulationMainFrame.class);
        
        Object o = getSelectionManager().getSelectedObject();
        if(!(o instanceof Entity))
        {
            return;
        }
        Entity e = (Entity) o;
        if(!e.hasPosition())
        {
            return;
        }
        
        List<AbstractPolygon> polygons = getItems();
        if(polygons.isEmpty())
        {
            return;
        }
        
        Object[] values = polygons.toArray();
        AbstractPolygon p = (AbstractPolygon) JOptionPane.showInputDialog(frame, 
                        "Choose route/area", "Add to route/area",
                        JOptionPane.INFORMATION_MESSAGE, null,
                        values, values[0]);
        
        if(p != null)
        {
            synchronized (e.getSimulation().getLock())
            {
                p.addPoint(e.getName());
            }
        }
        
        update();
    }
    
    private List<AbstractPolygon> getItems()
    {
        List<AbstractPolygon> r = new ArrayList<AbstractPolygon>();
        
        final Simulation sim = findService(Simulation.class);
        if(sim == null)
        {
            return r;
        }
        
        synchronized (sim.getLock())
        {
            for(Entity e : sim.getEntities())
            {
                final AbstractPolygon polygon = Adaptables.adapt(e, AbstractPolygon.class);
                if(polygon != null)
                {
                    r.add(polygon);
                }
            }
        }
        
        Collections.sort(r, AbstractPolygon.NAME_COMPARATOR);
        
        return r;
    }
}
