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
 * Created on Mar 30, 2009
 */
package com.soartech.simjr.ui.editor;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.undo.UndoableEdit;

import net.miginfocom.swing.MigLayout;

import com.soartech.simjr.scenario.EntityElement;
import com.soartech.simjr.scenario.EntityElementList;
import com.soartech.simjr.scenario.Model;
import com.soartech.simjr.scenario.ModelChangeEvent;
import com.soartech.simjr.scenario.ModelChangeListener;
import com.soartech.simjr.scenario.TerrainElement;
import com.soartech.simjr.scenario.TerrainImageElement;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.Simulation;

/**
 * @author ray
 */
public class TerrainPanel extends JPanel implements ModelChangeListener
{
    private static final long serialVersionUID = -973913392305900522L;
    
    private final ScenarioEditorServiceManager app;    private final Simulation sim;
    
    private final JTextField latField = new JTextField(15);
    private final JTextField lonField = new JTextField(15);
    private final JTextField metersPerPixelField = new JTextField(15);
    
    public TerrainPanel(ScenarioEditorServiceManager app)
    {
        super(new MigLayout());
        
        this.app = app;
        this.sim = app.findService(Simulation.class);
        
        add(new JLabel("Origin latitude"));
        latField.setHorizontalAlignment(JTextField.RIGHT);
        add(latField, "wrap");
        new EntryCompletionHandler(latField) {

            @Override
            public boolean verify(JComponent input)
            {
                updateLatitude();
                return true;
            }};
        add(new JLabel("Origin longitude"));
        
        lonField.setHorizontalAlignment(JTextField.RIGHT);
        add(lonField, "wrap");
        new EntryCompletionHandler(lonField) {

            @Override
            public boolean verify(JComponent input)
            {
                updateLongitude();
                return true;
            }};
                    
        add(new JLabel("Meters per pixel"));
        metersPerPixelField.setHorizontalAlignment(JTextField.RIGHT);
        add(metersPerPixelField, "wrap");
        new EntryCompletionHandler(metersPerPixelField) {

            @Override
            public boolean verify(JComponent input)
            {
                updateMetersPerPixel();
                return true;
            }};
                
        
        app.getModel().addModelChangeListener(this);
        onModelChanged(null);
    }
    
    private void updateLatitude()
    {
        final TerrainElement t = app.getModel().getTerrain();

        try
        {
            final double newLat = Double.valueOf(latField.getText());
            if(newLat < -90 || newLat > 90)
            {
                throw new NumberFormatException("Latitude out of bounds");
            }
            
            final UndoableEdit edit = t.setOrigin(newLat, t.getOriginLongitude());
            updateEntityLocations(edit);
            
            if(edit != null)
            {
                app.findService(UndoService.class).addEdit(edit);
            }
        }
        catch(NumberFormatException e)
        {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    private void updateLongitude()
    {
        final TerrainElement t = app.getModel().getTerrain();
        
        try
        {
            final double newLon = Double.valueOf(lonField.getText());
            if(newLon < -180 || newLon > 180)
            {
                throw new NumberFormatException("Longitude out of bounds");
            }
            
            final UndoableEdit edit = t.setOrigin(t.getOriginLatitude(), newLon);

            updateEntityLocations(edit);
            
            if(edit != null)
            {
                app.findService(UndoService.class).addEdit(edit);
            }
        }
        catch(NumberFormatException e)
        {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
        
    private void updateEntityLocations(UndoableEdit originEdit)
    {
        // Update element locations
        EntityElementList eEList = app.getModel().getEntities();
        for(EntityElement ee: eEList.getEntities())
        {
            Entity entity = sim.getEntity(ee.getName());
            final UndoableEdit entityEdit = ee.getLocation().setLocation(sim.getTerrain().toGeodetic(entity.getPosition()));
            if((originEdit != null) && (entityEdit != null))
            {
                originEdit.addEdit(entityEdit);
            }
        }
    }
    
    private void updateMetersPerPixel()
    {
        final TerrainImageElement t = app.getModel().getTerrain().getImage();
        try
        {
            final double mpp = Double.valueOf(metersPerPixelField.getText());
            if(mpp <= 0.0)
            {
                throw new NumberFormatException("Meters per pixel must be positive and non-zero");
            }
            
            UndoableEdit edit = t.setImageMetersPerPixel(mpp);
            if(edit != null)
            {
                app.findService(UndoService.class).addEdit(edit);
            }
        }
        catch(NumberFormatException e)
        {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }

    public void onModelChanged(ModelChangeEvent e)
    {
        if(e == null || e.property.equals(Model.LOADED) || e.property.equals(TerrainElement.ORIGIN) ||
           TerrainImageElement.isProperty(e.property))
        {
            latField.setText(Double.toString(app.getModel().getTerrain().getOriginLatitude()));
            lonField.setText(Double.toString(app.getModel().getTerrain().getOriginLongitude()));
            metersPerPixelField.setText(Double.toString(app.getModel().getTerrain().getImage().getImageMetersPerPixel()));
        }
    }
    
    
}
