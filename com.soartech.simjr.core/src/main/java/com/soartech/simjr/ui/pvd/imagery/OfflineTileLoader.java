// License: GPL. For details, see Readme.txt file.
package com.soartech.simjr.ui.pvd.imagery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.simjr.ui.pvd.imagery.fakeimpl.OsmFileCacheTileLoader;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.Tile;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.TileLoaderListener;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.TileSource;

/**
 * TileLoader that extends the file cache to ignore age, and treats missing tile errors differently.
 */
public class OfflineTileLoader extends OsmFileCacheTileLoader 
{
    private static final Logger logger = LoggerFactory.getLogger(OfflineTileLoader.class);
    private static final Charset TAGS_CHARSET = Charset.forName("UTF-8");

    /**
     * Create a OSMFileCacheTileLoader with given cache directory.
     * If cacheDir is not set or invalid, IOException will be thrown.
     * @param map the listener checking for tile load events (usually the map for display)
     * @param cacheDir directory to store cached tiles
     */
    public OfflineTileLoader(TileLoaderListener map, File cacheDir) throws IOException {
        super(map, cacheDir);
    }
    
    protected class OfflineFileLoadJob extends FileLoadJob 
    {
        private static final String ETAG_FILE_EXT = ".etag";
        private static final String TAGS_FILE_EXT = ".tags";
        
        File tileCacheDir;
        
        public OfflineFileLoadJob(Tile tile) 
        {
            super(tile);
        }

        /**
         * Modified this to skip any attempt at reloading tiles not found in cache
         */
        @Override
        public void run() 
        {
            //Get the parent class tile obj, it's not visible to subclasses
            Tile tile = getTile();
            
            //Skip this tile if it's already successfully loaded or is currently loading
            synchronized (tile) {
                if ( tile.isLoaded() || tile.hasError() || tile.isLoading()) {
                    return;
                }
                //Otherwise set it as loading
                tile.initLoading();
            }
            
            //Attempt to load it from file, and it if succeeded, we're done
            tileCacheDir = getSourceCacheDir(tile.getSource());
            
            boolean loaded = loadTileFromFile();
            if (loaded) {
                tile.setLoaded(true);
                listener.tileLoadingFinished(tile, true);
            }
            else {
                tile.setLoaded(false);
                tile.setError("Tile not found in offline cache!");
                //TODO: Also set tile's error image to something unique?
            }
        }

        /**
         * Simplified the logic here a bit to ignore cache age and certain errors.
         */
        @Override
        protected boolean loadTileFromFile() 
        {
            //Get the parent class tile obj, it's not visible
            Tile tile = getTile();
            
            File tileFile = getTileFile();
            if (!tileFile.exists() || !tileFile.canRead()) {
                return false;
            }
            
            FileInputStream fin = null;
            try 
            {
                //Populate the metadata of the tile
                loadTagsFromFile();
                
                //If there's "no-tile", set tile error appropriately.
                if ("no-tile".equals(tile.getValue("tile-info")))
                {
                    tile.setError("No tile at this zoom level");
                    if (tileFile.exists()) {
                        tileFile.delete();
                    }
                    tileFile = getTagsFile();
                } 
                //Otherwise, read tile image
                else 
                {
                    fin = new FileInputStream(tileFile);
                    try {
                        if (fin.available() == 0) {
                            throw new IOException("File empty");
                        }
                        tile.loadImage(fin);
                    } 
                    finally {
                        fin.close();
                    }
                }
                
                return true;
            } 
            catch (Exception e) 
            {
                logger.error("Exception loading tile: " + tile, e);
                tileFile = null;
            }
            
            return false;
        }
        
        /**
         * Straight copy from OsmFileCacheTileLoader.FileLoadJob
         * Must override this to call our loadOldETagFromFile
         */
        @Override
        protected void loadTagsFromFile() 
        {
            loadOldETagfromFile();
            File tagsFile = getTagsFile();
            try {
                final BufferedReader f = new BufferedReader(new InputStreamReader(new FileInputStream(tagsFile), TAGS_CHARSET));
                Tile tile = getTile();
                for (String line = f.readLine(); line != null; line = f.readLine()) {
                    final int i = line.indexOf('=');
                    if (i == -1 || i == 0) {
                        System.err.println("Malformed tile tag in file '" + tagsFile.getName() + "':" + line);
                        continue;
                    }
                    tile.putValue(line.substring(0,i),line.substring(i+1));
                }
                f.close();
            } catch (FileNotFoundException e) {
            } catch (Exception e) {
                System.err.println("Failed to load tile tags: " + e.getLocalizedMessage());
            }
        }
        
        /**
         * Straight copy from OsmFileCacheTileLoader.FileLoadJob
         * Must override this to access tileCacheDir
         */
        @Override
        protected File getTileFile() 
        {
            Tile tile = getTile();
            return new File(tileCacheDir + "/" + tile.getZoom() + "_" + tile.getXtile() + "_" + tile.getYtile() + "." + tile.getSource().getTileType());
        }

        /**
         * Straight copy from OsmFileCacheTileLoader.FileLoadJob
         * Must override this to access tileCacheDir
         */
        @Override
        protected File getTagsFile() 
        {
            Tile tile = getTile();
            return new File(tileCacheDir + "/" + tile.getZoom() + "_" + tile.getXtile() + "_" + tile.getYtile() + TAGS_FILE_EXT);
        }
        
        /**
         *  Load backward-compatiblity .etag file and if it exists move it to new .tags file
         * 
         * Straight copy from OsmFileCacheTileLoader.FileLoadJob
         * Must override this to access tileCacheDir
         */
        private void loadOldETagfromFile() 
        {
            Tile tile = getTile();
            
            File etagFile = new File(tileCacheDir, tile.getZoom() + "_" + tile.getXtile() + "_" + tile.getYtile() + ETAG_FILE_EXT);
            if (!etagFile.exists()) return;
            try {
                FileInputStream f = new FileInputStream(etagFile);
                byte[] buf = new byte[f.available()];
                f.read(buf);
                f.close();
                String etag = new String(buf, TAGS_CHARSET.name());
                tile.putValue("etag", etag);
                if (etagFile.delete()) {
                    saveTagsToFile();
                }
            } 
            catch (IOException e) {
                System.err.println("Failed to load compatiblity etag: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Override this to use OfflineFileLoadJob
     */
    @Override
    public TileJob createTileLoaderJob(final Tile tile) {
        return new OfflineFileLoadJob(tile);
    }
    
    /**
     * Override this to avoid creating new dir 
     */
    @Override
    protected File getSourceCacheDir(TileSource source) {
        File dir = sourceCacheDirMap.get(source); //TODO: Maybe bug in parent class, this map is never written to?
        if (dir == null) {
            dir = new File(cacheDirBase, source.getName().replaceAll("[\\\\/:*?\"<>|]", "_"));
            //if (!dir.exists()) {
            //    dir.mkdirs();
            //}
        }
        return dir;
    }

    /**
     * Override this to avoid creating new dir
     */
    @Override
    public void setTileCacheDir(String tileCacheDir) {
        File dir = new File(tileCacheDir);
        //dir.mkdirs();
        this.cacheDirBase = dir.getAbsolutePath();
    }
}
