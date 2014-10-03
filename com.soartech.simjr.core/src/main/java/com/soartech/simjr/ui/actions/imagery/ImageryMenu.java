package com.soartech.simjr.ui.actions.imagery;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

//import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.pvd.PlanViewDisplay;
import com.soartech.simjr.ui.pvd.PlanViewDisplayProvider;
import com.soartech.simjr.ui.pvd.PvdView;
import com.soartech.simjr.ui.pvd.imagery.MapTileRenderer;
import com.soartech.simjr.ui.pvd.imagery.fakeimpl.TileSource;

/**
 * Menu group for configurig various options of the map tile imagery renderer.
 */
public class ImageryMenu extends JMenu
{
    private static final long serialVersionUID = 1L;
    
    //Necessary for checking the currently enabled source
    //TODO: Push this behavior into the SetMapImageProviderAction and have it be pushed rather than pulled
    Map<TileSource, JCheckBoxMenuItem> sourceMenuMap = new HashMap<TileSource, JCheckBoxMenuItem>();
    
    private final ActionManager am;

    public ImageryMenu(ActionManager actionManager) 
    {
        super("Imagery");
        
        this.am = actionManager;
        
        JMenu sourcesMenu = new JMenu("Source");
        for(SetMapImageryProviderAction action: SetMapImageryProviderAction.getAvailableProviderActions(am)) {
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(action); 
            sourceMenuMap.put(action.getSource(), menuItem);
            sourcesMenu.add(menuItem);
        }
        
        final JCheckBoxMenuItem tileGridMenuItem = new JCheckBoxMenuItem(new SetTileGridVisibilityAction(am));
        
        //JMenu offlineMenu = new JMenu("Offline");
        //offlineMenu.add(new JMenuItem(new SetOfflineCacheAction(am)));
        //offlineMenu.add(new JMenuItem(new SetOfflineCacheAction(am)));
        
        addMenuListener(new MenuListener() {
            public void menuCanceled(MenuEvent e) { }
            public void menuDeselected(MenuEvent e) { }
            public void menuSelected(MenuEvent e) {
                PvdView activePvd = getPvd();
                if(activePvd != null && activePvd.getMapTileRenderer() != null) {
                    MapTileRenderer tileRenderer = activePvd.getMapTileRenderer();
                    setSelectedSource(tileRenderer.getTileSource());
                    tileGridMenuItem.setSelected(tileRenderer.getTileGridVisible());
                }
            }
        });
        
        add(sourcesMenu);
        //add(offlineMenu);
        add(new JCheckBoxMenuItem(new ShowMapOpacityControllerAction(am)));
        add(tileGridMenuItem);
        add(new JMenuItem(new ShowMapDownloaderAction(am)));
        
        //TODO: Re-enable when reimplemented
        this.setEnabled(false);
    }
    
    private PvdView getPvd() 
    {
        final PlanViewDisplayProvider prov = am.getServices().findService(PlanViewDisplayProvider.class);
        if(prov != null) { 
            PlanViewDisplay pvd = prov.getActivePlanViewDisplay();
            return (pvd != null) ? pvd.getView() : null;
        }
        else {
            return null;
        }
    }

    private void setSelectedSource(TileSource selectedSource)
    {
        for(Map.Entry<TileSource, JCheckBoxMenuItem> entry: sourceMenuMap.entrySet()) {
            TileSource s = entry.getKey();
            boolean isSelected = (s == null && selectedSource == null) ||
                    (s != null & selectedSource != null && s.getName() == selectedSource.getName());
            entry.getValue().setSelected(isSelected);
        }
    }
}
