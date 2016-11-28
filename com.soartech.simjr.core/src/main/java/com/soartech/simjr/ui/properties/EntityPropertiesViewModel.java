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
 */
package com.soartech.simjr.ui.properties;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.ui.SelectionManager;
import com.soartech.simjr.ui.SelectionManagerListener;
import com.soartech.simjr.ui.SimulationImages;
import com.soartech.simjr.ui.actions.AbstractSimulationAction;
import com.soartech.simjr.ui.actions.ActionManager;

/**
 * @author ray
 */
@SuppressWarnings("serial")
public class EntityPropertiesViewModel extends AbstractTableModel
{
    private ServiceManager services;
    
    private Entity entity;
    private List<Map.Entry<String, Object>> entries = new ArrayList<Map.Entry<String, Object>>();
    
    private Map<String, EditableProperty> editableProperties = new HashMap<String, EditableProperty>();
    
    private RefreshAction refreshAction;
    private CopyToClipboardAction copyAction;
    
    /**
     * Comparator that sorts by property name
     */
    private static final Comparator<Map.Entry<String, Object>> COMPARATOR =
        new Comparator<Map.Entry<String, Object>>() {

            public int compare(Entry<String, Object> o1, Entry<String, Object> o2)
            {
                return o1.getKey().compareTo(o2.getKey());
            }};

    public EntityPropertiesViewModel(ServiceManager serviceManager)
    {
        this.services = serviceManager;
        
        SelectionManager.findService(this.services).addListener(new SelectionListener());
        
        refreshAction = new RefreshAction();
        copyAction = new CopyToClipboardAction();
        
        EditableProperties.install(editableProperties);
        
        refresh();
    }
    
    public void refresh()
    {
        entity = null;
        entries.clear();
        Object o = SelectionManager.findService(this.services).getSelectedObject();
        if(o instanceof Entity)
        {
            entity = (Entity) o;
            
            // Lock the simulation so we get consistent values if it's running
            synchronized(entity.getSimulation().getLock())
            {
                entries.addAll(entity.getProperties().entrySet());
            }
        }
        Collections.sort(entries, COMPARATOR);
        fireTableDataChanged();
        
        refreshAction.update();
    }
    
    public AbstractSimulationAction getRefreshAction()
    {
        return refreshAction;
    }
    
    public AbstractSimulationAction getCopyAction()
    {
        return copyAction;
    }
        
    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int column)
    {
        if(column == 0)
        {
            return "Name";
        }
        else if(column == 1)
        {
            return "Value";
        }
        return super.getColumnName(column);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return 2;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
        return entries.size();
    }
    

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int r, int c)
    {
        if(entity == null || r >= getRowCount() || c != 1)
        {
            return false;
        }
        
        Map.Entry<String, Object> e = entries.get(r);
        EditableProperty prop = editableProperties.get(e.getKey());
        
        return prop != null;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int r, int c)
    {
        if(r >= getRowCount() || c >= 2)
        {
            return null;
        }
        
        Map.Entry<String, Object> e = entries.get(r);
        if(c == 0)
        {
            return e.getKey();
        }
        
        Object v = e.getValue();

        return v;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
    public void setValueAt(Object value, int r, int c)
    {
        if(entity == null || r >= getRowCount() || c != 1)
        {
            return;
        }
        
        Map.Entry<String, Object> e = entries.get(r);
        EditableProperty prop = editableProperties.get(e.getKey());
        if(prop != null)
        {
            synchronized(entity.getSimulation().getLock())
            {
                Object result = prop.setValue(entity, value, e.getValue());
                e.setValue(result);
                fireTableCellUpdated(r, c);
            }
        }
    }
    
    public TableCellEditor getPropertyEditory(int r)
    {
        if(entity == null || r >= getRowCount())
        {
            return null;
        }
        Map.Entry<String, Object> e = entries.get(r);
        EditableProperty prop = editableProperties.get(e.getKey());
        
        return prop != null ? prop.getEditor() : null;
    }

    private class SelectionListener implements SelectionManagerListener
    {
        public void selectionChanged(Object source)
        {
            refresh();
        }
    }
    
    private class RefreshAction extends AbstractSimulationAction
    {
        public RefreshAction()
        {
            super(services.findService(ActionManager.class), "Refresh", SimulationImages.REFRESH);
            
            setToolTip("Refresh property values");
        }

        @Override
        public void update()
        {
            setEnabled(getSelectionManager().getSelectedObject() != null);
        }

        public void actionPerformed(ActionEvent arg0)
        {
            refresh();
        }
    }

    private class CopyToClipboardAction extends AbstractSimulationAction implements ClipboardOwner
    {
        public CopyToClipboardAction()
        {
            super("Copy", SimulationImages.COPY);
            
            setToolTip("Copy properties to clipboard");
        }

        @Override
        public void update()
        {
        }

        public void actionPerformed(ActionEvent arg0)
        {
            StringBuffer b = new StringBuffer();
            
            for(Map.Entry<String, Object> e : entries)
            {
                b.append(e.getKey() + "=" + e.getValue() + "\n");
            }
            
            StringSelection ss = new StringSelection(b.toString()); 
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(ss, this);
        }

        public void lostOwnership(Clipboard clipboard, Transferable contents)
        {
        }
    }
}
