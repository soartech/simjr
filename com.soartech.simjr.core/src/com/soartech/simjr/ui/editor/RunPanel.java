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
 * Created on Apr 15, 2009
 */
package com.soartech.simjr.ui.editor;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.services.SimulationService;

/**
 * @author ray
 */
public class RunPanel extends JPanel implements SimulationService
{
    private static final long serialVersionUID = -1928294402222387723L;

    private JTextArea log = new JTextArea();
    
    public RunPanel()
    {
        super(new BorderLayout());
        log.setEditable(false);
        log.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(log);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public void clear()
    {
        if(!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable() {
    
                public void run()
                {
                    clear();
                }
                
            });
            return;
        }
        log.setText("");
    }
    
    public void append(final String message)
    {
        if(!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable() {
    
                public void run()
                {
                    append(message);
                }
            });
            return;
        }
        
        log.append(message);
        log.setCaretPosition(log.getText().length());        
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#shutdown()
     */
    public void shutdown() throws SimulationException
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#start(com.soartech.simjr.ProgressMonitor)
     */
    public void start(ProgressMonitor progress) throws SimulationException
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.adaptables.Adaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class<?> klass)
    {
        return Adaptables.adaptUnchecked(this, klass, false);
    }
}
