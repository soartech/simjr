/**
 * 
 */
package com.soartech.simjr.ui.pvd;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.shapesystem.CoordinateTransformer;
import com.soartech.shapesystem.SimplePosition;
import com.soartech.simjr.sim.DetailedTerrain;

/**
 * @author aron
 *
 */
public class SlippyMap 
{
    private static final Logger logger = LoggerFactory.getLogger(SlippyMap.class);
    
    private static final ExecutorService threadpool = Executors.newFixedThreadPool(10);

    private String tileset = "openstreetmap";
//    private String tileset = "satellite";
    
    private String mapserverUrl = "http://tile.openstreetmap.org/";
    private String esriMapserverUrl = "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/";
    
    private int currentZoomLevel = 0;
    private Vector3 origin;
    private DetailedTerrain terrain;
    
    private Map<String, TileDownloadable> tilesToDownload = Collections.synchronizedMap(new HashMap<String, TileDownloadable>());
    
    private class SlippyMapTile
    {
        BufferedImage image = null;
        String url = null;
        float opacity = 1.0f;
        Vector3 centerMeters = Vector3.ZERO;
        Vector3 originMeters = Vector3.ZERO;
        
        String tileNumber = null;
        int x = -1;
        int y = -1;
        int zoom = -1;
    }

    private DefaultPvdView pvd = null;
    
    private Map<String, SlippyMapTile> maptileCache = Collections.synchronizedMap(new HashMap<String, SlippyMapTile>());

    /**
     * 
     * @param zoomLevel
     */
    public SlippyMap(Vector3 origin, int zoomLevel, String source, DetailedTerrain terrain, DefaultPvdView pvd)
    {
        this.origin = origin;
        this.currentZoomLevel = zoomLevel;
        this.tileset = source;
        this.terrain = terrain;
        this.pvd = pvd;
        
        //load the default tile into memory
        BufferedImage img = null;
        try {
            String defaultTileLoc = "/simjr/images/default-tile.png";
            URL defaultTileUrl = SlippyMap.class.getResource(defaultTileLoc);
            if(defaultTileUrl == null)
            {
                defaultTileUrl = (new File(defaultTileLoc)).toURI().toURL();
            }
            img = ImageIO.read(new File(defaultTileUrl.getPath()));
            
            SlippyMapTile newMaptile = new SlippyMapTile();
            newMaptile.image = img;

            //save in memory cache
            maptileCache.put("default-tile", newMaptile);
        } catch (IOException e1) {
            logger.error(e1.getMessage());
        }
    }
    
    /**
     * Draw the map onto the given graphics context
     *
     * @param g2dIn The graphics context
     * @param transformer The coordinate transformer
     */
    public void draw(Graphics2D g2dIn, CoordinateTransformer transformer)
    {
        final Graphics2D g2d = (Graphics2D) g2dIn.create();
        
        //get the images for a given zoom level
//        logger.info("ZOOM LEVEL: " + currentZoomLevel);

        //each maptile is 256x256
        double width = (double)pvd.getWidth();
        double height = (double)pvd.getHeight();
        
        //get the number of tiles in each direction
        int numMaptilesX = (int) Math.ceil(width / 256.0);
        int numMaptilesY = (int) Math.ceil(height / 256.0);

        //make the num maptiles odd
        if(numMaptilesX % 2 == 0) {
            numMaptilesX += 1;
        } else {
            numMaptilesX += 2;
        }
        
        if(numMaptilesY % 2 == 0)
        {
            numMaptilesY += 1;
        } else {
            numMaptilesY += 2;
        }
        
//        logger.info("numMaptilesX: " + numMaptilesX);
//        logger.info("numMaptilesY: " + numMaptilesY);
        
        double centerLat = pvd.transformToLat(pvd.getCenterInMeters());
        double centerLon = pvd.transformToLon(pvd.getCenterInMeters());
        
//        logger.info("CENTER LAT: " + centerLat);
//        logger.info("CENTER LON: " + centerLon);
        
        Set<String> drawnTilesSet = new HashSet<String>();
        
        //get the tile in the center
        SlippyMapTile centerMaptile = getOrCreateMaptile(centerLat, centerLon, getCurrentZoomLevel());
//        logger.info("CENTER MAPTILE URL: " + centerMaptile.url);
        drawMaptile(centerMaptile, g2d, transformer, null, null);
        drawnTilesSet.add(centerMaptile.tileNumber);
        
        //get tiles in a grid around the center one
        List<SlippyMapTile> orderedTiles = new ArrayList<SlippyMapTile>();
        int numMaptilesAtZoom = (int) Math.pow(2, currentZoomLevel);
        
        int xx = ((numMaptilesX - 1) / 2);
        int yy = ((numMaptilesY - 1) / 2);
        for(int y = 0 - yy; y <= yy; y++)
        {
            for(int x = 0 - xx; x <= xx; x++)
            {
                orderedTiles.add(getOrCreateMaptile((centerMaptile.x + x + numMaptilesAtZoom) % (numMaptilesAtZoom), 
                                                    (centerMaptile.y + y + numMaptilesAtZoom) % (numMaptilesAtZoom), 
                                                    centerMaptile.zoom));
            }
        }
        
        //draw the tiles
        //the origin is 256 * xx
        int tileSize = 256;
        final SimplePosition originPixels = transformer.metersToScreen(centerMaptile.originMeters.x, centerMaptile.originMeters.y);
        int startX = ((int) originPixels.x) - (256 * xx);
        int startY = ((int) originPixels.y) - (256 * yy);
        int x = startX;
        int y = startY;
        for(SlippyMapTile tile : orderedTiles)
        {
//                logger.info("Drawing tile: " + tile.tileNumber);
//            logger.info("Drawing tile at: (" + x + ", " + y + ")");
            drawMaptile(tile, g2d, transformer, x, y);
            x += tileSize;
            if((x - startX) >= tileSize * numMaptilesX)
            {
                x = ((int) originPixels.x) - (256 * xx);
                y += tile.image.getHeight();
            }
        }
    }
    
    private void saveMaptileToDisk(SlippyMapTile maptile)
    {
        File dir = new File("maptiles" + File.separator + tileset);
        dir.mkdirs();
        
        try {
            File outputfile = new File(dir, maptile.tileNumber + ".png");
            outputfile.mkdirs();
            outputfile.createNewFile();
            
            ImageIO.write(maptile.image, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }
    
    public int getCurrentZoomLevel() {
        return currentZoomLevel;
    }

    public void setCurrentZoomLevel(int currentZoomLevel) {
        this.currentZoomLevel = currentZoomLevel;
    }

    private void drawMaptile(SlippyMapTile maptile, Graphics2D g2d, CoordinateTransformer transformer, Integer x, Integer y)
    {
        if(maptile == null)
        {
            logger.info("ERROR: NULL MAPTILE");
            return;
        }
        
//        logger.info("drawMaptile(): " + maptile.tileNumber);
        
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, maptile.opacity);
        g2d.setComposite(ac);
        
        //the maptiles are 256x256
        double widthInPixels = 256;
        double heightInPixels = 256;
        
        //draw the tile and return if we know its potision
        if(x != null && y != null)
        {
            g2d.drawImage(maptile.image,
                    x,
                    y,
                    (int) widthInPixels, (int) heightInPixels,
                    null);
            
            return;
        } 
        
        final SimplePosition centerPixels = transformer.metersToScreen(maptile.centerMeters.x, maptile.centerMeters.y);
        final SimplePosition originPixels = transformer.metersToScreen(maptile.originMeters.x, maptile.originMeters.y);
        
        double metersPerPixel = getMetersPerPixel(transformer, maptile, pvd);
        terrain.setCoordinateFrame(pvd.getCenterInMeters(), metersPerPixel);
//        logger.info("METERS/PIXEL: " + metersPerPixel);
        
        //calculate the tile position since we don't know it
        if(x != null)
        {
            g2d.drawImage(maptile.image,
                    x,
                    (int) (centerPixels.y - heightInPixels / 2),
                    (int) widthInPixels, (int) heightInPixels,
                    null);
        }
        else if(y != null)
        {
            g2d.drawImage(maptile.image,
                    (int) (centerPixels.x - widthInPixels / 2),
                    y,
                    (int) widthInPixels, (int) heightInPixels,
                    null);
        }
        else
        {
            g2d.drawImage(maptile.image,
                    (int) (centerPixels.x - widthInPixels / 2),
                    (int) (centerPixels.y - heightInPixels / 2),
                    (int) widthInPixels, (int) heightInPixels,
                    null);
        }
        
    }
    
    public SlippyMapTile getOrCreateMaptile(final double lat, final double lon, final int zoom)
    {
        //get the x and y tile indicies from the lat/lon
        int xtile = (int)Math.floor( (lon + 180) / 360 * (1<<zoom) ) ;
        int ytile = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom) ) ;
        
        return getOrCreateMaptile(xtile, ytile, zoom);
    }
    
    public SlippyMapTile getOrCreateMaptile(int xtile, int ytile, final int zoom)
    {
        if (xtile < 0)
        {
            xtile=0;
        }
        
        if (xtile >= (1<<zoom))
        {
            xtile=((1<<zoom)-1);
        }
        
        if (ytile < 0)
        {
            ytile=0;
        }
        
        if (ytile >= (1<<zoom))
        {
            ytile=((1<<zoom)-1);
        }

        //figure out which tileset we're using
        String tileNumber = "";
        String url = "";
        if(tileset.equals("satellite"))
        {
            tileNumber = "" + zoom + "/" + ytile + "/" + xtile;
            url = esriMapserverUrl + tileNumber + ".png";
        }
        else //use openstreetmap by default
        {
            tileset = "openstreetmap";
            tileNumber = "" + zoom + "/" + xtile + "/" + ytile; 
            url = mapserverUrl + tileNumber + ".png";
        }
        
        //load from cache if we already know about this maptile
        if(maptileCache.containsKey(url))
        {
            //load from cache
            return maptileCache.get(url);
        }
        else
        {
            //try to load from disk
            BufferedImage img = null;
            try {
                //try to load from disk before web
                File dir = new File("maptiles" + File.separator + tileset);
                img = ImageIO.read(new File(dir, tileNumber + ".png"));
                
            } catch (IOException e1) {
//                logger.info("Maptile: " + tileNumber + " not on disk, loading from url");
//                logger.error(e1.getMessage());
            }
            
            if(img == null)
            {
                TileDownloadable td = new TileDownloadable(xtile, ytile, zoom, tileNumber, url); 
                
                if(!tilesToDownload.containsKey(td.url))
                {
                    logger.info("Maptile: " + tileNumber + " not on disk, loading from url");
                    tilesToDownload.put(td.url, td);
                    return downloadTileAsync(td);
                }
                else
                {
                    return maptileCache.get("default-tile");  
                }
            }
            else
            {
                SlippyMapTile newMaptile = new SlippyMapTile();
                newMaptile.image = img;
                newMaptile.url = url;
                newMaptile.tileNumber = tileNumber; 
                newMaptile.x = xtile;
                newMaptile.y = ytile;
                newMaptile.zoom = zoom;
                
                //set origin (upper left)
                Geodetic.Point originDegrees = new Geodetic.Point(Math.toRadians(tile2lat(ytile, currentZoomLevel)), Math.toRadians(tile2lon(xtile, currentZoomLevel)), 0);
                Vector3 originMeters = pvd.getTerrain().fromGeodetic(originDegrees);
//                Vector3 centerMeters = new Vector3(tile2lon(xtile, pvd.getCurrentZoomLevel()), tile2lat(ytile, pvd.getCurrentZoomLevel()), 0);
                newMaptile.originMeters = originMeters;
                
                //set center
                BoundingBox bb = tile2boundingBox(xtile, ytile, zoom);
                double centerLat = (bb.north + bb.south) / 2;
                double centerLon = (bb.east + bb.west) / 2;
                Geodetic.Point centerDegrees = new Geodetic.Point(Math.toRadians(centerLat), Math.toRadians(centerLon), 0);
                Vector3 centerMeters = pvd.getTerrain().fromGeodetic(centerDegrees);
                newMaptile.centerMeters = centerMeters;
                
                //save in memory cache
                maptileCache.put(newMaptile.url, newMaptile);
                
                return newMaptile;
            }
        }
    }
    
    class TileDownloadable
    {
        String tileNumber;
        String url;
        int xtile;
        int ytile;
        int zoom;
        
        public TileDownloadable(int xtile, int ytile, final int zoom, String tileNumber, String url)
        {
            this.tileNumber = tileNumber;
            this.xtile = xtile;
            this.ytile = ytile;
            this.zoom = zoom;
            this.url = url;
        }
    }
    
    class TileDownloader implements Callable<SlippyMapTile>
    {
        private TileDownloadable td;
        
        public TileDownloader(TileDownloadable td) 
        {
            this.td = td;
        }
        
        @Override
        public SlippyMapTile call() throws Exception {
//            logger.info("*** TileDownloader call(): " + td.tileNumber);
            return downloadTile(td);
        }
        
    }
    
    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
    
    private SlippyMapTile downloadTileAsync(TileDownloadable td)
    {
        TileDownloader task = new TileDownloader(td);
        Future<SlippyMapTile> future = threadpool.submit(task);
        return maptileCache.get("default-tile"); 
    }
    
    private SlippyMapTile downloadTile(TileDownloadable td)
    {
        BufferedImage img = null;
      
        //load from web and save in cache
        try {
            if(img == null) {
                img = ImageIO.read(new URL(td.url));
                logger.info("... Downloading Maptile: " + td.tileNumber + " ...");
            } 
              
            SlippyMapTile newMaptile = new SlippyMapTile();
            newMaptile.image = img;
            newMaptile.url = td.url;
            newMaptile.tileNumber = td.tileNumber; 
            newMaptile.x = td.xtile;
            newMaptile.y = td.ytile;
            newMaptile.zoom = td.zoom;
              
            //set origin (upper left)
            Geodetic.Point originDegrees = new Geodetic.Point(Math.toRadians(tile2lat(td.ytile, currentZoomLevel)), Math.toRadians(tile2lon(td.xtile, currentZoomLevel)), 0);
            Vector3 originMeters = pvd.getTerrain().fromGeodetic(originDegrees);
    //        Vector3 centerMeters = new Vector3(tile2lon(xtile, pvd.getCurrentZoomLevel()), tile2lat(ytile, pvd.getCurrentZoomLevel()), 0);
            newMaptile.originMeters = originMeters;
              
            //set center
            BoundingBox bb = tile2boundingBox(td.xtile, td.ytile, td.zoom);
            double centerLat = (bb.north + bb.south) / 2;
            double centerLon = (bb.east + bb.west) / 2;
            Geodetic.Point centerDegrees = new Geodetic.Point(Math.toRadians(centerLat), Math.toRadians(centerLon), 0);
            Vector3 centerMeters = pvd.getTerrain().fromGeodetic(centerDegrees);
            newMaptile.centerMeters = centerMeters;
            
            //save in memory cache
            maptileCache.put(newMaptile.url, newMaptile);
            
            //save to disk cache
            saveMaptileToDisk(newMaptile);
            
            logger.info("Download Complete for Maptile: " + td.tileNumber);
            
            //remove from tilesToDownload if it exists. this
            tilesToDownload.remove(td.url);
            
            return newMaptile;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
      
        return maptileCache.get("default-tile");
    }
    
    public double getMetersPerPixel(CoordinateTransformer transformer, SlippyMapTile maptile, DefaultPvdView pvd)
    {
//      The distance represented by one pixel (S) is given by:
//      S=C*cos(y)/2^(z+8)
//      where...
//      C is the (equatorial) circumference of the Earth = 40,075 km
//      z is the zoom level
//      y is the latitude of where you're interested in the scale
      
//      Point pt = new Point(pvd.getWidth() / 2, pvd.getHeight() / 2);
//      final Vector3 fixedPoint = transformer.screenToMeters(pt.getX(), pt.getY());
        
//      logger.info("MAPTILE: " + maptile.tileNumber);
//      logger.info("CENTER: " + maptile.centerMeters.x + ", " + maptile.centerMeters.y);
        
      double c = 40075000.0; //in meters
      double y = pvd.transformToLat(maptile.centerMeters);
      double scale = (c * Math.cos(Math.toRadians(y))) / (Math.pow(2, (double)(currentZoomLevel + 8)));
      
      return scale;
    }
    
    class BoundingBoxPixels 
    {
        int north;
        int south;
        int east;
        int west;   
    }
    
    class BoundingBox 
    {
        double north;
        double south;
        double east;
        double west;   
    }
    
    BoundingBox tile2boundingBox(final int x, final int y, final int zoom) 
    {
        BoundingBox bb = new BoundingBox();
        bb.north = tile2lat(y, zoom);
        bb.south = tile2lat(y + 1, zoom);
        bb.west = tile2lon(x, zoom);
        bb.east = tile2lon(x + 1, zoom);
        return bb;
    }

    double tile2lon(int x, int z) 
    {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    double tile2lat(int y, int z) 
    {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }
}
