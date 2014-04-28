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
 * Created on Jun 14, 2007
 */
package com.soartech.simjr.ui.pvd.imagery;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.AttributionSupport;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JobDispatcher;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.TileController;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.shapesystem.CoordinateTransformer;
import com.soartech.shapesystem.Scalar;
import com.soartech.simjr.ui.pvd.PlanViewDisplay;

public class MapTileRenderer implements TileLoaderListener
{
    private static final Logger logger = Logger.getLogger(MapTileRenderer.class);
    
    //Vectors for clock-wise spiral tile painting
    protected static final Point[] move = { new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1) };
    
    public static final int MAX_ZOOM = 22;
    public static final int MIN_ZOOM = 0;
    private static final int DOWNLOAD_THREAD_COUNT = 8;
    
    private boolean tileGridVisible = true; //TODO: Expose this 
    private boolean scrollWrapEnabled = false; //TODO: Determine if this should be supported
    
    private TileController tileController;
    private TileSource tileSource;
    
    private int zoom = 10; 
    
    private float opacity = 0.75f; //TODO: Expose this via GUI or config
    
    private final PlanViewDisplay pvd;
    
    //Responsible for displaying correct map imagery attribution (copyright notice, etc)
    //TODO: Fix bug that causes attribution to not show up for mapnik sometimes
    private AttributionSupport attribution = new AttributionSupport();
    
    public MapTileRenderer(PlanViewDisplay renderTarget)
    {
        JobDispatcher.setMaxWorkers(DOWNLOAD_THREAD_COUNT);
        tileSource = new OsmTileSource.Mapnik(); //TODO: Pull this value from properties
        //TODO: Seems to be a bug where the source param is ignored in tilecontroller constructor
        tileController = new TileController(tileSource, new MemoryTileCache(), this);
        this.pvd = renderTarget;
    }
    
    public int getZoom() { return this.zoom; }
    
    public void setZoom(int zoom) 
    {
        logger.info("Setting zoom level to: " + zoom);
        this.zoom = zoom;
    }
    
    /**
     * Sets the map zoom level to the closest match to the given mpp.
     * 
     * TODO: Could improve this algorithm by taking the closest scale, rather than 
     * always one lower
     * 
     * @param targetMpp meters per pixel to approximate
     */
    public void approximateScale(double targetMpp) 
    {
        logger.info("Approximating scale: " + targetMpp + " meters per pixels.");
        
        if(tileSource == null) { return; }
        
        int targetZoom = tileSource.getMinZoom();
        double currentTileMpp = getMetersPerPixel(targetZoom);
        
        while (currentTileMpp > targetMpp && targetZoom < tileSource.getMaxZoom()) 
        {
            logger.info("Zoom " + targetZoom + " is too high: " + currentTileMpp + " < " + targetMpp);
            targetZoom++; //zoom in
            currentTileMpp = getMetersPerPixel(targetZoom);
        }
        
        logger.info("Zoom: " + targetZoom + " at " + currentTileMpp + " is closest to: " + targetMpp);
        zoom = targetZoom;
    }
    
    public TileSource getTileSource() 
    {
        return tileSource;
    }
    
    public void setTileSource(TileSource source)
    {
        this.tileSource = source;
        tileController.cancelOutstandingJobs();
        
        if(source != null) { 
            if (tileSource.getMaxZoom() > MAX_ZOOM) {
                throw new RuntimeException("Maximum zoom level too high");
            }
            if (tileSource.getMinZoom() < MIN_ZOOM) {
                throw new RuntimeException("Minumim zoom level too low");
            }
            
            tileController.setTileSource(tileSource);
            if (zoom > tileSource.getMaxZoom()) {
                setZoom(tileSource.getMaxZoom());
            }
            attribution.initialize(tileSource);
            pvd.repaint();
        }
    }
    
    public void setTileLoader(TileLoader loader)
    {
        tileController.setTileLoader(loader);
    }

    @Override
    public void tileLoadingFinished(Tile tile, boolean success)
    {
        pvd.repaint();
    }

    @Override
    public TileCache getTileCache()
    {
        return tileController.getTileCache();
    }
    
    public int getTileSize()
    {
        return tileSource != null ? tileSource.getTileSize() : -1;
    }
    
    public float getOpacity() { return opacity; }
    
    public void setOpacity(float opacity) { this.opacity = opacity; }
    
    public void paint(Graphics2D g1)
    {
        if(tileSource == null) { return; }
        
        Graphics2D g = (Graphics2D) g1.create();
        //AffineTransform current = g.getTransform();
        double scale = getScaleFactor();
        g.transform(AffineTransform.getScaleInstance(scale, scale));
        
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
        g.setComposite(ac);
        
        Point center = getCenter();              // center pt in tile grid pixels
        int tilesize = tileSource.getTileSize(); // size of each tile in pixels
        int tilex = center.x / tilesize;         // center x in tile coords
        int tiley = center.y / tilesize;         // center y in tile coords
        int off_x = (center.x % tilesize);       // distance in px from center pt to left edge of tile
        int off_y = (center.y % tilesize);       // distance in px from center pt to top edge of tile

        int w2 = (int) ((pvd.getWidth() / 2) / scale);  // half screen width
        int h2 = (int) ((pvd.getHeight() / 2) / scale); // half screen height
        int posx = w2 - off_x;                   // x coord in screen px of left edge of tile 
        int posy = h2 - off_y;                   // y coord in screen px of upper edge of tile

        int diff_left = off_x;                   // distance in px from center pt to left edge of tile
        int diff_right = tilesize - off_x;       // distance in px from center pt to right edge of tile
        int diff_top = off_y;                    // distance in px from center pt to top edge of tile
        int diff_bottom = tilesize - off_y;      // distance in px from center pt to bottom edge of tile

        //Determine an initial direction for rendering
        boolean start_left = diff_left < diff_right;
        boolean start_top = diff_top < diff_bottom;
        int iMove = 0;
        if (start_top) {
            if (start_left) {
                iMove = 2;
            } else {
                iMove = 3;
            }
        } else {
            if (start_left) {
                iMove = 1;
            } else {
                iMove = 0;
            }
        } 
        
        // calculate the visibility borders
        double x_min = -tilesize; 
        double y_min = -tilesize;
        double x_max = pvd.getWidth() / scale; 
        double y_max = pvd.getHeight() / scale;
        
        //logger.info("drawing tiles from: (" + x_min + " - " + x_max + ", " + y_min + " - " + y_max + ")");

        // calculate the length of the grid (number of squares per edge)
        int gridLength = 1 << zoom;  //Used for scroll wrap only

        // paint the tiles in a spiral, starting from center of the map
        boolean painting = true;
        //int debugCount = 0;
        int x = 0;
        while (painting) 
        {
            painting = false;
            for (int i = 0; i < 4; i++) 
            { 
                //when i = 0, 2 bump up x
                if (i % 2 == 0) {  x++; }
                
                for (int j = 0; j < x; j++) 
                {
                    if (x_min <= posx && posx <= x_max && y_min <= posy && posy <= y_max) 
                    {
                        // tile is visible
                        Tile tile;
                        if (scrollWrapEnabled) { 
                            // in case tilex is out of bounds, grab the tile to use for wrapping
                            int tilexWrap = (((tilex % gridLength) + gridLength) % gridLength);
                            tile = tileController.getTile(tilexWrap, tiley, zoom);
                        } else {
                            tile = tileController.getTile(tilex, tiley, zoom);
                        }
                        if (tile != null) {
                            tile.paint(g, posx, posy);
                            if (tileGridVisible) {
                                g.drawRect(posx, posy, tilesize, tilesize);
                            }
                        }
                        painting = true;
                        //debugCount++;
                        //logger.info("Drawing tile (" + tilex + "," + tiley + ") at: " + posx + "," + posy);
                    }
                    else {
                        //logger.info("Not drawing tile (" + tilex + "," + tiley + ") at: " + posx + "," + posy);
                    }
                    
                    //Increment to the next tile
                    Point p = move[iMove];
                    posx += p.x * tilesize;
                    posy += p.y * tilesize;
                    tilex += p.x;
                    tiley += p.y;
                    
                }//end for j
                
                //change direction if necessary (spiral)
                iMove = (iMove + 1) % move.length;
            }//end for i
        }
        
        //logger.info("Drew: " + debugCount + " tiles\n");
        
        // outer border of the map
        int mapSize = tilesize << zoom;
        if (scrollWrapEnabled) {
            g.drawLine(0, h2 - center.y,(int) (pvd.getWidth() / scale), h2 - center.y);
            g.drawLine(0, h2 - center.y + mapSize, (int) (pvd.getWidth() / scale), h2 - center.y + mapSize);
        } else {
            g.drawRect(w2 - center.x, h2 - center.y, mapSize, mapSize);
        }

        // g.drawString("Tiles in cache: " + tileCache.getTileCount(), 50, 20);

        // keep x-coordinates from growing without bound if scroll-wrap is enabled
        if (scrollWrapEnabled) {
            center.x = center.x % mapSize;
        }
        
        //g.setTransform(current);
        g.dispose(); //TODO: In a finally block

        attribution.paintAttribution(g1, pvd.getWidth(), pvd.getHeight(), 
                getPosition(0, 0), getPosition(pvd.getWidth(), pvd.getHeight()), zoom, pvd);
    }
    
    /**
     * Calculates the latitude/longitude coordinate of the center of the
     * currently displayed map area.
     *
     * @return latitude / longitude
     */
    public Coordinate getPosition() 
    {
        if(tileSource == null) { return null; }
        
        Point center = getCenter();
        double lon = tileSource.XToLon(center.x, zoom);
        double lat = tileSource.YToLat(center.y, zoom);
        return new Coordinate(lat, lon);
    }
    
    /**
     * Converts the relative pixel coordinate (regarding the top left corner of
     * the displayed map) into a latitude / longitude coordinate
     *
     * @param mapPoint relative pixel coordinate regarding the top left corner of the displayed map
     * @return latitude / longitude
     */
    public Coordinate getPosition(Point mapPoint) 
    {
        return getPosition(mapPoint, zoom);
    }
    
    /**
     * Converts the relative pixel coordinate (regarding the top left corner of
     * the displayed map) into a latitude / longitude coordinate
     *
     * @param mapPoint relative pixel coordinate regarding the top left corner of the displayed map
     * @return latitude / longitude
     */
    public Coordinate getPosition(Point mapPoint, int zoom) 
    {
        return getPosition(mapPoint.x, mapPoint.y, zoom);
    }
    
    /**
     * Converts the relative pixel coordinate (regarding the top left corner of
     * the displayed map) into a latitude / longitude coordinate
     *
     * @param mapPointX
     * @param mapPointY
     * @return latitude / longitude
     */
    public Coordinate getPosition(int mapPointX, int mapPointY) 
    {
        return getPosition(mapPointX, mapPointY, zoom);
    }
    
    /**
     * Converts the relative pixel coordinate (regarding the top left corner of
     * the displayed map) into a latitude / longitude coordinate
     *
     * @param mapPointX
     * @param mapPointY
     * @return latitude / longitude
     */
    public Coordinate getPosition(int mapPointX, int mapPointY, int targetZoom) 
    {
        if(tileSource == null) { return null; }
        
        Point center = getCenter(targetZoom);
        int x = center.x + mapPointX - pvd.getWidth() / 2;
        int y = center.y + mapPointY - pvd.getHeight() / 2;
        double lon = tileSource.XToLon(x, targetZoom);
        double lat = tileSource.YToLat(y, targetZoom);
        return new Coordinate(lat, lon);
    }
    
    /**
     * Calculates the meters per pixel for the current source, zoom, and pvd.
     *
     * @return meters per pixel
     */
    public double getMetersPerPixel() 
    {
        return getMetersPerPixel(zoom);
    }
    
    /**
     * Calculates the meters per pixel for the current source and pvd, with the given zoom
     * 
     * @return meters per pixel
     */
    public double getMetersPerPixel(int zoomLevel)
    {
        if(tileSource == null) { return -1; }
        
        //Measure pixel distance from 5,5 to center
        Point origin = new Point(5,5);
        Point center = new Point(pvd.getWidth()/2, pvd.getHeight()/2);
        double pDistance = center.distance(origin);
        
        //Measure meters distance from 5,5 to center
        Coordinate originCoord = getPosition(origin, zoomLevel);
        Coordinate centerCoord = getPosition(center, zoomLevel);
        double mDistance = tileSource.getDistance(originCoord.getLat(), originCoord.getLon(),
                                                  centerCoord.getLat(), centerCoord.getLon());
        
        return mDistance/pDistance;
    }

    /**
     * Determines the center point of the screen in JOSM "pixelspace", pixel coordinates of the tile grid.
     * @return
     */
    public Point getCenter()
    {
        return getCenter(zoom);
    }
    
    /**
     * Determines the center point of the screen in JOSM "pixelspace", pixel coordinates of the tile grid.
     * @return
     */
    public Point getCenter(int zoomLevel)
    {
        if(tileSource == null) { return null; }
        
        //TODO: this is by no means efficient
        CoordinateTransformer transformer = pvd.getTransformer();
        Vector3 metersCenter = transformer.screenToMeters(pvd.getWidth()/2, pvd.getHeight()/2);
        Geodetic.Point latLonCenter = pvd.getTerrain().toGeodetic(metersCenter);
        Point center = new Point(tileSource.LonToX(Math.toDegrees(latLonCenter.longitude), zoomLevel), 
                                 tileSource.LatToY(Math.toDegrees(latLonCenter.latitude), zoomLevel));
        return center;
    }
    
    /**
     * Calculates the necessary scale factor to align tiles with the current PVD scale.
     * @return tileMpp / pvdMpp
     */
    public double getScaleFactor()
    {
        double simPpm = pvd.getTransformer().scalarToPixels(Scalar.createMeter(1));
        return getMetersPerPixel() * simPpm;
    }
}
