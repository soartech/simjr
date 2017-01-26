package com.soartech.simjr.ui.pvd;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.jdesktop.swingx.JXPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MapLicenseAttributionHtml extends JXPanel {

    private static final Logger logger = LoggerFactory.getLogger(MapLicenseAttributionHtml.class);
    
    private static final long serialVersionUID = 1430443477729968775L;
    
    private DefaultPvdView pvd = null;
    private JEditorPane htmlLabel = new JEditorPane();
    
    public MapLicenseAttributionHtml()
    {
        super(new BorderLayout());
        
        htmlLabel.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if(Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        } catch (IOException e1) {
                            logger.error(e1.getMessage());
                        } catch (URISyntaxException e1) {
                            logger.error(e1.getMessage());
                        }
                    }
                }
            }
        });
        
        htmlLabel.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        htmlLabel.setEditable(false);
        
        add(htmlLabel, BorderLayout.CENTER);
        
        setAlpha(0.7f);
    }
    
    public void setText(String text)
    {
        htmlLabel.setText(text);
    }
    
    public void setActivePvd(DefaultPvdView newPvd)
    {
        pvd = newPvd;
    }

}
