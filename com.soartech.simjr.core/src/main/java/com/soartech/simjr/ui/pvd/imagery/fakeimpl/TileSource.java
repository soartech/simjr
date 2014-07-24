package com.soartech.simjr.ui.pvd.imagery.fakeimpl;


public abstract class TileSource
{
    public static class BingAerialTileSource extends TileSource {
        
    };
    
    public static class MapQuestOsmTileSource extends TileSource {
        
    };
    
    public static class MapQuestOpenAerialTileSource extends TileSource {
        
    };
    
    public String getName()
    {
        return null;
    }
    
    public String getTileType()
    {
        return null;
    }

    public int getMinZoom()
    {
        return 0;
    }

    public int getMaxZoom()
    {
        return 0;
    }

    public double getDistance(double lat, double lon, double lat2, double lon2)
    {
        return 0;
    }

    public double XToLon(int x, int zoom)
    {
        return 0;
    }

    public double YToLat(int y, int zoom)
    {
        return 0;
    }

    public int getTileSize()
    {
        return 0;
    }

    public int LonToX(double degrees, int zoomLevel)
    {
        return 0;
    }

    public int LatToY(double degrees, int zoomLevel)
    {
        return 0;
    }
}
