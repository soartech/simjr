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

/**
 * @author ray
 */
public class PolarStereographic extends GeoTransBase
{
    public static final class Point
    {
        double Easting;

        double Northing;
    }

    /** ********************************************************************* */
    /*
     * GLOBAL DECLARATIONS
     * 
     */

    /* Ellipsoid Parameters, default to WGS 84 */
    private double Polar_a = GeoTransConstants.WGS84_SEMI_MAJOR_AXIS; /*
                                         * Semi-major axis of ellipsoid in
                                         * meters
                                         */

    private double Polar_f = GeoTransConstants.WGS84_FLATTENING; /* Flattening of ellipsoid */

    private double es = 0.08181919084262188000; /* Eccentricity of ellipsoid */

    private double es_OVER_2 = .040909595421311; /* es / 2.0 */

    private double Southern_Hemisphere = 0; /* Flag variable */

    private double mc = 1.0;

    private double tc = 1.0;

    private double e4 = 1.0033565552493;

    private double Polar_a_mc = GeoTransConstants.WGS84_SEMI_MAJOR_AXIS; /* Polar_a * mc */

    private double two_Polar_a = 12756274.0; /* 2.0 * Polar_a */

    /* Polar Stereographic projection Parameters */
    private double Polar_Origin_Lat = ((PI * 90) / 180); /*
                                                             * Latitude of
                                                             * origin in radians
                                                             */

    private double Polar_Origin_Long = 0.0; /* Longitude of origin in radians */

    private double Polar_False_Easting = 0.0; /* False easting in meters */

    private double Polar_False_Northing = 0.0; /* False northing in meters */

    /* Maximum variance for easting and northing values for WGS 84. */
    private double Polar_Delta_Easting = 12713601.0;

    private double Polar_Delta_Northing = 12713601.0;

    private double POLAR_POW(double EsSin)
    {
        return pow((1.0 - EsSin) / (1.0 + EsSin), es_OVER_2);
    }

    /*
     * These state variables are for optimization purposes. The only function
     * that should modify them is Set_Polar_Stereographic_Parameters.
     */

    /** ********************************************************************* */
    /*
     * FUNCTIONS
     * 
     */

    public PolarStereographic()
    {

    }

    public PolarStereographic(double a, double f,
            double Latitude_of_True_Scale, double Longitude_Down_from_Pole,
            double False_Easting, double False_Northing)

    { /* BEGIN Set_Polar_Stereographic_Parameters */
        /*
         * The function Set_Polar_Stereographic_Parameters receives the
         * ellipsoid parameters and Polar Stereograpic projection parameters as
         * inputs, and sets the corresponding state variables. If any errors
         * occur, error code(s) are returned by the function, otherwise
         * POLAR_NO_ERROR is returned.
         * 
         * a : Semi-major axis of ellipsoid, in meters (input) f : Flattening of
         * ellipsoid (input) Latitude_of_True_Scale : Latitude of true scale, in
         * radians (input) Longitude_Down_from_Pole : Longitude down from pole,
         * in radians (input) False_Easting : Easting (X) at center of
         * projection, in meters (input) False_Northing : Northing (Y) at center
         * of projection, in meters (input)
         */

        double es2;
        double slat, clat;
        double essin;
        double one_PLUS_es, one_MINUS_es;
        double pow_es;
        double inv_f = 1 / f;
        final double epsilon = 1.0e-2;

        if (a <= 0.0)
        { /* Semi-major axis must be greater than zero */
            throw new IllegalArgumentException(
                    "Semi-major axis must be greater than zero");
        }
        if ((inv_f < 250) || (inv_f > 350))
        { /* Inverse flattening must be between 250 and 350 */
            throw new IllegalArgumentException(
                    "Inverse flattening must be between 250 and 350");
        }
        if ((Latitude_of_True_Scale < -PI_OVER_2)
                || (Latitude_of_True_Scale > PI_OVER_2))
        { /* Origin Latitude out of range */
            throw new IllegalArgumentException("Origin Latitude out of range");
        }
        if ((Longitude_Down_from_Pole < -PI)
                || (Longitude_Down_from_Pole > TWO_PI))
        { /* Origin Longitude out of range */
            throw new IllegalArgumentException("Origin Longitude out of range");
        }

        Polar_a = a;
        two_Polar_a = 2.0 * Polar_a;
        Polar_f = f;

        if (Longitude_Down_from_Pole > PI)
            Longitude_Down_from_Pole -= TWO_PI;
        if (Latitude_of_True_Scale < 0)
        {
            Southern_Hemisphere = 1;
            Polar_Origin_Lat = -Latitude_of_True_Scale;
            Polar_Origin_Long = -Longitude_Down_from_Pole;
        }
        else
        {
            Southern_Hemisphere = 0;
            Polar_Origin_Lat = Latitude_of_True_Scale;
            Polar_Origin_Long = Longitude_Down_from_Pole;
        }
        Polar_False_Easting = False_Easting;
        Polar_False_Northing = False_Northing;

        es2 = 2 * Polar_f - Polar_f * Polar_f;
        es = sqrt(es2);
        es_OVER_2 = es / 2.0;

        if (fabs(fabs(Polar_Origin_Lat) - PI_OVER_2) > 1.0e-10)
        {
            slat = sin(Polar_Origin_Lat);
            essin = es * slat;
            pow_es = POLAR_POW(essin);
            clat = cos(Polar_Origin_Lat);
            mc = clat / sqrt(1.0 - essin * essin);
            Polar_a_mc = Polar_a * mc;
            tc = tan(PI_Over_4 - Polar_Origin_Lat / 2.0) / pow_es;
        }
        else
        {
            one_PLUS_es = 1.0 + es;
            one_MINUS_es = 1.0 - es;
            e4 = sqrt(pow(one_PLUS_es, one_PLUS_es)
                    * pow(one_MINUS_es, one_MINUS_es));
        }

        /* Calculate Radius */
        Point p = fromGeodetic(0, Polar_Origin_Long);
        Polar_Delta_Northing = fabs(p.Northing) + epsilon;
        Polar_Delta_Easting = Polar_Delta_Northing;

    } /* END OF Set_Polar_Stereographic_Parameters */

    // void Get_Polar_Stereographic_Parameters (double *a,
    // double *f,
    // double *Latitude_of_True_Scale,
    // double *Longitude_Down_from_Pole,
    // double *False_Easting,
    // double *False_Northing)
    //
    // { /* BEGIN Get_Polar_Stereographic_Parameters */
    // /*
    // * The function Get_Polar_Stereographic_Parameters returns the current
    // * ellipsoid parameters and Polar projection parameters.
    // *
    // * a : Semi-major axis of ellipsoid, in meters (output)
    // * f : Flattening of ellipsoid (output)
    // * Latitude_of_True_Scale : Latitude of true scale, in radians (output)
    // * Longitude_Down_from_Pole : Longitude down from pole, in radians
    // (output)
    // * False_Easting : Easting (X) at center of projection, in meters (output)
    // * False_Northing : Northing (Y) at center of projection, in meters
    // (output)
    // */
    //
    // *a = Polar_a;
    // *f = Polar_f;
    // *Latitude_of_True_Scale = Polar_Origin_Lat;
    // *Longitude_Down_from_Pole = Polar_Origin_Long;
    // *False_Easting = Polar_False_Easting;
    // *False_Northing = Polar_False_Northing;
    // return;
    // } /* END OF Get_Polar_Stereographic_Parameters */

    public Point fromGeodetic(double Latitude,
            double Longitude)

    { /* BEGIN Convert_Geodetic_To_Polar_Stereographic */

        /*
         * The function Convert_Geodetic_To_Polar_Stereographic converts
         * geodetic coordinates (latitude and longitude) to Polar Stereographic
         * coordinates (easting and northing), according to the current
         * ellipsoid and Polar Stereographic projection parameters. If any
         * errors occur, error code(s) are returned by the function, otherwise
         * POLAR_NO_ERROR is returned.
         * 
         * Latitude : Latitude, in radians (input) Longitude : Longitude, in
         * radians (input) Easting : Easting (X), in meters (output) Northing :
         * Northing (Y), in meters (output)
         */

        double dlam;
        double slat;
        double essin;
        double t;
        double rho;
        double pow_es;

        if ((Latitude < -PI_OVER_2) || (Latitude > PI_OVER_2))
        { /* Latitude out of range */
            throw new IllegalArgumentException("Latitude out of range");
        }
        if ((Latitude < 0) && (Southern_Hemisphere == 0))
        { /* Latitude and Origin Latitude in different hemispheres */
            throw new IllegalArgumentException(
                    "Latitude and Origin Latitude in different hemispheres");
        }
        if ((Latitude > 0) && (Southern_Hemisphere == 1))
        { /* Latitude and Origin Latitude in different hemispheres */
            throw new IllegalArgumentException(
                    "Latitude and Origin Latitude in different hemispheres");
        }
        if ((Longitude < -PI) || (Longitude > TWO_PI))
        { /* Longitude out of range */
            throw new IllegalArgumentException("Longitude out of range");
        }

        Point point = new Point();

        if (fabs(fabs(Latitude) - PI_OVER_2) < 1.0e-10)
        {
            point.Easting = 0.0;
            point.Northing = 0.0;
        }
        else
        {
            if (Southern_Hemisphere != 0)
            {
                Longitude *= -1.0;
                Latitude *= -1.0;
            }
            dlam = Longitude - Polar_Origin_Long;
            if (dlam > PI)
            {
                dlam -= TWO_PI;
            }
            if (dlam < -PI)
            {
                dlam += TWO_PI;
            }
            slat = sin(Latitude);
            essin = es * slat;
            pow_es = POLAR_POW(essin);
            t = tan(PI_Over_4 - Latitude / 2.0) / pow_es;

            if (fabs(fabs(Polar_Origin_Lat) - PI_OVER_2) > 1.0e-10)
                rho = Polar_a_mc * t / tc;
            else
                rho = two_Polar_a * t / e4;

            point.Easting = rho * sin(dlam) + Polar_False_Easting;

            if (Southern_Hemisphere != 0)
            {
                point.Easting *= -1.0;
                point.Northing = rho * cos(dlam) + Polar_False_Northing;
            }
            else
                point.Northing = -rho * cos(dlam) + Polar_False_Northing;

        }
        return point;
    } /* END OF Convert_Geodetic_To_Polar_Stereographic */

    public Geodetic.Point toGeodetic(
            double Easting, double Northing)

    { /* BEGIN Convert_Polar_Stereographic_To_Geodetic */
        /*
         * The function Convert_Polar_Stereographic_To_Geodetic converts Polar
         * Stereographic coordinates (easting and northing) to geodetic
         * coordinates (latitude and longitude) according to the current
         * ellipsoid and Polar Stereographic projection Parameters. If any
         * errors occur, the code(s) are returned by the function, otherwise
         * POLAR_NO_ERROR is returned.
         * 
         * Easting : Easting (X), in meters (input) Northing : Northing (Y), in
         * meters (input) Latitude : Latitude, in radians (output) Longitude :
         * Longitude, in radians (output)
         * 
         */

        double dy, dx;
        double rho;
        double t;
        double PHI, sin_PHI;
        double tempPHI = 0.0;
        double essin;
        double pow_es;
        double temp;

        if ((Easting > (Polar_False_Easting + Polar_Delta_Easting))
                || (Easting < (Polar_False_Easting - Polar_Delta_Easting)))
        { /* Easting out of range */
            throw new IllegalArgumentException("Easting out of range");
        }
        if ((Northing > (Polar_False_Northing + Polar_Delta_Northing))
                || (Northing < (Polar_False_Northing - Polar_Delta_Northing)))
        { /* Northing out of range */
            throw new IllegalArgumentException("Northing out of range");
        }
        temp = sqrt(Easting * Easting + Northing * Northing);

        if ((temp > (Polar_False_Easting + Polar_Delta_Easting))
                || (temp > (Polar_False_Northing + Polar_Delta_Northing))
                || (temp < (Polar_False_Easting - Polar_Delta_Easting))
                || (temp < (Polar_False_Northing - Polar_Delta_Northing)))
        { /* Point is outside of projection area */
            throw new IllegalArgumentException(
                    "Point is outside of projection area");
        }

        Geodetic.Point point = new Geodetic.Point();

        dy = Northing - Polar_False_Northing;
        dx = Easting - Polar_False_Easting;
        if ((dy == 0.0) && (dx == 0.0))
        {
            point.latitude = PI_OVER_2;
            point.longitude = Polar_Origin_Long;

        }
        else
        {
            if (Southern_Hemisphere != 0)
            {
                dy *= -1.0;
                dx *= -1.0;
            }

            rho = sqrt(dx * dx + dy * dy);
            if (fabs(fabs(Polar_Origin_Lat) - PI_OVER_2) > 1.0e-10)
                t = rho * tc / (Polar_a_mc);
            else
                t = rho * e4 / (two_Polar_a);
            PHI = PI_OVER_2 - 2.0 * atan(t);
            while (fabs(PHI - tempPHI) > 1.0e-10)
            {
                tempPHI = PHI;
                sin_PHI = sin(PHI);
                essin = es * sin_PHI;
                pow_es = POLAR_POW(essin);
                PHI = PI_OVER_2 - 2.0 * atan(t * pow_es);
            }
            point.latitude = PHI;
            point.longitude = Polar_Origin_Long + atan2(dx, -dy);

            if (point.longitude > PI)
                point.longitude -= TWO_PI;
            else if (point.longitude < -PI)
                point.longitude += TWO_PI;

            if (point.latitude > PI_OVER_2) /*
                                             * force distorted values to 90, -90
                                             * degrees
                                             */
                point.latitude = PI_OVER_2;
            else if (point.latitude < -PI_OVER_2)
                point.latitude = -PI_OVER_2;

            if (point.longitude > PI) /*
                                         * force distorted values to 180, -180
                                         * degrees
                                         */
                point.longitude = PI;
            else if (point.longitude < -PI)
                point.longitude = -PI;

        }
        if (Southern_Hemisphere != 0)
        {
            point.latitude *= -1.0;
            point.longitude *= -1.0;
        }

        return point;
    } /* END OF Convert_Polar_Stereographic_To_Geodetic */
}
