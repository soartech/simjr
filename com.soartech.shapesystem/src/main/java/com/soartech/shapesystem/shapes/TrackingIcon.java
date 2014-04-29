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
 * Created on May 21, 2007
 */
package com.soartech.shapesystem.shapes;

import java.util.ArrayList;
import java.util.List;

import com.soartech.shapesystem.CoordinateTransformer;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.PrimitiveRenderer;
import com.soartech.shapesystem.PrimitiveRendererFactory;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.Shape;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.ShapeSystem;
import com.soartech.shapesystem.SimplePosition;
import com.soartech.shapesystem.SimpleRotation;

/**
 * @author ray
 */
public class TrackingIcon extends Shape
{

    private String image;
    private boolean alwaysShown;

    /**
     * 
     * @param name
     * @param layer
     * @param pos
     * @param rot
     * @param image
     * @param alwaysShown
     */
    public TrackingIcon(String name, String layer, Position pos, Rotation rot,
            String image, boolean alwaysShown)
    {
        super(name, layer, pos, rot, new ShapeStyle());
        
        this.image = image;
        this.alwaysShown = alwaysShown;
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#calculateBase(com.soartech.shapesystem.ShapeSystem, com.soartech.shapesystem.CoordinateTransformer)
     */
    @Override
    protected void calculateBase(ShapeSystem system,
            CoordinateTransformer transformer)
    {
        points.add(new SimplePosition());
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#draw(com.soartech.shapesystem.PrimitiveRendererFactory)
     */
    @Override
    public void draw(PrimitiveRendererFactory rendererFactory)
    {
        PrimitiveRenderer renderer = rendererFactory.createPrimitiveRenderer(style);
        // Grab the sizes of the image and screen that we're working with   
        final double padding  = 5.0;
        final double screen_w = renderer.getViewportWidth();
        final double screen_h = renderer.getViewportHeight();
        final double icon_w   = renderer.getImageWidth(image);
        final double icon_h   = renderer.getImageHeight(image);

        // Build border edges
        final double top    = 0        + (icon_h/2 + padding);
        final double left   = 0        + (icon_w/2 + padding);
        final double right  = screen_w - (icon_w/2 + padding);
        final double bottom = screen_h - (icon_h/2 + padding);

        // Build line segment from center of screen to tracked point
        final double x1 = points.get(0).x;
        final double y1 = points.get(0).y;
        final double x2 = screen_w / 2;
        final double y2 = screen_h / 2;

        // We can early terminate by checking if the tracked location
        // is inside the viewable screen area
        if (!alwaysShown && x1 > left && x1 < right && y1 > top && y1 < bottom)
        {
           return;
        }

        // Test against each edge of the screen
        final int edges = 4;
        SimplePosition edge[] = new SimplePosition[edges];

        edge[0] = find_intersection(x1, y1, x2, y2,     0.,    top, screen_w,      top);
        edge[1] = find_intersection(x1, y1, x2, y2,     0., bottom, screen_w,   bottom);
        edge[2] = find_intersection(x1, y1, x2, y2,  left,      0.,     left, screen_h);
        edge[3] = find_intersection(x1, y1, x2, y2, right,      0.,    right, screen_h);


        // Check for any hit and average the location of any hits we did
        // get (which  helps to smooth overlapping hits in the corners).
        boolean hit_edge = false;

        int hits = 0;
        double intersection_x = 0;
        double intersection_y = 0;
        for (int i = 0; i < edges; ++i)
        {
          hit_edge = hit_edge || edge[i] != null;

          if (edge[i] != null)
          {
              intersection_x += edge[i].x;
              intersection_y += edge[i].y;
              ++hits;
          }
        }

        if (hit_edge)
        {
           intersection_x /= hits;
           intersection_y /= hits;
        }
        else
        {
           // Just point our "intersected" coordinates at the tracked object's
           // coordinates if the thing is still on-screen.
           intersection_x = x1;
           intersection_y = y1;
        }

        // The intersection tests could cause an icon to dig too far
        // into a corner, so we clamp the values one last time
        intersection_x = Math.max(intersection_x,   left);
        intersection_x = Math.min(intersection_x,  right);
        intersection_y = Math.max(intersection_y,    top);
        intersection_y = Math.min(intersection_y, bottom);

        renderer.drawImage(new SimplePosition(intersection_x, intersection_y), 
                           SimpleRotation.fromDegrees(0), image);
        
    }

    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#getBaseReferences()
     */
    @Override
    protected List<String> getBaseReferences()
    {
        return new ArrayList<String>();
    }
    
    /* (non-Javadoc)
     * @see com.soartech.shapesystem.Shape#hitTest(double, double, double)
     */
    @Override
    public boolean hitTest(double x, double y, double tolerance)
    {
        // TODO implement mouse hit testing
        return false;
    }

    private SimplePosition find_intersection(double x1, double y1, 
                                               double x2, double y2, 
                                               double x3, double y3, 
                                               double x4, double y4)
     {
        double ua_numerator = (x4 - x3)*(y1 - y3) - (y4 - y3)*(x1 - x3);
        double ub_numerator = (x2 - x1)*(y1 - y3) - (y2 - y1)*(x1 - x3);
        double denominator  = (y4 - y3)*(x2 - x1) - (x4 - x3)*(y2 - y1);

        // A denominator of zero means the two line segments are parallel
        final double ep = 0.00001;
        if (denominator >= -ep && denominator <= ep) return null;
        
        double ua = ua_numerator / denominator;
        double ub = ub_numerator / denominator;

        // Check to see that an intersection occurs within the segments (and
        // not just on the infinite line).
        if (ua < 0 || ua > 1 || ub < 0 || ub > 1) return null;
        SimplePosition info = new SimplePosition();
        info.x = x1 + ua*(x2 - x1);
        info.y = y1 + ua*(y2 - y1);
        
        return info;
     }

}
