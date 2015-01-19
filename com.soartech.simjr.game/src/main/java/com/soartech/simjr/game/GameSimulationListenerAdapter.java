/**
 * 
 */
package com.soartech.simjr.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.SimulationListenerAdapter;

/**
 * @author aron
 *
 */
public class GameSimulationListenerAdapter extends SimulationListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(GameSimulationListenerAdapter.class);
    
    public GameSimulationListenerAdapter() {
        super();
    }

    @Override
    public void onEntityAdded(Entity e) {
        
        logger.info("Entity Added: " + e.getName());
        
        //make some updates to the cylinder entities after creation
        if(e.getProperty(EntityConstants.PROPERTY_CATEGORY).equals("Cylinder")) {

            //set the line color for no-fly circles
            e.setProperty(EntityConstants.PROPERTY_SHAPE_LINE_COLOR, "red");
            
            //set the cylinder area size
            e.setProperty(EntityConstants.PROPERTY_SHAPE_WIDTH_METERS, 27780.00);
            
            //turn off the label
            e.setProperty(EntityConstants.PROPERTY_SHAPE_LABEL_VISIBLE, false);
        }
        
        if(e.getProperty(EntityConstants.PROPERTY_CATEGORY).equals("Areas")) {
            
            //set the fill color of slow down areas
            e.setProperty(EntityConstants.PROPERTY_SHAPE_FILL_COLOR, "yellow");
        }

    }
    
}
