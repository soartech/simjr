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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXPanel;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.shapesystem.swing.SwingCoordinateTransformer;
import com.soartech.simjr.ui.pvd.imagery.MapTileRenderer;

/**
 * Displays a large set of coordinate system information in a transparent
 * panel on the map. 
 */
public class MapDebugPanel extends JXPanel
{
    private static final long serialVersionUID = 1L;

    private final MapTileRenderer mapRenderer;
    private SwingCoordinateTransformer transformer;
    private PvdView pvd = null;
    
    private final JLabel simCenterPxLabel = new BoldJLabel();
    private final JLabel simZoomLabel = new BoldJLabel();
    private final JLabel simMppLabel = new BoldJLabel();
    private final JLabel simMetersCenterLabel = new BoldJLabel();
    private final JLabel simMetersUpperLeftLabel = new BoldJLabel();
    
    private final JLabel osmZoomLabel = new BoldJLabel();
    private final JLabel osmCenterLabel = new BoldJLabel();
    private final JLabel osmMppLabel = new BoldJLabel();
    private final JLabel mouseOsmPxCoords = new BoldJLabel();
    private final JLabel mouseOsmTileCoords = new BoldJLabel();
    private final JLabel osmScaleFactorLabel = new BoldJLabel();
    private final JLabel osmTileSizeLabel = new BoldJLabel();
    
    private final JLabel mouseScreenCoords = new BoldJLabel();
    private final JLabel mouseLatlonCoords = new BoldJLabel();
    private final JLabel mouseMetersCoords = new BoldJLabel();
    
    //TODO: White bordered text would be more visible
    private class BoldJLabel extends JLabel {
        private static final long serialVersionUID = 1L;
        public BoldJLabel() {
            super();
            bold();
        }
        public BoldJLabel(String text) {
            super(text);
            bold();
        }
        private void bold() {
            setFont(getFont().deriveFont(Font.BOLD, 12));
        }
    }
    
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
    
        
    public MapDebugPanel(SwingCoordinateTransformer transformer, final MapTileRenderer mapRenderer)
    {
        super();
        
        setLayout(new MigLayout("gapy 0", "[]5[]", ""));
        
        this.mapRenderer = mapRenderer;
        this.transformer = transformer;
        
        add(new BoldJLabel("Sim Center (px):"));
        add(simCenterPxLabel, "wrap");
        
        add(new BoldJLabel("Sim Center (m):" ));
        add(simMetersCenterLabel, "wrap");
        
        add(new BoldJLabel("Sim UL (m):" ));
        add(simMetersUpperLeftLabel, "wrap");
        
        add(new BoldJLabel("Sim Zoom:" ));
        add(simZoomLabel, "wrap");
        
        add(new BoldJLabel("Sim mpp:"));
        add(simMppLabel, "wrap");
        
        add(new BoldJLabel("OSM Zoom:"));
        add(osmZoomLabel, "wrap");
        
        add(new BoldJLabel("OSM Center (px):"));
        add(osmCenterLabel, "wrap");
        
        add(new BoldJLabel("OSM mpp:"));
        add(osmMppLabel, "wrap");
        
        add(new BoldJLabel("OSM Scale:"));
        add(osmScaleFactorLabel, "wrap");
        
        add(new BoldJLabel("OSM Tile size:"));
        add(osmTileSizeLabel, "wrap");
        
        add(new BoldJLabel("OSM Mouse (px): "));
        add(mouseOsmPxCoords, "wrap");
        
        add(new BoldJLabel("OSM Mouse (tile): "));
        add(mouseOsmTileCoords, "wrap");
        
        add(new BoldJLabel("Mouse Screen:"));
        add(mouseScreenCoords, "wrap");
        
        add(new BoldJLabel("Mouse Lat/Lon:"));
        add(mouseLatlonCoords, "wrap");
        
        add(new BoldJLabel("Mouse Meters:"));
        add(mouseMetersCoords, "wrap");
        
        setAlpha(0.9f);
        
        this.setPreferredSize(new Dimension(300, getPreferredSize().height));
    }
    
    public void setActivePvd(PvdView newPvd)
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
        if(pvd == null || mapRenderer == null || mapRenderer.getTileSource() == null) { return; }
        
        Point mousePtPx = pvd.getMousePosition();
        if(mousePtPx != null)
        {
            simCenterPxLabel.setText(String.format("%8.2f, %8.2f", new Object[] { transformer.getPanOffsetX(), -transformer.getPanOffsetY() }));
            simZoomLabel.setText(Double.toString(transformer.getScale()));
            simMppLabel.setText(Double.toString(transformer.screenToMeters(1)));
            
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
            osmMppLabel.setText(Double.toString(mapRenderer.getMetersPerPixel()));
            
            double scale = mapRenderer.getScaleFactor();
            
            Point.Double osmUpperLeft = new Point.Double(osmCenterPx.x - (pvd.getWidth()/2 / scale), osmCenterPx.y - (pvd.getHeight()/2) / scale);
            Point.Double osmMousePx = new Point.Double(osmUpperLeft.x + mousePtPx.x / scale, osmUpperLeft.y + mousePtPx.y / scale);
            mouseOsmPxCoords.setText(String.format("%8.2f, %8.2f", new Object[] { osmMousePx.x, osmMousePx.y }));
            
            double tileSize = mapRenderer.getTileSize();
            osmTileSizeLabel.setText(String.format("%8.2f", tileSize));
            mouseOsmTileCoords.setText(String.format("%8.2f, %8.2f", new Object[] { osmMousePx.x / tileSize, osmMousePx.y / tileSize}));
            
            osmScaleFactorLabel.setText(String.format("%8.6f", scale));
        }
    }
}
