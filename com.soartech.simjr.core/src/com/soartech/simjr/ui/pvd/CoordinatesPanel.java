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
 * Created on Jun 11, 2007
 */
package com.soartech.simjr.ui.pvd;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXPanel;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;

/**
 * @author ray
 */
public class CoordinatesPanel extends JXPanel
{
    private static final long serialVersionUID = -8184639289927366228L;

    private PlanViewDisplay pvd = null;
    private JComboBox combo = new JComboBox(new Object[] { new Mgrs(), new Meters(), new LatLon() });
    private JLabel label = new JLabel("");
    private MouseMotionListener mouseListener = new MouseMotionAdapter() {

        public void mouseMoved(MouseEvent e)
        {
            update();
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseMotionAdapter#mouseDragged(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseDragged(MouseEvent arg0)
        {
            update();
        }
    };
        
    public CoordinatesPanel()
    {
        super(new BorderLayout());
        
        combo.setEditable(false);
        combo.setSelectedIndex(0);
        
        combo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0)
            {
                update();
            }});
        
        add(combo, BorderLayout.WEST);
        add(label, BorderLayout.CENTER);
        
        setAlpha(0.7f);
    }
    
    public void setActivePvd(PlanViewDisplay newPvd)
    {
        if(pvd != null)
        {
            pvd.removeMouseMotionListener(mouseListener);
        }
        
        pvd = newPvd;
        
        pvd.addMouseMotionListener(mouseListener);
        
        update();
    }
    
    private void update()
    {
        if(pvd == null)
        {
            label.setText("");
            return;
        }
        
        java.awt.Point p = pvd.getMousePosition();
        if(p == null)
        {
            return;
        }
        
        Vector3 meters = pvd.getTransformer().screenToMeters(p.x, p.y);
        CoordSystem sys = (CoordSystem) combo.getSelectedItem();
        label.setText("   " + sys.transform(meters) + "   ");
    }
    
    private interface CoordSystem
    {
        String transform(Vector3 meters);
    }
    
    private static class Meters implements CoordSystem
    {
        public String transform(Vector3 meters)
        {
            return String.format("%8.3f, %8.3f", 
                    new Object[] { meters.x, meters.y });
        }

        public String toString()
        {
            return "Meters";
        }
    }
    
    private class LatLon implements CoordSystem
    {
        public String transform(Vector3 meters)
        {
            Geodetic.Point gp = pvd.getTerrain().toGeodetic(meters);
            double lat = Math.toDegrees(gp.latitude);
            double lon = Math.toDegrees(gp.longitude);
            
            return String.format("%8.3f, %8.3f", new Object[]{ lat, lon });
        }
        
        public String toString()
        {
            return "Lat/Lon";
        }
    }
    
    private class Mgrs implements CoordSystem
    {
        public String transform(Vector3 meters)
        {
            return pvd.getTerrain().toMgrs(meters);
        }
        public String toString()
        {
            return "MGRS";
        }
    }
}
