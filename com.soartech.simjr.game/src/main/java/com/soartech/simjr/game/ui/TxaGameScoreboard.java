/**
 * 
 */
package com.soartech.simjr.game.ui;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.simjr.game.message.TxaInterfaceManager;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.ui.SimulationImages;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

/**
 * @author aron
 *
 */
public class TxaGameScoreboard extends DefaultSingleCDockable 
{
    private static final Logger logger = LoggerFactory.getLogger(TxaGameScoreboard.class);
    
    private ServiceManager services;
    
    //these strings need t match the entity names coming from NGTS over the TXA interface
    public static final String PLAYER1 = "Player-1";
    public static final String PLAYER2 = "Player-2";
    
    
    private Label scoreLabel;
    private Checkbox cb1;
    private Checkbox cb2;
    
    private Label goalsLabel1;
    private Label goalsLabel2;
    
    private Label rulesLabel1;
    private Label rulesLabel2;
    
    private JPanel scorePanel;
    
    private JPanel goalsPanel1;
    private JPanel goalsPanel2;
    
    private JPanel rulesPanel1;
    private JPanel rulesPanel2;
    
    private java.awt.List goalsListWidget1 = new java.awt.List();
    private java.awt.List goalsListWidget2 = new java.awt.List();
    
    private java.awt.List rulesListWidget1 = new java.awt.List();
    private java.awt.List rulesListWidget2 = new java.awt.List();
    
    private boolean updatingRules1 = false;
    private boolean updatingRules2 = false;
    private boolean updatingGoals1 = false;
    private boolean updatingGoals2 = false;
    
    public TxaGameScoreboard(final ServiceManager services) {
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
        
        JPanel content = new JPanel(new GridLayout(5,1));
        
        //add content
        scorePanel = new JPanel(new BorderLayout());
        scoreLabel = new Label("Current Score: ");
        cb1 = new Checkbox("Match speed autopilot", false);
        cb1.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {             
                TxaInterfaceManager txaInterface = TxaInterfaceManager.findService(services);
                if(e.getStateChange()==1) {
                    //checked
                    txaInterface.setMatchSpeedAutopilot(true);
                } else {
                    //unchecked
                    txaInterface.setMatchSpeedAutopilot(false);
                }
            }
         });
        
        cb2 = new Checkbox("Obstacle avoidance autopilot", false);
        cb2.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                TxaInterfaceManager txaInterface = TxaInterfaceManager.findService(services);
                if(e.getStateChange()==1) {
                    //checked
                    txaInterface.setObstableAvoidAutopilot(true);
                } else {
                    //unchecked
                    txaInterface.setObstableAvoidAutopilot(true);
                }
            }
         });
        
        scorePanel.add(scoreLabel, BorderLayout.NORTH);
        
        JPanel cbPanel = new JPanel(new GridLayout(2, 1));
        cbPanel.add(cb1);
        cbPanel.add(cb2);
        
        scorePanel.add(cbPanel, BorderLayout.CENTER);
        content.add(scorePanel);
        
        goalsPanel1 = new JPanel(new BorderLayout());
        goalsLabel1 = new Label("P1 Goals: ");
        goalsPanel1.add(goalsLabel1, BorderLayout.NORTH);
        goalsPanel1.add(goalsListWidget1, BorderLayout.CENTER);
        content.add(goalsPanel1);
        
        rulesPanel1 = new JPanel(new BorderLayout());
        rulesLabel1 = new Label("P1 Rules: ");
        rulesPanel1.add(rulesLabel1, BorderLayout.NORTH);
        rulesPanel1.add(rulesListWidget1, BorderLayout.CENTER);
        content.add(rulesPanel1);
        
        goalsPanel2 = new JPanel(new BorderLayout());
        goalsLabel2 = new Label("P2 Goals: ");
        goalsPanel2.add(goalsLabel2, BorderLayout.NORTH);
        goalsPanel2.add(goalsListWidget2, BorderLayout.CENTER);
        content.add(goalsPanel2);
        
        rulesPanel2 = new JPanel(new BorderLayout());
        rulesLabel2 = new Label("P2 Rules: ");
        rulesPanel2.add(rulesLabel2, BorderLayout.NORTH);
        rulesPanel2.add(rulesListWidget2, BorderLayout.CENTER);
        content.add(rulesPanel2);
        
        add(content, BorderLayout.CENTER);
    }
    
    public void setScore(int score)
    {
        scoreLabel.setText("Current Score: " + score);
    }
    
    public void setGoals(List<String> goals, String player)
    {
        if(player.equals(PLAYER1) && !updatingGoals1)
        {
            updatingGoals1 = true;
            
            goalsListWidget1.removeAll();
            for(String s : goals)
            {
                goalsListWidget1.add(s);
            }
            
            updatingGoals1 = false;
        } 
        else if(player.equals(PLAYER2) && !updatingGoals2)
        {
            updatingGoals2 = true;
            
            goalsListWidget2.removeAll();
            for(String s : goals)
            {
                goalsListWidget2.add(s);
            }
            
            updatingGoals2 = false;
        }
    }
    
    public void setRules(List<String> rules, String player)
    {
        if(player.equals(PLAYER1) && !updatingRules1)
        {
            updatingRules1 = true;
            
            rulesListWidget1.removeAll();
            for(String s : rules)
            {
                rulesListWidget1.add(s);
            }
            
            updatingRules1 = false;
        } 
        else if(player.equals(PLAYER2) && !updatingRules2)
        {
            updatingRules2 = true;
            
            rulesListWidget2.removeAll();
            for(String s : rules)
            {
                rulesListWidget2.add(s);
            }
            
            updatingRules2 = false;
        }
    }
    
}
