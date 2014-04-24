package com.soartech.simjr.ui.editor.actions;

import javax.swing.Icon;

/**
 * Decorates an {@link AbstractEditorAction} with an icon.
 * TODO: There needs to be a builder system for editor actions in Sim Jr.
 */
public class IconDecorator
{
    public static <T extends AbstractEditorAction> T decorate(T editorAction, Icon icon)
    {
        editorAction.setIcon(icon);
        return editorAction;
    }
}
