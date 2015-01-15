/**
 * 
 */
package com.soartech.simjr.game.ui;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.SingleCDockable;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.SimulationListenerAdapter;
import com.soartech.simjr.ui.SimulationApplication;
import com.soartech.simjr.ui.SimulationImages;
import com.soartech.simjr.ui.SimulationMainFrame;

/**
 * @author aron
 *
 */
public class TxaGameSimulationApplication extends SimulationApplication {

    public static final String TXA_SCOREBOARD_FRAME_KEY = "__txaScoreboard";
    public static final String TXA_ENTITY_CONTROLLER1_FRAME_KEY = "__txaEntityController1";
    public static final String TXA_ENTITY_CONTROLLER2_FRAME_KEY = "__txaEntityController2";
    
    private final CLocation txaScoreboardLocation =        CLocation.base().normalRectangle(0.8, 0, 0.2, 0.5);
    private final CLocation txaEntityController1Location = CLocation.base().normalRectangle(0.7, 0.7, 0.3, 0.3);
    private final CLocation txaEntityController2Location = CLocation.base().normalRectangle(0.4, 0.7, 0.3, 0.3);
    
//    private final CLocation consoleLocation = CLocation.base().normalRectangle(0, 0.4, 0.4, 0.3).stack(0);
    private final CLocation e1Location = CLocation.base().normalRectangle(0, 0.7, 0.8, 0.3).stack(2);
    
    private TxaGameScoreboard scoreboardDockable = null; 
    private TxaEntityControllerDockable controllerDockable1 = null;
    private TxaEntityControllerDockable controllerDockable2 = null;
    
    private Entity fakeEntity1 = null;
    private Entity fakeEntity2 = null;
    
    @Override
    protected SimulationMainFrame createMainFrame(ProgressMonitor progress) 
    {
        progress.subTask("Initializing main window ...");
        SimulationMainFrame mainFrame = new SimulationMainFrame(this);
        
        //remove the entity list and properties
        SingleCDockable entitiesList = mainFrame.removeDockable(SimulationMainFrame.ENTITIES_FRAME_KEY);
        SingleCDockable entitiesPropertiesList = mainFrame.removeDockable(SimulationMainFrame.ENTITY_PROPERTIES_FRAME_KEY);
        SingleCDockable radioMessagesDockable = mainFrame.removeDockable(SimulationMainFrame.RADIO_MESSAGES_FRAME_KEY);
        
//        mainFrame.moveDockable(consoleLocation, SimulationMainFrame.ENTITIES_FRAME_KEY);
        
        //add txa game scoreboard and controls
        scoreboardDockable = new TxaGameScoreboard(this);
        controllerDockable1 = new TxaEntityControllerDockable(this, TXA_ENTITY_CONTROLLER1_FRAME_KEY);
        controllerDockable2 = new TxaEntityControllerDockable(this, TXA_ENTITY_CONTROLLER2_FRAME_KEY);
        mainFrame.addDockable(scoreboardDockable, txaScoreboardLocation, TXA_SCOREBOARD_FRAME_KEY);
        mainFrame.addDockable(controllerDockable1, txaEntityController1Location, TXA_ENTITY_CONTROLLER1_FRAME_KEY);
        mainFrame.addDockable(controllerDockable2, txaEntityController2Location, TXA_ENTITY_CONTROLLER2_FRAME_KEY);
        
//        mainFrame.moveDockable(e2Location, SimulationMainFrame.ENTITIES_FRAME_KEY);
//        mainFrame.addDockable(entitiesList, e1Location, SimulationMainFrame.ENTITIES_FRAME_KEY);
        
        mainFrame.setDefaultCloseOperation(SimulationMainFrame.DISPOSE_ON_CLOSE);
        mainFrame.addWindowListener(exitHandler);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setIconImage(SimulationImages.SIMJR_ICON.getImage());
        mainFrame.setVisible(true);
        
        //create the help menu
        JMenu txaMenu = new JMenu("TXA");
        txaMenu.add(new AbstractAction("Toggle aided gameplay") {
            private static final long serialVersionUID = -2762087716549789767L;
            public void actionPerformed(ActionEvent arg0) {
                scoreboardDockable.toggleAidedControls();
            }
        });
        JMenuBar menuBar = mainFrame.getJMenuBar();
        menuBar.add(txaMenu);
        
        //register the entity listener
        Simulation sim = Simulation.findService(this);
        sim.addListener(new MySimulationListenerAdapter());
        
        return mainFrame;
    }
    
    @Override
    protected void completeInitialization(SimulationMainFrame mainFrame) 
    {
        Simulation sim = Simulation.findService(this);
        this.fakeEntity1 = sim.getEntity("Fake Player 1");
        this.fakeEntity2 = sim.getEntity("Fake Player 2");
        
        super.completeInitialization(mainFrame);
    }

    public void setScore(int score)
    {
        scoreboardDockable.setScore(score);
    }
    
    public void setGoals(List<String> goals, String player)
    {
        scoreboardDockable.setGoals(goals, player);
    }
    
    public void setRules(List<String> rules, String player)
    {
        scoreboardDockable.setRules(rules, player);
    }

    public class MySimulationListenerAdapter extends SimulationListenerAdapter {

        @Override
        public void onEntityAdded(Entity e) {

            synchronized (e.getSimulation().getLock())
            {
                //make some updates to the cylinder entities after creation
                if(e.getProperty(EntityConstants.PROPERTY_CATEGORY).equals("Vehicles")) {
                    //add entity controllers up to two
                    if(controllerDockable2.getEntity() == null)
                    {
                        controllerDockable2.addEntityController(fakeEntity1, e);
                    } 
                    else if(controllerDockable1.getEntity() == null)
                    {
                        controllerDockable1.addEntityController(fakeEntity2, e);
                    }
                }
            }

        }
        
    }

    /**
     * @param args
     */
    public static void main(final String[] args)
    {
        main(new TxaGameSimulationApplication(), args);
    }
}
