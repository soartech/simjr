/*
 * Soar Technology Proprietary, Restricted Rights
 * (c) 2007  Soar Technology, Inc.
 *
 * Created on Sep 19, 2007
 */
package com.soartech.simjr.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.soartech.simjr.ui.SimulationMainFrame;

/**
 * @author ray
 */
public class SaveDockingLayoutAction extends AbstractSimulationAction
{
    private static final long serialVersionUID = -4848088200853262889L;

    public SaveDockingLayoutAction(ActionManager actionManager)
    {
        super(actionManager, "Save window layout ...");
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update()
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0)
    {
        SimulationMainFrame frame = findService(SimulationMainFrame.class);
        if(frame == null)
        {
            return;
        }
        
        File cd = new File(System.getProperty("user.dir"));
        JFileChooser chooser = new JFileChooser(cd);
        chooser.addChoosableFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f)
            {
                return !f.getName().startsWith(".") && 
                       (f.isDirectory() || f.getName().endsWith(".sjl"));
            }

            @Override
            public String getDescription()
            {
                return "Sim Jr layout files (*.sjl)";
            }});
        
        if(chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION)
        {
            return;
        }
        
        File file = chooser.getSelectedFile();
        frame.saveDockingLayoutToFile(file.getAbsolutePath());
    }
}
