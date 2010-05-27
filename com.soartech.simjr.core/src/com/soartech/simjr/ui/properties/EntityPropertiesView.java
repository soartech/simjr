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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

import com.soartech.math.Vector3;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.entities.FuelModel;
import com.soartech.simjr.ui.SelectionManager;
import com.soartech.simjr.ui.SelectionManagerListener;
import com.soartech.simjr.ui.SimulationApplication;
import com.soartech.simjr.ui.SimulationImages;
import com.soartech.simjr.ui.actions.AbstractSimulationAction;

/**
 * @author ray
 */
@SuppressWarnings("serial")
public class EntityPropertiesView extends DefaultSingleCDockable
{
    private SimulationApplication app;
    private JLabel title = new JLabel();
    private EntityPropertiesViewModel tableModel;
    private JTable table;
    
    private ColorRenderer colorRenderer = new ColorRenderer(true);
    private VectorRenderer vectorRenderer = new VectorRenderer();
    private DoubleRenderer doubleRenderer = new DoubleRenderer();
    private FuelRenderer fuelRenderer = new FuelRenderer();
    
    public EntityPropertiesView(SimulationApplication app)
    {
        super("EntityPropertiesView");

        //DF settings
        setLayout(new BorderLayout());
        setCloseable(true);
        setMinimizable(false);
        setExternalizable(true);
        setMaximizable(true);
        setTitleText("Entity Properties");
        setResizeLocked(true);
        setTitleIcon(SimulationImages.PROPERTIES);
        
        this.app = app;
        
        SelectionManager.findService(this.app).addListener(new SelectionListener());
        
        tableModel = new EntityPropertiesViewModel(app);
        
        JPanel header = new JPanel(new BorderLayout());
        header.add(title, BorderLayout.WEST);
        JToolBar tools = new JToolBar();
        tools.setFloatable(false);
        tools.setRollover(true);
        
        tools.add(tableModel.getCopyAction());
        tools.add(tableModel.getRefreshAction());
        tools.add(createAutoRefreshAction(new JToggleButton()));
        header.add(tools, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);
        
        table = new JTable(tableModel) {

            /* (non-Javadoc)
             * @see javax.swing.JTable#getCellRenderer(int, int)
             */
            @Override
            public TableCellRenderer getCellRenderer(int row, int column)
            {
                if(column == 1)
                {
                    Object value = tableModel.getValueAt(row, column);
                    if(value instanceof Color)
                    {
                        return colorRenderer;
                    }
                    else if(value instanceof Vector3)
                    {
                        return vectorRenderer;
                    }
                    else if(value instanceof Double)
                    {
                        return doubleRenderer;
                    }
                    else if(value instanceof FuelModel)
                    {
                        return fuelRenderer;
                    }
                    else
                    {
                        return getDefaultRenderer(value.getClass());
                    }
                }
                return super.getCellRenderer(row, column);
            }

            /* (non-Javadoc)
             * @see javax.swing.JTable#getCellEditor(int, int)
             */
            @Override
            public TableCellEditor getCellEditor(int row, int column)
            {
                if(column == 1)
                {
                    TableCellEditor editor = tableModel.getPropertyEditory(row);
                    if(editor != null)
                    {
                        return editor;
                    }
                    Object value = tableModel.getValueAt(row, column);
                    return getDefaultEditor(value.getClass());
                }
                return super.getCellEditor(row, column);
            }
            
            
        };
        table.addMouseMotionListener(new TableMouseMotionListener());
        
        
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
    
    public void refreshModel()
    {
        tableModel.refresh();
    }
    
    private String getValueToolTip(Object value)
    {
        if(value == null)
        {
            return "";
        }
        if(!(value instanceof Map<?,?>))
        {
            return value.toString();
        }
        
        StringBuilder b = new StringBuilder();
        b.append("<html>");
        for(Object o : ((Map<?,?>) value).entrySet())
        {
            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
            
            b.append(e.getKey() + " : " + e.getValue() + "<br>");
        }
        b.append("</html>");
        return b.toString();
    }
    
    /**
     * Create the auto-refresh toggle action on the given button (JToggleButton
     * or JCheckBoxMenuItem).
     * 
     * @param button The toggle button
     * @return The button
     */
    private AbstractButton createAutoRefreshAction(final AbstractButton button)
    {
        AbstractSimulationAction action = new AbstractSimulationAction("", SimulationImages.AUTO_REFRESH)
        {
            private Timer timer = new Timer(1000, new ActionListener() {

                public void actionPerformed(ActionEvent e)
                {
                    refreshModel();
                }});
            
            public void update()
            {
            }

            public void actionPerformed(ActionEvent e)
            {
                if(button.isSelected())
                {
                    timer.start();
                }
                else
                {
                    timer.stop();
                }
            }
        };
        
        action.setToolTip("Auto-refresh properties once a second");
        button.setAction(action);
        
        return button;
    }
    
    private class SelectionListener implements SelectionManagerListener
    {
        public void selectionChanged(Object source)
        {
            Object o = SelectionManager.findService(app).getSelectedObject();
            
            String s = "";
            if(o instanceof Entity)
            {
                s = "<html>&nbsp;&nbsp;<b>" + ((Entity) o).getName() + "</b></html>";
            }
            title.setText(s);
        }
    }
    
    private class TableMouseMotionListener extends MouseMotionAdapter
    {
        /* (non-Javadoc)
         * @see java.awt.event.MouseMotionAdapter#mouseMoved(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseMoved(MouseEvent e)
        {
            int c = table.columnAtPoint(e.getPoint());
            int r = table.rowAtPoint(e.getPoint());
            if(c < 0 || r < 0)
            {
                table.setToolTipText("");
            }
            else
            {
                Object v = tableModel.getValueAt(r, c);
                table.setToolTipText(getValueToolTip(v));
            }
        }
    }
}
