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
 * Created on Mar 27, 2009
 */
package com.soartech.simjr.ui.editor;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.undo.UndoableEdit;

import org.apache.log4j.Logger;

import com.soartech.math.geotrans.Geodetic;
import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.app.ApplicationState;
import com.soartech.simjr.app.DefaultApplicationStateService;
import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.scenario.model.ModelChangeEvent;
import com.soartech.simjr.scenario.model.ModelChangeListener;
import com.soartech.simjr.scenario.model.ModelException;
import com.soartech.simjr.services.DefaultServiceManager;
import com.soartech.simjr.sim.SimpleTerrain;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.actions.AddDistanceToolAction;
import com.soartech.simjr.ui.actions.AdjustMapOpacityAction;
import com.soartech.simjr.ui.actions.ClearDistanceToolsAction;
import com.soartech.simjr.ui.actions.EditRouteAction;
import com.soartech.simjr.ui.actions.ShowAllAction;
import com.soartech.simjr.ui.editor.actions.CloneEntityAction;
import com.soartech.simjr.ui.editor.actions.DeleteEntityAction;
import com.soartech.simjr.ui.editor.actions.ImportOSMAction;
import com.soartech.simjr.ui.editor.actions.NewAction;
import com.soartech.simjr.ui.editor.actions.OpenAction;
import com.soartech.simjr.ui.editor.actions.RedoAction;
import com.soartech.simjr.ui.editor.actions.RunAction;
import com.soartech.simjr.ui.editor.actions.SaveAction;
import com.soartech.simjr.ui.editor.actions.UndoAction;
import com.soartech.simjr.util.SwingTools;

/**
 * @author ray
 */
public class ScenarioEditorApplication extends DefaultServiceManager implements PropertyChangeListener, ScenarioEditorServiceManager
{
    private static final Logger logger = Logger.getLogger(ScenarioEditorApplication.class);
    
    private ScenarioEditorMainFrame frame;
    private Model model = new Model();
    
    public final WindowAdapter exitHandler = new WindowAdapter()
    {
        public void windowClosed(WindowEvent arg0)
        {
            try
            {
                shutdownServices();
            }
            catch (SimulationException e)
            {
                logger.error(e);
            }
        }
    };
    
    private ScenarioEditorApplication(String args[])
    {
        addService(this);
        
        addService(new PreferenceProvider("simjr/editor"));
        
        // Set up an action manager
        final ActionManager actionManager = new ActionManager(this);
        addService(actionManager);
        
        // Whenever the model changes, update actions
        model.addModelChangeListener(new ModelChangeListener() {

            public void onModelChanged(ModelChangeEvent e)
            {
                actionManager.updateActions();
            }});
        
        // Register undo manager
        final UndoService undoService = new UndoService() {

            private static final long serialVersionUID = 6703957396760212343L;

            @Override
            public synchronized boolean addEdit(UndoableEdit anEdit)
            {
                // Update actions whenever an undoable edit is added. Kind of a hack.
                boolean r = super.addEdit(anEdit);
                actionManager.updateActions();
                return r;
            }};
        addService(undoService);
        
        // Register app state, required by PVD
        final DefaultApplicationStateService appState = new DefaultApplicationStateService();
        addService(appState);
        
        // Register a dummy simulation, required by PVD
        addService(new Simulation(new SimpleTerrain(new Geodetic.Point())));
        
        // Register scenario runner
        addService(new ScenarioRunner(this));
        
        registerActions(actionManager);

        processCommandLine(args);
        
        frame = new ScenarioEditorMainFrame(this);
        frame.addWindowListener(exitHandler);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1024, 768);
        frame.setVisible(true);
        
        appState.setState(ApplicationState.RUNNING);
        
        model.fireChange(new ModelChangeEvent(model, model, Model.LOADED));
        
        actionManager.updateActions();
    }

    private void processCommandLine(String[] args)
    {
        if(args.length == 0)
        {
            return;
        }
        
        final String file = args[0];
        try
        {
            model.load(new File(file));
        }
        catch (ModelException e)
        {
            logger.error(e);
            JOptionPane.showMessageDialog(null, 
                                          String.format("Failed to load '%s': %s", file, e.getMessage()), 
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerActions(final ActionManager actionManager)
    {
        new NewAction(actionManager);
        new OpenAction(actionManager);
        new ImportOSMAction(actionManager);
        new SaveAction(actionManager, false);
        new SaveAction(actionManager, true);
        new UndoAction(actionManager);
        new RedoAction(actionManager);
        new DeleteEntityAction(actionManager);
        new CloneEntityAction(actionManager);
        new ShowAllAction(actionManager);
        new AddDistanceToolAction(actionManager);
        new ClearDistanceToolsAction(actionManager);
        new EditRouteAction(actionManager);
        new RunAction(actionManager);
        new AdjustMapOpacityAction(actionManager);
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.editor.ScenarioEditorServiceManager#getModel()
     */
    public Model getModel()
    {
        return model;
    }
    
    public JFrame getFrame()
    {
        return frame;
    }
    
    public void showError(String text, Throwable e)
    {
        JOptionPane.showMessageDialog(frame, text, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public void showPanel(JComponent component)
    {
       
    }
    
    public File selectFile(Object... filters)
    {
        final String cd = findService(PreferenceProvider.class).getPreferences().get("chooser.currentDirectory", null);
        final JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(cd != null ? new File(cd) : null);
        SwingTools.addFileFilters(chooser, filters);
        if(chooser.showOpenDialog(getFrame()) != JFileChooser.APPROVE_OPTION)
        {
            return null;
        }
        
        final File selectedFile = chooser.getSelectedFile();
        findService(PreferenceProvider.class).getPreferences().put("chooser.currentDirectory", selectedFile.getParent());
        return selectedFile;
    }
    
    /**
     * Check if the current scenario is changed. If it is, prompt the user to save before continuing.
     * 
     * @return false if the user asked to cancel
     */
    @Override
    public boolean saveIfModelIsChanged()
    {
        if(getModel().isDirty())
        {
            int r = JOptionPane.showConfirmDialog(getFrame(), 
                    "The current scenario has changed. Would you like to save your changes?" , "Save?", JOptionPane.YES_NO_CANCEL_OPTION);
            if(r == JOptionPane.CANCEL_OPTION)
            {
                return false;
            }
            if(r == JOptionPane.YES_OPTION)
            {
                findService(ActionManager.class).getAction(SaveAction.SAVE).actionPerformed(null);
            }
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        findService(ActionManager.class).updateActions();
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.DefaultServiceManager#shutdownServices()
     */
    @Override
    public void shutdownServices() throws SimulationException
    {
        saveIfModelIsChanged();
        super.shutdownServices();
        logger.info("Exiting");
        System.exit(0);
    }

    /**
     * @param args
     */
    public static void main(final String[] args)
    {
        logger.info("Sim Jr Scenario Editor started");
        logger.info("   simjr.home=" + SimJrProps.get(SimJrProps.HOME));
        logger.info("   current directory = " + System.getProperty("user.dir"));
        
        logger.info(SimJrProps.get("simjr.editor.multiline"));

        SwingUtilities.invokeLater(new Runnable() { public void run() { new ScenarioEditorApplication(args); }});
    }

    @Override
    public void shutdown() throws SimulationException
    {        
    }

    @Override
    public void start(ProgressMonitor progress) throws SimulationException
    {        
    }

    @Override
    public Object getAdapter(Class<?> klass)
    {
        return null;
    }
}
