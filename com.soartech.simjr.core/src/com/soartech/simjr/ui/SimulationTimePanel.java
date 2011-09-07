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
package com.soartech.simjr.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.sim.ScalableTickPolicy;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.SimulationListenerAdapter;

/**
 * @author ray
 */
public class SimulationTimePanel extends JPanel
{
    private static final long serialVersionUID = 316622550743340039L;
    
    private ServiceManager services;
    private Simulation sim;
    private JComboBox factor = new JComboBox(
            new Object[] { new ScaleFactor(1), 
                           new ScaleFactor(2),
                           new ScaleFactor(5),
                           new ScaleFactor(10),
                           new ScaleFactor(20),
                           new ScaleFactor(50),
                           new ScaleFactor(100),
                           new ScaleFactor(250),
                           new ScaleFactor(500),
                           new ScaleFactor(1000)});
    
    private JLabel label = new JLabel("              ");
    
    public SimulationTimePanel(ServiceManager services)
    {
        super(new BorderLayout());
        
        this.services = services;
        this.sim = this.services.findService(Simulation.class);
        this.sim.addListener(new Listener());
        
        this.setMaximumSize(new Dimension(200, 28));
        
        add(label, BorderLayout.CENTER);
        setBorder(BorderFactory.createLoweredBevelBorder());
        
        factor.setEditable(false);
        factor.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent arg0)
            {
                update();
            }});
        add(factor, BorderLayout.WEST);
        
        update();
    }
    
    public void setScaleAtLeast(final int f)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                ScaleFactor selected = null;
                for (int i = 0; i < factor.getItemCount(); ++i)
                {
                    selected = (ScaleFactor) factor.getItemAt(i);
                    if (f <= selected.factor)
                    {
                        factor.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });
    }
    
    private void update()
    {
        final String t = String.format("  %5.1fs", sim.getTime());
        label.setText(t + 
                (sim.isPaused() ? " (paused)  " : "  "));
        
        final ScaleFactor f = (ScaleFactor) factor.getSelectedItem();
        final ScalableTickPolicy tickPolicy = Adaptables.adapt(sim, ScalableTickPolicy.class);
        if(tickPolicy != null)
        {
            tickPolicy.setTimeFactor(f.factor);
        }
        factor.setEnabled(tickPolicy != null);
    }
    
    private void safeUpdate()
    {
        SwingUtilities.invokeLater(new Runnable() {

            public void run()
            {
                update();
            }});
    }
    
    private class ScaleFactor
    {
        public final int factor;
        
        public ScaleFactor(int factor)
        {
            this.factor = factor;
        }
        public String toString()
        {
            return factor + "x";
        }
    }
    
    private class Listener extends SimulationListenerAdapter
    {
        /* (non-Javadoc)
         * @see com.soartech.simjr.SimulationAdapter#onPause()
         */
        @Override
        public void onPause()
        {
            safeUpdate();
        }

        /* (non-Javadoc)
         * @see com.soartech.simjr.SimulationAdapter#onStart()
         */
        @Override
        public void onStart()
        {
            safeUpdate();
        }

        /* (non-Javadoc)
         * @see com.soartech.simjr.SimulationAdapter#onTick(double)
         */
        public void onTick(double dt)
        {
            safeUpdate();
        }

        /* (non-Javadoc)
         * @see com.soartech.simjr.sim.SimulationListenerAdapter#onTimeSet(double)
         */
        @Override
        public void onTimeSet(double oldTime)
        {
            safeUpdate();
        }
    }
}
