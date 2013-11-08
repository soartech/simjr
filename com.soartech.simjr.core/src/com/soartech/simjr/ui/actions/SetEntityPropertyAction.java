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
 * Created on Jun 20, 2007
 */
package com.soartech.simjr.ui.actions;

import java.awt.event.ActionEvent;

import org.apache.log4j.Logger;

import com.soartech.simjr.sim.Entity;

/**
 * @author ray
 */
public class SetEntityPropertyAction extends AbstractSimulationAction
{
    private static final Logger logger = Logger
            .getLogger(SetEntityPropertyAction.class);
    
    private static final long serialVersionUID = -9209479573442338190L;
    
    private String property;
    private Object value;
    
    public SetEntityPropertyAction(ActionManager actionManager, String property, Object value)
    {
        this(actionManager, property, value, null);
    }
    
    /**
     */
    public SetEntityPropertyAction(ActionManager actionManager, String property, Object value,
                                   String desc)
    {
        super(actionManager, desc != null ? desc : "Set selection's " + property + " to " + value);
        
        this.property = property;
        this.value = value;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update()
    {
        Object s = getSelectionManager().getSelectedObject();
        
        setEnabled(s instanceof Entity);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0)
    {
        Object s = getSelectionManager().getSelectedObject();
        if(!(s instanceof Entity))
        {
            return;
        }
        
        Entity e = (Entity) s;
        
        logger.info("Setting " + e.getName() + "." + property + " to " + value);
        e.setProperty(property, value);
    }

}
