package com.soartech.simjr.ui;

import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.soartech.simjr.sim.EntityPrototype;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.SwingConstants;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;

import java.awt.Font;

public class NewFlightGroupDialog extends JDialog {
    private static final long serialVersionUID = 3205689398331417843L;
    
    private boolean userClickedOk = false;
    
    private JTextField textField_GroupName;
    private JComboBox comboBox_Prototype;
    private JComboBox comboBox_GroupSize;
    
    public static NewFlightGroupDialog select(Frame owner, List<EntityPrototype> prototypes, EntityPrototype defaultPrototype,
                                        Integer[] groupSizes, Integer defaultGroupSize, String defaultGroupName)
    {
        List<EntityPrototype> sortedPrototypes = new ArrayList<EntityPrototype>(prototypes);
        Collections.sort(sortedPrototypes, new Comparator<Object>()
        {
            public int compare(Object o1, Object o2)
            {
                return o1.toString().compareTo(o2.toString());
            }
        });
        
        final NewFlightGroupDialog dialog = new NewFlightGroupDialog(sortedPrototypes.toArray(), defaultPrototype, groupSizes, defaultGroupSize, defaultGroupName);
        if(owner != null)
        {
            final Rectangle frame = owner.getBounds();
            dialog.setLocation(frame.x + (frame.width - dialog.getWidth()) / 2, frame.y + (frame.height - dialog.getHeight()) / 2);
        }
        dialog.setVisible(true);
        return dialog;
    }
    
    private NewFlightGroupDialog(Object[] sortedPrototypes, EntityPrototype defaultPrototype, Integer[] groupSizes, Integer defaultGroupSize, String defaultGroupName)
    {
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("New Flight Group");
        setModalityType(ModalityType.APPLICATION_MODAL);
        setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        
        JPanel panel_Prototype = new JPanel();
        panel_Prototype.setLayout(null);
        
        JLabel lblEntityType = new JLabel("Entity Type:");
        lblEntityType.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblEntityType.setBounds(12, 8, 88, 16);
        lblEntityType.setHorizontalAlignment(SwingConstants.LEFT);
        panel_Prototype.add(lblEntityType);
        
        comboBox_Prototype = new JComboBox(sortedPrototypes);
        comboBox_Prototype.setBounds(100, 5, 176, 22);
        comboBox_Prototype.setSelectedItem(defaultPrototype);
        panel_Prototype.add(comboBox_Prototype);
        
        JPanel panel_GroupSize = new JPanel();
        panel_GroupSize.setLayout(null);
        
        JLabel lblGroupSize = new JLabel("Group Size:");
        lblGroupSize.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblGroupSize.setBounds(12, 8, 87, 16);
        lblGroupSize.setHorizontalAlignment(SwingConstants.LEFT);
        panel_GroupSize.add(lblGroupSize);
        
        comboBox_GroupSize = new JComboBox(groupSizes);
        comboBox_GroupSize.setBounds(100, 5, 60, 22);
        comboBox_GroupSize.setSelectedItem(defaultGroupSize);
        panel_GroupSize.add(comboBox_GroupSize);
        
        JPanel panel_Buttons = new JPanel();
        
        JButton btnOkButton = new JButton("OK");
        btnOkButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                userClickedOk = true;
                NewFlightGroupDialog.this.dispose();
            }
        });
        panel_Buttons.add(btnOkButton);
        
        JButton btnCancelButton = new JButton("Cancel");
        btnCancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                NewFlightGroupDialog.this.dispose();
            }
        });
        panel_Buttons.add(btnCancelButton);
        
        JPanel panel_GroupName = new JPanel();
        panel_GroupName.setLayout(null);
        
        JLabel lblGroupName = new JLabel("Group Name:");
        lblGroupName.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblGroupName.setBounds(12, 8, 89, 16);
        lblGroupName.setHorizontalAlignment(SwingConstants.LEFT);
        panel_GroupName.add(lblGroupName);
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(
            groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
                    .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
                        .addComponent(panel_GroupName, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panel_GroupSize, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                        .addComponent(panel_Prototype, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panel_Buttons, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap(71, Short.MAX_VALUE))
        );
        groupLayout.setVerticalGroup(
            groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addGap(1)
                    .addComponent(panel_Prototype, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(panel_GroupSize, GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(panel_GroupName, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addComponent(panel_Buttons, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
        
        textField_GroupName = new JTextField(defaultGroupName);
        textField_GroupName.setBounds(100, 5, 171, 22);
        textField_GroupName.setHorizontalAlignment(SwingConstants.LEFT);
        panel_GroupName.add(textField_GroupName);
        textField_GroupName.setColumns(15);
        
        getContentPane().setLayout(groupLayout);
        this.pack();
    }
    
    public boolean getUserCancelled()
    {
        return !userClickedOk;
    }

    public EntityPrototype getPrototype()
    {
        return (EntityPrototype)comboBox_Prototype.getSelectedItem();
    }

    public int getGroupSize()
    {
        return (Integer)comboBox_GroupSize.getSelectedItem();
    }

    public String getGroupName()
    {
        return textField_GroupName.getText().trim();
    }
}
