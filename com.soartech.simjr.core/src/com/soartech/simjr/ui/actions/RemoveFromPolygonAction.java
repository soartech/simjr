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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JOptionPane;

import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.entities.AbstractPolygon;
import com.soartech.simjr.ui.SimulationMainFrame;

/**
 * @author ray
 */
public class RemoveFromPolygonAction extends AbstractSimulationAction
{
    private static final long serialVersionUID = -4856565292519302309L;
    
    private static final AbstractPolygon ALL_POLYGONS = new AbstractPolygon("Remove from all") {

        /* (non-Javadoc)
         * @see com.soartech.simjr.sim.entities.AbstractPolygon#isClosed()
         */
        @Override
        public boolean isClosed()
        {
            return false;
        }};
    
    public RemoveFromPolygonAction(ActionManager actionManager)
    {
        super(actionManager, "Remove from Route/Area", Entity.class, true);
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update()
    {
        Object o = getSelectionManager().getSelectedObject();
        setEnabled(o instanceof Entity);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0)
    {
        Object o = getSelectionManager().getSelectedObject();
        if(!(o instanceof Entity))
        {
            return;
        }
        Entity e = (Entity) o;
        String name = e.getName();
        
        List<AbstractPolygon> polygons = AbstractPolygon.getPolygonsContainingPoint(e.getSimulation(), name);
        if(polygons.isEmpty())
        {
            return;
        }
        
        Collections.sort(polygons, new Comparator<AbstractPolygon>() {

            public int compare(AbstractPolygon o1, AbstractPolygon o2)
            {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }});
        polygons.add(0, ALL_POLYGONS);
        
        Object[] values = polygons.toArray();
        SimulationMainFrame frame = findService(SimulationMainFrame.class);
        AbstractPolygon p = (AbstractPolygon) JOptionPane.showInputDialog(frame, 
                        "Choose route/area", "Remove from route/area",
                        JOptionPane.INFORMATION_MESSAGE, null,
                        values, values[0]);
        
        if(p != null)
        {
            synchronized (e.getSimulation().getLock())
            {
                if(p == ALL_POLYGONS)
                {
                    for(AbstractPolygon ap : polygons)
                    {
                        ap.removePoint(name);
                    }
                }
                else
                {
                    p.removePoint(name);
                }
            }
        }
        
        update();
    }
}
