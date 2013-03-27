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
 * Created on May 17, 2007
 */
package com.soartech.shapesystem;

import java.util.ArrayList;
import java.util.List;

import com.soartech.shapesystem.swing.SwingCoordinateTransformer;


/**
 * @author ray
 */
public abstract class Shape
{

    public boolean problemThisFrame;
    protected List<SimplePosition> points = new ArrayList<SimplePosition>();
    protected List<SimpleRotation> angles = new ArrayList<SimpleRotation>();
    protected ShapeStyle style;
    private boolean calculatedThisFrame;
    private Position position;
    private Rotation rotation;
    private final String name;
    private final String layer;
    private boolean visible = true;
    
    public Shape(String name, String layer, Position pos, Rotation rot,
                 ShapeStyle style)
    {
        this.name = name;
        this.layer = layer;
        this.calculatedThisFrame = false;
        this.position = pos;
        this.rotation = rot;
        this.style = style != null ? style : new ShapeStyle();
        this.problemThisFrame = false;
    }
    
    @Override
    public String toString()
    {
        return name;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getLayer()
    {
        return layer;
    }
    
    public Position getPosition()
    {
        return position;
    }
    
    public void setPosition(Position pos)
    {
        this.position = pos;
    }

    public Rotation getRotation()
    {
        return rotation;
    }
    
    public void setRotation(Rotation rotation)
    {
        this.rotation = rotation;
    }

    public ShapeStyle getStyle()
    {
        return style;
    }

    public void reset()
    {
        points.clear();
        angles.clear();
        calculatedThisFrame = false;
        problemThisFrame = false;
    }
    
    /**
     * @return the visible
     */
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * @param visible the visible to set
     */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }
    
    public abstract boolean hitTest(double x, double y, double tolerance);

    public SimpleTransformation calculate(ShapeSystem system, CoordinateTransformer transformer)
    {
        if(calculatedThisFrame)
        {
            return buildCompletedTransform(system);
        }
        
        calculatedThisFrame = true;
        
        List<String> references = getReferences();
        for(String id : references)
        {
            Shape reference = system.getShape(id);
            if(reference == null)
            {
                system.errorInShape(this, "Undefined reference to shape '" + id + "'");
                break;
            }
            
            reference.calculate(system, transformer);
            if(reference.problemThisFrame)
            {
                problemThisFrame = true;
                break;
            }
        }
        
        if(problemThisFrame)
        {
            return buildCompletedTransform(system);
        }
        
        calculateBase(system, transformer);
        if(points.isEmpty())
        {
        	points.add(new SimplePosition());
        }
        if(angles.isEmpty())
        {
        	angles.add(SimpleRotation.fromDegrees(0.0));
        }

        rotate(SimpleRotation.fromDegrees(rotation.getDegrees()));
        
        switch(rotation.getType())
        {
        case WORLD:
            // Do nothing
            break;
        case RELATIVE:
            if(!rotation.hasParent())
            {
                system.errorInShape(this, "rotation is relative_to, but no parent is specified.");
            }
            else
            {
                Shape parent = system.getShape(rotation.getParent());
                rotate(SimpleRotation.fromDegrees(parent.rotation.getDegrees()));
                
                // TODO: JCC - Why on earth does this work? Surely this breaks something
                rotate(SimpleRotation.fromRadians(((SwingCoordinateTransformer)transformer).getRotation()));
            }
            break;
        case POINT_AT:
            if(!rotation.hasParent())
            {
                system.errorInShape(this, "rotation is point_at, but no parent is specified.");
            }
            else
            {
                // We need to do something a little bizarre here and pre-calculate
                // our translation so we can find the angle between where we'll
                // eventually be and where our parent is.
                SimplePosition early_pos = simplifyLocalPositionPart(system, transformer);
                if (position.getType() == PositionType.RELATIVE)
                {               
                   Shape pos_parent = system.getShape(position.getParent());
                   early_pos.translate(pos_parent.calculate(system, transformer).position);
                }
                
                // We don't have to side-step calling an infinitely recursive Calculate()
                // here like we have to worry about on ourselves.
                //
                // (We can also ignore the rotate-to-position-parent's-rotation step during
                // this early calculation because rotation doesn't affect the PointAt behavior.)
                Shape rot_parent = system.getShape(rotation.getParent());
                SimplePosition rot_parent_pos = rot_parent.calculate(system, transformer).position;
                
                // Find the angle between the two positions
                double del_x =  (rot_parent_pos.x - early_pos.x);
                double del_y = -(rot_parent_pos.y - early_pos.y);

                double radians = Math.atan2(del_y, del_x);
                double degrees = Rotation.fromRadians(radians, RotationType.WORLD).getDegrees();
                
                rotate(SimpleRotation.fromDegrees(degrees));
            }
            break;
        }
        
        SimplePosition our_translate = simplifyLocalPositionPart(system, transformer);
        
        switch(position.getType())
        {
        case WORLD:
            translate(our_translate);
            break;
        case RELATIVE:
            Shape pos_parent = system.getShape(position.getParent());
            SimpleTransformation parent = pos_parent.calculate(system, transformer);

            // First, rotate our own translation axis
            our_translate.rotate(parent.rotation);

            // Translate to our local position -- the axis of which may have
            // been rotated because of a relative_to position parent
            translate(our_translate);

            // Then translate by any inherited position
            translate(parent.position);   
            break;
        }
        return buildCompletedTransform(system);
    }
    
    public List<String> getReferences()
    {
        List<String> references = getBaseReferences();
        
        if(position.hasParent())
        {
            references.add(position.getParent());
        }
        if(rotation.hasParent())
        {
            references.add(rotation.getParent());
        }
        return references;
    }
        
    public abstract void draw(PrimitiveRendererFactory rendererFactory);
    
    protected abstract void calculateBase(ShapeSystem system, CoordinateTransformer transformer);

    protected abstract List<String> getBaseReferences();
    
    protected SimplePosition simplifyLocalPositionPart(ShapeSystem system, CoordinateTransformer transformer)
    {
        Position p = position;
        switch(p.getType())
        {
        case RELATIVE:
            // For our purposes in this function, this doesn't really affect us,
            // but this also happens to be our centralized error checking place
            if (p.getParent().length() == 0)
            {
               system.errorInShape(this, "position is relative_to, but no parent is specified.");
               return new SimplePosition();
            }
         
            // At this point we know we there aren't any LatLon's and ScalarToPixels
            // works for both meters and pixels.  So, this is easy.
            return new SimplePosition(transformer.scalarToPixels(p.getX()), 
                                      transformer.scalarToPixels(p.getY()));
        case WORLD:
            double x = p.getX().getValue();
            double y = p.getY().getValue();

            // We can optimize a few things if we know that both components are in
            // the same units
            if (p.getX().getUnit() == p.getY().getUnit())
            {
               switch (p.getX().getUnit())
               {
               case Meters: return transformer.metersToScreen(x, y);
               case Pixels: return new SimplePosition(x, y);
               }
            }
            else
            {
               // Check to see if we're allowed to do single-coordinate type conversions
               if (!transformer.supportsSingleWorldCoordinates())
               {
                  system.errorInShape(this, "Current coordinate transformer doesn't support mixed-unit world coordinates.");
                  return new SimplePosition();
               }
               
               double pixel_x = 0;
               switch (p.getX().getUnit())
               {
               case Meters: pixel_x = transformer.metersXToScreen(x); break;
               case Pixels: pixel_x = x;                            break;
               }
               
               double pixel_y = 0;
               switch (p.getY().getUnit())
               {
               case Meters: pixel_y = transformer.metersYToScreen(y); break;
               case Pixels: pixel_y = y;                             break;
               }
               
               return new SimplePosition(pixel_x, pixel_y);
            }
        }
        
        system.errorInShape(this, "INTERNAL: SimplifyLocalPositionPart() hit unreachable code.");
        return new SimplePosition();
    }
    
    private void translate(SimplePosition p)
    {
        for(SimplePosition point : points)
        {
            point.translate(p);
        }
    }
    
    private void rotate(SimpleRotation r)
    {
        for(SimplePosition p : points)
        {
            p.rotate(r);
        }
        
        for(SimpleRotation angle : angles)
        {
            angle.rotate(r);
        }
    }
    
    private SimpleTransformation buildCompletedTransform(ShapeSystem system)
    {
        SimpleTransformation r = new SimpleTransformation();
        if(problemThisFrame)
        {
            return r;
        }
        
        if(points.isEmpty())
        {
            system.errorInShape(this, "INTERNAL: No simple points found when building completed transform!");
            return r;
         }
         if (angles.isEmpty())
         {
            system.errorInShape(this, "INTERNAL: No simple angles found when building completed transform!");
            return r;
         }
         
         r.position = points.get(points.size() - 1);
         r.rotation = angles.get(angles.size() - 1);
         
         return r;
      }   
}
