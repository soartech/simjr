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
import java.util.Vector;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author rdf
 *
 */
public class MinimumBoundingRectangleTest
{

    @Test
    public void testUnitSquare()
    {
        List<Vector3> unitSquare = new ArrayList<Vector3>();
        unitSquare.add(new Vector3(0.,0.,0.));
        unitSquare.add(new Vector3(1.,0.,0.));
        unitSquare.add(new Vector3(1.,1.,0.));
        unitSquare.add(new Vector3(0.,1.,0.));
        
        Polygon mbr = MinimumBoundingRectangle.calculateMBRforPolygon(unitSquare);
        
        Assert.assertEquals(4, mbr.getPoints().size());

        Assert.assertEquals(new Vector3(0.0,1.0,0.0), mbr.getPoints().get(0));
        Assert.assertEquals(new Vector3(0.0,0.0,0.0), mbr.getPoints().get(1));
        Assert.assertEquals(new Vector3(1.0,0.0,0.0), mbr.getPoints().get(2));
        Assert.assertEquals(new Vector3(1.0,1.0,0.0), mbr.getPoints().get(3));
    }

    @Test
    public void testRotatedSquare()
    {
        List<Vector3> unitSquare = new ArrayList<Vector3>();
        unitSquare.add(new Vector3(0.,1.,0.));
        unitSquare.add(new Vector3(1.,0.,0.));
        unitSquare.add(new Vector3(0.,-1.,0.));
        unitSquare.add(new Vector3(-1.,0.,0.));
        
        Polygon mbr = MinimumBoundingRectangle.calculateMBRforPolygon(unitSquare);
        
        Assert.assertEquals(4, mbr.getPoints().size());

        double eps = 1.e-8;
        Assert.assertTrue(new Vector3(0.0,1.0,0.0).epsilonEquals(mbr.getPoints().get(0), eps));
        Assert.assertTrue(new Vector3(-1.0,0.0,0.0).epsilonEquals(mbr.getPoints().get(1), eps));
        Assert.assertTrue(new Vector3(0.0,-1.0,0.0).epsilonEquals(mbr.getPoints().get(2), eps));
        Assert.assertTrue(new Vector3(1.0,0.0,0.0).epsilonEquals(mbr.getPoints().get(3), eps));
    }

    @Test
    public void testParallelogram()
    {
        List<Vector3> unitSquare = new ArrayList<Vector3>();
        unitSquare.add(new Vector3(0.,0.,0.));
        unitSquare.add(new Vector3(100.,0.,0.));
        unitSquare.add(new Vector3(101.,1.,0.));
        unitSquare.add(new Vector3(1.,1.,0.));
        
        Polygon mbr = MinimumBoundingRectangle.calculateMBRforPolygon(unitSquare);
        
        Assert.assertEquals(4, mbr.getPoints().size());

        double eps = 1.e-8;
        Assert.assertTrue(new Vector3(0.0,1.0,0.0).epsilonEquals(mbr.getPoints().get(0), eps));
        Assert.assertTrue(new Vector3(0.0,0.0,0.0).epsilonEquals(mbr.getPoints().get(1), eps));
        Assert.assertTrue(new Vector3(101.0,0.0,0.0).epsilonEquals(mbr.getPoints().get(2), eps));
        Assert.assertTrue(new Vector3(101,1.0,0.0).epsilonEquals(mbr.getPoints().get(3), eps));        
    }
}
