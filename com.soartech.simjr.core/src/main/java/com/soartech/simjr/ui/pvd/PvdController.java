package com.soartech.simjr.ui.pvd;

import java.awt.Point;

import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.ui.ObjectContextMenu;

public interface PvdController
{
    void dispose();
    
    void attachToView(PvdView view, Simulation sim, ServiceManager app);
    
    /**
     * @return the currently installed context menu
     */
    ObjectContextMenu getContextMenu();
    
    Point getContextMenuPoint();

    /**
     * @param contextMenu the new context menu
     */
    void setContextMenu(ObjectContextMenu contextMenu);
    
    void setContextMenuEnabled(boolean enabled);
}
