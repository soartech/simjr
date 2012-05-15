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
 * Created on Jun 19, 2007
 */
package com.soartech.simjr.ui;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

import com.soartech.simjr.radios.RadioHistory;
import com.soartech.simjr.radios.RadioMessage;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.ui.actions.AbstractSimulationAction;

/**
 * A panel that displays radio messages as they appear in the simulation's
 * radio history
 * 
 * @author ray
 */
public class RadioMessagePanel extends DefaultSingleCDockable
{
    private ServiceManager services;
    private RadioHistory history;
    
    private RadioMessageTableModel tableModel;
    private JXTable table;
    private JScrollPane tableScrollPane;
    
    /**
     * @param app
     */
    public RadioMessagePanel(ServiceManager services)
    {
        super("RadioMessagePanel");
        
        this.services = services;
        this.history = this.services.findService(RadioHistory.class);

        //DF settings
        setLayout(new BorderLayout());
        setCloseable(true);
        setMinimizable(false);
        setExternalizable(true);
        setMaximizable(true);
        setTitleText("Radio Messages");
        setResizeLocked(true);
        setTitleIcon(SimulationImages.SPEECH_BUBBLE);
        
        JPanel header = new JPanel(new BorderLayout());
        JToolBar tools = new JToolBar();
        tools.setFloatable(false);
        tools.setRollover(true);
        
        tools.add(new ClearAction());
        tools.add(new CopyAction());
        header.add(tools, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        
        tableModel = new RadioMessageTableModel(history);
        table = new JXTable(tableModel);
        
        DefaultTableCellRenderer bubbleRenderer = new DefaultTableCellRenderer();
        bubbleRenderer.setIcon(SimulationImages.SPEECH_BUBBLE);
        
        final TableColumn bubbleColumn = table.getColumnModel().getColumn(0);
        bubbleColumn.setCellRenderer(bubbleRenderer);
        bubbleColumn.setMaxWidth(SimulationImages.SPEECH_BUBBLE.getIconWidth() + 6);
        table.setShowGrid(false);
        this.table.setHighlighters(HighlighterFactory.createAlternateStriping());
        this.table.setColumnControlVisible(true);
        
        tableModel.addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent e)
            {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Rectangle cellRect = table.getCellRect(table.getRowCount() + 1, 0, true);
                        tableScrollPane.getViewport().setViewPosition(cellRect.getLocation());
                        table.packAll();
                    }
                });
            }});

        add(tableScrollPane = new JScrollPane(table), BorderLayout.CENTER);
    }
        
    private class CopyAction extends AbstractSimulationAction implements ClipboardOwner
    {
        private static final long serialVersionUID = -4313617598371767224L;

        public CopyAction()
        {
            super("Copy to clipboard", SimulationImages.COPY);
            
            setToolTip("Copy messages to clipboard");
        }

        @Override
        public void update()
        {
        }

        public void actionPerformed(ActionEvent arg0)
        {
            StringBuffer buffer = new StringBuffer();
            
            for(RadioMessage m : history.getMessages())
            {
                buffer.append(m);
                buffer.append("\n");
            }
            
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection ss = new StringSelection(buffer.toString());
            
            cb.setContents(ss, this);
        }

        public void lostOwnership(Clipboard arg0, Transferable arg1)
        {
        }
        
    }
    
    private class ClearAction extends AbstractSimulationAction
    {
        private static final long serialVersionUID = -2309149921793913774L;

        public ClearAction()
        {
            super("Clear", SimulationImages.CLEAR);
            
            setToolTip("Clear all messages");
        }

        @Override
        public void update()
        {
        }

        public void actionPerformed(ActionEvent arg0)
        {
            tableModel.clear();
        }
    }

}
