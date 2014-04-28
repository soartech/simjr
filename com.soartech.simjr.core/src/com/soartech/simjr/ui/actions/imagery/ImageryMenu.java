package com.soartech.simjr.ui.actions.imagery;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOpenAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.pvd.PlanViewDisplay;
import com.soartech.simjr.ui.pvd.PlanViewDisplayProvider;
import com.soartech.simjr.ui.pvd.imagery.MapTileRenderer;

/**
 * Menu group for configurig various options of the map tile imagery renderer.
 */
public class ImageryMenu extends JMenu
{
    private static final long serialVersionUID = 1L;
    
    TileSource[] tileSources = new TileSource[] { 
            null,
            new OsmTileSource.Mapnik(),
            new OsmTileSource.CycleMap(),
            new BingAerialTileSource(),
            new MapQuestOsmTileSource(),
            new MapQuestOpenAerialTileSource()
    };
    Map<TileSource, JCheckBoxMenuItem> sourceMenuMap = new HashMap<TileSource, JCheckBoxMenuItem>();
    
    private final ActionManager am;

    public ImageryMenu(ActionManager actionManager) 
    {
        super("Imagery");
        
        this.am = actionManager;
        
        JMenu sourcesMenu = new JMenu("Source");
        for(TileSource s: tileSources) {
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(new SetMapImageryProviderAction(am, s)); 
            sourceMenuMap.put(s, menuItem);
            sourcesMenu.add(menuItem);
        }
        
        final JCheckBoxMenuItem tileGridMenuItem = new JCheckBoxMenuItem(new SetTileGridVisibilityAction(am));
        
        addMenuListener(new MenuListener() {
            public void menuCanceled(MenuEvent e) { }
            public void menuDeselected(MenuEvent e) { }
            public void menuSelected(MenuEvent e) {
                PlanViewDisplay activePvd = getPvd();
                if(activePvd != null && activePvd.getMapTileRenderer() != null) {
                    MapTileRenderer tileRenderer = activePvd.getMapTileRenderer();
                    setSelectedSource(activePvd.getMapTileRenderer().getTileSource());
                    tileGridMenuItem.setSelected(tileRenderer.getTileGridVisible());
                }
            }
        });
        
        add(sourcesMenu);
        add(new JCheckBoxMenuItem(new ShowMapOpacityControllerAction(am)));
        add(tileGridMenuItem);
        add(new JMenuItem(new ShowMapDownloaderAction(am)));
    }
    
    private PlanViewDisplay getPvd() 
    {
        final PlanViewDisplayProvider prov = am.getServices().findService(PlanViewDisplayProvider.class);
        if(prov != null) { 
            return prov.getActivePlanViewDisplay();
        }
        else {
            return null;
        }
    }

    public void setSelectedSource(TileSource selectedSource)
    {
        for(Map.Entry<TileSource, JCheckBoxMenuItem> entry: sourceMenuMap.entrySet()) {
            TileSource s = entry.getKey();
            boolean isSelected = (s == null && selectedSource == null) ||
                    (s != null & selectedSource != null && s.getName() == selectedSource.getName());
            entry.getValue().setSelected(isSelected);
        }
    }
}
