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
 * Created on Jun 11, 2007
 */
package com.soartech.simjr.ui.pvd.imagery;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXPanel;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import com.soartech.simjr.ui.pvd.PlanViewDisplay;
import com.soartech.simjr.ui.pvd.imagery.MapTileRenderer.TileSourceListener;

public class MapImageryDownloader extends JXPanel implements TileSourceListener
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(MapImageryDownloader.class);
    
    private static final int WIDTH = 125, HEIGHT = 375;

    private final PlanViewDisplay pvd;
    
    private JSlider zoomSlider;
    private final JButton downloadButton = new JButton("Download");
    private final JButton doneButton = new JButton("X");
    
    /**
     * UI for capturing map imagery
     * 
     * TODO: Update zoom slider on zoom, tile source change
     * 
     * @param mapRenderer
     */
    public MapImageryDownloader(final PlanViewDisplay pvd)
    {
        super();
        
        this.pvd = pvd;
        
        setLayout(new MigLayout("gapx 0"));
        
        MapTileRenderer mapRenderer = pvd.getMapTileRenderer();
        zoomSlider = new JSlider(JSlider.VERTICAL, MapTileRenderer.MIN_ZOOM, MapTileRenderer.MAX_ZOOM, mapRenderer.getZoom());
        zoomSlider.setPreferredSize(new Dimension(50, 300));
        zoomSlider.setMajorTickSpacing(1);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setPaintLabels(true);
        zoomSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                if (!source.getValueIsAdjusting()) {
                    logger.info("zoom: " + source.getValue());
                    //int opacity = (int)source.getValue();
                    //MapImageryDownloader.this.mapRenderer.setOpacity(opacity/100.0f);
                }
            }
        });
        updateZoomSlider(mapRenderer.getTileSource());
        
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                download();
            }
        });
        
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onComplete();
            }
        });
        
        add(downloadButton);
        add(doneButton, "wrap");
        
        JPanel zoomPanel = new JPanel();
        zoomPanel.add(new JLabel("Zoom Depth"));
        zoomPanel.add(zoomSlider);
        zoomPanel.setPreferredSize(new Dimension(50, 325));
        add(zoomPanel, "span");
        
        pvd.addComponentListener(resizeListener);
        mapRenderer.addTileSourceListener(this);

        setAlpha(0.95f);
        
        pvd.add(this);
        updateGuiPosition();
    }
    
    //Keep the controls in the correct position
    private ComponentAdapter resizeListener = new ComponentAdapter() {
        public void componentResized(ComponentEvent evt) {
            logger.info("Updating GUI position");
            updateGuiPosition();
        }
    };
    
    private void updateGuiPosition()
    {
        this.setBounds(pvd.getWidth() - WIDTH - 10, 10, WIDTH, HEIGHT);
        pvd.repaint();
        pvd.revalidate();
    }
    
    /**
     * Called when the user is done with this UI.
     */
    private void onComplete()
    {
        logger.info("MapImageryDownloader complete");
        pvd.remove(this);
        pvd.removeComponentListener(resizeListener);
        pvd.repaint();
    }
    
    /**
     * Called to initialize download of tile imagery.
     */
    private void download()
    {
        logger.info("TODO: Capture currently viewable area imagery");
    }
    
    private void updateZoomSlider(TileSource ts)
    {
        if(ts == null) {
            zoomSlider.setEnabled(false);
            downloadButton.setEnabled(false);
        }
        else {
            zoomSlider.setEnabled(true);
            downloadButton.setEnabled(true);
            
            zoomSlider.setMinimum(ts.getMinZoom());
            zoomSlider.setMaximum(ts.getMaxZoom());
        }
    }

    @Override
    public void onTileSourceChanged(TileSource ts)
    {
        updateZoomSlider(ts);
    }
}
