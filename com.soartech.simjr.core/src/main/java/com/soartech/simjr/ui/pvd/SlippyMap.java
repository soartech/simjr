/**
 * 
 */
package com.soartech.simjr.ui.pvd;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private static final Logger logger = LoggerFactory.getLogger(MapImage.class);
    
    private int currentZoomLevel = 0;
    private Vector3 origin;
    private DetailedTerrain terrain;
    
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
    
    private Map<String, SlippyMapTile> maptileCache = new HashMap<String, SlippyMapTile>();

    /**
     * 
     * @param zoomLevel
     */
    public SlippyMap(Vector3 origin, int zoomLevel, DetailedTerrain terrain, DefaultPvdView pvd)
    {
        this.origin = origin;
        this.currentZoomLevel = zoomLevel;
        this.terrain = terrain;
        this.pvd = pvd;
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
        int xx = ((numMaptilesX - 1) / 2);
        int yy = ((numMaptilesY - 1) / 2);
        for(int y = 0 - yy; y <= yy; y++)
        {
            for(int x = 0 - xx; x <= xx; x++)
            {
                orderedTiles.add(getOrCreateMaptile(centerMaptile.x + x, centerMaptile.y + y, centerMaptile.zoom));
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
        File dir = new File("maptiles" + File.separator + "cache");
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
        
        final SimplePosition centerPixels = transformer.metersToScreen(maptile.centerMeters.x, maptile.centerMeters.y);
        final SimplePosition originPixels = transformer.metersToScreen(maptile.originMeters.x, maptile.originMeters.y);
        
        double metersPerPixel = getMetersPerPixel(transformer, maptile, pvd);
        terrain.setCoordinateFrame(pvd.getCenterInMeters(), metersPerPixel);
//        logger.info("METERS/PIXEL: " + metersPerPixel);
        
        //the maptiles are 256x256
        double widthInPixels = 256;
        double heightInPixels = 256;
        
        if(x != null && y != null)
        {
            g2d.drawImage(maptile.image,
                    x,
                    y,
                    (int) widthInPixels, (int) heightInPixels,
                    null);
        } 
        else if(x != null)
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
    
    public SlippyMapTile getOrCreateMaptile(final double lat, final double lon, final int zoom)
    {
        //get the x and y tile indicies from the lat/lon
        int xtile = (int)Math.floor( (lon + 180) / 360 * (1<<zoom) ) ;
        int ytile = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom) ) ;
        
        return getOrCreateMaptile(xtile, ytile, zoom);
    }
    
    public SlippyMapTile getOrCreateMaptile(int xtile, int ytile, final int zoom)
    {
        //get the x and y tile indicies from the lat/lon
//        int xtile = (int)Math.floor( (lon + 180) / 360 * (1<<zoom) ) ;
//        int ytile = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom) ) ;
        
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
        
        String tileNumber = "" + zoom + "/" + xtile + "/" + ytile; 
        String url = "http://tile.openstreetmap.org/" + tileNumber + ".png";
        
        //load from cache if we already know about this maptile
        if(maptileCache.containsKey(url))
        {
            //load from cache
            return maptileCache.get(url);
        }
        else
        {
            
            BufferedImage img = null;
            try {
                //try to load from disk before web
                File dir = new File("maptiles" + File.separator + "cache");
                img = ImageIO.read(new File(dir, tileNumber + ".png"));
                
            } catch (IOException e1) {
//                e1.printStackTrace();
//                logger.info("Maptile: " + tileNumber + " not on disk, loading from url");
//                logger.error(e1.getMessage());
            }
            
            //load from web and save in cache
            try {
                if(img == null) {
                    img = ImageIO.read(new URL(url));
                    logger.info("Maptile: " + tileNumber + " not on disk, loading from url");
                } else {
                    logger.info("Using Maptile: " + tileNumber + " from disk");
                }
                
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
                
                //save to disk cache
                saveMaptileToDisk(newMaptile);
                
                return newMaptile;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                logger.error(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e.getMessage());
            }
        }
        
        return null;
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
