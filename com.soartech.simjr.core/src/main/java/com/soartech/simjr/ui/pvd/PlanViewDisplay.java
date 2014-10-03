/*
 * Copyright (c) 2010, Soar Technology, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * * Neither the name of Soar Technology, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without the specific prior written permission of Soar Technology, Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on May 22, 2007
 */
package com.soartech.simjr.ui.pvd;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.shapesystem.CoordinateTransformer;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.SimplePosition;
import com.soartech.shapesystem.swing.SwingCoordinateTransformer;
import com.soartech.shapesystem.swing.SwingPrimitiveRendererFactory;
import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.app.ApplicationState;
import com.soartech.simjr.app.ApplicationStateService;
import com.soartech.simjr.radios.RadioHistory;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.Terrain;
import com.soartech.simjr.ui.ObjectContextMenu;
import com.soartech.simjr.ui.SelectionManager;
import com.soartech.simjr.ui.SelectionManagerListener;
import com.soartech.simjr.ui.pvd.imagery.MapOpacityController;
import com.soartech.simjr.ui.pvd.imagery.MapTileRenderer;
import com.soartech.simjr.ui.shapes.DetonationShapeManager;
import com.soartech.simjr.ui.shapes.EntityShape;
import com.soartech.simjr.ui.shapes.EntityShapeManager;
import com.soartech.simjr.ui.shapes.SpeechBubbleManager;
import com.soartech.simjr.ui.shapes.TimedShapeManager;
import com.soartech.simjr.util.StringTools;
import com.soartech.simjr.util.SwingTools;

/**
 * @author ray
 */
public class PlanViewDisplay extends JPanel
{
    private static final Logger logger = LoggerFactory.getLogger(PlanViewDisplay.class);
    
    private static final long serialVersionUID = 6151999888052532421L;

    private ServiceManager app;
    private Simulation sim;
    private final SwingCoordinateTransformer transformer = new SwingCoordinateTransformer(this);
    private final SwingPrimitiveRendererFactory factory = new SwingPrimitiveRendererFactory(transformer);

    private final ShapeSystem shapeSystem;
    private final TimedShapeManager timedShapes;
    private final PanAnimator panAnimator = new PanAnimator(transformer);

    private SelectionManagerListener selectionListener;
    private Timer repaintTimer; // VIEW
    private EntityShapeManager shapeAdapter; // VIEW
    private DistanceToolManager distanceTools; // VIEW
    private DetonationShapeManager detonationShapes; // VIEW
    private SpeechBubbleManager speechBubbles; // VIEW
    private final GridManager grid = new GridManager(transformer); // VIEW
    
    private MapImage mapBackgroundImage; // VIEW
    private MapTileRenderer tileRenderer; // VIEW
    private final MapOpacityController mapOpacityController; // VIEW
    private MapDebugPanel mapDebugPanel; // VIEW
    private final CoordinatesPanel coordinatesPanel; // VIEW
    private final AppStateIndicator appStateIndicator; // VIEW
    
    private final PvdController controller;
    
    public PlanViewDisplay(ServiceManager app, PlanViewDisplay toCopy)
    {
        this(app, toCopy, null);
    }
    
    public PlanViewDisplay(ServiceManager app, PlanViewDisplay toCopy, PvdController controller)
    {
        setLayout(null);
        
        this.app = app;
        this.appStateIndicator = new AppStateIndicator(this.app.findService(ApplicationStateService.class), this);
        
        this.shapeSystem = new ShapeSystem();
        this.shapeSystem.displayErrorsInFrame(SimJrProps.get("simjr.pvd.displayErrors", true));
        
        this.timedShapes = new TimedShapeManager(shapeSystem);

        this.sim = this.app.findService(Simulation.class);
        this.shapeAdapter = new EntityShapeManager(sim, shapeSystem, factory);
        this.distanceTools = new DistanceToolManager(app, shapeSystem);
        this.detonationShapes = new DetonationShapeManager(sim, timedShapes);
        this.speechBubbles = new SpeechBubbleManager(sim, this.app.findService(RadioHistory.class), shapeAdapter);
        
        setToolTipText(""); // Enable tooltips
        setFocusable(true);
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLoweredBevelBorder());
        
        this.selectionListener = new SelectionManagerListener() {
            public void selectionChanged(Object source) {
                shapeAdapter.updateSelection(getSelectedEntities());
                repaint();
            }
        };
        final SelectionManager sm = SelectionManager.findService(this.app);
        if(sm != null) {
            sm.addListener(selectionListener);
        }
        
        if(toCopy != null) {
            setMapImage(toCopy.getMapImage());
        }
        
        // Periodically redraw the screen rather than trying to only redraw
        // when something changes in the simulation
        repaintTimer = new Timer(200, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!panAnimator.isAnimating()) {
                    repaint();
                }
            }
        });
        
        this.tileRenderer = new MapTileRenderer(this);
        this.tileRenderer.approximateScale(transformer.screenToMeters(1));

        this.coordinatesPanel = new CoordinatesPanel();
        addCoordinatesPanel();
        
        this.mapOpacityController = new MapOpacityController(tileRenderer);
        addMapOpacityController();

        if(SimJrProps.get("simjr.map.imagery.debug", false)) { 
            this.mapDebugPanel = new MapDebugPanel(transformer, tileRenderer);
            addMapDebugPanel();
        } else {
            this.mapDebugPanel = null;
        }
        
        repaintTimer.start();
        
        if (controller == null)
        {
            this.controller = new PvdController();
        }
        else
        {
            this.controller = controller;
        }
        
        this.controller.attachToView(this, this.sim, this.app);
    }

    public void dispose()
    {
        logger.info("Disposing PVD " + this);
        SelectionManager.findService(this.app).removeListener(selectionListener);
        repaintTimer.stop();
        appStateIndicator.dispose();
        shapeAdapter.dispose();
        speechBubbles.dispose();
        detonationShapes.dispose();
    }

    public Point getContextMenuPoint()
    {
        return controller.getContextMenuPoint();
    }
    
    public void setContextMenu(ObjectContextMenu contextMenu)
    {
        controller.setContextMenu(contextMenu);
    }
    
    public void setContextMenuEnabled(boolean enabled)
    {
        controller.setContextMenuEnabled(enabled);
    }
    
    public boolean isDraggingEntity()
    {
        return controller.isDraggingEntity();
    }
    
    public Entity getLockEntity()
    {
        return controller.getLockEntity();
    }
    
    public void setLockEntity(Entity lockEntity)
    {
        controller.setLockEntity(lockEntity);
    }
    
    /**
     * @param point
     * @return The first entity under the given screen point (within some tolerance), or
     * <code>null</code> if there are no entities nearby.
     */
    public Entity getEntityAtScreenPoint(Point point)
    {
        final List<Entity> entities = shapeAdapter.getEntitiesAtScreenPoint(point.getX(), point.getY(), SimJrProps.get("simjr.pvd.mouse.tolerance", 15.0));
        return !entities.isEmpty() ? entities.get(0) : null;
    }

    /**
     * @return A list of all the entities that are selected.
     */
    public List<Entity> getSelectedEntities()
    {
        return Adaptables.adaptCollection(SelectionManager.findService(app).getSelection(), Entity.class);
    }
    
    /**
     * @return The first selected entity, or <code>null</code> if no entities are selected.
     */
    public Entity getSelectedEntity()
    {
        final List<Entity> selection = getSelectedEntities();
        return !selection.isEmpty() ? selection.get(0) : null;
    }
    
    public EntityShapeManager getShapeAdapter()
    {
        return shapeAdapter;
    }
    
    public ShapeSystem getShapeSystem()
    {
        return shapeSystem;
    }
    
    public Terrain getTerrain()
    {
        return sim.getTerrain();
    }
    
    public CoordinateTransformer getTransformer()
    {
        return transformer;
    }
    
    public MapTileRenderer getMapTileRenderer()
    {
        return tileRenderer;
    }
    
    public void setMapImage(MapImage map)
    {
        this.mapBackgroundImage = map;
    }
    
    public MapImage getMapImage()
    {
        return mapBackgroundImage;
    }
    
    public GridManager getGrid()
    {
        return grid;
    }
    
    public DistanceToolManager getDistanceTools()
    {
        return distanceTools;
    }

    public void highlightEntity(Entity e)
    {
        shapeAdapter.highlightEntity(e);
        repaint();
    }
    
    /**
     * Translate the view by the specified amount.
     * 
     * @param delta The desired translation, in pixels.
     */
    public void pan(Point screenDelta)
    {
        double offsetX = transformer.getPanOffsetX() + screenDelta.getX();
        double offsetY = transformer.getPanOffsetY() + screenDelta.getY();
        transformer.setPanOffset(offsetX, offsetY);
        
        repaint();
    }
    
    /**
     * Zoom in or out while keeping the specified point fixed on the screen.
     * 
     * @param pt The point to keep fixed while zooming, in screen coordinates.
     * @param amount The amount to zoom.  Positive values zoom out; negative
     * values zoom in.  Each unit of zoom is about +/-10%.
     */
    public void zoomRelativeToPoint(Point pt, int amount)
    {
        // capture fixedPoint which is under mouse cursor
        final Vector3 fixedPoint = transformer.screenToMeters(pt.getX(), pt.getY());

        // set the scale
        final double factor = Math.pow(0.9, amount);
        transformer.setScale(transformer.getScale() * factor);

        // change offset so that the fixedPoint continues to be under the mouse
        // Note: treat the new screen position as the pan origin
        final SimplePosition newScreenPosition = transformer.metersToScreen(fixedPoint.x, fixedPoint.y);
        final double newX = transformer.getPanOffsetX() + pt.getX() - newScreenPosition.x;
        final double newY = transformer.getPanOffsetY() + pt.getY() - newScreenPosition.y;
        transformer.setPanOffset(newX, newY);

        //Scale tiles appropriately
        tileRenderer.approximateScale(transformer.screenToMeters(1));
        
        repaint();
    }

    /**
     * Zoom in or out while keeping the center of the view fixed.
     * 
     * @param amount The amount to zoom.  Positive values zoom out; negative
     * values zoom in.  Each unit of zoom is about +/-10%.
     */
    public void zoom(int amount)
    {
        zoomRelativeToPoint(new Point(getWidth() / 2, getHeight() / 2), amount);
    }

    private void addCoordinatesPanel()
    {
        coordinatesPanel.setActivePvd(this);
        coordinatesPanel.setBounds(12, 10, 300, 20);
        add(coordinatesPanel);
    }
    
    private void addMapOpacityController()
    {
        mapOpacityController.setBounds(0, 30, mapOpacityController.getPreferredSize().width, mapOpacityController.getPreferredSize().height);
        add(mapOpacityController);
        showMapOpacityController(false);
    }

    private void addMapDebugPanel()
    {
        mapDebugPanel.setActivePvd(this);
        mapDebugPanel.setBounds(10, 75, mapDebugPanel.getPreferredSize().width, mapDebugPanel.getPreferredSize().height);
        add(mapDebugPanel);
    }
    
    public void showMapOpacityController(boolean show)
    {
        mapOpacityController.setVisible(show);
    }
    
    /**
     * @param screenDelta The screen vector, in pixels
     * @return The displacement vector, in meters, corresponding to the given screen
     * vector.
     */
    public Vector3 getDisplacementInMeters(Point screenDelta)
    {
        return new Vector3(
                transformer.screenToMeters(screenDelta.getX()),
                -transformer.screenToMeters(screenDelta.getY()), // Y down
                 0.0); // Preserve altitude
    }
    
    /**
     * @return The current extents of the view in meters. Origin is at the
     *  <b>bottom</b> left. 
     */
    public Rectangle2D getViewExtentsInMeters()
    {
        Vector3 bottomLeft = transformer.screenToMeters(0, getHeight());
        Vector3 topRight = transformer.screenToMeters(getWidth(), 0);
        
        return new Rectangle2D.Double(bottomLeft.x, bottomLeft.y,
                                      topRight.x - bottomLeft.x,
                                      topRight.y - bottomLeft.y);
    }
    
    /**
     * @return The center of the display in meters. Z is "fixed" to ground 
     *      level.
     */
    public Vector3 getCenterInMeters()
    {
        Vector3 pos = transformer.screenToMeters(getWidth() / 2, getHeight() / 2);
        Geodetic.Point lla = sim.getTerrain().toGeodetic(pos);
        lla.altitude = 0.0;
        return sim.getTerrain().fromGeodetic(lla);
    }
    
    /**
     * Pan so that the given position is shown in the center of the display.
     * 
     * @param p The position to show, in meters.
     */
    public void showPosition(Vector3 p)
    {
        panAnimator.panToPosition(p);
    }

    /**
     * Force the given position to be shown in the center of the display immediately.
     * 
     * @param p The position to show, in meters.
     * @param doRepaint If true, a repaint is forced.
     */
    private void jumpToPosition(Vector3 p, boolean doRepaint)
    {
        panAnimator.jumpToPosition(p, doRepaint);
    }
    
    /**
     * Force the display to zoom out to show all of the entities in the 
     * simulation. Does nothing if there are no entities
     */
    public void showAll()
    {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        
        boolean visibleEntities = false;
        synchronized(sim.getLock())
        {
            List<Entity> entities = sim.getEntitiesFast();
            if(entities.isEmpty()) {
                return;
            }
            
            for(Entity e : entities)
            {
                if(e.hasPosition())
                {
                    Vector3 p = e.getPosition();
                    minX = Math.min(minX, p.x);
                    minY = Math.min(minY, p.y);
                    maxX = Math.max(maxX, p.x);
                    maxY = Math.max(maxY, p.y);
                    
                    visibleEntities = true;
                }
            }
        }
        
        if(!visibleEntities) {
            return;
        }
        
        if(maxX - minX < 10.0)
        {
            maxX += 10.0;
            minX -= 10.0;
        }
        if(maxY - minY < 10.0)
        {
            maxY += 10.0;
            minY -= 10.0;
        }
        
        double centerX = (maxX + minX) / 2.0;
        double centerY = (maxY + minY) / 2.0;
        
        jumpToPosition(new Vector3(centerX, centerY, 0.0), false);
        
        double desiredWidth = maxX - minX;
        double desiredHeight = maxY - minY;
        
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        Rectangle2D extents = getViewExtentsInMeters();
        
        if(extents.isEmpty()) {
            return;
        }
        
        // First zoom in
        while(desiredWidth < extents.getWidth() ||  desiredHeight < extents.getHeight())
        {
            zoomRelativeToPoint(center, -1);
            extents = getViewExtentsInMeters();
        }
        
        // Now zoom back out
        while(desiredWidth >= extents.getWidth() ||  desiredHeight >= extents.getHeight())
        {
            zoomRelativeToPoint(center, 1);
            extents = getViewExtentsInMeters();
        }
        
        // one more for good measure. Otherwise we may end up with entities right
        // on the edge of the screen.
        zoomRelativeToPoint(center, 1);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        if(appStateIndicator.getState() != ApplicationState.RUNNING) {
            return;
        }
        
        // Briefly lock the sim to update entity shapes and stuff.
        double time = 0.0;
        synchronized (sim.getLock())
        {
            Entity lockEntity = controller.getLockEntity();
            if(lockEntity != null)
            {
                Double agl = (Double) lockEntity.getProperty(EntityConstants.PROPERTY_AGL);
                transformer.setRotation(-lockEntity.getHeading() + Math.PI/2);
                jumpToPosition(EntityShape.adjustPositionForShadow(lockEntity.getPosition(), agl), false);
            }
            time = sim.getTime();
            shapeAdapter.update();
            detonationShapes.update();
            speechBubbles.update();
            shapeSystem.update(transformer);
        }
        
        // Set up the graphics contexts...
        Graphics2D g2d = (Graphics2D) g;
        SwingTools.enableAntiAliasing(g2d);
        
        Graphics2D g2dCopy = (Graphics2D) g2d.create();
        SwingTools.enableAntiAliasing(g2dCopy);
        
        // Now draw everything again. None of the following code should be
        // dependent on a sim lock.
        paintMapBackground(g2d);
        
        //TODO: Reenable when reimplementd
        //tileRenderer.paint(g2d);
        
        grid.draw(g2d);
        factory.setGraphics2D(g2dCopy, getWidth(), getHeight());
        
        timedShapes.update(time);
        
        shapeSystem.draw(factory);
        shapeSystem.displayErrors(factory);
        //shapeSystem.displayDebugging(factory, transformer);
        
        g2dCopy.dispose();
    }
    
    private void paintMapBackground(Graphics2D g2d)
    {
        if(mapBackgroundImage != null) {
            mapBackgroundImage.draw(g2d, transformer);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getToolTipText(MouseEvent ev)
    {
        // Build the tooltip. By using html, we can make the tooltip 
        // multiline and generally nicer looking.
        final Entity e = getEntityAtScreenPoint(ev.getPoint());
        if(e == null)
        {
            return null;
        }
        
        final Map<String, Object> props = e.getProperties();
        String s = "<html>";
        s += "<b>" + e.getName() + " - " + e.getPrototype() + "</b><br>";
        final Object mgrs = props.get(EntityConstants.PROPERTY_MGRS);
        if(mgrs != null)
        {
            s += "<b>Location:</b> " + mgrs + "<br>";
        }
        final Object freq = props.get(EntityConstants.PROPERTY_FREQUENCY);
        if(freq != null)
        {
            s += "<b>Frequency:</b> " + freq + "<br>";
        }
        final Object voice = props.get(EntityConstants.PROPERTY_VOICE);
        if(voice != null)
        {
            s += "<b>Voice:</b> " + voice + "<br>";
        }

        final List<Entity> contents = (List<Entity>) props.get(EntityConstants.PROPERTY_CONTAINS);
        if(contents != null && !contents.isEmpty())
        {
            s += "<b>Contains:</b> " + StringTools.join(contents, ", ");
        }
        s += "</html>";
        
        return s;
    }
}
