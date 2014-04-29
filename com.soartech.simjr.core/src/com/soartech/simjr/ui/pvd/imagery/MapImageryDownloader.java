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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Hashtable;

import javax.swing.BoxLayout;
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
import com.soartech.simjr.ui.pvd.imagery.MapTileRenderer.TileZoomListener;

public class MapImageryDownloader extends JXPanel implements TileSourceListener, TileZoomListener
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(MapImageryDownloader.class);
    
    private static final int WIDTH = 125, HEIGHT = 375;

    private final PlanViewDisplay pvd;
    
    private RangeSlider zoomSlider;
    private final JLabel captureStats = new JLabel();
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
        zoomSlider = new RangeSlider(JSlider.VERTICAL, MapTileRenderer.MIN_ZOOM, MapTileRenderer.MAX_ZOOM, mapRenderer.getZoom());
        zoomSlider.setPreferredSize(new Dimension(50, 300));
        zoomSlider.setMajorTickSpacing(1);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setPaintLabels(true);
        zoomSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                RangeSlider source = (RangeSlider)e.getSource();
                if (!source.getValueIsAdjusting()) {
                    logger.info("zoom: " + source.getValue() + " - " + source.getUpperValue());
                }
            }
        });
        updateZoomSliderSource(mapRenderer.getTileSource());
        updateCurrentZoom(mapRenderer.getZoom());
        
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
        zoomPanel.setLayout(new BoxLayout(zoomPanel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Zoom Depth");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        zoomPanel.add(label);
        zoomPanel.add(zoomSlider);
        zoomPanel.setPreferredSize(new Dimension(50, 325));
        add(zoomPanel, "span, align right");
        
        pvd.addComponentListener(resizeListener);
        mapRenderer.addTileSourceListener(this);
        mapRenderer.addTileZoomListener(this);

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
    
    private void updateZoomSliderSource(TileSource ts)
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
    
    private void updateCurrentZoom(int currentZoom)
    {
        logger.info("Setting current zoom: " + currentZoom);
        @SuppressWarnings("unchecked")
        Hashtable<Integer, Component> labels = zoomSlider.createStandardLabels(1);
        labels.put(new Integer(currentZoom), new JLabel("<html><b>" + currentZoom + "*</b></html>"));
        zoomSlider.setLabelTable(labels);
        zoomSlider.repaint();
    }
    
    //TODO: On PVD drag, zoom, slider change
    private void updateCaptureStats()
    {
        logger.info("Updating capture stats.");
        
        //TODO: Determine all visible tiles
        //  Calculate lat/lon for corners
        //  Get tile X/Y min/max range for each active zoom level
        //  Sum total tile number
        //TODO: Estimate a download size based on number of tiles
        
        //TODO: Consider displaying num tiles per zoom level on slider
    }
    
    /**
     * TODO: Consider displaying progress bar, definitely run in background thread
     * TODO: Consider a cancel button to stop download
     */
    private void downloadTiles()
    {
        //TODO: Download tiles
        //For each zoom level
        //  Determine visible tiles
        //  For each visible tile
        //    download it, save it and metadata to encoded file name
    }

    @Override
    public void onTileSourceChanged(TileSource ts)
    {
        updateZoomSliderSource(ts);
    }

    @Override
    public void onTileZoomChanged(int zoom)
    {
        updateCurrentZoom(zoom);
    }
}
