package com.soartech.shapesystem.shapes;

import java.util.List;

import com.google.common.collect.Lists;
import com.soartech.math.Polygon;
import com.soartech.math.Vector3;
import com.soartech.shapesystem.SimplePosition;

public class Util
{
    /**
     * Creates a {@link Polygon} that is the convex hull of the points
     * projected on the <code>z = 0.0</code> plane.
     */
    protected static Polygon createPlanarContextHull(List<SimplePosition> points)
    {
        List<Vector3> hullPoints = Lists.newArrayList();
        for(SimplePosition p : points)
        {
            hullPoints.add(new Vector3(p.x, p.y, 0.0));
        }
        Polygon p = Polygon.createConvexHull(hullPoints);
        return p;
    }
}
