package com.soartech.simjr.ui.pvd.imagery.fakeimpl;

public interface TileLoaderListener
{

    public void tileLoadingFinished(Tile tile, boolean b);

    public TileCache getTileCache();
}
