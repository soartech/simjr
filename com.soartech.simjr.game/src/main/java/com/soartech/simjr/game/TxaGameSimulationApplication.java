/**
 * 
 */
package com.soartech.simjr.game;

import bibliothek.gui.dock.common.CLocation;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.ui.SimulationApplication;
import com.soartech.simjr.ui.SimulationImages;
import com.soartech.simjr.ui.SimulationMainFrame;

/**
 * @author aron
 *
 */
public class TxaGameSimulationApplication extends SimulationApplication {

    public static final String TXA_SCOREBOARD_FRAME_KEY = "__txaScoreboard";
    private final CLocation txaScoreboardLocation = CLocation.base().normalRectangle(0.8, 0, 0.2, 0.5);
    
    @Override
    protected SimulationMainFrame createMainFrame(ProgressMonitor progress) 
    {
        progress.subTask("Initializing main window ...");
        SimulationMainFrame mainFrame = new SimulationMainFrame(this);
        
        //remove the entity list and properties
        mainFrame.removeDockable(SimulationMainFrame.ENTITIES_FRAME_KEY);
        mainFrame.removeDockable(SimulationMainFrame.ENTITY_PROPERTIES_FRAME_KEY);
        
        //add txa game scoreboard
        mainFrame.addDockable(new TxaGameScoreboard(this), txaScoreboardLocation, TXA_SCOREBOARD_FRAME_KEY);
        
        mainFrame.setDefaultCloseOperation(SimulationMainFrame.DISPOSE_ON_CLOSE);
        mainFrame.addWindowListener(exitHandler);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setIconImage(SimulationImages.SIMJR_ICON.getImage());
        mainFrame.setVisible(true);
        return mainFrame;
    }

    /**
     * @param args
     */
    public static void main(final String[] args)
    {
        main(new TxaGameSimulationApplication(), args);
    }
}
