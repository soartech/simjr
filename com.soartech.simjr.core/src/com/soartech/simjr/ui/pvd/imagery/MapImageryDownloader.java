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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
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
    
    private static final long AVG_TILE_SIZE_BYTES = 50000;

    private final PlanViewDisplay pvd;
    private final MapTileRenderer mapRenderer;
    
    private RangeSlider zoomSlider;
    private final JButton downloadButton = new JButton("Download");
    private final JButton doneButton = new JButton("Cancel");
    
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
        this.mapRenderer = pvd.getMapTileRenderer();
        
        setLayout(new MigLayout("gapx 0"));
        
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
                    updateTileInformation();
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
        
        add(downloadButton, "wrap 0, align right, growx");
        add(doneButton, "wrap, align right, growx");
        
        JPanel zoomPanel = new JPanel();
        zoomPanel.setLayout(new BoxLayout(zoomPanel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Zoom Depth");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        zoomPanel.add(label);
        zoomPanel.add(zoomSlider);
        zoomPanel.setPreferredSize(new Dimension(50, 325));
        add(zoomPanel, "span, align right");
        
        //setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setAlpha(0.95f);
        
        pvd.addComponentListener(resizeListener);
        pvd.addMouseWheelListener(pvdMouseWheelListener);
        pvd.addMouseMotionListener(pvdMouseMotionAdapter);
        
        mapRenderer.addTileSourceListener(this);
        mapRenderer.addTileZoomListener(this);
        
        pvd.add(this);
        updateGuiPosition();
        updateTileInformation();
    }
    
    //Keep the controls in the correct position
    private ComponentAdapter resizeListener = new ComponentAdapter() {
        public void componentResized(ComponentEvent evt) {
            logger.info("Updating GUI position");
            updateGuiPosition();
            updateTileInformation();
        }
    };
    
    //Maintains the current set of visible tiles
    private MouseMotionAdapter pvdMouseMotionAdapter = new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            if(SwingUtilities.isLeftMouseButton(e)) {
                if (!pvd.isDraggingEntity()) {
                    logger.info("PVD panning");
                    updateTileInformation();
                }
            }
        }
    }; 

    //Maintains the current set of visible tiles
    private MouseWheelListener pvdMouseWheelListener = new MouseWheelListener() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            logger.info("PVD mouse wheeled");
            updateTileInformation();
        }
    };
    
    private void updateGuiPosition()
    {
        this.setBounds(pvd.getWidth() - getPreferredSize().width - 10, 10, getPreferredSize().width, getPreferredSize().height);
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
        pvd.removeMouseMotionListener(pvdMouseMotionAdapter);
        pvd.removeMouseWheelListener(pvdMouseWheelListener);
        
        mapRenderer.removeTileSourceListener(this);
        mapRenderer.removeTileZoomListener(this);
        
        pvd.repaint();
    }
    
    /**
     * Called to initialize download of tile imagery.
     * TODO: Consider displaying progress bar, definitely run in background thread
     * TODO: Consider a cancel button to stop download
     */
    private void download()
    {
        //TODO: Download tiles
        //For each zoom level
        //  Determine visible tiles
        //  For each visible tile
        //    download it, save it and metadata to encoded file name
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
    
    /**
     * Updates the download button with the current tile information.
     * 
     * TODO: On another thread?
     */
    private void updateTileInformation()
    {
        logger.info("Updating tile info");
        
        Map<Integer, Rectangle> visibleRegions = getVisibleTileRegions();
        
        int totalTiles = 0;
        for(Map.Entry<Integer, Rectangle> entry: visibleRegions.entrySet())
        {
            Rectangle region = entry.getValue();
            int tiles = region.width * region.height;
            logger.info("zoom: " + entry.getKey() + " -> " + tiles + " tiles ("  + region + ")");
            totalTiles += tiles;
        }
        
        logger.info("Total tiles: " + totalTiles);
        
        downloadButton.setText("<html><b>DOWNLOAD</b><br><i>" + 
                NumberFormat.getIntegerInstance().format(totalTiles) + " tiles<br>(~" + 
                readableFileSize(totalTiles * AVG_TILE_SIZE_BYTES, false) + ")</i></html>");
        
        //TODO: Estimate a download size based on number of tiles
        
        //TODO: Consider displaying num tiles per zoom level on slider
    }
    
    /**
     * Converts a file size into a human readable format.
     * @param size
     * @return
     */
    private static String readableFileSize(long bytes, boolean si) 
    {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
    
    /**
     * Calculates the visible tile regions in tile coordinates per zoom level
     */
    private Map<Integer, Rectangle> getVisibleTileRegions()
    {
        Map<Integer, Rectangle> visibleRegions = new HashMap<Integer, Rectangle>();
        
        if(mapRenderer.getTileSource() == null) { return visibleRegions; }
        
        int minZoom = zoomSlider.getValue();
        int maxZoom = zoomSlider.getUpperValue();
        
        for(int z = minZoom; z <= maxZoom; z++) 
        {
            logger.info("Calculating tiles at zoom level: " + z);
            
            Point ul = mapRenderer.getTileCoordinates(new Point(0, 0), z);
            Point ur = mapRenderer.getTileCoordinates(new Point(pvd.getWidth(), 0), z);
            Point lr = mapRenderer.getTileCoordinates(new Point(pvd.getWidth(), pvd.getHeight()), z);
            Point ll = mapRenderer.getTileCoordinates(new Point(0, pvd.getHeight()), z);
            
            int tileMinX = Math.min(ul.x, ll.x);
            int tileMinY = Math.min(ul.y, ur.y);
            int tileMaxX = Math.min(ur.x, lr.x);
            int tileMaxY = Math.min(lr.y, ll.y);
            
            Rectangle visibleRegion = new Rectangle(tileMinX, tileMinY, 1 + tileMaxX - tileMinX, 1 + tileMaxY - tileMinY);
            visibleRegions.put(new Integer(z), visibleRegion);
        }
        
        return visibleRegions;
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
