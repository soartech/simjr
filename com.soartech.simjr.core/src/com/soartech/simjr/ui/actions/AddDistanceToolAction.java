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
 * Created on Jul 11, 2007
 */
package com.soartech.simjr.ui.actions;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.ui.pvd.PlanViewDisplay;
import com.soartech.simjr.ui.pvd.PlanViewDisplayProvider;

/**
 * @author ray
 */
public class AddDistanceToolAction extends AbstractSimulationAction
{
    private static final long serialVersionUID = 1502100875724697682L;
    
    public AddDistanceToolAction(ActionManager actionManager)
    {
        super(actionManager, "Add distance tool");
        
        setAcceleratorKey(KeyStroke.getKeyStroke("control D"));
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update()
    {
        Object o = getSelectionManager().getSelectedObject();
        setEnabled(o instanceof Entity && EntityTools.isVisible((Entity) o));
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0)
    {
        final Simulation sim = findService(Simulation.class);
        if(sim == null)
        {
            return;
        }
        
        final Object o = getSelectionManager().getSelectedObject();
        if(!(o instanceof Entity))
        {
            return;
        }
        
        final PlanViewDisplayProvider pvdPro = findService(PlanViewDisplayProvider.class);
        if(pvdPro == null)
        {
            return;
        }
        
        Entity start = (Entity) o;
        
        List<Entity> entities = sim.getEntities();
        Iterator<Entity> it = entities.iterator();
        
        while(it.hasNext())
        {
            Entity e = it.next();
            
            if(e == start || !EntityTools.isVisible(e))
            {
                it.remove();
            }
        }
        
        if(entities.isEmpty())
        {
            return;
        }
        
        Collections.sort(entities, EntityTools.NAME_COMPARATOR);
        
        final PlanViewDisplay pvd = pvdPro.getActivePlanViewDisplay();
        if(pvd == null)
        {
            return;
        }
        
        Object[] values = entities.toArray();
        Entity end = (Entity) JOptionPane.showInputDialog(pvd, 
                        "Choose target entity", "Distance Tool",
                        JOptionPane.INFORMATION_MESSAGE, null,
                        values, values[0]);
        
        if(end != null)
        {
            pvd.getDistanceTools().addDistanceTool(start, end);
        }
    }

}
