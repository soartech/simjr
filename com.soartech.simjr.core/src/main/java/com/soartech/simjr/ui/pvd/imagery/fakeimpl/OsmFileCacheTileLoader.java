package com.soartech.simjr.ui.pvd.imagery.fakeimpl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class OsmFileCacheTileLoader implements TileLoader
{
    
    public OsmFileCacheTileLoader(TileLoaderListener map, File cacheDir) throws IOException
    {
    }
    
    public abstract static class TileJob implements Runnable
    {
    }
    
    public static class FileLoadJob extends TileJob
    {
        public FileLoadJob(Tile tile)
        {
        }
        
        @Override
        public void run()
        {
        }

        protected boolean loadTileFromFile()
        {
            return false;
        }

        protected void loadTagsFromFile()
        {
        }

        protected File getTileFile()
        {
            return null;
        }

        protected File getTagsFile()
        {
            return null;
        }
        
        protected void saveTagsToFile()
        {
        }
    }
    
    protected String cacheDirBase;
    protected TileLoaderListener listener;
    protected Map<String, File> sourceCacheDirMap;
    
    public Tile getTile()
    {
        return null;
    }

    public TileJob createTileLoaderJob(Tile tile)
    {
        return null;
    }

    protected File getSourceCacheDir(TileSource source)
    {
        return null;
    }

    public void setTileCacheDir(String tileCacheDir)
    {
    }
}
