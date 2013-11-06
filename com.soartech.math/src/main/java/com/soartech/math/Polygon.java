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
 * Created on Jun 18, 2006
 */
package com.soartech.math;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a convex polygon as an un-closed list of points in 
 * counter-clockwise order.
 * 
 * <p>Although points are stored as Vector3 objects, only the X and Y components
 * of the points are considered in the polygon.
 * 
 * @author ray
 */
public class Polygon
{
    List<Vector3> points = new ArrayList<Vector3>();

    /**
     * Create a polygon that is the convex hull of a collection of X-Y points.
     * 
     * @param hullPoints
     *            Input points that hull is calculated from. Only the X and Y
     *            coordinates of the points are considered.
     * @return A polygon that is the convex hull of a set of points
     */
    public static Polygon createConvexHull(Collection<Vector3> hullPoints)
    {
        // Filter out duplicates
        List<Vector3> points = new ArrayList<Vector3>();
        for(Vector3 v : hullPoints)
        {
            if(!points.contains(v))
            {
                points.add(v);
            }
        }

        // Hull algorithm does not work for points or line segments...
        if(points.size() < 3)
        {
            return new Polygon(points);
        }
        
        // Sort points for algorithm
        lexicalSort(points);
        
        // Perform convex hull algorithm
        points = andrewsChainHull(points);
        
        return new Polygon(points);
    }
    
    /**
     * Create a polygon that is the concave hull of a collection of X-Y points.
     * 
     * @param hullPoints
     *            An ordered list of input points that hull is calculated from. 
     *            Only the X and Y coordinates of the points are considered.
     * @return A polygon that is the concave hull of a set of points
     */
    public static Polygon createPolygon(Collection<Vector3> hullPoints)
    {
        return new Polygon(new ArrayList<Vector3>(hullPoints));
    }

    /**
     * @return Returns an unmodifiable list of the the points of the polygon.
     */
    public List<Vector3> getPoints()
    {
        return points;
    }

    /**
     * Returns true if the polygon contains the given point, ignoring the
     * point's Z coordinate.
     * 
     * @param point
     *            The point to test
     * @return True if the polygon contains the point
     */
    public boolean contains(Vector3 point)
    {
        double x = point.x;
        double y = point.y;
        int npol = points.size();

        int i, j;
        boolean c = false;
        for (i = 0, j = npol - 1; i < npol; j = i++)
        {
            Vector3 pi = points.get(i);
            Vector3 pj = points.get(j);
            if ((((pi.y <= y) && (y < pj.y)) || ((pj.y <= y) && (y < pi.y)))
                    && (x < (pj.x - pi.x) * (y - pi.y) / (pj.y - pi.y) + pi.x))
            {
                c = !c;
            }
        }
        return c;
    }

    /**
     * Clients should use the public factory methods above in lieue of this
     * private constructor.
     * 
     * @param points The points in the polygon
     */
    private Polygon(List<Vector3> points)
    {
        // Since this is private, points only comes from our factory methods
        // so we don't need to make a full copy. We make it unmodifiable though
        // so that clients don't try to change the list returned by getPoints()
        this.points = Collections.unmodifiableList(points);
    }

    // isLeft(): tests if a point is Left|On|Right of an infinite line.
    // Input: three points P0, P1, and P2
    // Return: >0 for P2 left of the line through P0 and P1
    // =0 for P2 on the line
    // <0 for P2 right of the line
    // See: the January 2001 Algorithm on Area of Triangles
    private static double isLeft(Vector3 P0, Vector3 P1, Vector3 P2)
    {
        return (P1.x - P0.x) * (P2.y - P0.y) - (P2.x - P0.x) * (P1.y - P0.y);
    }

    private static List<Vector3> asList(Vector3 H[], int max)
    {
        List<Vector3> returnList = new ArrayList<Vector3>(max);
        for (int j = 0; j < max; ++j)
        {
            returnList.add(H[j]);
        }
        return returnList;
    }
    
    private static class LexicalPointComparator implements Comparator<Vector3>
    {
        public int compare(Vector3 a, Vector3 b)
        {
            if(a.x > b.x)
            {
                return 1;
            }
            else if(a.x == b.x)
            {
                if(a.y > b.y)
                {
                    return 1;
                }
                else if(a.y == b.y)
                {
                    return 0;
                }
                else
                {
                    return -1;
                }
            }
            else
            {
                return -1;
            }
        }
    }
    
    private static final Comparator<Vector3> LEXICAL_POINT_COMPARATOR = 
        new LexicalPointComparator();
    
    /**
     * Performs a lexical sort of a set of point with increasing X, followed by
     * increasing Y.
     * 
     * @param points List of points, sorted in place
     */
    private static void lexicalSort(List<Vector3> points)
    {
        Collections.sort(points, LEXICAL_POINT_COMPARATOR);
    }

    /**
     * Andrew's monotone chain algorithm for calculating a 2D convex hull.
     * 
     * <p> Adapted from http://softsurfer.com/Archive/algorithm_0109/algorithm_0109.htm
     * 
     * @param input List of 2D points (X/Y) presorted by increasing X and 
     *          increasing Y.
     * @return List of points in the convex hull in counter-clockwise order.
     */
    private static List<Vector3> andrewsChainHull(List<Vector3> input)
    {
        int numPoints = input.size();
        
        // the output array H[] will be used as the stack
        Vector3 stack[] = new Vector3[input.size() * 2];

        int bot = 0, top = (-1); // indices for bottom and top of the stack
        int i; // array scan index

        // Get the indices of points with min x-coord and min|max y-coord
        double xmin = input.get(0).x;
        for (i = 1; i < numPoints; i++)
            if (input.get(i).x != xmin)
                break;
        
        int minmin = 0;
        int minmax = i - 1;
        
        if (minmax == numPoints - 1)
        { // degenerate case: all x-coords == xmin
            stack[++top] = input.get(minmin);
            if (input.get(minmax).y != input.get(minmin).y) // a nontrivial segment
                stack[++top] = input.get(minmax);
            // DR - we don't want a closed polygon
            //H[++top] = P.get(minmin); // add polygon endpoint
            return asList(stack, top + 1);
        }

        // Get the indices of points with max x-coord and min|max y-coord
        double xmax = input.get(numPoints - 1).x;
        for (i = numPoints - 2; i >= 0; i--)
            if (input.get(i).x != xmax)
                break;
        
        int maxmax = numPoints - 1;
        int maxmin = i + 1;

        // Compute the lower hull on the stack H
        stack[++top] = input.get(minmin); // push minmin point onto stack
        i = minmax;
        while (++i <= maxmin)
        {
            // the lower line joins P.get(minmin) with P.get(maxmin)
            if (isLeft(input.get(minmin), input.get(maxmin), input.get(i)) >= 0
                    && i < maxmin)
                continue; // ignore P.get(i) above or on the lower line

            while (top > 0) // there are at least 2 points on the stack
            {
                // test if P.get(i) is left of the line at the stack top
                if (isLeft(stack[top - 1], stack[top], input.get(i)) > 0)
                    break; // P.get(i) is a new hull vertex
                else
                    top--; // pop top point off stack
            }
            stack[++top] = input.get(i); // push P.get(i) onto stack
        }

        // Next, compute the upper hull on the stack H above the bottom hull
        if (maxmax != maxmin) // if distinct xmax points
            stack[++top] = input.get(maxmax); // push maxmax point onto stack
        bot = top; // the bottom point of the upper hull stack
        i = maxmin;
        while (--i >= minmax)
        {
            // the upper line joins P.get(maxmax) with P.get(minmax)
            if (isLeft(input.get(maxmax), input.get(minmax), input.get(i)) >= 0
                    && i > minmax)
                continue; // ignore P.get(i) below or on the upper line

            while (top > bot) // at least 2 points on the upper stack
            {
                // test if P.get(i) is left of the line at the stack top
                if (isLeft(stack[top - 1], stack[top], input.get(i)) > 0)
                    break; // P.get(i) is a new hull vertex
                else
                    top--; // pop top point off stack
            }
            stack[++top] = input.get(i); // push P.get(i] onto stack
        }
// DR - we don't want a closed polygon        
//        if (minmax != minmin)
//        {
//            H[++top] = P.get(minmin); // push joining endpoint onto stack
//        }

        // At this point stack contains the convex hull. Depending on minmax and
        // minmin we have to leave off the last point because it's a duplicate
        // of the first
        return asList(stack, minmax != minmin ? top + 1 : top);
    }

}
