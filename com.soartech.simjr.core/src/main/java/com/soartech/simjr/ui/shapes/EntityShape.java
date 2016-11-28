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
 * Created on Jun 20, 2007
 */
package com.soartech.simjr.ui.shapes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.soartech.math.Vector3;
import com.soartech.shapesystem.FillStyle;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.RotationType;
import com.soartech.shapesystem.Shape;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.TextStyle;
import com.soartech.shapesystem.shapes.Frame;
import com.soartech.shapesystem.shapes.Text;
import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPropertyListener;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.entities.DamageStatus;

/**
 * Base class for entity shapes drawn in the PVD. Mainly this pulls together
 * one or more ShapeSystem shapes and manages them as a single unit so they
 * can be easily added or removed from the system.
 * 
 * @author ray
 */
public class EntityShape implements EntityPropertyListener
{
    private Entity entity;
    private ShapeSystem system;
    protected Frame frame;
    private Frame bodyFrame;
    private Frame shadowFrame;
    private List<Shape> shapes = new ArrayList<Shape>();
    private List<Shape> hitableShapes = new ArrayList<Shape>();
    private boolean destroyed = false;
    private boolean updateVisibility = true;
    private boolean updateDamage = false;
    private boolean updateForce = false;
    private boolean updateColor = false;
    private EntityVisibleRangeShape visibleRange = new EntityVisibleRangeShape(this, EntityConstants.PROPERTY_VISIBLE_RANGE, Color.BLUE);
    private EntityVisibleRangeShape radarRange = new EntityVisibleRangeShape(this, EntityConstants.PROPERTY_RADAR, Color.RED);
    private CalculatedImpactPointShape ccip = new CalculatedImpactPointShape(this);
    
    private List<LabelFrame> labels = new ArrayList<LabelFrame>();
    public class LabelFrame 
    {
        public final static String MAIN_LABEL = "_mainLabel";
        public final Frame frame;
        public final Text label;
        public final String labelType;
        
        public LabelFrame(Frame frame, Text label, String labelType)
        {
            this.frame = frame;
            this.label = label;
            this.labelType = labelType;
        }
    }
    
    /**
     * Get the name of the body frame for a given entity
     * 
     * @param entity the entity
     * @return the name of the entity's body frame
     */
    public static String getBodyFrameName(Entity entity)
    {
        return entity.getName() + ".bodyFrame";
    }

    /**
     * Constructor
     * 
     * @param entity The entity represented by the shape
     * @param system The shape system
     */
    public EntityShape(Entity entity, ShapeSystem system)
    {
        this.entity = entity;
        this.system = system;
        
        Vector3 pos = entity.getPosition();
        frame = new Frame(entity.getName(), "", 
                          Position.createWorldMeters(pos.x, pos.y),
                          Rotation.IDENTITY);
        addShape(frame);
        
        bodyFrame = new Frame(getBodyFrameName(entity), "", 
                new Position(entity.getName()),
                Rotation.IDENTITY);
        addShape(bodyFrame);

        entity.addPropertyListener(this);
    }

    /**
     * @return The entity
     */
    public Entity getEntity()
    {
        return entity;
    }
    
    /**
     * Return true if the given point is within this shape.
     * 
     * @param x The x screen coordinate
     * @param y The y screen coordinate
     * @return True if this shape contains the given point
     */
    public boolean hitTest(double x, double y, double tolerance)
    {
        for(Shape s : hitableShapes)
        {
            if(s.hitTest(x, y, tolerance))
            {
                return true;
            }
        }
        for (LabelFrame lf : labels)
        {
            if (lf.label.hitTest(x, y, tolerance))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Return the minimum distance between this shape's components and
     * the point.
     * 
     * @param x The x screen coordinate
     * @param y The y screen coordinate
     */
    public double minDistance(double x, double y)
    {
        double min = Double.MAX_VALUE;
        for(Shape s : hitableShapes)
        {
            double d = s.distance(x, y);
            if (d < min)
            {
                min = d;
            }
        }
        return min;
    }
    
    /**
     * Add a shape to be managed by this object. This is typically called by
     * sub-classes.  Shapes added with addShape are not hitable.
     * 
     * @param shape The shape.
     */
    public void addShape(Shape shape)
    {
        shapes.add(shape);
        system.addShape(shape);
        
        if(!frame.isVisible())
        {
            shape.setVisible(false);
        }
    }
    
    /**
     * Add a Shape to the set of Shapes that are tested by hitTest().
     * 
     * @param shape Shape to be added to this EntityShape.
     */
    public void addHitableShape(Shape shape)
    {
        hitableShapes.add(shape);
        addShape(shape);
    }
    
    /**
     * Remove a shape previously added with addShape() or addHitableShape().
     * 
     * @param shape The shape to remove
     */
    public void removeShape(Shape shape)
    {
        if(shape == null)
        {
            return;
        }
        
        shapes.remove(shape);
        hitableShapes.remove(shape);
        system.removeShape(shape.getName());
    }
        
    /**
     * @return The base frame shape created for the entity.
     */
    public Frame getRootFrame()
    {
        return frame;
    }
    
    /**
     * @return the bodyFrame
     */
    public Frame getBodyFrame()
    {
        return bodyFrame;
    }
    
    /**
     * Return the frame of the shadow of the EntityShape.
     * 
     * @return The Frame of this EntityShape's shadow.
     */
    public Frame getShadowFrame()
    {
        if(shadowFrame == null)
        {
            shadowFrame = new Frame(frame.getName() + ".shadowFrame", EntityConstants.LAYER_SHADOWS, 
                    Position.createRelativePixel(0, 0, frame.getName()),
                    Rotation.IDENTITY);
            addShape(shadowFrame);
        }
        
        return shadowFrame;
    }

    public String getPrimaryDisplayShape()
    {
        return bodyFrame.getName();
    }
    
    public Shape createBodyShape(String shapeId, ShapeStyle shapeStyle)
    {
    	return null;
    }

    public List<LabelFrame> getLabels()
    {
        return labels;
    }
    
    public Text createLabel(int xOffset, int yOffset, String text)
    {
        LabelFrame lf = createLabel(xOffset, yOffset, text, new Color(0xF0, 0xF0, 0xE0), LabelFrame.MAIN_LABEL); 
        
        return lf.label;
    }
    
    public LabelFrame createLabel(int xOffset, int yOffset, String text, Color color, String labelType)
    {
        LabelFrame lf = createLabel(xOffset, yOffset, text, getPrimaryDisplayShape(), color, labelType); 
        
        return lf;
    }
    
    public LabelFrame createLabel(int xOffset, int yOffset, String text, String relative, Color color, String labelType)
    {
        String name = getRootFrame().getName();
        Frame labelFrame = new Frame(name + labelType + ".labelFrame", EntityConstants.LAYER_LABELS, 
                Position.createRelativePixel(0, 0, relative),
                Rotation.IDENTITY);
        TextStyle labelStyle = new TextStyle();
        labelStyle.setFillStyle(FillStyle.FILLED);
        labelStyle.setFillColor(color);
        labelStyle.setOpacity(0.75f);
        Text label = new Text(name + labelType + ".label", EntityConstants.LAYER_LABELS,
                 Position.createRelativePixel(xOffset, yOffset, name + labelType + ".labelFrame"),
                 Rotation.IDENTITY,
                 labelStyle,
                 text);
        
        //remove the previous label of the same type
        removeLabel(labelType);
        
        //add the new label frame shapes to this entity
        addShape(labelFrame);
        addShape(label);
        
        //save the LabelFrame object
        LabelFrame lf = new LabelFrame(labelFrame, label, labelType);
        labels.add(lf);
        
        return lf;
    }
    
    public void removeLabel(String labelType)
    {
        //remove any previous label frame shapes of the same type
        LabelFrame toRemove = null;
        for(LabelFrame lf : labels)
        {
            if(lf.labelType.equals(labelType))
            {
                removeShape(lf.frame);
                removeShape(lf.label);
                toRemove = lf;
                break;
            }
        }
        
        //keep the LabelFrame list synchronized with the shape list
        if(toRemove != null)
        {
            labels.remove(toRemove);
        }
    }

    public void update()
    {
        if(frame != null)
        {
            Vector3 pos = entity.getPosition();
            frame.setPosition(Position.createWorldMeters(pos.x, pos.y));
            frame.setRotation(Rotation.fromRadians(entity.getHeading(), RotationType.WORLD));
            
            updateBodyFrame();
        }
        
        if(updateColor)
        {
            updateColor = false;
            Color fillColor = (Color) entity.getProperty(EntityConstants.PROPERTY_SHAPE_FILL_COLOR);
            Color lineColor = (Color) entity.getProperty(EntityConstants.PROPERTY_SHAPE_LINE_COLOR);
            
            for ( Shape s : shapes ) {
                
                boolean updateColorForThisShape = true;
                for(LabelFrame lf : labels)
                {
                    if(s == lf.frame || s == lf.label)
                    {
                        updateColorForThisShape = false;
                        break;
                    }
                }
                
                if (updateColorForThisShape) {
                    s.getStyle().setFillColor(fillColor);
                    s.getStyle().setLineColor(lineColor);
                }
            }
        }
        
        if(updateVisibility)
        {
            updateVisibility = false;
            boolean visible = EntityTools.isVisible(entity);
            
            for(Shape shape : shapes)
            {
                shape.setVisible(visible);
            }
            
            //update labels
            for(LabelFrame lf : labels)
            {
                boolean labelVisible = 
                        (Boolean) EntityTools.getProperty(entity.getProperties(), 
                                EntityConstants.PROPERTY_SHAPE_LABEL_VISIBLE, true);
                
                boolean displayLabelIfHasParent = (Boolean) EntityTools.getProperty(entity.getProperties(),
                                EntityConstants.PROPERTY_SHAPE_LABEL_DISPLAYIFPARENT, true);
                
                if (!displayLabelIfHasParent && entity.getParent() != null)
                {
                    lf.label.setVisible(false);
                }
                else if(visible && !labelVisible)
                {
                    lf.label.setVisible(false);
                }
            }
        }
        if(updateDamage)
        {
            updateDamage = false;
            DamageStatus status = (DamageStatus) EntityTools.getDamage(entity);
            if(!destroyed && status == DamageStatus.destroyed)
            {
                destroyed = true;
                destroyed();
            }
        }
        if(updateForce)
        {
            updateForce = false;
            updateForce();
        }
        visibleRange.update();
        radarRange.update();
        ccip.update();
    }
    
    private void updateBodyFrame()
    {
        Double agl = (Double) getEntity().getProperty(EntityConstants.PROPERTY_AGL);
        double z = 0.0;
        if(agl != null)
        {
            z = agl;
        }
        else
        {
            z = getEntity().getPosition().z;
        }

        Vector3 position = adjustPositionForShadow(getEntity().getPosition(), z);
        bodyFrame.setPosition(Position.createWorldMeters(position.x, position.y));
        bodyFrame.setRotation(Rotation.fromRadians(getEntity().getHeading(), RotationType.WORLD));
        if(shadowFrame != null)
        {
            shadowFrame.setRotation(Rotation.fromRadians(getEntity().getHeading(), RotationType.WORLD));
        }
    }
    
    public static Vector3 adjustPositionForShadow(Vector3 position, double agl)
    {
        final Double shadowAglScale = SimJrProps.get("simjr.simulation.shadow-agl-scale", 2.0);
        
        agl = Math.max(0.0, agl);
        agl /= shadowAglScale;
        
        return new Vector3(position.x - agl, position.y + agl, agl);
    }


    /**
     * Remove this shape (and all its constituent shapes) from the display 
     */
    public void remove()
    {
        entity.removePropertyListener(this);
        for(Shape shape : shapes)
        {
            system.removeShape(shape.getName());
        }
        shapes.clear();
        frame = null;
    }
    
    protected static Color getForceColor(String force)
    {
        if(EntityConstants.FORCE_FRIENDLY.equals(force))
        {
            return Color.BLUE;
        }
        if(EntityConstants.FORCE_OPPOSING.equals(force))
        {
            return Color.RED;
        }
        if(EntityConstants.FORCE_NEUTRAL.equals(force))
        {
            return Color.YELLOW;
        }
        return Color.GREEN;
    }
    
    /**
     * Called when the associated entity's force is changed.
     */
    protected void updateForce()
    {
    }

    
    /**
     * Called when the associated entity is destroyed. 
     */
    protected void destroyed()
    {
        final Color grey = Color.LIGHT_GRAY;
        
        for(Shape shape : shapes)
        {
            ShapeStyle style = shape.getStyle();
            style.setFillColor(grey);
            style.setLineColor(grey);
        }
    }

    public void onPropertyChanged(Entity entity, String propertyName)
    {
        if(EntityConstants.PROPERTY_VISIBLE.equals(propertyName) ||
           EntityConstants.PROPERTY_SHAPE_LABEL_VISIBLE.equals(propertyName) ||
           EntityConstants.PROPERTY_SHAPE_LABEL_DISPLAYIFPARENT.equals(propertyName))
        {
            updateVisibility = true;
        }
        else if(EntityConstants.PROPERTY_DAMAGE.equals(propertyName))
        {
            updateDamage = true;
        }
        else if(EntityConstants.PROPERTY_FORCE.equals(propertyName))
        {
            updateForce = true;
        }
        else if(EntityConstants.PROPERTY_VISIBLE_RANGE.equals(propertyName) ||
                EntityConstants.PROPERTY_VISIBLE_RANGE_VISIBLE.equals(propertyName))
        {
            visibleRange.setNeedsUpdate();
        }
        else if(EntityConstants.PROPERTY_RADAR.equals(propertyName) ||
                EntityConstants.PROPERTY_RADAR_VISIBLE.equals(propertyName))
        {
            radarRange.setNeedsUpdate();
        }
        else if(EntityConstants.PROPERTY_CCIP.equals(propertyName))
        {
            ccip.setNeedsUpdate();
        }
        else if (propertyName.equals(EntityConstants.PROPERTY_SHAPE_FILL_COLOR) ) 
        {
            updateColor = true;
        }
        else if (propertyName.equals(EntityConstants.PROPERTY_SHAPE_LINE_COLOR) )
        {
            updateColor = true;
        }

    }
}
