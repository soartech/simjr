/* The Government has SBIR Data rights to this software. All other parties have
 * no rights to use, distribute, reproduce, modify, reverse engineer, or 
 * otherwise utilize this software. 
 * 
 * All ownership rights are retained by Soar Technology, Inc.
 * 
 * (C)2015 SoarTech, Proprietary, All Rights Reserved.
 */

package com.soartech.math;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rdf
 *
 */
public class MinimumBoundingRectangle
{
    
    /**
     * Calculates a minimum bounding rectangle from a list of polygon points (end
     * point does not need to be repeated). Assumes all points come in with z=0 and
     * works on points only in the x,y plane.
     * 
     * @param polygon
     * @return polygon of 4 points representing the bounding rectangle polygon
     */
    public static Polygon calculateMBRforPolygon(List<Vector3> polyPoints)
    {
        Polygon convexhull = Polygon.createConvexHull(polyPoints);
        
        List<Vector3> points = convexhull.getPoints();
        double areaMin = Double.POSITIVE_INFINITY;
        double bestParMax = 0.;
        double bestParMin = 0.;
        double bestPerpMax = 0.;
        double bestPerpMin = 0.;
        Vector3 bestStartPt = null;
        Vector3 bestParVec = null;
        Vector3 bestPerpVec = null;
        
        for (int i=0; i < points.size(); i++)
        {
            Vector3 startpt = points.get(i);
            
            int endindex = (i+1) % points.size();
            Vector3 endpt = points.get(endindex);
            
            Vector3 parVec = endpt.subtract(startpt).normalized();
            Vector3 perpVec = new Vector3(parVec.y, -parVec.x, 0.);

            double parMax = Double.NEGATIVE_INFINITY;
            double parMin = Double.POSITIVE_INFINITY;
            double perpMax = Double.NEGATIVE_INFINITY;
            double perpMin = Double.POSITIVE_INFINITY;
            
            for (int j=0; j < points.size(); j++)
            {
                Vector3 curpt = points.get(j);
                Vector3 curvec = curpt.subtract(startpt);
                double par = curvec.dot(parVec);
                double perp = curvec.dot(perpVec);
                    
                parMax = Math.max(parMax, par);
                parMin = Math.min(parMin, par);
                perpMax = Math.max(perpMax, perp);
                perpMin = Math.min(perpMin,  perp);
            }
                        
            double area = (perpMax - perpMin) * (parMax - parMin);
            if ( area < areaMin )
            {
                areaMin = area;
                bestParMax = parMax;
                bestParMin = parMin;
                bestPerpMax = perpMax;
                bestPerpMin = perpMin;
                bestStartPt = startpt;
                bestParVec = parVec;
                bestPerpVec = perpVec;
            }
        }
        
        List<Vector3> retval = new ArrayList<Vector3>();
        retval.add(bestStartPt.add(bestParVec.multiply(bestParMin)).add(bestPerpVec.multiply(bestPerpMin)));
        retval.add(bestStartPt.add(bestParVec.multiply(bestParMin)).add(bestPerpVec.multiply(bestPerpMax)));
        retval.add(bestStartPt.add(bestParVec.multiply(bestParMax)).add(bestPerpVec.multiply(bestPerpMax)));
        retval.add(bestStartPt.add(bestParVec.multiply(bestParMax)).add(bestPerpVec.multiply(bestPerpMin)));
        
        return Polygon.createPolygon(retval);
    }
    
}
