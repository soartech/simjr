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
 * Created on Jun 9, 2006
 */
package com.soartech.math.geotrans;

import com.soartech.math.Vector3;

/**
 * @author ray
 */
public class Geocentric extends GeoTransBase
{
    private static final double COS_67P5 = 0.38268343236508977; /*
                                                                 * cosine of
                                                                 * 67.5 degrees
                                                                 */

    private static final double AD_C = 1.0026000; /* Toms region 1 constant */

    /** ************************************************************************ */
    /*
     * GLOBAL DECLARATIONS
     */
    /* Ellipsoid parameters, default to WGS 84 */
    private double Geocent_a = GeoTransConstants.WGS84_SEMI_MAJOR_AXIS; /*
                                             * Semi-major axis of ellipsoid in
                                             * meters
                                             */

    private double Geocent_f = GeoTransConstants.WGS84_FLATTENING; /* Flattening of ellipsoid */

    private double Geocent_e2 = 0.0066943799901413800; /* Eccentricity squared */

    private double Geocent_ep2 = 0.00673949675658690300; /*
                                                             * 2nd eccentricity
                                                             * squared
                                                             */

    /*
     * These state variables are for optimization purposes. The only function
     * that should modify them is Set_Geocentric_Parameters.
     */

    public Geocentric()
    {

    }

    /**
     * 
     */
    public Geocentric(double a, double f)
    { /* BEGIN Set_Geocentric_Parameters */
        /*
         * The function Set_Geocentric_Parameters receives the ellipsoid
         * parameters as inputs and sets the corresponding state variables.
         * 
         * a : Semi-major axis of ellipsoid, in meters. (input) f : Flattening
         * of ellipsoid. (input)
         */

        double inv_f = 1 / f;

        if (a <= 0.0)
        {
            throw new IllegalArgumentException("a must be positive");
        }
        if ((inv_f < 250) || (inv_f > 350))
        {
            throw new IllegalArgumentException(
                    "Inverse flattening must be between 250 and 350");
        }
        Geocent_a = a;
        Geocent_f = f;
        Geocent_e2 = 2 * Geocent_f - Geocent_f * Geocent_f;
        Geocent_ep2 = (1 / (1 - Geocent_e2)) - 1;

    } /* END OF Set_Geocentric_Parameters */

    /*
     * The function Get_Geocentric_Parameters returns the ellipsoid parameters
     * to be used in geocentric coordinate conversions.
     * 
     * a : Semi-major axis of ellipsoid, in meters. (output) f : Flattening of
     * ellipsoid. (output)
     */
    public double getA()
    {
        return Geocent_a;
    }

    public double getF()
    {
        return Geocent_f;
    }

    public Vector3 fromGeodetic(double Latitude, double Longitude,
            double Height)
    { /* BEGIN Convert_Geodetic_To_Geocentric */
        /*
         * The function Convert_Geodetic_To_Geocentric converts geodetic
         * coordinates (latitude, longitude, and height) to geocentric
         * coordinates (X, Y, Z), according to the current ellipsoid parameters.
         * 
         * Latitude : Geodetic latitude in radians (input) Longitude : Geodetic
         * longitude in radians (input) Height : Geodetic height, in meters
         * (input) X : Calculated Geocentric X coordinate, in meters (output) Y :
         * Calculated Geocentric Y coordinate, in meters (output) Z : Calculated
         * Geocentric Z coordinate, in meters (output)
         * 
         */
        double X, Y, Z;
        double Rn; /* Earth radius at location */
        double Sin_Lat; /* sin(Latitude) */
        double Sin2_Lat; /* Square of sin(Latitude) */
        double Cos_Lat; /* cos(Latitude) */

        if ((Latitude < -PI_OVER_2) || (Latitude > PI_OVER_2))
        { /* Latitude out of range */
            throw new IllegalArgumentException("Latitude out of range");
        }
        if ((Longitude < -PI) || (Longitude > (2 * PI)))
        { /* Longitude out of range */
            throw new IllegalArgumentException("Longitude out of range");

        }
        if (Longitude > PI)
            Longitude -= (2 * PI);
        Sin_Lat = sin(Latitude);
        Cos_Lat = cos(Latitude);
        Sin2_Lat = Sin_Lat * Sin_Lat;
        Rn = Geocent_a / (sqrt(1.0e0 - Geocent_e2 * Sin2_Lat));
        X = (Rn + Height) * Cos_Lat * cos(Longitude);
        Y = (Rn + Height) * Cos_Lat * sin(Longitude);
        Z = ((Rn * (1 - Geocent_e2)) + Height) * Sin_Lat;

        return new Vector3(X, Y, Z);
    } /* END OF Convert_Geodetic_To_Geocentric */

    public Geodetic.Point toGeodetic(double X, double Y, double Z)
    { /* BEGIN Convert_Geocentric_To_Geodetic */
        /*
         * The function Convert_Geocentric_To_Geodetic converts geocentric
         * coordinates (X, Y, Z) to geodetic coordinates (latitude, longitude,
         * and height), according to the current ellipsoid parameters.
         * 
         * X : Geocentric X coordinate, in meters. (input) Y : Geocentric Y
         * coordinate, in meters. (input) Z : Geocentric Z coordinate, in
         * meters. (input) Latitude : Calculated latitude value in radians.
         * (output) Longitude : Calculated longitude value in radians. (output)
         * Height : Calculated height value, in meters. (output)
         * 
         * The method used here is derived from 'An Improved Algorithm for
         * Geocentric to Geodetic Coordinate Conversion', by Ralph Toms, Feb
         * 1996
         */

        /* Note: Variable names follow the notation used in Toms, Feb 1996 */

        double W; /* distance from Z axis */
        double W2; /* square of distance from Z axis */
        double T0; /* initial estimate of vertical component */
        double T1; /* corrected estimate of vertical component */
        double S0; /* initial estimate of horizontal component */
        double S1; /* corrected estimate of horizontal component */
        double Sin_B0; /* sin(B0), B0 is estimate of Bowring aux variable */
        double Sin3_B0; /* cube of sin(B0) */
        double Cos_B0; /* cos(B0) */
        double Sin_p1; /* sin(phi1), phi1 is estimated latitude */
        double Cos_p1; /* cos(phi1) */
        double Rn; /* Earth radius at location */
        double Sum; /* numerator of cos(phi1) */
        boolean At_Pole = false; /* indicates location is in polar region */
        double Geocent_b = Geocent_a * (1 - Geocent_f); /*
                                                         * Semi-minor axis of
                                                         * ellipsoid, in meters
                                                         */

        Geodetic.Point point = new Geodetic.Point();

        if (X != 0.0)
        {
            point.longitude = atan2(Y, X);
        }
        else
        {
            if (Y > 0)
            {
                point.longitude = PI_OVER_2;
            }
            else if (Y < 0)
            {
                point.longitude = -PI_OVER_2;
            }
            else
            {
                At_Pole = true;
                point.longitude = 0.0;
                if (Z > 0.0)
                { /* north pole */
                    point.latitude = PI_OVER_2;
                }
                else if (Z < 0.0)
                { /* south pole */
                    point.latitude = -PI_OVER_2;
                }
                else
                { /* center of earth */
                    point.latitude = PI_OVER_2;
                    point.altitude = -Geocent_b;
                    return point;
                }
            }
        }
        W2 = X * X + Y * Y;
        W = sqrt(W2);
        T0 = Z * AD_C;
        S0 = sqrt(T0 * T0 + W2);
        Sin_B0 = T0 / S0;
        Cos_B0 = W / S0;
        Sin3_B0 = Sin_B0 * Sin_B0 * Sin_B0;
        T1 = Z + Geocent_b * Geocent_ep2 * Sin3_B0;
        Sum = W - Geocent_a * Geocent_e2 * Cos_B0 * Cos_B0 * Cos_B0;
        S1 = sqrt(T1 * T1 + Sum * Sum);
        Sin_p1 = T1 / S1;
        Cos_p1 = Sum / S1;
        Rn = Geocent_a / sqrt(1.0 - Geocent_e2 * Sin_p1 * Sin_p1);
        if (Cos_p1 >= COS_67P5)
        {
            point.altitude = W / Cos_p1 - Rn;
        }
        else if (Cos_p1 <= -COS_67P5)
        {
            point.altitude = W / -Cos_p1 - Rn;
        }
        else
        {
            point.altitude = Z / Sin_p1 + Rn * (Geocent_e2 - 1.0);
        }
        if (!At_Pole)
        {
            point.latitude = atan(Sin_p1 / Cos_p1);
        }
        return point;
    } /* END OF Convert_Geocentric_To_Geodetic */

}
