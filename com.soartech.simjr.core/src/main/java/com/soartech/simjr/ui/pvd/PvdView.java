package com.soartech.simjr.ui.pvd;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

import com.soartech.math.Vector3;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.swing.SwingCoordinateTransformer;
import com.soartech.simjr.sim.DetailedTerrain;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.Terrain;
import com.soartech.simjr.ui.shapes.EntityShapeManager;

/**
 * @author mjquist
 *
 */
public interface PvdView
{
    JComponent getComponent();
    
    void dispose();

    void setIsDraggingEntity(boolean b);

    boolean isDraggingEntity();

    /**
     * Get the entity for the PVD is locked onto (the area of the PVD moves so that this
     * entity is always in the center).
     * 
     * @return the lockEntity
     */
    Entity getLockEntity();

    /**
     * Set the entity for the PVD to lock onto (the area of the PVD will move so that this
     * entity is always in the center).
     * 
     * @param lockEntity the lockEntity to set
     */
    void setLockEntity(Entity lockEntity);

    /**
     * @param point
     * @return The first entity under the given screen point (within some tolerance), or
     * <code>null</code> if there are no entities nearby.
     */
    Entity getEntityAtScreenPoint(Point point);

    EntityShapeManager getShapeAdapter();

    ShapeSystem getShapeSystem();

    Terrain getTerrain();

    SwingCoordinateTransformer getTransformer();

    void setMapImage(MapImage map);

    MapImage getMapImage();

    GridManager getGrid();

    DistanceToolManager getDistanceTools();

    void highlightEntity(Entity e);

    /**
     * Translate the view by the specified amount.
     * 
     * @param delta The desired translation, in pixels.
     */
    void pan(Point screenDelta);

    /**
     * Zoom in or out while keeping the specified point fixed on the screen.
     * 
     * @param pt The point to keep fixed while zooming, in screen coordinates.
     * @param amount The amount to zoom.  Positive values zoom out; negative
     * values zoom in.  Each unit of zoom is about +/-10%.
     */
    void zoomRelativeToPoint(Point pt, int amount);

    /**
     * Zoom in or out while keeping the center of the view fixed.
     * 
     * @param amount The amount to zoom.  Positive values zoom out; negative
     * values zoom in.  Each unit of zoom is about +/-10%.
     */
    void zoom(int amount);
    
    void zoomToLevel(int zoomLevel);
    
    void zoomToLevelRelativeToPoint(Point pt, int zoomLevel);
    
    void zoomLevelByAmount(int zoomLevelAmount);
    
    void zoomLevelAmountRelativeToPoint(Point pt, int zoomLevelAmount);

    /**
     * @param screenDelta The screen vector, in pixels
     * @return The displacement vector, in meters, corresponding to the given screen
     * vector.
     */
    Vector3 getDisplacementInMeters(Point screenDelta);

    /**
     * @return The current extents of the view in meters. Origin is at the
     *  <b>bottom</b> left. 
     *  
     *  TODO: figure it what this should do if there is a rotation, and fix it.
     */
    Rectangle2D getViewExtentsInMeters();

    /**
     * @return The center of the display in meters. Z is "fixed" to ground 
     *      level.
     */
    Vector3 getCenterInMeters();

    /**
     * Pan so that the given position is shown in the center of the display.
     * 
     * @param p The position to show, in meters.
     */
    void showPosition(Vector3 p);

    /**
     * Force the given position to be shown in the center of the display immediately.
     * 
     * @param p The position to show, in meters.
     * @param doRepaint If true, a repaint is forced.
     */
    void jumpToPosition(Vector3 p, boolean doRepaint);

    /**
     * Force the display to zoom out to show all of the entities in the 
     * simulation. Does nothing if there are no entities
     */
    void showAll();

    /**
     * Default cursor to use when not performing a user-specific operation
     * (e.g., dragging the pvd.)
     */
    void setCursorPreference(Cursor cursor);

    /**
     * @return the {@link Cursor} preferred when not dragging.
     */
    Cursor getCursorPreference();

    /**
     * The {@link Cursor} preferred when the user drags the PVD.
     */
    void setDraggingCursor(Cursor cursor);

    /**
     * @return the {@link Cursor} preferred when the user drags the PVD.
     */
    Cursor getDraggingCursor();
    
    SlippyMap getSlippyMap();
    
    void loadSlippyMap(Vector3 origin, int zoomLevel, String source, DetailedTerrain terrain);
}
