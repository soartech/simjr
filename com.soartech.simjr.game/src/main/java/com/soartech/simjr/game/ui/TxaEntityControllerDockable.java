/**
 * 
 */
package com.soartech.simjr.game.ui;

import java.awt.BorderLayout;
import java.awt.Label;

import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.ui.SimulationImages;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

/**
 * @author aron
 *
 */
public class TxaEntityControllerDockable extends DefaultSingleCDockable 
{
    private static final Logger logger = LoggerFactory.getLogger(TxaEntityControllerDockable.class);
    
    private ServiceManager services;
    private Entity entity = null;
    
    public TxaEntityControllerDockable(ServiceManager services, String id)
    {
        super(id);
        
        this.services = services;

        //DF settings
        setLayout(new BorderLayout());
        setCloseable(true);
        setMinimizable(false);
        setExternalizable(true);
        setMaximizable(true);
        setTitleText("Txa Controller");
        setResizeLocked(true);
        setTitleIcon(SimulationImages.CONSOLE);
        
        JPanel header = new JPanel(new BorderLayout());
        
        JToolBar tools = new JToolBar();
        tools.setFloatable(false);
//        tools.add(new ClearAction());
        header.add(tools, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);
    }
    
    public final Entity getEntity()
    {
        return entity;
    }
    
    public void addEntityController(Entity fakeEntity, Entity ngtsEntity)
    {
        if(this.entity == null && fakeEntity != null)
        {
                this.entity = fakeEntity;
                logger.info("*** Adding entity controller for: " + ngtsEntity.getName());
                setTitleText(ngtsEntity.getName());
                TxaEntityVelocityController foo = new TxaEntityVelocityController(services, fakeEntity, ngtsEntity);
                add(foo, BorderLayout.CENTER);
        }
    }
}
