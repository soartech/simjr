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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.shapesystem.CoordinateTransformer;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.swing.SwingCoordinateTransformer;
import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.ui.pvd.PlanViewDisplay;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.AttributionSupport;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.Coordinate;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.JobDispatcher;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.MemoryTileCache;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.OsmTileSource;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.Tile;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.TileCache;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.TileController;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.TileLoader;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.TileLoaderListener;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.TileSource;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.TileSource.BingAerialTileSource;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.TileSource.MapQuestOpenAerialTileSource;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.TileSource.MapQuestOsmTileSource;

public class MapTileRenderer implements TileLoaderListener
{
    private static final Logger logger = LoggerFactory.getLogger(MapTileRenderer.class);
    
    //Vectors for clock-wise spiral tile painting
    protected static final Point[] move = { new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1) };
    
    public static final int MAX_ZOOM = 22;
    public static final int MIN_ZOOM = 0;
    private static final int DOWNLOAD_THREAD_COUNT = 8;
    
    private boolean tileGridVisible = SimJrProps.get("simjr.map.imagery.grid", false); 
    private final boolean scrollWrapEnabled = false; //TODO: Determine if this should be supported in SimJr
    
    private TileController tileController;
    private TileSource tileSource;
    
    private int zoom = 10; 
    private float opacity = (float) SimJrProps.get("simjr.map.imagery.opacity", 0.75);
    
    //Responsible for displaying correct map imagery attribution (copyright notice, etc)
    private AttributionSupport attribution = new AttributionSupport();
    
    private final PlanViewDisplay pvd;
    
    //Notify listeners when the tile source changes
    public interface TileSourceListener { public void onTileSourceChanged(TileSource ts); }
    private final List<TileSourceListener> tileSourceListeners = new ArrayList<TileSourceListener>();
    public boolean addTileSourceListener(TileSourceListener l)    { return tileSourceListeners.add(l); }
    public boolean removeTileSourceListener(TileSourceListener l) { return tileSourceListeners.remove(l); }
    private void notifyTileSourceListeners(TileSource s) {
        for(TileSourceListener l: tileSourceListeners) {
            l.onTileSourceChanged(s);
        }
    }
    
    //Notify listeners when the tile zoom level changes
    public interface TileZoomListener { public void onTileZoomChanged(int zoom); }
    private final List<TileZoomListener> tileZoomListeners = new ArrayList<TileZoomListener>();
    public boolean addTileZoomListener(TileZoomListener l)    { return tileZoomListeners.add(l); }
    public boolean removeTileZoomListener(TileZoomListener l) { return tileZoomListeners.remove(l); }
    private void notifyTileZoomListeners(int zoom) {
        for(TileZoomListener l: tileZoomListeners) {
            l.onTileZoomChanged(zoom);
        }
    }    
    
    /**
     * Creates a MapTileRenderer that renders tiles on the given PVD.
     * @param renderTarget
     */
    public MapTileRenderer(PlanViewDisplay renderTarget)
    {
        this.pvd = renderTarget;
        
        JobDispatcher.setMaxWorkers(DOWNLOAD_THREAD_COUNT);
        tileSource = getTileSource(SimJrProps.get("simjr.map.imagery.source", "mapnik")); //TODO: Pull this value from properties
        tileController = new TileController(tileSource, new MemoryTileCache(), this);
        //TODO: Seems to be a bug where the source param is ignored in tilecontroller constructor, reset it here
        setTileSource(tileSource); //initializes attribution
        
        String offlineDir = SimJrProps.get("simjr.map.imagery.offline");
        if(offlineDir != null) {
            logger.info("Using offline tile loader from directory: " + offlineDir);
            File dir = new File(offlineDir);
            if(dir.exists() && dir.isDirectory() && dir.canRead()) {
                try {
                    TileLoader loader = new OfflineTileLoader(this, dir);
                    setTileLoader(loader);
                }
                catch(IOException e) {
                    logger.error("Unable to create tile loader from directory: " + offlineDir, e);
                }
            }
            else {
                logger.error("Unable to read from directory: " + offlineDir);
            }
        }
    }
    
    public int getZoom() { return this.zoom; }
    public void setZoom(int zoom)  {
        this.zoom = zoom;
        notifyTileZoomListeners(zoom);
        
    }
    
    public float getOpacity() { return opacity; }
    public void setOpacity(float opacity) { this.opacity = opacity; }
    
    public boolean getTileGridVisible() { return tileGridVisible; }
    public void setTileGridVisible(boolean show) { this.tileGridVisible = show; }
    
    private static TileSource getTileSource(String sourceName)
    {
        //#Mapnik, Cyclemap, Bing Aerial Maps, MapQuest-OSM, MapQuest Open Aerial
        final Map<String, Class<? extends TileSource>> sourceMap = ImmutableMap.<String, Class<? extends TileSource>>builder()
                .put("mapnik", OsmTileSource.Mapnik.class)
                .put("cyclemap", OsmTileSource.CycleMap.class)
                .put("bing aerial maps", BingAerialTileSource.class)
                .put("mapquest-osm", MapQuestOsmTileSource.class)
                .put("mapquest open aerial", MapQuestOpenAerialTileSource.class)
                .build();
        TileSource source = null;
        Class<? extends TileSource> sourceClass = sourceMap.get(sourceName.toLowerCase()); 
        if(sourceClass != null) {
            try {
                source = sourceClass.newInstance();
            }
            catch (InstantiationException e) {
                logger.error("Unable to create tile source from: " + sourceName, e);
            }
            catch (IllegalAccessException e) {
                logger.error("Unable to create tile source from: " + sourceName, e);
            }
        }
        return source;
    }
    
    /**
     * Sets the map zoom level to the closest match to the given mpp.
     * 
     * TODO: Could improve this algorithm by taking the closest scale, rather than 
     * always one lower
     * TODO: Do we ever want to approximate a scale other than the PVD's current mpp?
     * 
     * @param targetMpp meters per pixel to approximate
     */
    public void approximateScale(double targetMpp) 
    {
        //logger.info("Approximating scale: " + targetMpp + " meters per pixels.");
        
        if(tileSource == null) { return; }
        
        int targetZoom = tileSource.getMinZoom();
        double currentTileMpp = getMetersPerPixel(targetZoom);
        
        while (currentTileMpp > targetMpp && targetZoom < tileSource.getMaxZoom()) 
        {
            //logger.info("Zoom " + targetZoom + " is too high: " + currentTileMpp + " < " + targetMpp);
            targetZoom++; //zoom in
            currentTileMpp = getMetersPerPixel(targetZoom);
        }
        
        //logger.info("Zoom: " + targetZoom + " at " + currentTileMpp + " is closest to: " + targetMpp);
        setZoom(targetZoom);
    }
    
    /**
     * Approximates the scale to the current pvd scale. 
     */
    public void approximateScale()
    {
        //Don't like the cast here. 
        approximateScale(((SwingCoordinateTransformer)pvd.getTransformer()).screenToMeters(1));
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
        
        notifyTileSourceListeners(tileSource);
    }
    
    //TODO: Unused, necessary?
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
    
    public void paint(Graphics2D g)
    {
        if(tileSource == null) { return; }
        
        Graphics2D gScaled = (Graphics2D) g.create();
        double scale = getScaleFactor();
        gScaled.transform(AffineTransform.getScaleInstance(scale, scale));
        
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
        gScaled.setComposite(ac);
        
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
                            tile.paint(gScaled, posx, posy);
                            if (tileGridVisible) {
                                gScaled.drawRect(posx, posy, tilesize, tilesize);
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
            gScaled.drawLine(0, h2 - center.y,(int) (pvd.getWidth() / scale), h2 - center.y);
            gScaled.drawLine(0, h2 - center.y + mapSize, (int) (pvd.getWidth() / scale), h2 - center.y + mapSize);
        } else {
            gScaled.drawRect(w2 - center.x, h2 - center.y, mapSize, mapSize);
        }

        // g.drawString("Tiles in cache: " + tileCache.getTileCount(), 50, 20);

        // keep x-coordinates from growing without bound if scroll-wrap is enabled
        if (scrollWrapEnabled) {
            center.x = center.x % mapSize;
        }
        
        gScaled.dispose(); //TODO: In a finally block

        attribution.paintAttribution(g, pvd.getWidth(), pvd.getHeight(), 
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
        
        //Get meters coordinate of center
        Vector3 metersCenter = transformer.screenToMeters(pvd.getWidth()/2, pvd.getHeight()/2);
        
        //Convert to lat/lon TODO: This seems like it might have issues far from origin
        Geodetic.Point latLonCenter = pvd.getTerrain().toGeodetic(metersCenter);
        
        //Translate that lat/lon into tile coords
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
        return getScaleFactor(zoom);
    }
    
    public double getScaleFactor(int zoomLevel)
    {
        double simPpm = pvd.getTransformer().scalarToPixels(Scalar.createMeter(1));
        return getMetersPerPixel(zoomLevel) * simPpm;
    }
    
    public Point getTileCoordinates(Point screenCoordsPx, int zoomLevel)
    {
        if(tileSource == null) { return null; }
        
        Point osmCenterPx = getCenter(zoomLevel);
        double scale = getScaleFactor(zoomLevel);
        Point.Double osmUpperLeftPx = new Point.Double(osmCenterPx.x - (pvd.getWidth()/2 / scale), osmCenterPx.y - (pvd.getHeight()/2) / scale);
        Point.Double osmMousePx =   new Point.Double(osmUpperLeftPx.x + screenCoordsPx.x / scale, osmUpperLeftPx.y + screenCoordsPx.y / scale);
        double tileSize = getTileSize();
        return new Point((int) (osmMousePx.x / tileSize), (int) (osmMousePx.y / tileSize));
    }
}
