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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import bibliothek.gui.dock.StackDockStation;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.location.CStackLocation;
import bibliothek.gui.dock.common.menu.CLayoutChoiceMenuPiece;
import bibliothek.gui.dock.common.menu.CLookAndFeelMenuPiece;
import bibliothek.gui.dock.common.menu.CThemeMenuPiece;
import bibliothek.gui.dock.common.menu.SingleCDockableListMenuPiece;
import bibliothek.gui.dock.common.theme.ThemeMap;
import bibliothek.gui.dock.facile.menu.FreeMenuPiece;
import bibliothek.gui.dock.facile.menu.RootMenuPiece;
import bibliothek.gui.dock.facile.menu.SubmenuPiece;
import bibliothek.gui.dock.support.lookandfeel.LookAndFeelList;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;

import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.scenario.model.ModelChangeEvent;
import com.soartech.simjr.scenario.model.ModelChangeListener;
import com.soartech.simjr.ui.view3DPanel;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.actions.AddDistanceToolAction;
import com.soartech.simjr.ui.actions.AdjustMapOpacityAction;
import com.soartech.simjr.ui.actions.ClearDistanceToolsAction;
import com.soartech.simjr.ui.actions.ShowAllAction;
import com.soartech.simjr.ui.editor.actions.ImportOSMAction;
import com.soartech.simjr.ui.editor.actions.NewAction;
import com.soartech.simjr.ui.editor.actions.NewEntityAction;
import com.soartech.simjr.ui.editor.actions.OpenAction;
import com.soartech.simjr.ui.editor.actions.RedoAction;
import com.soartech.simjr.ui.editor.actions.RunAction;
import com.soartech.simjr.ui.editor.actions.SaveAction;
import com.soartech.simjr.ui.editor.actions.UndoAction;

/**
 * @author ray
 * Modified to support the dockable framework  ~ Joshua Haley
 */
public class ScenarioEditorMainFrame extends JFrame implements ModelChangeListener
{
    public static final String MAP_FRAME_KEY ="__mapFrame";
    public static final String SCRIPTS_FRAME_KEY = "_scriptFrame";
    public static final String SOURCE_FRAME_KEY = "_sourceFrame";
    public static final String VIEW3D_FRAME_KEY ="_view3DFrame";
    public static final String ENTITYPROPERTIES_FRAME_KEY ="_entityPropertyFrame";
    public static final String SCENARIOEDITOR_FRAME_KEY = "_scenarioPropertyFrame";
    
    private final CStackLocation defaultMAPLocation = CStackLocation.base().normalRectangle(0, 0, 0.8, 0.7).stack(0);
    private final CStackLocation defaultView3DLocation = defaultMAPLocation.stack(1);
    private final CStackLocation defaultScriptPanelLocation = defaultMAPLocation.stack(2);
    private final CStackLocation defaultSourcePanelLocation  = defaultMAPLocation.stack(3);
    private final CStackLocation defaultEntityPropertiesLocation= CStackLocation.base().normalRectangle(0, 0.7, 0.8, 0.3).stack(0);
    private final CStackLocation defaultScenarioPropertiesLocation = defaultEntityPropertiesLocation.stack(1);
    private final CStackLocation defaultSingleDockableLocation = defaultEntityPropertiesLocation.stack(2);
    
    private static final long serialVersionUID = 691070210836482404L;
    private final ScenarioEditorApplication app;
    private Map<String,SingleCDockable> singleDockables = new HashMap<String,SingleCDockable>();
    
    
    /**
     * The common controller for Docking Frames.
     */
    private CControl control;
    
    private Dimension frameDimension;
    
    public ScenarioEditorMainFrame(ScenarioEditorApplication scenarioEditorApplication)
    {
        this.app = scenarioEditorApplication;
        
        setTitle(SimJrProps.get("simjr.window.title","Sim Jr Scenario Editor"));
        frameDimension = new Dimension(SimJrProps.get("simjr.window.width", 1000), SimJrProps.get("simjr.window.height", 800));
        setSize(frameDimension);

        //Dockable Frames integration
        control = new CControl(this);
 
        add(control.getContentArea());
        
        //set the default theme
        ThemeMap themes = control.getThemes();
        themes.select(ThemeMap.KEY_FLAT_THEME);
        
        //set the default look and feel
        LookAndFeelList lafList = LookAndFeelList.getDefaultList();
        lafList.setLookAndFeel(lafList.getSystem());

        // Listen for window closing event so we can save dock layout before
        // the frame is dispose.
        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent arg0)
            {
                control.destroy();
            }});

        EntityPropertiesPanel props = new EntityPropertiesPanel(app, app.getModel());
        MapPanel mapPanel = new MapPanel(app, props);
        
        addDockable(new ScriptsPanel(app), defaultScriptPanelLocation, SCRIPTS_FRAME_KEY);
        addDockable(new SourcePanel(app), defaultSourcePanelLocation, SOURCE_FRAME_KEY);
        addDockable(new view3DPanel(app), defaultView3DLocation, VIEW3D_FRAME_KEY);
        
        
        addDockable(mapPanel, this.defaultMAPLocation, MAP_FRAME_KEY);
        addDockable(new ScenarioPropertyEditor(app), this.defaultScenarioPropertiesLocation, SCENARIOEDITOR_FRAME_KEY);
        addDockable(props, this.defaultEntityPropertiesLocation, ENTITYPROPERTIES_FRAME_KEY);
        initMenu();

       

        this.app.getModel().addModelChangeListener(this);
        resetDockingLayout();
        
        /*Kludge-- Seems to be the only way to ensure that mapPanel gets default visibility 
         * At some point I am going to have to find out the best way to actually control layout/ordering ~ Josh Haley
         */
        singleDockables.get(MAP_FRAME_KEY).setVisible(false);
        singleDockables.get(MAP_FRAME_KEY).setVisible(true);
    }
    private void addDockable(SingleCDockable dockable, CLocation location, String key)
    {
        dockable.setLocation(location);
        singleDockables.put(key, dockable);
        control.addDockable(dockable);
        dockable.setVisible(true);        
    }
    
    private void updateTitle()
    {
        final File file = app.getModel().getFile();
        setTitle("SimJr Scenario Editor: " + (file != null ? file.getAbsolutePath() : "untitled") + (app.getModel().isDirty() ? " *" : ""));
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.editor.model.ModelChangeListener#onModelChanged(com.soartech.simjr.ui.editor.model.ModelChangeEvent)
     */
    public void onModelChanged(ModelChangeEvent e)
    {
        updateTitle();
    }

    /**
     * Reset the frames to show the built-in layout as determined by the 
     * default frame locations in this class's CLocation member variables. 
     * 
     * This should be the same layout you see upon creating a new simulation.
     * 
     * This method does not save the layout, it only resets the frame's positions.
     * 
     */
    public void resetDockingLayout()
    {
        //close each single dockable
        for(SingleCDockable dockable : singleDockables.values())
        {
            dockable.setVisible(false);
        }

        //reset the location of each single dockable
        singleDockables.get(MAP_FRAME_KEY).setLocation(defaultMAPLocation.getParent());
        singleDockables.get(VIEW3D_FRAME_KEY).setLocation(defaultView3DLocation.getParent());
        singleDockables.get(SCRIPTS_FRAME_KEY).setLocation(defaultScriptPanelLocation.getParent());
        singleDockables.get(SOURCE_FRAME_KEY).setLocation(defaultSourcePanelLocation.getParent());
        
        singleDockables.get(SCENARIOEDITOR_FRAME_KEY).setLocation(defaultScenarioPropertiesLocation.getParent());
        singleDockables.get(ENTITYPROPERTIES_FRAME_KEY).setLocation(defaultEntityPropertiesLocation.getParent());

        //show each single dockable
        for(SingleCDockable dockable : singleDockables.values())
        {
            dockable.setVisible(true);
        }
    }
    
    /**
     * Apply the default docking layout from the set that has been loaded.
     * 
     * The default is the one named "default". If a layout named "default" 
     * does not exist, then the first layout in the set will be loaded.
     */
    public void applyDefaultDockingLayout()
    {
        List<String> layouts = Arrays.asList(control.layouts());
        if(layouts.contains("default"))
        {
            control.load("default");
        }
        
        if(layouts.size() > 0)
        {
            control.load(layouts.get(0));
        }
    }
    
    private JMenuItem createMenuItemFromAction(FreeMenuPiece piece, Class<?> klass)
    {
        ActionManager am = app.findService(ActionManager.class);
        
        //add the action to the menu to create the component, 
        //then remove it from the menu and return it so it can be used
        JMenuItem item = piece.getMenu().add(am.getAction(klass.getCanonicalName()));
        piece.getMenu().remove(item);
        return item;
    }
    
    private void initMenu()
    {
        final JMenuBar bar = new JMenuBar();
        
        final ActionManager am = app.findService(ActionManager.class);
        final JMenu file = new JMenu("File");
        file.add(am.getAction(NewAction.class.getCanonicalName()));
        file.add(am.getAction(OpenAction.class.getCanonicalName()));
        
        // Putting importers into a sub menu just to keep things clean
        final JMenu importMenu = new JMenu("Import");
        importMenu.add(am.getAction(ImportOSMAction.class.getCanonicalName()));
        file.add(importMenu);
        
        file.add(am.getAction(SaveAction.SAVE));
        file.add(am.getAction(SaveAction.SAVE_AS));
        bar.add(file);
        
        final JMenu edit = new JMenu("Edit");
        edit.add(am.getAction(UndoAction.class.getCanonicalName()));
        edit.add(am.getAction(RedoAction.class.getCanonicalName()));
        bar.add(edit);
        
        RootMenuPiece view = new RootMenuPiece( "View", false );
        //add components to the piece, then add pieces to the viewMenuRoot
        FreeMenuPiece piece1 = new FreeMenuPiece();
        view.add(piece1);
        piece1.add(createMenuItemFromAction(piece1, ShowAllAction.class));//(am.getAction(ShowAllAction.class.getCanonicalName()));
        piece1.add(createMenuItemFromAction(piece1, AddDistanceToolAction.class));//am.getAction(AddDistanceToolAction.class.getCanonicalName()));
        piece1.add(createMenuItemFromAction(piece1, ClearDistanceToolsAction.class));//am.getAction(ClearDistanceToolsAction.class.getCanonicalName()));
        piece1.add(createMenuItemFromAction(piece1, AdjustMapOpacityAction.class));//am.getAction(AdjustMapOpacityAction.class.getCanonicalName()));
        piece1.add(new JSeparator());
        
        //add the list of views to show/hide to the view menu
        SingleCDockableListMenuPiece piece2 = new SingleCDockableListMenuPiece(control);
        view.add(piece2);
        //FreeMenuPiece piece2 = new FreeMenuPiece();
        //view.add(new JSeparator());
        /*view.add(new AbstractAction("Refresh") {
            private static final long serialVersionUID = -7408029630861071126L;

            public void actionPerformed(ActionEvent e)
            {
                am.updateActions();
            }});*/
        
        //add the layouts submenu to the view menu
        CLayoutChoiceMenuPiece piece4 = new CLayoutChoiceMenuPiece(control, false);
        SubmenuPiece submenuPiece = new SubmenuPiece(); 
        submenuPiece.getRoot().add(piece4);
        submenuPiece.getMenu().setText("Layouts");
        view.add(submenuPiece);
        
        //add look and feel submenu to the view menu
        CLookAndFeelMenuPiece piece5 = new CLookAndFeelMenuPiece(control);
        SubmenuPiece submenuPiece2 = new SubmenuPiece(); 
        submenuPiece2.getRoot().add(piece5);
        submenuPiece2.getMenu().setText("LookAndFeel");
        view.add(submenuPiece2);
        
        //add theme submenu to the view menu
        CThemeMenuPiece piece6 = new CThemeMenuPiece(control);
        SubmenuPiece submenuPiece3 = new SubmenuPiece(); 
        submenuPiece3.getRoot().add(piece6);
        submenuPiece3.getMenu().setText("Theme");
        view.add(submenuPiece3);

        bar.add(view.getMenu());
        
        final JMenu insert = new JMenu("Insert");
        insert.add(new NewEntityAction(am, "New Entity", "any", "ctrl E"));
        insert.add(new NewEntityAction(am, "New Waypoint", "waypoint", (String) null));
        insert.add(new NewEntityAction(am, "New Route", "route", (String) null));
        insert.add(new NewEntityAction(am, "New Area", "area", (String) null));
        insert.add(new NewEntityAction(am, "New Circular Region", "cylinder", (String) null));
        bar.add(insert);
        
        final JMenu run = new JMenu("Run");
        run.add(am.getAction(RunAction.class.getCanonicalName()));
        bar.add(run);
        
        setJMenuBar(bar);
    }

}
