/* The Government has SBIR Data rights to this software. All other parties have
 * no rights to use, distribute, reproduce, modify, reverse engineer, or 
 * otherwise utilize this software. 
 * 
 * All ownership rights are retained by Soar Technology, Inc.
 * 
 * (C)2015 SoarTech, Proprietary, All Rights Reserved.
 */

package com.soartech.math.utils;

import java.util.List;

import com.soartech.math.Vector3;

/**
 * Helper class that handles path interpolation book keeping. By maintaining
 * a current index of last point in the path reached and a distance from
 * it towards the end of the path.
 * 
 * @author rdf
 *
 */
public class PathInterpolater
{
    private List<Vector3> path;
    private int currentPtIndex = 0;
    private double distFromCurPt = 0.0;
    private boolean wraparound = false;
    
    public PathInterpolater(List<Vector3> path)
    {
        this(path, false);
    }
    
    public PathInterpolater(List<Vector3> path, boolean wraparound)
    {
        this.path = path;
        this.wraparound = wraparound;
    }
    
    private void updateIndexAndDistance()
    {
        if ( isPathComplete() )
        {
            // If we're on we've finished the last segment 
            // the distance is always 0, we're at the end.
            this.distFromCurPt = 0.0;
        }
        else
        {
            double curSegDist = getCurrentSegmentDistance();
            if ( this.distFromCurPt > curSegDist )
            {
                currentPtIndex += 1;
                if ( wraparound && currentPtIndex >= path.size()-1 )
                {
                    currentPtIndex = 0;
                }
                
                distFromCurPt -= curSegDist;
                // Recursively see if we've also completed the next segment
                updateIndexAndDistance();
            }
        }
    }
    
    public int getCurrentPointIndex()
    {
        return this.currentPtIndex;
    }
    
    public double getDistanceFromCurrentPoint()
    {
        return this.distFromCurPt;
    }
    
    public boolean isPathComplete()
    {
        return currentPtIndex >= path.size()-1;
    }
    
    public Vector3 getPosition()
    {
        if ( isPathComplete() )
        {
            return path.get(path.size()-1);
        }
        else
        {
            Vector3 startPt = path.get(currentPtIndex);
            Vector3 endPt = path.get(currentPtIndex+1);
            Vector3 pathnorm = endPt.subtract(startPt).normalized();
            return startPt.add(pathnorm.multiply(distFromCurPt));
        }
        
    }
    
    private double getCurrentSegmentDistance()
    {
        if ( isPathComplete() )
        {
            return 0.;
        }
        else
        {
            Vector3 startPt = path.get(currentPtIndex);
            Vector3 endPt = path.get(currentPtIndex+1);
            return startPt.distance(endPt);
        }
    }

    /**
     * @param d
     */
    public void addDistance(double dist)
    {
        this.distFromCurPt += dist;
        this.updateIndexAndDistance();
    }

    /**
     * 
     */
    public void reset()
    {
        currentPtIndex = 0;
        distFromCurPt = 0.0;
    }
    

}
