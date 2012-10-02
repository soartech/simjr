package com.soartech.simjr.ui.editor;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

import com.soartech.simjr.ui.editor.ScenarioEditorServiceManager;
import com.soartech.simjr.ui.editor.TerrainPanel;

public class ScenarioPropertyEditor extends DefaultSingleCDockable{
	
    private final ScenarioEditorServiceManager app;
	
	public ScenarioPropertyEditor(ScenarioEditorServiceManager app)
	{
		super("ScenarioProperties");
		this.app = app;
		setLayout(new MigLayout());
		
        setCloseable(true);
        setMinimizable(true);
        setExternalizable(true);
        setMaximizable(true);
        setTitleText("ScenarioProperties");
        setResizeLocked(true);
		
		
		final TerrainPanel terrain = new TerrainPanel(app);
        terrain.setBorder(BorderFactory.createTitledBorder("Terrain"));
        add(terrain, BorderLayout.EAST);
	}
}
