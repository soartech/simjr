package com.soartech.simjr.ui.editor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.undo.UndoableEdit;

import net.miginfocom.swing.MigLayout;

import com.soartech.simjr.scenario.MetadataElement;
import com.soartech.simjr.scenario.TerrainElement;
import com.soartech.simjr.scenario.model.ModelChangeEvent;
import com.soartech.simjr.scenario.model.ModelChangeListener;

/**
 * Information Panel for editing scenario meta data such as the name and description.
 * @author joshua.haley
 *
 */
class ScenarioMetaDataPanel extends JPanel implements ModelChangeListener
{

    private final ScenarioEditorServiceManager app;

    private final JTextField nameField = new JTextField(15);
    private final JTextArea descriptionArea = new JTextArea(5, 30);

    public ScenarioMetaDataPanel(ScenarioEditorServiceManager app)
    {
        super(new MigLayout());
        this.app = app;

        add(new JLabel("Scenario Name"));
        add(nameField, "wrap");
        new EntryCompletionHandler(nameField)
        {

            @Override
            public boolean verify(JComponent input)
            {
                updateName();
                return true;
            }
        };
        

        add(new JLabel("Scenario Description"));
        descriptionArea.setLineWrap(true);
        add(descriptionArea);
        
        new EntryCompletionHandler(descriptionArea)
        {
            @Override
            public boolean verify(JComponent input)
            {
                updateDescription();
                return true;
            }
            
        };
        
        app.getModel().addModelChangeListener(this);
        onModelChanged(null);
    }

    private void updateName()
    {
        final MetadataElement m = app.getModel().getMeta();
        final String newName = nameField.getText().trim();

         UndoableEdit edit = m.setName(newName);
        
         if(edit != null)
         {
             app.findService(UndoService.class).addEdit(edit);
         }
    }
    
    private void updateDescription()
    {
        final MetadataElement m = app.getModel().getMeta();
        final String description = descriptionArea.getText().trim();
        
        UndoableEdit edit = m.setDescription(description);
        
        if(edit != null)
        {
            app.findService(UndoService.class).addEdit(edit);
        }
    }

    @Override
    public void onModelChanged(ModelChangeEvent e)
    {
        final MetadataElement m = app.getModel().getMeta();
        String name = m.getName();
        if(!nameField.getText().trim().equals(name))
        {
            nameField.setText(name);
        }
        
        String description = m.getDescription();
        if(!descriptionArea.getText().trim().equals(description))
        {
            descriptionArea.setText(description);
        }
    }
}

