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
 * Created on May 29, 2007
 */
package com.soartech.simjr.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.SimulationListenerAdapter;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.actions.CenterViewOnEntityAction;

/**
 * @author ray
 */
public class EntityListPanel extends DefaultSingleCDockable
{
    private static final long serialVersionUID = -6833121917989575975L;

    private ServiceManager services;
    private Simulation sim;
    private ObjectContextMenu contextMenu;
    
    private DefaultMutableTreeNode root = new DefaultMutableTreeNode("Entities");
    private DefaultTreeModel treeModel = new DefaultTreeModel(root);
    private JTree entityTree = new JTree(treeModel) {

        private static final long serialVersionUID = -2987687108569893057L;

        /* (non-Javadoc)
         * @see javax.swing.JComponent#paint(java.awt.Graphics)
         */
        @Override
        public void paint(Graphics g)
        {
            // Since the renderers access the entities we have to lock the sim
            // while painting.
            synchronized (sim.getLock())
            {
                super.paint(g);
            }
        }};
        
    private Map<String, DefaultMutableTreeNode> categoryNodes = new HashMap<String, DefaultMutableTreeNode>();
    private Map<Entity, DefaultMutableTreeNode> entityNodes = new HashMap<Entity, DefaultMutableTreeNode>();
    private boolean ignoreTreeSelectionChange = false;
    private Component component;
    
    public EntityListPanel(ServiceManager services)
    {
        super("EntityListPanel");
        this.services = services;
        this.sim = services.findService(Simulation.class);
        this.contextMenu = new ObjectContextMenu(this.services);
        
        //DF settings
        setLayout(new BorderLayout());
        setCloseable(true);
        setMinimizable(false);
        setExternalizable(true);
        setMaximizable(true);
        setTitleText("Entity List");
        setResizeLocked(true);
        
        this.sim.addListener(new SimListener());
        SelectionManager.findService(this.services).addListener(new SelectionManagerListener() {

            public void selectionChanged(Object source)
            {
                appSelectionChanged(source);
            }});
        
        entityTree.setRootVisible(false);
        entityTree.addMouseListener(new DoubleClickHandler());
        entityTree.addMouseListener(new ContextMenuClickHandler());
        
        entityTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        entityTree.addTreeSelectionListener(new TreeSelectionListener(){

            public void valueChanged(TreeSelectionEvent e)
            {
                listSelectionChanged();
            }});
        
        for(String category : EntityConstants.ALL_CATEGORIES)
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(category, true);
            root.add(node);
            categoryNodes.put(category, node);
        }
        
        component = new JScrollPane(entityTree);
        add(component, BorderLayout.CENTER);
        
        update();
    }
    
    public Component getComponent()
    {
        return component;
    }
    
    private DefaultMutableTreeNode getCategoryNode(Entity e)
    {
        String category = (String) e.getProperty(EntityConstants.PROPERTY_CATEGORY);

        DefaultMutableTreeNode node = categoryNodes.get(category);
        if(node == null)
        {
            if(category != null)
            {
                node = new DefaultMutableTreeNode(category);
            }
            else
            {
                node = new DefaultMutableTreeNode("Unknown");
            }
            categoryNodes.put(category, node);
            root.add(node);
        }
        return node;
    }
    
    private void update()
    {
        List<TreePath> expanded = new ArrayList<TreePath>();
        
        for(DefaultMutableTreeNode node : categoryNodes.values())
        {
            TreePath path = new TreePath(node.getPath());
            if(entityTree.isExpanded(path))
            {
                expanded.add(path);
            }
            node.removeAllChildren();
        }
        entityNodes.clear();
        
        synchronized(sim.getLock())
        {
            List<EntityListAdapter> adapters = new ArrayList<EntityListAdapter>();
            for(Entity entity : sim.getEntitiesFast())
            {
                adapters.add(new EntityListAdapter(entity));
            }
            
            Collections.sort(adapters);
            for(EntityListAdapter adapter : adapters)
            {
                DefaultMutableTreeNode categoryNode = getCategoryNode(adapter.entity);
                DefaultMutableTreeNode entityNode = new DefaultMutableTreeNode(adapter, false);
                categoryNode.add(entityNode);
                entityNodes.put(adapter.entity, entityNode);
            }
        }
        
        treeModel.reload();
        for(TreePath path : expanded)
        {
            entityTree.expandPath(path);
        }
    }
    
    private void listSelectionChanged()
    {
        if(ignoreTreeSelectionChange)
        {
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) entityTree.getLastSelectedPathComponent();
        if(node == null)
        {
            return;
        }
        Object o = node.getUserObject();
        if(!(o instanceof EntityListAdapter))
        {
            return;
        }
        EntityListAdapter s = (EntityListAdapter) o;
        
        if(s != null)
        {
            SelectionManager.findService(this.services).setSelection(this, s.entity);
        }
    }
    
    private void appSelectionChanged(Object source)
    {
        if(source == this)
        {
            return;
        }
        Object o = SelectionManager.findService(this.services).getSelectedObject();
        if(!(o instanceof Entity))
        {
            return;
        }
        Entity e = (Entity) o;
        DefaultMutableTreeNode node = entityNodes.get(e);
        if(node == null)
        {
            return;
        }
        TreePath path = new TreePath(node.getPath());
        entityTree.scrollPathToVisible(path);
        
        try
        {
            ignoreTreeSelectionChange = true;
            entityTree.getSelectionModel().setSelectionPath(path);
        }
        finally
        {
            ignoreTreeSelectionChange = false;
        }
            
    }
    
    private void safeUpdate()
    {
        if(SwingUtilities.isEventDispatchThread())
        {
            update();
        }
        else
        {
            SwingUtilities.invokeLater(new Runnable() {

                public void run()
                {
                    update();
                }});
        }
    }
    
    private class DoubleClickHandler extends MouseAdapter
    {

        @Override
        public void mouseClicked(MouseEvent e)
        {
            if(e.getClickCount() == 2 && !SwingUtilities.isRightMouseButton(e))
            {
                ActionManager am = services.findService(ActionManager.class);
                if(am != null)
                {
                    am.executeAction(CenterViewOnEntityAction.class.getCanonicalName());
                }
            }
        }
    }
    
    private class ContextMenuClickHandler extends MouseAdapter
    {
        public void mouseReleased(MouseEvent e)
        {
            if(SwingUtilities.isRightMouseButton(e))
            {
                TreePath path = entityTree.getClosestPathForLocation(e.getPoint().x, e.getPoint().y);
                if(path != null)
                {
                    entityTree.getSelectionModel().setSelectionPath(path);
                    contextMenu.show(entityTree, e.getX(), e.getY());
                }
            }
        }
    }
    
    private class SimListener extends SimulationListenerAdapter
    {

        @Override
        public void onEntityAdded(Entity e)
        {
            safeUpdate();
        }

        @Override
        public void onEntityRemoved(Entity e)
        {
            safeUpdate();
        }

        @Override
        public void onPause()
        {
            safeUpdate();
        }

        @Override
        public void onStart()
        {
            safeUpdate();
        }
        
    }
    
    private class EntityListAdapter implements Comparable<EntityListAdapter>
    {
        public Entity entity;
        
        public EntityListAdapter(Entity entity)
        {
            this.entity = entity;
        }

        @Override
        public String toString()
        {
            String text = entity.getName();
            if(!EntityTools.isVisible(entity))
            {
                text += " (hidden)";
            }
            else
            {
                // TODO: This is kind of a hack. Figure out how to force JTree
                // to resize cells when text changes.
                text += "              ";
            }
            return text;
        }

        public int compareTo(EntityListAdapter o)
        {
            return entity.getName().compareTo(o.entity.getName());
        }
    }
}
