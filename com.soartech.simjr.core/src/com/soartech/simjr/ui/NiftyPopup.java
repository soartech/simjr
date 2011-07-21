package com.soartech.simjr.ui;

import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.Popup;

class NiftyPopup extends Popup implements WindowFocusListener
{
    private final JWindow dialog;

    public NiftyPopup(Frame base, JPanel panel, int x, int y)
    {
        super();
        dialog = new JWindow(base);
        dialog.setFocusable(true);
        dialog.setLocation(x, y);
        dialog.setContentPane(panel);
        panel.setBorder(new JPopupMenu().getBorder());
        dialog.setSize(panel.getPreferredSize());
        dialog.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                {
                    dialog.setVisible(false);
                }
            }
        });
    }

    @Override
    public void show()
    {
        dialog.addWindowFocusListener(this);
        dialog.setVisible(true);
    }

    @Override
    public void hide()
    {
        dialog.setVisible(false);
        dialog.removeWindowFocusListener(this);
    }

    public void windowGainedFocus(WindowEvent e)
    {
        // NO-OP
    }

    public void windowLostFocus(WindowEvent e)
    {
        hide();
    }
}