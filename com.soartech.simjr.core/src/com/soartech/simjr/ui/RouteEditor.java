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
 * Created on Sep 20, 2007
 */
package com.soartech.simjr.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jidesoft.popup.JidePopup;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.entities.AbstractPolygon;

/**
 * @author ray
 */
public class RouteEditor extends JPanel
{
    private static final long serialVersionUID = 2478667900472212227L;

    private AbstractPolygon route;
    
    private JidePopup popup;
    private DefaultListModel listModel = new DefaultListModel();
    private JList list = new JList(listModel);
    private AbstractAction upAction = new AbstractAction("Move Up") {

        private static final long serialVersionUID = 3201009709921102514L;

        public void actionPerformed(ActionEvent arg0)
        {
            moveUp();
        }};
    private AbstractAction downAction = new AbstractAction("Move Down") {

        private static final long serialVersionUID = 6588530952903664502L;

        public void actionPerformed(ActionEvent arg0)
        {
            moveDown();
        }};
        
    private AbstractAction removeAction = new AbstractAction("Remove Point") {

        private static final long serialVersionUID = 6588530952903664502L;

        public void actionPerformed(ActionEvent arg0)
        {
            removePoint();
        }};
        
    private AbstractAction AddAction = new AbstractAction("Add Point") {

        private static final long serialVersionUID = 6588530952903664502L;

        public void actionPerformed(ActionEvent arg0)
        {
            addPoint();
        }};
    
    public static void showPopupEditor(Component owner, Point point, AbstractPolygon route)
    {
        JidePopup popup = new JidePopup();
        popup.setMovable(true);
        popup.setResizable(true);
        popup.setOwner(owner);
        popup.setContentPane(new RouteEditor(route, popup));
        popup.setFocusable(true);
        popup.setMovable(true);
        popup.showPopup(point.x, point.y);
        popup.removeExcludedComponent(owner);
    }
        
    public RouteEditor(AbstractPolygon route, JidePopup popup)
    {
        super(new BorderLayout());
        
        this.route = route;
        
        this.popup = popup;
        
        add(new JLabel("Editing route: " + route.getName()), BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);
        
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e)
            {
                updateActions();
            }});
        
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(1, 5));
        buttons.add(new JButton(upAction));
        buttons.add(new JButton(downAction));
        buttons.add(new JSeparator(SwingConstants.VERTICAL));
        buttons.add(new JButton(AddAction));
        buttons.add(new JButton(removeAction));
        
        add(buttons, BorderLayout.SOUTH);
        
        for(String point : route.getPointNames())
        {
            listModel.addElement(point);
        }
        updateActions();
    }
    
    private void updateActions()
    {
        upAction.setEnabled(false);
        downAction.setEnabled(false);
        removeAction.setEnabled(false);
        
        int index = list.getSelectedIndex();
        if(index == -1)
        {
            return;
        }
        
        removeAction.setEnabled(true);
        upAction.setEnabled(index > 0);
        downAction.setEnabled(index < listModel.getSize() - 1);
    }
    
    private void applyChanges()
    {
        List<String> newPoints = new ArrayList<String>();
        for(Object o : listModel.toArray())
        {
            newPoints.add(o.toString());
        }
        
        route.setPointNames(newPoints);
    }

    private void moveUp()
    {
        int index = list.getSelectedIndex();
        if(index == -1 || index == 0)
        {
            return;
        }
        
        Object temp = listModel.get(index);
        listModel.set(index, listModel.get(index - 1));
        listModel.set(index - 1, temp);
        
        list.setSelectedIndex(index - 1);
        
        applyChanges();
        updateActions();
    }
    
    private void moveDown()
    {
        int index = list.getSelectedIndex();
        if(index == -1 || index == listModel.getSize() - 1)
        {
            return;
        }
        
        Object temp = listModel.get(index);
        listModel.set(index, listModel.get(index + 1));
        listModel.set(index + 1, temp);
        
        list.setSelectedIndex(index + 1);
        
        applyChanges();
        updateActions();
    }
    
    protected void removePoint()
    {
        int index = list.getSelectedIndex();
        if(index == -1)
        {
            return;
        }
        
        listModel.remove(index);
        
        applyChanges();
    }
    
    private void addPoint()
    {
        Simulation sim = route.getEntity().getSimulation();
        
        List<Entity> r = new ArrayList<Entity>();
        for(Entity e : sim.getEntities())
        {
            final AbstractPolygon polygon = Adaptables.adapt(e, AbstractPolygon.class);
            if(polygon == null && e.hasPosition())
            {
                r.add(e);
            }
        }
        
        if(r.isEmpty())
        {
            return;
        }
        
        Collections.sort(r, new Comparator<Entity>() {

            public int compare(Entity o1, Entity o2)
            {
                // Push control points to the top of the list
                final boolean o1Control = o1.getPrototype().hasSubcategory("control");
                final boolean o2Control = o2.getPrototype().hasSubcategory("control");
                if(o1Control && !o2Control)
                {
                    return -1;
                }
                else if(o2Control && !o2Control)
                {
                    return 1;
                }
                // Then sort by name
                return EntityTools.NAME_COMPARATOR.compare(o1, o2);
            }});
        
        if(popup != null) 
        {
            popup.setTransient(false);
        }
        Object[] values = r.toArray();
        Entity e = (Entity) JOptionPane.showInputDialog(this, 
                        "Choose point", "Add point to route",
                        JOptionPane.INFORMATION_MESSAGE, null,
                        values, values[0]);
        if(popup != null)
        {
            popup.setTransient(true);
        }
        if(e != null)
        {
            listModel.addElement(e.getName());
        }
        
        applyChanges();
    }
}
