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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ray
 */
public class ShapeSystem
{
    public static final int DEFAULT_ZORDER = 1;
    
    private List<Layer> layers = new ArrayList<Layer>();
    private boolean layersNeedSorting = false;
    private List<String> errors = new ArrayList<String>();
    private Map<String, Shape> shapeMap = new LinkedHashMap<String, Shape>();
    
    public class Layer implements Comparable<Layer>
    {
        private String name;
        private int zorder;
        private List<Shape> shapes = new ArrayList<Shape>();
        
        private Layer(String name, int zorder)
        {
            this.name = name;
            this.zorder = zorder;
        }
        
        @Override
        public String toString()
        {
            return name + "{ " + Arrays.toString(shapes.toArray(new Shape[shapes.size()])) + " }";
        }

        /**
         * @return the zorder
         */
        public int getZorder()
        {
            return zorder;
        }

        /**
         * @param zorder the zorder to set
         */
        public void setZorder(int zorder)
        {
            this.zorder = zorder;
            layersNeedSorting = true;
        }

        /**
         * @return the name
         */
        public String getName()
        {
            return name;
        }

        public int compareTo(Layer o)
        {
            return zorder - o.zorder;
        }
    }
    
    public Layer getLayer(String layerName)
    {
        for(Layer layer : layers)
        {
            if(layerName.equals(layer.name))
            {
                return layer;
            }
        }
        Layer layer = new Layer(layerName, DEFAULT_ZORDER);
        this.layers.add(layer);
        
        return layer;
    }
    
    public void update(CoordinateTransformer transformer)
    {
        errors.clear();
        
        for(Shape shape : shapeMap.values())
        {
            shape.reset();
        }
        
        // TODO: We shouldn't need to check this every frame
        //checkForDuplicateShapes();
        // TODO: checkForCircularReferences is just wrong. Fix it.
        //checkForCircularReferences();
        
        for(Shape shape : shapeMap.values())
        {
            shape.calculate(this, transformer);
        }
        
        if(layersNeedSorting)
        {
            Collections.sort(layers);
            layersNeedSorting = false;
        }
    }
    
    private List<Shape> getShapesInZorder(int low, int hi)
    {
        List<Shape> shapes = new ArrayList<Shape>();
        
        for(Layer layer : layers)
        {
            if(layer.zorder < low) continue;
            if(layer.zorder > hi) break;
            
            shapes.addAll(layer.shapes);
        }
        return shapes;
    }
    
    /**
     * Draw all shapes in all layers
     * 
     * @param rendererFactory
     */
    public void draw(PrimitiveRendererFactory rendererFactory)
    {
        draw(rendererFactory, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    
    /**
     * Draw all shapes in layers {@code [low, hi]}
     * 
     * @param rendererFactory
     * @param low
     * @param hi
     */
    public void draw(PrimitiveRendererFactory rendererFactory, int low, int hi)
    {
        if(low == Integer.MIN_VALUE && hi == Integer.MAX_VALUE)
        {
            // avoid getShapesInZorder copy cost
            for(Layer layer : layers)
            {
                for(Shape shape : layer.shapes)
                {
                    drawShape(rendererFactory, shape);
                }
            }
        }
        else
        {
            for(Shape shape : getShapesInZorder(low, hi))
            {
                drawShape(rendererFactory, shape);
            }
        }
        displayErrors(rendererFactory);
    }
    
    private void drawShape(PrimitiveRendererFactory rendererFactory, Shape shape)
    {
        if(!shape.problemThisFrame && shape.isVisible())
        {
            shape.draw(rendererFactory);
        }
    }
    
    public void displayErrors(PrimitiveRendererFactory rendererFactory)
    {
        if(errors.isEmpty())
        {
            return;
        }
        
        PrimitiveRenderer renderer = rendererFactory.createPrimitiveRenderer(new ShapeStyle());
        SimplePosition p = new SimplePosition(15.0, 15.0);
        renderer.drawText(p, "Shape system drawing errors:");
        
        for(String error : errors)
        {
            p.y += 16;
            renderer.drawText(p, error);
        }
    }
    
    public void displayDebugging(PrimitiveRendererFactory rendererFactory,
            CoordinateTransformer transformer)
    {
        PrimitiveRenderer renderer = rendererFactory
                .createPrimitiveRenderer(new ShapeStyle());

        SimplePosition p = new SimplePosition(505, 15);
        renderer.drawText(p, "Shape System Drawing Information:");

        for (Shape s : shapeMap.values())
        {
            // TODO: Make the font point-size more explicit here. (It's 14pt +
            // 2pt line spacing).
            p.y += 16;

            SimpleTransformation t = s.calculate(this, transformer);
            renderer.drawText(p, "shape: " + s.getName() + ", pos: ("
                    + t.position.x + ", " + t.position.y + "), rot: "
                    + t.rotation.inDegrees());
        }

    }
    
    public List<String> getErrors()
    {
        return new ArrayList<String>(errors);
    }
    
    public void error(String error)
    {
        errors.add(error);
    }
    
    public void errorInShape(Shape shape, String error)
    {
        shape.problemThisFrame = true;
        errors.add("In " + shape.getName() + ": " + error);
    }

    public void addShape(Shape shape)
    {
        if(null != shapeMap.put(shape.getName(), shape))
        {
            throw new IllegalArgumentException("Shape with name '" + shape.getName() + "' already registered");
        }
        getLayer(shape.getLayer()).shapes.add(shape);
    }
    
    public Shape getShape(String shapeId)
    {
        return shapeMap.get(shapeId);
    }
    
    public void removeShape(String shapeId)
    {
        Shape shape = shapeMap.remove(shapeId);
        if(shape != null)
        {
            getLayer(shape.getLayer()).shapes.remove(shape);
        }
    }
    
    private void checkForCircularReferences()
    {
        for(Shape shape : shapeMap.values())
        {
            findCycles(shape);
        }
    }
    
    private void findCycles(Shape shape)
    {
        Set<Shape> lineage = new HashSet<Shape>();
        lineage.add(shape);
        
        Shape found = recursiveFindCycle(shape, lineage);
        while(found != null)
        {
            shape.problemThisFrame = true;
            found.problemThisFrame = true;
            
            errorInShape(shape, "Circular reference detected!");
            
            found = recursiveFindCycle(shape, lineage);
        }
        
    }
    
    private Shape recursiveFindCycle(Shape shape, Set<Shape> lineage)
    {
        if(shape == null || shape.problemThisFrame)
        {
            return null;
        }
        
        Set<String> refs = new HashSet<String>(shape.getReferences());
        
        for(String refId : refs)
        {
            Shape ref = getShape(refId);
            if(ref == null || ref.problemThisFrame)
            {
                continue;
            }
            
            if(!lineage.add(ref))
            {
                return ref;
            }
            
            Shape found = recursiveFindCycle(ref, lineage);
            if(found != null)
            {
                return found;
            }
        }
        return null;
    }
}
