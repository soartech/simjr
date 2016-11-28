/* The Government has SBIR Data rights to this software. All other parties have
 * no rights to use, distribute, reproduce, modify, reverse engineer, or 
 * otherwise utilize this software. 
 * 
 * All ownership rights are retained by Soar Technology, Inc.
 * 
 * (C)2015 SoarTech, Proprietary, All Rights Reserved.
 */

package com.soartech.math.utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.soartech.math.Vector3;

/**
 * @author rdf
 *
 */
public class PathInterpolaterTest
{

    @Test
    public void testBasicPathInterpolater()
    {
        List<Vector3> path = new ArrayList<Vector3>();
        path.add(new Vector3(0, 0, 0));
        path.add(new Vector3(1, 0, 0));
        path.add(new Vector3(1, 1, 0));
        path.add(new Vector3(0, 1, 0));
        path.add(new Vector3(0, 2, 0));
        
        double eps = 0.00001;
        PathInterpolater interp = new PathInterpolater(path);
        Assert.assertEquals(0, interp.getCurrentPointIndex());
        Assert.assertEquals(0.0, interp.getDistanceFromCurrentPoint(), eps);
        Assert.assertTrue(new Vector3(0, 0, 0).epsilonEquals(interp.getPosition()));
        
        interp.addDistance(0.1);
        Assert.assertEquals(0, interp.getCurrentPointIndex());
        Assert.assertEquals(0.1, interp.getDistanceFromCurrentPoint(), eps);
        Assert.assertTrue(new Vector3(0.1, 0, 0).epsilonEquals(interp.getPosition()));

        interp.addDistance(2.0);
        Assert.assertEquals(2, interp.getCurrentPointIndex());
        Assert.assertEquals(0.1, interp.getDistanceFromCurrentPoint(), eps);
        Assert.assertTrue(new Vector3(0.9, 1.0, 0).epsilonEquals(interp.getPosition()));

        interp.addDistance(1.0);
        Assert.assertEquals(3, interp.getCurrentPointIndex());
        Assert.assertEquals(0.1, interp.getDistanceFromCurrentPoint(), eps);
        Assert.assertTrue(new Vector3(0.0, 1.1, 0).epsilonEquals(interp.getPosition()));
        
        // This should put us a little past the end and should cap things
        interp.addDistance(1.0);
        Assert.assertEquals(4, interp.getCurrentPointIndex());
        Assert.assertEquals(0., interp.getDistanceFromCurrentPoint(), eps);
        Assert.assertTrue(new Vector3(0.0, 2, 0).epsilonEquals(interp.getPosition()));
        Assert.assertTrue(interp.isPathComplete());

        // This shouldn't change anything, we're already at the end
        interp.addDistance(1.0);
        Assert.assertEquals(4, interp.getCurrentPointIndex());
        Assert.assertEquals(0., interp.getDistanceFromCurrentPoint(), eps);
        Assert.assertTrue(new Vector3(0.0, 2, 0).epsilonEquals(interp.getPosition()));
        Assert.assertTrue(interp.isPathComplete());
    }
    
    @Test
    public void testWrapAroundPathInterpolator()
    {
        List<Vector3> path = new ArrayList<Vector3>();
        path.add(new Vector3(0, 0, 0));
        path.add(new Vector3(1, 0, 0));
        path.add(new Vector3(1, 1, 0));
        path.add(new Vector3(0, 1, 0));
        path.add(new Vector3(0, 2, 0));
        
        double eps = 0.00001;
        PathInterpolater interp = new PathInterpolater(path, true);
        Assert.assertEquals(0, interp.getCurrentPointIndex());
        Assert.assertEquals(0.0, interp.getDistanceFromCurrentPoint(), eps);
        Assert.assertTrue(new Vector3(0, 0, 0).epsilonEquals(interp.getPosition()));
        
        interp.addDistance(0.1);
        Assert.assertEquals(0, interp.getCurrentPointIndex());
        Assert.assertEquals(0.1, interp.getDistanceFromCurrentPoint(), eps);
        Assert.assertTrue(new Vector3(0.1, 0, 0).epsilonEquals(interp.getPosition()));

        interp.addDistance(2.0);
        Assert.assertEquals(2, interp.getCurrentPointIndex());
        Assert.assertEquals(0.1, interp.getDistanceFromCurrentPoint(), eps);
        Assert.assertTrue(new Vector3(0.9, 1.0, 0).epsilonEquals(interp.getPosition()));

        interp.addDistance(1.0);
        Assert.assertEquals(3, interp.getCurrentPointIndex());
        Assert.assertEquals(0.1, interp.getDistanceFromCurrentPoint(), eps);
        Assert.assertTrue(new Vector3(0.0, 1.1, 0).epsilonEquals(interp.getPosition()));
        
        // This should put us a little past the end and should cap things
        interp.addDistance(1.0);
        Assert.assertEquals(0, interp.getCurrentPointIndex());
        Assert.assertEquals(0.1, interp.getDistanceFromCurrentPoint(), eps);
        Assert.assertTrue(new Vector3(0.1, 0., 0).epsilonEquals(interp.getPosition()));

        // This shouldn't change anything, we're already at the end
        interp.addDistance(1.0);
        Assert.assertEquals(1, interp.getCurrentPointIndex());
        Assert.assertEquals(0.1, interp.getDistanceFromCurrentPoint(), eps);
        Assert.assertTrue(new Vector3(1.0, 0.1, 0).epsilonEquals(interp.getPosition()));        
    }
}
