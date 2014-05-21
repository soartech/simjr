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
import java.util.Map;

import com.soartech.math.LineSegmentDistance;
import com.soartech.math.Vector3;
import com.soartech.shapesystem.CapStyle;
import com.soartech.shapesystem.CoordinateTransformer;
import com.soartech.shapesystem.FillStyle;
import com.soartech.shapesystem.JoinStyle;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.PositionType;
import com.soartech.shapesystem.PrimitiveRenderer;
import com.soartech.shapesystem.PrimitiveRendererFactory;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.ScalarUnit;
import com.soartech.shapesystem.Shape;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.SimplePosition;
import com.soartech.shapesystem.shapes.Text;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.entities.AbstractPolygon;

/**
 * @author ray
 */
public class RouteShape extends EntityShape implements EntityConstants
{
    public static final String NAME = "route";
    
    public static final EntityShapeFactory FACTORY = new Factory();
    public static class Factory extends AbstractEntityShapeFactory {

        public EntityShape create(Entity entity, ShapeSystem system)
        {
            final AbstractPolygon polygon = Adaptables.adapt(entity, AbstractPolygon.class);
            return new RouteShape(polygon, system);
        }
        
        /* (non-Javadoc)
         * @see com.soartech.simjr.ui.pvd.AbstractEntityShapeFactory#createSelection(java.lang.String, com.soartech.simjr.Entity)
         */
        @Override
        public Shape createSelection(String id, Entity selected)
        {
            final AbstractPolygon polygon = Adaptables.adapt(selected, AbstractPolygon.class);
            final ShapeStyle style = createSelectionStyle();
            style.setLineColor(style.getFillColor());
            style.setCapStyle(CapStyle.ROUND);
            style.setJoinStyle(JoinStyle.ROUND);
            setLineWidth(selected.getProperties(), style, true);
            return new SystemShape(null, polygon, id, LAYER_SELECTION, style);   
        }

        public String toString() { return NAME; }
    };
    
    private static final ShapeStyle labelStyle = new ShapeStyle();
    static
    {
        labelStyle.setFillStyle(FillStyle.FILLED);
        labelStyle.setFillColor(new Color(0xF0, 0xF0, 0xE0));
        labelStyle.setOpacity(0.75f);
    }

    /**
     * @param route
     * @param system
     */
    public RouteShape(AbstractPolygon route, ShapeSystem system)
    {
        super(route.getEntity(), system); 
        
        Map<String, Object> props = route.getEntity().getProperties();
        String layer = EntityTools.getProperty(props, 
                                          PROPERTY_SHAPE_LAYER, 
                                          LAYER_ROUTE).toString();
        
        ShapeStyle style = new ShapeStyle();
        
        Color lineColor = (Color) EntityTools.getLineColor(route.getEntity(), Color.YELLOW);
        style.setLineColor(lineColor);
        
        Number opacity = (Number) EntityTools.getProperty(props, PROPERTY_SHAPE_OPACITY, 1.0);
        if(opacity != null)
        {
            style.setOpacity(opacity.floatValue());
        }
        
        setLineWidth(props, style, false);
        
        addHitableShape(new SystemShape(this, route, route.getName() + ".route", layer, style));
    }

    public static void setLineWidth(Map<String, Object> props, ShapeStyle style, boolean selection)
    {
        // First check for width in meters
        Number lineWidth = (Number) EntityTools.getProperty(props, PROPERTY_SHAPE_WIDTH_METERS, null);
        
        if(lineWidth != null)
        {
            style.setLineThickness(Scalar.createMeter(lineWidth.doubleValue() * (selection ? 1.5 : 1.0)));
        }
        else
        {
            // Next for width in pixels
            lineWidth = (Number) EntityTools.getProperty(props, PROPERTY_SHAPE_WIDTH_PIXELS, null);
            if(lineWidth != null)
            {
                style.setLineThickness(Scalar.createPixel(lineWidth.doubleValue() + (selection ? 10.0 : 0)));
            }
            else
            {
                // No setting
                style.setLineThickness(Scalar.createPixel(selection ? 20.0 : 2.0));
            }
        }
    }

    private static class SystemShape extends Shape
    {
        private Text label = null;
        private AbstractPolygon route;
        private List<SimplePosition> cachedPoints = new ArrayList<SimplePosition>();
        
        public SystemShape(RouteShape routeShape, AbstractPolygon route, String id, String layer, ShapeStyle style)
        {
            super(id, layer, new Position(), Rotation.IDENTITY, style);
            this.route = route;
            if (null != routeShape)
            {
                this.label = routeShape.createLabel(0, 0, "");
            }
        }

        @Override
        protected void calculateBase(ShapeSystem system, CoordinateTransformer transformer)
        {
            cachedPoints.clear();
            
            for(String name : route.getPointNames())
            {
                Shape shape = system.getShape(name);
                if(shape != null)
                {
                    cachedPoints.add(shape.calculate(system, transformer).position);
                }
                else
                {
                    cachedPoints.add(new SimplePosition());
                }
            }
        }

        @Override
        public void draw(PrimitiveRendererFactory rendererFactory)
        {
            if(cachedPoints.size() < 2)
            {
                label.setStyle(labelStyle);
                label.setText("Empty route " + route.getName());
                return;
            }
            
            final ShapeStyle frontStyle = getStyle();
            final ShapeStyle backStyle = frontStyle.copy();
            
            //Calculate the size of the route outline rather than applying a default scale.
            // Using the scale transform causes the line to appear much thicker than it really is
            double size = backStyle.getLineThickness().getValue();
            ScalarUnit unit = backStyle.getLineThickness().getUnit();
            Scalar backGroundThickness;
            if(unit.equals(ScalarUnit.Meters))
            {
                backGroundThickness = Scalar.createMeter(size +5);
            }
            else
            {
                backGroundThickness = Scalar.createPixel(size +5);
            }
            backStyle.setLineThickness(backGroundThickness);
            backStyle.setLineColor(backStyle.getLineColor().darker());
            
            // Draw the route twice. Once with a thicker, darker line. Once with a thinner line.
            for(ShapeStyle style : new ShapeStyle[] {backStyle, frontStyle} )
            {
                final PrimitiveRenderer renderer = rendererFactory.createPrimitiveRenderer(style);
                // First check for width in meters
                Map<String, Object> props = route.getEntity().getProperties();
                Number lineWidth = (Number) EntityTools.getProperty(props, PROPERTY_SHAPE_WIDTH_METERS, null);
                if(lineWidth != null && lineWidth.doubleValue() > 0.5)
                {
                    style.setLineThickness(Scalar.createMeter(lineWidth.doubleValue() * (1.0)));
                }
                else
                {
                    style.setLineThickness(Scalar.createPixel(5));
                }
                renderer.drawPolyline(cachedPoints);
                
                drawLabel(renderer);
            }
        }
        
        private void drawLabel(PrimitiveRenderer renderer)
        {
            if (label == null)
            {
                return;
            }
            
            List<SimplePosition> consequtiveVisiblePoints = new ArrayList<SimplePosition>();
            SimplePosition visiblePoint = null;
            for(SimplePosition point : cachedPoints)
            {
                if (renderer.isPointInViewport(point))
                {
                    visiblePoint = point;
                    consequtiveVisiblePoints.add(point);
                    if (consequtiveVisiblePoints.size() == 2)
                    {
                        break;
                    }
                }
                else
                {
                    consequtiveVisiblePoints.clear();
                }
            }
        
            boolean labelVisible = (consequtiveVisiblePoints.size() == 2 || visiblePoint != null) &&
                    (Boolean) EntityTools.getProperty(route.getEntity().getProperties(), 
                            EntityConstants.PROPERTY_SHAPE_LABEL_VISIBLE, true);

            SimplePosition labelPos = new SimplePosition();
            label.setVisible(labelVisible);
            if (labelVisible)
            {
                if (consequtiveVisiblePoints.size() == 2)
                {
                    labelPos = SimplePosition.midPoint(consequtiveVisiblePoints.get(0), consequtiveVisiblePoints.get(1));
                }
                else
                {
                    labelPos = new SimplePosition(visiblePoint.x - 20, visiblePoint.y - 20);
                }
                
                label.setPosition(new Position(PositionType.WORLD, new Scalar(labelPos.x), new Scalar(labelPos.y)));
                label.setText(route.getName());
                label.setStyle(labelStyle);
            }
        }

        @Override
        protected List<String> getBaseReferences()
        {
            return new ArrayList<String>(route.getPointNames());
        }

        /* (non-Javadoc)
         * @see com.soartech.shapesystem.Shape#hitTest(double, double, double)
         */
        @Override
        public boolean hitTest(double x, double y, double tolerance)
        {
            if(!isVisible())
            {
                return false;
            }
            
            if(cachedPoints.size() < 2)
            {
                return false;
            }
            
            SimplePosition last = null;
            for(SimplePosition p : cachedPoints)
            {
                if(last != null)
                {
                    Vector3 start = new Vector3(last.x, last.y, 0.0);
                    Vector3 end = new Vector3(p.x, p.y, 0.0);
                    Vector3 dir = end.subtract(start);
                    
                    double d = LineSegmentDistance.toPoint(start, end, dir, new Vector3(x, y, 0.0));
                    if(d < tolerance)
                    {
                        return true;
                    }
                }
                
                last = p;
            }
            
            return false;
        }

        @Override
        public double distance(double x, double y)
        {
            if(!isVisible())
            {
                return Double.MAX_VALUE;
            }
            
            if(cachedPoints.size() < 2)
            {
                return Double.MAX_VALUE;
            }
            
            SimplePosition last = null;
            double min = Double.MAX_VALUE;
            for(SimplePosition p : cachedPoints)
            {
                if(last != null)
                {
                    Vector3 start = new Vector3(last.x, last.y, 0.0);
                    Vector3 end = new Vector3(p.x, p.y, 0.0);
                    Vector3 dir = end.subtract(start);
                    
                    double d = LineSegmentDistance.toPoint(start, end, dir, new Vector3(x, y, 0.0));
                    if(d < min)
                    {
                        min = d;
                    }
                }
                
                last = p;
            }
            
            return min;
        }
        
    }
}
