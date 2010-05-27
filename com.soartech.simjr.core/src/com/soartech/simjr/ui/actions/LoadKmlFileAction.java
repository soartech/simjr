/**
 * 
 */
package com.soartech.simjr.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.soartech.simjr.ui.SimulationMainFrame;

/**
 * @author aron
 *
 */
public class LoadKmlFileAction extends AbstractSimulationAction
{
	private static final long serialVersionUID = 8899798502611162716L;

	public LoadKmlFileAction(ActionManager actionManager)
    {
        super(actionManager, "Load KML file ...");
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
    @Override
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
                       (f.isDirectory() || f.getName().endsWith(".kml"));
            }

            @Override
            public String getDescription()
            {
                return "Google Earth KML files (*.kml)";
            }});
        
        if(chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
        {
            return;
        }
        
        File file = chooser.getSelectedFile();
//        frame.loadDockingLayoutFromFile(file.getAbsolutePath());
        
    }

}
