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
package com.soartech.simjr.ui.pvd;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXPanel;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOpenAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.shapesystem.swing.SwingCoordinateTransformer;

public class MapController extends JXPanel
{
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(MapController.class);

    private final MapRenderer mapRenderer;
    private SwingCoordinateTransformer transformer;
    private PlanViewDisplay pvd = null;
    
    private final JComboBox<TileSource> sourceSelector = new JComboBox<TileSource>(new TileSource[] {
            new OsmTileSource.Mapnik(),
            new OsmTileSource.CycleMap(),
            new BingAerialTileSource(),
            new MapQuestOsmTileSource(),
            new MapQuestOpenAerialTileSource()
    });
    private JComboBox<TileLoader> loaderSelector; 
    
    private final JLabel simCenterPxLabel = new JLabel();
    private final JLabel simZoomLabel = new JLabel();
    private final JLabel simMppLabel = new JLabel();
    private final JLabel simMetersCenterLabel = new JLabel();
    private final JLabel simMetersUpperLeftLabel = new JLabel();
    
    private final JLabel osmZoomLabel = new JLabel();
    private final JLabel osmCenterLabel = new JLabel();
    private final JLabel osmMppLabel = new JLabel();
    private final JLabel mouseOsmPxCoords = new JLabel();
    private final JLabel mouseOsmTileCoords = new JLabel();
    private final JLabel osmScaleFactorLabel = new JLabel();
    private final JLabel osmTileSizeLabel = new JLabel();
    
    private final JLabel mouseScreenCoords = new JLabel();
    private final JLabel mouseLatlonCoords = new JLabel();
    private final JLabel mouseMetersCoords = new JLabel();
    
    private MouseMotionListener mouseListener = new MouseMotionAdapter() {
        public void mouseMoved(MouseEvent e) {
            update();
        }
        @Override
        public void mouseDragged(MouseEvent e) {
            update();
        }
    };
    
    private MouseWheelListener mouseWheelListener = new MouseWheelListener() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            update();
        }
    };
    
        
    public MapController(SwingCoordinateTransformer transformer, final MapRenderer mapRenderer)
    {
        super();
        
        setLayout(new MigLayout("gapy 0", "[]5[]", ""));
        
        this.mapRenderer = mapRenderer;
        this.transformer = transformer;
        
        try {
            loaderSelector = new JComboBox<TileLoader>(new TileLoader[] { 
                    new OsmFileCacheTileLoader(mapRenderer),
                    new OsmTileLoader(mapRenderer)
            });
            loaderSelector.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    mapRenderer.setTileLoader((TileLoader)e.getItem());
                }
            });
            mapRenderer.setTileLoader((TileLoader)loaderSelector.getSelectedItem());
        }
        catch(IOException ioe) {
            logger.error("Unable to create loaders!", ioe);
        }
        
        sourceSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                mapRenderer.setTileSource((TileSource)e.getItem());
            }
        });
        
        final JXPanel configPanel = new JXPanel();
        configPanel.add(sourceSelector);
        configPanel.add(loaderSelector);
        configPanel.setAlpha(0.7f);
        add(configPanel, "span 2, wrap 30");
        
        add(new JLabel("Sim Center (px):"));
        add(simCenterPxLabel, "wrap");
        
        add(new JLabel("Sim Center (m):" ));
        add(simMetersCenterLabel, "wrap");
        
        add(new JLabel("Sim UL (m):" ));
        add(simMetersUpperLeftLabel, "wrap");
        
        add(new JLabel("Sim Zoom:" ));
        add(simZoomLabel, "wrap");
        
        add(new JLabel("Sim mpp:"));
        add(simMppLabel, "wrap");
        
        add(new JLabel("OSM Zoom:"));
        add(osmZoomLabel, "wrap");
        
        add(new JLabel("OSM Center (px):"));
        add(osmCenterLabel, "wrap");
        
        add(new JLabel("OSM mpp:"));
        add(osmMppLabel, "wrap");
        
        add(new JLabel("OSM Scale:"));
        add(osmScaleFactorLabel, "wrap");
        
        add(new JLabel("OSM Tile size:"));
        add(osmTileSizeLabel, "wrap");
        
        add(new JLabel("OSM Mouse (px): "));
        add(mouseOsmPxCoords, "wrap");
        
        add(new JLabel("OSM Mouse (tile): "));
        add(mouseOsmTileCoords, "wrap");
        
        add(new JLabel("Mouse Screen:"));
        add(mouseScreenCoords, "wrap");
        
        add(new JLabel("Mouse Lat/Lon:"));
        add(mouseLatlonCoords, "wrap");
        
        add(new JLabel("Mouse Meters:"));
        add(mouseMetersCoords, "wrap");
        
        setBackground(Color.WHITE);
        //setAlpha(0.8f);
    }
    
    public void setActivePvd(PlanViewDisplay newPvd)
    {
        if(pvd != null) {
            pvd.removeMouseMotionListener(mouseListener);
            pvd.removeMouseWheelListener(mouseWheelListener);
        }
        
        pvd = newPvd;
        pvd.addMouseMotionListener(mouseListener);
        pvd.addMouseWheelListener(mouseWheelListener);
        update();
    }
    
    private void update()
    {
        if(pvd != null && mapRenderer != null && pvd.getMousePosition() != null)
        {
            simCenterPxLabel.setText(String.format("%8.2f, %8.2f", new Object[] { transformer.getPanOffsetX(), transformer.getPanOffsetY() }));
            simZoomLabel.setText(Double.toString(transformer.getScale()));
            simMppLabel.setText(Double.toString(transformer.screenToMeters(1)));
            Point mousePtPx = pvd.getMousePosition();
            
            Vector3 metersUpperLeft = transformer.screenToMeters(0, 0);
            Vector3 metersCenter = transformer.screenToMeters(pvd.getWidth()/2, pvd.getHeight()/2);
            
            simMetersCenterLabel.setText(String.format("%8.2f, %8.2f", new Object[]{ metersCenter.x, metersCenter.y }));
            simMetersUpperLeftLabel.setText(String.format("%8.2f, %8.2f", new Object[]{ metersUpperLeft.x, metersUpperLeft.y }));
            
            mouseScreenCoords.setText(mousePtPx.x + ", " + mousePtPx.y);
            Vector3 mouseMeters = pvd.getTransformer().screenToMeters(mousePtPx.x, mousePtPx.y);
            mouseMetersCoords.setText(String.format("%8.2f, %8.2f", new Object[]{ mouseMeters.x, mouseMeters.y }));
            Geodetic.Point mouseLatlon = pvd.getTerrain().toGeodetic(mouseMeters); 
            mouseLatlonCoords.setText(String.format("%8.6f, %8.6f", new Object[]{ Math.toDegrees(mouseLatlon.latitude), 
                                                                                  Math.toDegrees(mouseLatlon.longitude) }));
            
            Point osmCenterPx = mapRenderer.getCenter();
            osmZoomLabel.setText(Integer.toString(mapRenderer.getZoom()));
            osmCenterLabel.setText(osmCenterPx.x + ", " + osmCenterPx.y);
            osmMppLabel.setText(Double.toString(mapRenderer.getMeterPerPixel()));
            
            double scale = mapRenderer.getScaleFactor();
            
            Point.Double osmUpperLeft = new Point.Double(osmCenterPx.x - (pvd.getWidth()/2 / scale), osmCenterPx.y - (pvd.getHeight()/2) / scale);
            Point.Double osmMousePx = new Point.Double(osmUpperLeft.x + mousePtPx.x / scale, osmUpperLeft.y + mousePtPx.y / scale);
            logger.info("Center: " + osmCenterPx.x + ", " + osmCenterPx.y + "  " + 
                        "UpperLeft: " + osmUpperLeft.x + ", " + osmUpperLeft.y + "  " +
                        "Mouse: " + osmMousePx.x + ", " + osmMousePx.y
                        );
            mouseOsmPxCoords.setText(String.format("%8.2f, %8.2f", new Object[] { osmMousePx.x, osmMousePx.y }));
            
            double tileSize = mapRenderer.getTileSize();
            osmTileSizeLabel.setText(String.format("%8.2f", tileSize));
            mouseOsmTileCoords.setText(String.format("%8.2f, %8.2f", new Object[] { osmMousePx.x / tileSize, osmMousePx.y / tileSize}));
            
            osmScaleFactorLabel.setText(String.format("%8.6f", scale));
        }
    }
}
