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
 * Created on Aug 10, 2007
 */
package com.soartech.simjr.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

import com.soartech.simjr.console.ConsoleManager;
import com.soartech.simjr.console.ConsoleManagerListener;
import com.soartech.simjr.console.ConsoleParticipant;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.ui.actions.AbstractSimulationAction;
import com.soartech.simjr.util.SwingTools;

/**
 * This is the console panel. It displays a list of consoles, retrieved from the
 * console manager, a message log, and a command entry box. Executed commands
 * are routed to the selected console participant.
 * 
 * @author ray
 */
public class ConsolePanel extends DefaultSingleCDockable
{
    private static final long serialVersionUID = 4927911084432287867L;

    private ServiceManager services;
    
    private DefaultComboBoxModel consoleListModel = new DefaultComboBoxModel();
    private JComboBox consoleList = new JComboBox(consoleListModel);
    private LogWindow outputLog = new LogWindow();
    private JComboBox commandBox = new JComboBox();
    
    public ConsolePanel(final ServiceManager services)
    {
        super("ConsolePanel");
        
        this.services = services;
        
        //DF settings
        setLayout(new BorderLayout());
        setCloseable(true);
        setMinimizable(false);
        setExternalizable(true);
        setMaximizable(true);
        setTitleText("Console");
        setResizeLocked(true);
        setTitleIcon(SimulationImages.CONSOLE);
        
        JPanel header = new JPanel(new BorderLayout());
        header.add(SwingTools.createLabeledComponent("Consoles: ", consoleList), BorderLayout.CENTER);
        
        JToolBar tools = new JToolBar();
        tools.setFloatable(false);
        tools.add(new RefreshAction());
        tools.add(new ClearAction());
        //tools.add(new OpenDebuggerAction());
        header.add(tools, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);
        add(outputLog, BorderLayout.CENTER);
        
        JPanel footer = new JPanel(new BorderLayout());
        footer.add(commandBox, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
                
        commandBox.setEditable(true);
        commandBox.getEditor().addActionListener(new CommandAction());
        
        ConsoleManager cm = this.services.findService(ConsoleManager.class);
        if(cm != null)
        {
            cm.addListener(new ConsoleManagerListener() {

                public void onChanged(ConsoleManager manager)
                {
                    populateConsoleList();
                }});
        }
        
        populateConsoleList();
    }
    
    private void populateConsoleList()
    {
        consoleListModel.removeAllElements();
        
        ConsoleManager soar = services.findService(ConsoleManager.class);
        if(soar == null)
        {
            return;
        }
        
        for(ConsoleParticipant console : soar.getParticipants())
        {
            consoleListModel.addElement(console);
        }
    }
    
    private void executeCommand()
    {
        ConsoleParticipant selectedItem = (ConsoleParticipant) consoleList.getSelectedItem();
        if(selectedItem == null)
        {
            populateConsoleList();
            selectedItem = (ConsoleParticipant) consoleList.getSelectedItem();
            if(selectedItem == null)
            {
                return;
            }
        }
        
        String command = commandBox.getEditor().getItem().toString().trim();
        String result = selectedItem.executeCommand(command);
        
        outputLog.append(selectedItem.getName() + "> " + command);
        if(result != null && result.length() > 0)
        {
            outputLog.append(result);
        }
        
        addCommand(command);
    }
    
    private void addCommand(String command)
    {
        for(int i = 0; i < commandBox.getItemCount(); ++i)
        {
            if(command.equals(commandBox.getItemAt(i)))
            {
                commandBox.removeItemAt(i);
                break;
            }
        }
        commandBox.insertItemAt(command, 0);
        commandBox.setSelectedItem("");
    }
    
    private class CommandAction extends AbstractSimulationAction
    {
        private static final long serialVersionUID = 7127822126098873871L;

        public CommandAction()
        {
            super("");
        }
        
        @Override
        public void update()
        {
        }

        public void actionPerformed(ActionEvent e)
        {
            executeCommand();
        }
    }
    
    private class RefreshAction extends AbstractSimulationAction
    {
        private static final long serialVersionUID = -2309149921793913774L;

        public RefreshAction()
        {
            super("Refresh", SimulationImages.REFRESH);
            
            setToolTip("Refresh console list");
        }

        @Override
        public void update()
        {
        }

        public void actionPerformed(ActionEvent arg0)
        {
            populateConsoleList();
        }
    }
    
    private class ClearAction extends AbstractSimulationAction
    {
        private static final long serialVersionUID = -2309149921793913774L;

        public ClearAction()
        {
            super("Clear", SimulationImages.CLEAR);
            
            setToolTip("Clear contents of output window");
        }

        @Override
        public void update()
        {
        }

        public void actionPerformed(ActionEvent arg0)
        {
            outputLog.clear();
        }
    }
}
