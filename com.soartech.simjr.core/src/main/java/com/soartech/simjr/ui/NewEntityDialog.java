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
 * Created on Jul 6, 2007
 */
package com.soartech.simjr.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.soartech.math.Angles;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.entities.DamageStatus;

/**
 * Dialog used by the "New" scripts.
 * 
 * @author ray
 */
public class NewEntityDialog extends JDialog
{
    private static final long serialVersionUID = -6326500527566215504L;

    private ServiceManager services;
    
    public boolean cancelled = false;
    public String name = "";
    public String force = EntityConstants.FORCE_OPPOSING;
    public double orientation = Angles.navRadiansToMathRadians(0);
    public DamageStatus damage = DamageStatus.intact;
    
    private JTextField nameField = new JTextField(name);
    private JComboBox<String> forceField = new JComboBox<String>(EntityConstants.ALL_FORCES);
    private JComboBox<DamageStatus> damageField = new JComboBox<DamageStatus>(DamageStatus.values());
    private JTextField headingField = new JTextField("" + Math.toDegrees(Angles.navRadiansToMathRadians(orientation)));
    
    public NewEntityDialog(ServiceManager services, JFrame parentFrame)
    {
        super(parentFrame, "Create New Entity", true);
        
        this.services = services;
        
        setSize(400, 150);
        setLocationRelativeTo(parentFrame);
        
        getContentPane().setLayout(new GridLayout(5, 2));
        
        getContentPane().add(new JLabel("Name:"));
        getContentPane().add(nameField);

        getContentPane().add(new JLabel("Force:"));
        getContentPane().add(forceField);
        forceField.setEditable(false);
        forceField.setSelectedItem(force);
        
        getContentPane().add(new JLabel("Damage:"));
        getContentPane().add(damageField);
        damageField.setEditable(false);
        damageField.setSelectedItem(damage);
        
        getContentPane().add(new JLabel("Heading (degrees):"));
        getContentPane().add(headingField);
        
        JButton ok = new JButton(new AbstractAction("Ok") {

            private static final long serialVersionUID = -8793662384191695735L;

            public void actionPerformed(ActionEvent arg0)
            {
                onOk();
            }});
        
        getContentPane().add(ok);
        getRootPane().setDefaultButton(ok);
        
        JButton cancel = new JButton(new AbstractAction("Cancel") {

            private static final long serialVersionUID = 1307058943954289385L;

            public void actionPerformed(ActionEvent arg0)
            {
                cancelled = true;
                NewEntityDialog.this.dispose();
            }});
        
        getContentPane().add(cancel);
    }
    
    private void onOk()
    {
        name = nameField.getText();
        
        if(name.length() == 0)
        {
            showError("Entity name may not be empty");
            return;            
        }
        
        Simulation sim = services.findService(Simulation.class);
        if(null != sim && null != sim.getEntity(name))
        {
            showError("An entity with name '" + name + "' already exists");
            return;
        }
        
        force = (String) forceField.getSelectedItem();
        damage = (DamageStatus) damageField.getSelectedItem();
        
        try
        {
            double heading = Double.parseDouble(headingField.getText());
            orientation = Angles.navRadiansToMathRadians(Math.toRadians(heading));
        }
        catch(NumberFormatException e)
        {
            showError("Invalid heading " + headingField.getText());
            return;
        }
        dispose();
    }
    
    private void showError(String message)
    {
        JOptionPane.showMessageDialog(this, message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
        cancelled = true;
        dispose();        
    }
}
