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
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.simjr.ui.pvd.PlanViewDisplay;
import com.soartech.simjr.ui.pvd.imagery.MapTileRenderer.TileSourceListener;
import com.soartech.simjr.ui.pvd.imagery.MapTileRenderer.TileZoomListener;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.OsmFileCacheTileLoader;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.OsmFileCacheTileLoader.TileJob;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.Tile;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.TileCache;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.TileLoaderListener;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.TileSource;

public class MapImageryDownloader extends JXPanel implements TileSourceListener, TileZoomListener
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.getLogger(MapImageryDownloader.class);
    
    private static final long AVG_TILE_SIZE_BYTES = 45000;
    private static final String LAST_USED_FOLDER = "LAST_USED_FOLDER";

    private final PlanViewDisplay pvd;
    private final MapTileRenderer mapRenderer;
    
    private ScheduledThreadPoolExecutor scheduler;
    private final static int MAX_TILE_DOWNLOADS_PER_SECOND = 10;
    private final static int THREAD_POOL_SIZE = 4;
    
    private RangeSlider zoomSlider;
    private final JButton downloadButton = new JButton("Download");
    private final JButton cancelButton = new JButton("Cancel");
    
    private final ActionListener cancelButtonClose = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            onComplete();
        }
    };
    
    private final ActionListener cancelButtonStop = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            scheduler.shutdownNow();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        scheduler.awaitTermination(10, TimeUnit.SECONDS);
                    }
                    catch (InterruptedException e) {
                        logger.warn(e.toString());
                    }
                    finally {
                        onDownloadCompleted();
                    }
                }
            });
        }
    };
    
    /**
     * UI for capturing map imagery
     * 
     * @param mapRenderer
     */
    public MapImageryDownloader(final PlanViewDisplay pvd)
    {
        super();
        
        this.pvd = pvd;
        this.mapRenderer = pvd.getMapTileRenderer();
        
        setLayout(new MigLayout("gapx 0"));
        
        zoomSlider = new RangeSlider(JSlider.VERTICAL, MapTileRenderer.MIN_ZOOM, MapTileRenderer.MAX_ZOOM, 0);
        zoomSlider.setUpperValue(mapRenderer.getZoom());
        zoomSlider.setPreferredSize(new Dimension(50, 300));
        zoomSlider.setMajorTickSpacing(1);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setPaintLabels(true);
        zoomSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                RangeSlider source = (RangeSlider)e.getSource();
                if (!source.getValueIsAdjusting()) {
                    updateDownloadButton();
                }
            }
        });
        onTileSourceChanged(mapRenderer.getTileSource());
        onTileZoomChanged(mapRenderer.getZoom());
        
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDownload();
            }
        });
        
        onDownloadCompleted(); // Setup buttons, mouse listeners, resize listeners
        
        add(downloadButton, "wrap 0, align right, growx");
        add(cancelButton, "wrap, align right, growx");
        
        JPanel zoomPanel = new JPanel();
        zoomPanel.setLayout(new BoxLayout(zoomPanel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Zoom Depth");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        zoomPanel.add(label);
        zoomPanel.add(zoomSlider);
        zoomPanel.setPreferredSize(new Dimension(50, 325));
        add(zoomPanel, "span, align right");
        
        setAlpha(0.95f);
        
        mapRenderer.addTileSourceListener(this);
        mapRenderer.addTileZoomListener(this);
        
        pvd.addComponentListener(resizeListenerPositioner);
        
        pvd.add(this);
        updateGuiPosition();
        updateDownloadButton();
    }
    
    //Keep the controls in the correct position
    private ComponentAdapter resizeListenerPositioner = new ComponentAdapter() {
        public void componentResized(ComponentEvent evt) {
            updateGuiPosition();
        }
    };
    
    //Keep the controls in the correct position
    private ComponentAdapter resizeListenerDownloadUpdater = new ComponentAdapter() {
        public void componentResized(ComponentEvent evt) {
            updateDownloadButton();
        }
    };
    
    //Maintains the current set of visible tiles
    private MouseMotionAdapter pvdMouseMotionAdapter = new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            if(SwingUtilities.isLeftMouseButton(e)) {
                if (!pvd.isDraggingEntity()) {
                    updateDownloadButton();
                }
            }
        }
    }; 

    //Maintains the current set of visible tiles
    private MouseWheelListener pvdMouseWheelListener = new MouseWheelListener() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            updateDownloadButton();
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
        pvd.removeComponentListener(resizeListenerDownloadUpdater);
        pvd.removeComponentListener(resizeListenerPositioner);
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
    private void onDownload()
    {
        Preferences prefs = Preferences.userRoot().node(getClass().getName());
        final JFileChooser fc = new JFileChooser(prefs.get(LAST_USED_FOLDER, new File(".").getAbsolutePath()));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            final File destDir = fc.getSelectedFile();
            logger.info("User selected: " + destDir);
            prefs.put(LAST_USED_FOLDER, destDir.getAbsolutePath());
            
            Map<Integer, Rectangle> regions = getVisibleTileRegions();
            scheduleTileDownloads(destDir, regions);
        }
    }
    
    /**
     * Updates the download button with the current tile information.
     * TODO: On another thread?
     */
    private void updateDownloadButton()
    {
        Map<Integer, Rectangle> visibleRegions = getVisibleTileRegions();
        
        int totalTiles = 0;
        for(Map.Entry<Integer, Rectangle> entry: visibleRegions.entrySet())
        {
            Rectangle region = entry.getValue();
            int tiles = region.width * region.height;
            totalTiles += tiles;
        }
        
        downloadButton.setText("<html><b>DOWNLOAD</b><br><i>" + 
                NumberFormat.getIntegerInstance().format(totalTiles) + " tiles<br>(~" + 
                readableFileSize(totalTiles * AVG_TILE_SIZE_BYTES, false) + ")</i></html>");
        
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
    
    private void onDownloadStarted()
    {
        downloadButton.setEnabled(false);
        downloadButton.setText("<html><b>DOWNLOADING..</b><br><i>Initializing..</i></html>");
        
        cancelButton.removeActionListener(cancelButtonClose);
        cancelButton.addActionListener(cancelButtonStop);
        cancelButton.setText("Cancel");
        
        pvd.removeMouseMotionListener(pvdMouseMotionAdapter);
        pvd.removeMouseWheelListener(pvdMouseWheelListener);
        pvd.removeComponentListener(resizeListenerDownloadUpdater);
        
        updateGuiPosition();
    }
    
    private void onDownloadCompleted()
    {
        cancelButton.removeActionListener(cancelButtonStop);
        cancelButton.addActionListener(cancelButtonClose);
        cancelButton.setText("Close");
        
        downloadButton.setEnabled(true);
        updateDownloadButton();
        
        pvd.addMouseMotionListener(pvdMouseMotionAdapter);
        pvd.addMouseWheelListener(pvdMouseWheelListener);
        pvd.addComponentListener(resizeListenerDownloadUpdater);
        
        updateGuiPosition();
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
    
    private int getTotalTileCount(Map<Integer, Rectangle> regions)
    {
        int t = 0;
        for(Rectangle r: regions.values()) { t += r.height * r.width; }
        return t;
    }
    
    private void scheduleTileDownloads(File destinationDir, Map<Integer, Rectangle> regions) 
    {
        final int totalTiles = getTotalTileCount(regions);
        final OsmFileCacheTileLoader tileLoader;
        
        try {
            tileLoader = new OsmFileCacheTileLoader(new TileLoaderListener() {
                private AtomicInteger count = new AtomicInteger(0);
                @Override
                public void tileLoadingFinished(final Tile tile, final boolean success) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            //TODO: What to do when success == false?
                            int currentCount = count.incrementAndGet();
                            if(currentCount == totalTiles) {
                                onDownloadCompleted();
                            }
                            else {
                                double percent =  (double)currentCount / totalTiles;
                                downloadButton.setText("<html><b>DOWNLOADING..</b><br>" + 
                                        "<i>" + NumberFormat.getPercentInstance().format(percent) +"</i></html>");
                            }
                        }
                    });
                }
                @Override public TileCache getTileCache() { return null; } //TODO: Can this be utilized?
            }, destinationDir);
        }
        catch (IOException e) {
            logger.error("Unable to create tile loader!", e);
            return;
        }
        
        scheduler = new ScheduledThreadPoolExecutor(THREAD_POOL_SIZE);
        onDownloadStarted();
        
        for(Map.Entry<Integer, Rectangle> entry: regions.entrySet()) {
            int zoom = entry.getKey();
            Rectangle region = entry.getValue();
            for(int x = region.x; x < region.x + region.width; x++) {
                for(int y = region.y; y < region.y + region.height; y++) {
                    Tile tile = new Tile(mapRenderer.getTileSource(), x, y, zoom);
                    TileJob job = tileLoader.createTileLoaderJob(tile);
                    int delay = scheduler.getQueue().size() / MAX_TILE_DOWNLOADS_PER_SECOND;
                    scheduler.schedule(job, delay, TimeUnit.SECONDS);
                }
            }
        }
    }
    
    @Override
    public void onTileSourceChanged(TileSource ts)
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
    public void onTileZoomChanged(int zoom)
    {
        @SuppressWarnings("unchecked")
        Hashtable<Integer, Component> labels = zoomSlider.createStandardLabels(1);
        labels.put(new Integer(zoom), new JLabel("<html><b>" + zoom + "*</b></html>"));
        zoomSlider.setLabelTable(labels);
        zoomSlider.repaint();
    }
}
