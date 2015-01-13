/**
 * 
 */
package com.soartech.simjr.game.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.ui.SimulationImages;
import com.soartech.simjr.util.SwingTools;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

/**
 * @author aron
 *
 */
public class TxaGameScoreboard extends DefaultSingleCDockable 
{
    private ServiceManager services;
    
    public TxaGameScoreboard(ServiceManager services) {
        super("Txa Game Scoreboard");
        
        this.services = services;

        //DF settings
        setLayout(new BorderLayout());
        setCloseable(true);
        setMinimizable(false);
        setExternalizable(true);
        setMaximizable(true);
        setTitleText("Scoreboard");
        setResizeLocked(true);
        setTitleIcon(SimulationImages.CONSOLE);
        
        JPanel header = new JPanel(new BorderLayout());
        
        JToolBar tools = new JToolBar();
        tools.setFloatable(false);
//        tools.add(new ClearAction());
        header.add(tools, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);
    }

}
