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
 * Created on Jun 15, 2006
 */
package com.soartech.math;

/**
 * Implements algorithm that calculates minimum distance between to 3
 * dimensional line segments.
 * 
 * <p>
 * Adapted from code at http://www.geometrictools.com/Intersection.html
 * 
 * @author ray
 */
public class LineSegmentDistance
{
    /**
     * Calculates the minumim distance between a point and a line segment.
     * 
     * @param start The start of the line segment
     * @param end The end of the line segment
     * @param direction The <b>unnormalized</b> direction of the line segment, i.e.
     *          {@code end} - {@code start}. This is for efficiency purposes
     *          because this value can easily be cached to avoid frequent
     *          recalculation. If not set correctly, results are undefined.
     * @param point The point to test
     * @return The minimum distance between {@code point} and the line segment
     *      defined by {@code start} and {@code end}.
     */
    public static double toPoint(Vector3 start, Vector3 end, Vector3 direction, Vector3 point)
    {       
        // If the line segment is degenerate, just return distance to start
        // point to avoid divide by zero below.
        if(direction.epsilonEquals(Vector3.ZERO))
        {
            return point.distance(start);
        }
        
        double u = direction.dot(point.subtract(start)) / direction.lengthSquared();
        double d = 0.0;
        if(u <= 0)
        {
            // start of line segment
            d = point.distance(start);
        }
        else if(u >= 1)
        {
            // end of line segment
            d = point.distance(end);
        }
        else
        {
            // interpolate along line segment
            d = point.distance(start.add(direction.multiply(u)));
        }
        
        return d;
    }
    
    
    /**
     * Calculate the minimum distance between two line segments
     * 
     * @param aStart
     *            Start of first line segment
     * @param aEnd
     *            End of first line segment
     * @param bStart
     *            Start of second line segment
     * @param bEnd
     *            End of second line segment
     * @return Minimum distance between two segments
     */
    public static double calculate(Vector3 aStart, Vector3 aEnd,
            Vector3 bStart, Vector3 bEnd)
    {
        LineSegmentDistance dist = new LineSegmentDistance(aStart, aEnd,
                bStart, bEnd);

        return dist.get();
    }

    
    // THE CODE BELOW HERE IS UGLY BECAUSE IT IS ADAPTED FROM C++.
    // It's all private so we'll leave it that way until we need more of an
    // API exposed.
    private static final double ZERO_TOLERANCE = 1e-08;

    private static class LineSegment
    {
        Vector3 Origin; // Midpoint of line segment

        Vector3 Direction; // Unit direction vector

        double Extent; // Extent of segment

        // start = Origin - Extent * Direction
        // end = Origin - Extent * Direction

        public LineSegment(Vector3 start, Vector3 end)
        {
            this.Origin = start.add(end).multiply(0.5); // midpoint
            Vector3 diff = end.subtract(start);
            this.Direction = diff.normalized();
            this.Extent = diff.length() / 2.0;
        }

        public LineSegment(Vector3 origin, Vector3 direction, double extent)
        {
            this.Origin = origin;
            this.Direction = direction;
            this.Extent = extent;
        }
    }

    private LineSegment m_rkSegment0;

    private LineSegment m_rkSegment1;


    // ----------------------------------------------------------------------------

    private LineSegmentDistance(Vector3 aStart, Vector3 aEnd, Vector3 bStart,
            Vector3 bEnd)
    {
        m_rkSegment0 = new LineSegment(aStart, aEnd);
        m_rkSegment1 = new LineSegment(bStart, bEnd);
    }

    // ----------------------------------------------------------------------------

    private double get()
    {
        double fSqrDist = getSquared();
        return Math.sqrt(fSqrDist);
    }

    // ----------------------------------------------------------------------------

    private double getSquared()
    {
        Vector3 kDiff = m_rkSegment0.Origin.subtract(m_rkSegment1.Origin);
        double fA01 = m_rkSegment0.Direction.multiply(-1).dot(
                m_rkSegment1.Direction);
        double fB0 = kDiff.dot(m_rkSegment0.Direction);
        double fB1 = kDiff.multiply(-1).dot(m_rkSegment1.Direction);
        double fC = kDiff.lengthSquared();
        double fDet = Math.abs(1.0 - fA01 * fA01);
        double fS0, fS1, fSqrDist, fExtDet0, fExtDet1, fTmpS0, fTmpS1;

        if (fDet >= ZERO_TOLERANCE)
        {
            // segments are not parallel
            fS0 = fA01 * fB1 - fB0;
            fS1 = fA01 * fB0 - fB1;
            fExtDet0 = m_rkSegment0.Extent * fDet;
            fExtDet1 = m_rkSegment1.Extent * fDet;

            if (fS0 >= -fExtDet0)
            {
                if (fS0 <= fExtDet0)
                {
                    if (fS1 >= -fExtDet1)
                    {
                        if (fS1 <= fExtDet1) // region 0 (interior)
                        {
                            // minimum at two interior points of 3D lines
                            double fInvDet = (1.0) / fDet;
                            fS0 *= fInvDet;
                            fS1 *= fInvDet;
                            fSqrDist = fS0 * (fS0 + fA01 * fS1 + (2.0) * fB0)
                                    + fS1 * (fA01 * fS0 + fS1 + (2.0) * fB1)
                                    + fC;
                        }
                        else
                        // region 3 (side)
                        {
                            fS1 = m_rkSegment1.Extent;
                            fTmpS0 = -(fA01 * fS1 + fB0);
                            if (fTmpS0 < -m_rkSegment0.Extent)
                            {
                                fS0 = -m_rkSegment0.Extent;
                                fSqrDist = fS0 * (fS0 - (2.0) * fTmpS0) + fS1
                                        * (fS1 + (2.0) * fB1) + fC;
                            }
                            else if (fTmpS0 <= m_rkSegment0.Extent)
                            {
                                fS0 = fTmpS0;
                                fSqrDist = -fS0 * fS0 + fS1
                                        * (fS1 + (2.0) * fB1) + fC;
                            }
                            else
                            {
                                fS0 = m_rkSegment0.Extent;
                                fSqrDist = fS0 * (fS0 - (2.0) * fTmpS0) + fS1
                                        * (fS1 + (2.0) * fB1) + fC;
                            }
                        }
                    }
                    else
                    // region 7 (side)
                    {
                        fS1 = -m_rkSegment1.Extent;
                        fTmpS0 = -(fA01 * fS1 + fB0);
                        if (fTmpS0 < -m_rkSegment0.Extent)
                        {
                            fS0 = -m_rkSegment0.Extent;
                            fSqrDist = fS0 * (fS0 - (2.0) * fTmpS0) + fS1
                                    * (fS1 + (2.0) * fB1) + fC;
                        }
                        else if (fTmpS0 <= m_rkSegment0.Extent)
                        {
                            fS0 = fTmpS0;
                            fSqrDist = -fS0 * fS0 + fS1 * (fS1 + (2.0) * fB1)
                                    + fC;
                        }
                        else
                        {
                            fS0 = m_rkSegment0.Extent;
                            fSqrDist = fS0 * (fS0 - (2.0) * fTmpS0) + fS1
                                    * (fS1 + (2.0) * fB1) + fC;
                        }
                    }
                }
                else
                {
                    if (fS1 >= -fExtDet1)
                    {
                        if (fS1 <= fExtDet1) // region 1 (side)
                        {
                            fS0 = m_rkSegment0.Extent;
                            fTmpS1 = -(fA01 * fS0 + fB1);
                            if (fTmpS1 < -m_rkSegment1.Extent)
                            {
                                fS1 = -m_rkSegment1.Extent;
                                fSqrDist = fS1 * (fS1 - (2.0) * fTmpS1) + fS0
                                        * (fS0 + (2.0) * fB0) + fC;
                            }
                            else if (fTmpS1 <= m_rkSegment1.Extent)
                            {
                                fS1 = fTmpS1;
                                fSqrDist = -fS1 * fS1 + fS0
                                        * (fS0 + (2.0) * fB0) + fC;
                            }
                            else
                            {
                                fS1 = m_rkSegment1.Extent;
                                fSqrDist = fS1 * (fS1 - (2.0) * fTmpS1) + fS0
                                        * (fS0 + (2.0) * fB0) + fC;
                            }
                        }
                        else
                        // region 2 (corner)
                        {
                            fS1 = m_rkSegment1.Extent;
                            fTmpS0 = -(fA01 * fS1 + fB0);
                            if (fTmpS0 < -m_rkSegment0.Extent)
                            {
                                fS0 = -m_rkSegment0.Extent;
                                fSqrDist = fS0 * (fS0 - (2.0) * fTmpS0) + fS1
                                        * (fS1 + (2.0) * fB1) + fC;
                            }
                            else if (fTmpS0 <= m_rkSegment0.Extent)
                            {
                                fS0 = fTmpS0;
                                fSqrDist = -fS0 * fS0 + fS1
                                        * (fS1 + (2.0) * fB1) + fC;
                            }
                            else
                            {
                                fS0 = m_rkSegment0.Extent;
                                fTmpS1 = -(fA01 * fS0 + fB1);
                                if (fTmpS1 < -m_rkSegment1.Extent)
                                {
                                    fS1 = -m_rkSegment1.Extent;
                                    fSqrDist = fS1 * (fS1 - (2.0) * fTmpS1)
                                            + fS0 * (fS0 + (2.0) * fB0) + fC;
                                }
                                else if (fTmpS1 <= m_rkSegment1.Extent)
                                {
                                    fS1 = fTmpS1;
                                    fSqrDist = -fS1 * fS1 + fS0
                                            * (fS0 + (2.0) * fB0) + fC;
                                }
                                else
                                {
                                    fS1 = m_rkSegment1.Extent;
                                    fSqrDist = fS1 * (fS1 - (2.0) * fTmpS1)
                                            + fS0 * (fS0 + (2.0) * fB0) + fC;
                                }
                            }
                        }
                    }
                    else
                    // region 8 (corner)
                    {
                        fS1 = -m_rkSegment1.Extent;
                        fTmpS0 = -(fA01 * fS1 + fB0);
                        if (fTmpS0 < -m_rkSegment0.Extent)
                        {
                            fS0 = -m_rkSegment0.Extent;
                            fSqrDist = fS0 * (fS0 - (2.0) * fTmpS0) + fS1
                                    * (fS1 + (2.0) * fB1) + fC;
                        }
                        else if (fTmpS0 <= m_rkSegment0.Extent)
                        {
                            fS0 = fTmpS0;
                            fSqrDist = -fS0 * fS0 + fS1 * (fS1 + (2.0) * fB1)
                                    + fC;
                        }
                        else
                        {
                            fS0 = m_rkSegment0.Extent;
                            fTmpS1 = -(fA01 * fS0 + fB1);
                            if (fTmpS1 > m_rkSegment1.Extent)
                            {
                                fS1 = m_rkSegment1.Extent;
                                fSqrDist = fS1 * (fS1 - (2.0) * fTmpS1) + fS0
                                        * (fS0 + (2.0) * fB0) + fC;
                            }
                            else if (fTmpS1 >= -m_rkSegment1.Extent)
                            {
                                fS1 = fTmpS1;
                                fSqrDist = -fS1 * fS1 + fS0
                                        * (fS0 + (2.0) * fB0) + fC;
                            }
                            else
                            {
                                fS1 = -m_rkSegment1.Extent;
                                fSqrDist = fS1 * (fS1 - (2.0) * fTmpS1) + fS0
                                        * (fS0 + (2.0) * fB0) + fC;
                            }
                        }
                    }
                }
            }
            else
            {
                if (fS1 >= -fExtDet1)
                {
                    if (fS1 <= fExtDet1) // region 5 (side)
                    {
                        fS0 = -m_rkSegment0.Extent;
                        fTmpS1 = -(fA01 * fS0 + fB1);
                        if (fTmpS1 < -m_rkSegment1.Extent)
                        {
                            fS1 = -m_rkSegment1.Extent;
                            fSqrDist = fS1 * (fS1 - (2.0) * fTmpS1) + fS0
                                    * (fS0 + (2.0) * fB0) + fC;
                        }
                        else if (fTmpS1 <= m_rkSegment1.Extent)
                        {
                            fS1 = fTmpS1;
                            fSqrDist = -fS1 * fS1 + fS0 * (fS0 + (2.0) * fB0)
                                    + fC;
                        }
                        else
                        {
                            fS1 = m_rkSegment1.Extent;
                            fSqrDist = fS1 * (fS1 - (2.0) * fTmpS1) + fS0
                                    * (fS0 + (2.0) * fB0) + fC;
                        }
                    }
                    else
                    // region 4 (corner)
                    {
                        fS1 = m_rkSegment1.Extent;
                        fTmpS0 = -(fA01 * fS1 + fB0);
                        if (fTmpS0 > m_rkSegment0.Extent)
                        {
                            fS0 = m_rkSegment0.Extent;
                            fSqrDist = fS0 * (fS0 - (2.0) * fTmpS0) + fS1
                                    * (fS1 + (2.0) * fB1) + fC;
                        }
                        else if (fTmpS0 >= -m_rkSegment0.Extent)
                        {
                            fS0 = fTmpS0;
                            fSqrDist = -fS0 * fS0 + fS1 * (fS1 + (2.0) * fB1)
                                    + fC;
                        }
                        else
                        {
                            fS0 = -m_rkSegment0.Extent;
                            fTmpS1 = -(fA01 * fS0 + fB1);
                            if (fTmpS1 < -m_rkSegment1.Extent)
                            {
                                fS1 = -m_rkSegment1.Extent;
                                fSqrDist = fS1 * (fS1 - (2.0) * fTmpS1) + fS0
                                        * (fS0 + (2.0) * fB0) + fC;
                            }
                            else if (fTmpS1 <= m_rkSegment1.Extent)
                            {
                                fS1 = fTmpS1;
                                fSqrDist = -fS1 * fS1 + fS0
                                        * (fS0 + (2.0) * fB0) + fC;
                            }
                            else
                            {
                                fS1 = m_rkSegment1.Extent;
                                fSqrDist = fS1 * (fS1 - (2.0) * fTmpS1) + fS0
                                        * (fS0 + (2.0) * fB0) + fC;
                            }
                        }
                    }
                }
                else
                // region 6 (corner)
                {
                    fS1 = -m_rkSegment1.Extent;
                    fTmpS0 = -(fA01 * fS1 + fB0);
                    if (fTmpS0 > m_rkSegment0.Extent)
                    {
                        fS0 = m_rkSegment0.Extent;
                        fSqrDist = fS0 * (fS0 - (2.0) * fTmpS0) + fS1
                                * (fS1 + (2.0) * fB1) + fC;
                    }
                    else if (fTmpS0 >= -m_rkSegment0.Extent)
                    {
                        fS0 = fTmpS0;
                        fSqrDist = -fS0 * fS0 + fS1 * (fS1 + (2.0) * fB1) + fC;
                    }
                    else
                    {
                        fS0 = -m_rkSegment0.Extent;
                        fTmpS1 = -(fA01 * fS0 + fB1);
                        if (fTmpS1 < -m_rkSegment1.Extent)
                        {
                            fS1 = -m_rkSegment1.Extent;
                            fSqrDist = fS1 * (fS1 - (2.0) * fTmpS1) + fS0
                                    * (fS0 + (2.0) * fB0) + fC;
                        }
                        else if (fTmpS1 <= m_rkSegment1.Extent)
                        {
                            fS1 = fTmpS1;
                            fSqrDist = -fS1 * fS1 + fS0 * (fS0 + (2.0) * fB0)
                                    + fC;
                        }
                        else
                        {
                            fS1 = m_rkSegment1.Extent;
                            fSqrDist = fS1 * (fS1 - (2.0) * fTmpS1) + fS0
                                    * (fS0 + (2.0) * fB0) + fC;
                        }
                    }
                }
            }
        }
        else
        {
            // segments are parallel
            double fE0pE1 = m_rkSegment0.Extent + m_rkSegment1.Extent;
            double fSign = (fA01 > 0.0 ? -1.0 : 1.0);
            double fLambda = -fB0;
            if (fLambda < -fE0pE1)
            {
                fLambda = -fE0pE1;
            }
            else if (fLambda > fE0pE1)
            {
                fLambda = fE0pE1;
            }

            fS1 = fSign * fB0 * m_rkSegment1.Extent / fE0pE1;
            fS0 = fLambda + fSign * fS1;
            fSqrDist = fLambda * (fLambda + (2.0) * fB0) + fC;
        }

        /*
        m_kClosestPoint0 = m_rkSegment0.Origin.add(m_rkSegment0.Direction
                .multiply(fS0));
        m_kClosestPoint1 = m_rkSegment1.Origin.add(m_rkSegment1.Direction
                .multiply(fS1));
        m_fSegment0Parameter = fS0;
        m_fSegment1Parameter = fS1;
        */
        return Math.abs(fSqrDist);
    }

    //    ----------------------------------------------------------------------------

}
