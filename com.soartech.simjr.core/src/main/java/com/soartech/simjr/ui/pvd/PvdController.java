package com.soartech.simjr.ui.pvd;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.entities.AbstractPolygon;
import com.soartech.simjr.ui.ObjectContextMenu;
import com.soartech.simjr.ui.SelectionManager;
import com.soartech.simjr.ui.SimulationMainFrame;

/**
 * 
 * @author mjquist
 *
 */
public class PvdController
{
    private Simulation sim;
    private ServiceManager app;

    private IPvdView view;
    
    private ObjectContextMenu contextMenu;
    private Point contextMenuPoint;
    private boolean contextMenuEnabled = true;

    private Point lastDragPoint = new Point(0, 0);
    private Point panOrigin = new Point();
    
    public PvdController()
    {
        // nothing until view is attached
    }

    void attachToView(IPvdView view, Simulation sim, ServiceManager app)
    {
        this.view = view;
        this.sim = sim;
        this.app = app;
        
        this.contextMenu = new ObjectContextMenu(app);

        view.getComponent().addMouseListener(new MouseHandler());
        view.getComponent().addMouseMotionListener(new MouseMotionHandler());
        view.getComponent().addMouseWheelListener(new MouseWheelHandler());
    }
    
    /**
     * @return the currently installed context menu
     */
    public ObjectContextMenu getContextMenu()
    {
        return contextMenu;
    }
    
    public Point getContextMenuPoint()
    {
        return contextMenuPoint;
    }

    /**
     * @param contextMenu the new context menu
     */
    public void setContextMenu(ObjectContextMenu contextMenu)
    {
        if (contextMenu == null)
        {
            throw new NullPointerException("Context menu cannot be null");
        }
        this.contextMenu = contextMenu;
    }
    
    public void setContextMenuEnabled(boolean enabled)
    {
        this.contextMenuEnabled = enabled;
    }

    private void mouseMoved(MouseEvent e)
    {
        view.highlightEntity(view.getEntityAtScreenPoint(e.getPoint()));
    }
    
    private void mousePressed(MouseEvent e)
    {
        if (e.isControlDown())
        {
            return;
        }
        
        final Entity entityUnderCursor = view.getEntityAtScreenPoint(e.getPoint());
        final List<Entity> selectedEntities = PlanViewDisplay.getSelectedEntities(this.app);
        
        if(SwingUtilities.isRightMouseButton(e) || !selectedEntities.contains(entityUnderCursor))
        {
            SelectionManager sm = SelectionManager.findService(this.app);
            sm.setSelection(this, entityUnderCursor);
        }

        if(SwingUtilities.isRightMouseButton(e))
        {
            return;
        }
        
        view.setIsDraggingEntity(entityUnderCursor != null);
        lastDragPoint.setLocation(e.getPoint());
        
        if (!view.isDraggingEntity())
        {
            panOrigin.setLocation(e.getPoint());
        }
        
        view.getComponent().repaint();
    }
    
    /**
     * Restores the cursor following a drag/pan operation.
     */
    private void mouseReleased(MouseEvent e)
    {
        view.getComponent().requestFocus();
        
        final SelectionManager sm = SelectionManager.findService(this.app);
        final List<Entity> selectedEntities = PlanViewDisplay.getSelectedEntities(app);
        final Entity entityUnderCursor = view.getEntityAtScreenPoint(e.getPoint());
        
        if (SwingUtilities.isRightMouseButton(e) && contextMenuEnabled)
        {
            contextMenuPoint = e.getPoint();
            contextMenu.show(view.getComponent(), e.getX(), e.getY());
        }
        else if (!e.isControlDown() && selectedEntities.size() > 1)
        {
            // Multi-selection management is done on mouse release.
            sm.setSelection(this, entityUnderCursor);
        }
        else if (e.isControlDown())
        {
            // Multi-selection management is done on mouse release.
            // Ctrl-click adds/removes an entity from the selection
            final List<Object> newSel = new ArrayList<Object>(sm.getSelection());
            if(!newSel.remove(entityUnderCursor))
            {
                newSel.add(0, entityUnderCursor);
            }
            sm.setSelection(this, newSel);
        }
        
        view.setIsDraggingEntity(false);

        view.getComponent().repaint();
        
        dragFinished();
    }
    
    private void dragEntity(MouseEvent e)
    {
        assert view.isDraggingEntity();
        
        final Entity entity = PlanViewDisplay.getSelectedEntity(app);
        if(entity == null)
        {
            return;
        }
        
        final Boolean locked = (Boolean) entity.getProperty(EntityConstants.PROPERTY_LOCKED);
        if(locked != null && locked.booleanValue())
        {
            return;
        }
        
        final Point screenDelta = new Point(e.getX() - lastDragPoint.x, e.getY() - lastDragPoint.y);
        final Vector3 delta = view.getDisplacementInMeters(screenDelta);
        lastDragPoint.setLocation(e.getPoint());

        synchronized(sim.getLock())
        {
            // If it's a polygon (route, area, etc) move all the points together
            final List<Entity> points;
            final AbstractPolygon polygon = Adaptables.adapt(entity, AbstractPolygon.class);
            if(polygon != null)
            {
                points = polygon.getPoints();
            }
            else
            {
                points = Arrays.asList(entity);
            }
            
            for(Entity p : points)
            {
                moveEntityPreservingAltitude(p, delta);
            }
            
            // Update the properties display while we're dragging
            // TODO: This is a hack.
            final SimulationMainFrame mainFrame = SimulationMainFrame.findService(app);
            if(mainFrame != null)
            {
                mainFrame.getPropertiesView().refreshModel();
            }
        }
        
        // Don't wait for the timer. This makes the UI a little snappier
        view.getComponent().repaint();
    }

    private void moveEntityPreservingAltitude(Entity p, final Vector3 delta)
    {
        // Preserve altitude. z is *not* altitude
        final Geodetic.Point oldLla = sim.getTerrain().toGeodetic(p.getPosition());
        final Vector3 newPosition = p.getPosition().add(delta);
        final Geodetic.Point newLla = sim.getTerrain().toGeodetic(newPosition);
        newLla.altitude = oldLla.altitude;
        p.setPosition(sim.getTerrain().fromGeodetic(newLla));
        
        // Update calculated properties if the sim isn't running
        if(sim.isPaused())
        {
            p.updateProperties();
        }
    }
    
    private void dragPan(MouseEvent e)
    {
        Point screenDelta = new Point();
        screenDelta.setLocation(e.getPoint().x - panOrigin.getX(), e.getPoint().y - panOrigin.getY());
        
        view.pan(screenDelta);
        
        // reset the pan origin
        panOrigin.setLocation(e.getPoint());
    }
    
    protected void dragFinished() { }

    private class MouseHandler extends MouseAdapter
    {
        /* (non-Javadoc)
         * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
         */
        @Override
        public void mousePressed(MouseEvent e)
        {
            PvdController.this.mousePressed(e);
        }
        
        public void mouseReleased(MouseEvent e)
        {
            PvdController.this.mouseReleased(e);
        }
    }
    
    private class MouseMotionHandler extends MouseMotionAdapter
    {

        /* (non-Javadoc)
         * @see java.awt.event.MouseMotionAdapter#mouseDragged(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseDragged(MouseEvent e)
        {
            if(SwingUtilities.isLeftMouseButton(e))
            {
                if (view.isDraggingEntity())
                {
                    PvdController.this.dragEntity(e);
                }
                else
                {
                    PvdController.this.dragPan(e);
                }
            }
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseMotionAdapter#mouseMoved(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseMoved(MouseEvent e)
        {
            PvdController.this.mouseMoved(e);
        }
    }
    
    private class MouseWheelHandler implements MouseWheelListener
    {
        public void mouseWheelMoved(MouseWheelEvent e) 
        {
            view.zoomRelativeToPoint(e.getPoint(), e.getWheelRotation());
        }
    }
}
