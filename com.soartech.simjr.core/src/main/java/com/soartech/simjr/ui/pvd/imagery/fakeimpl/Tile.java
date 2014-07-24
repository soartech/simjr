package com.soartech.simjr.ui.pvd.imagery.fakeimpl;

import java.awt.Graphics2D;
import java.io.FileInputStream;

public class Tile
{
    public Tile(TileSource tileSource, int x, int y, int zoom)
    {
    }

    public TileSource getSource() 
    {
        return null;
    }
    
    public int getXtile()
    {
        return 0;
    }
    
    public int getYtile()
    {
        return 0;
    }
    
    public int getZoom()
    {
        return 0;
    }
    
    public boolean hasError()
    {
        return true;
    }
    
    public boolean isLoaded()
    {
        return false;
    }
    
    public boolean isLoading()
    {
        return false;
    }
    
    public void initLoading()
    {
        
    }
    
    public String getValue(String value)
    {
        return null;
    }
    
    public void loadImage(FileInputStream fis)
    {
        
    }
    
    public void putValue(String key, String value)
    {
        
    }
    
    public void setError(String error)
    {
        
    }
    
    public void setLoaded(boolean loaded)
    {
        
    }

    public void paint(Graphics2D gScaled, int posx, int posy)
    {
    }
}
