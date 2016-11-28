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
public class LocalCartesian extends GeoTransBase
{

    /* Ellipsoid Parameters, default to WGS 84 */
    private double LocalCart_a = GeoTransConstants.WGS84_SEMI_MAJOR_AXIS; /*
                                             * Semi-major axis of ellipsoid in
                                             * meters
                                             */

    private double LocalCart_f = GeoTransConstants.WGS84_FLATTENING; /* Flattening of ellipsoid */

    private double es2 = 0.0066943799901413800; /*
                                                 * Eccentricity
                                                 * (0.08181919084262188000)
                                                 * squared
                                                 */

    private double u0 = GeoTransConstants.WGS84_SEMI_MAJOR_AXIS; /* Geocentric origin coordinates in */

    private double v0 = 0.0; /* terms of LocalSystem Cartesian origin */

    private double w0 = 0.0; /* parameters */

    /* LocalSystem Cartesian Projection Parameters */
    private double LocalCart_Origin_Lat = 0.0; /* Latitude of origin in radians */

    private double LocalCart_Origin_Long = 0.0; /*
                                                 * Longitude of origin in
                                                 * radians
                                                 */

    private double LocalCart_Origin_Height = 0.0; /*
                                                     * Height of origin in
                                                     * meters
                                                     */

    private double LocalCart_Orientation = 0.0; /*
                                                 * Orientation of Y axis in
                                                 * radians
                                                 */

    private double Sin_LocalCart_Origin_Lat = 0.0; /* sin(LocalCart_Origin_Lat) */

    private double Cos_LocalCart_Origin_Lat = 1.0; /* cos(LocalCart_Origin_Lat) */

    private double Sin_LocalCart_Origin_Lon = 0.0; /* sin(LocalCart_Origin_Lon) */

    private double Cos_LocalCart_Origin_Lon = 1.0; /* cos(LocalCart_Origin_Lon) */

    private double Sin_LocalCart_Orientation = 0.0; /* sin(LocalCart_Orientation) */

    private double Cos_LocalCart_Orientation = 1.0; /* cos(LocalCart_Orientation) */

    private double Sin_Lat_Sin_Orient = 0.0; /*
                                                 * sin(LocalCart_Origin_Lat) *
                                                 * sin(LocalCart_Orientation)
                                                 */

    private double Sin_Lat_Cos_Orient = 0.0; /*
                                                 * sin(LocalCart_Origin_Lat) *
                                                 * cos(LocalCart_Orientation)
                                                 */

    /**
     * 
     */
    public LocalCartesian(double a, double f, double Origin_Latitude,
            double Origin_Longitude, double Origin_Height, double Orientation)

    { /* BEGIN Set_Local_Cartesian_Parameters */
        /*
         * The function Set_Local_Cartesian_Parameters receives the ellipsoid
         * parameters and local origin parameters as inputs and sets the
         * corresponding state variables.
         * 
         * a : Semi-major axis of ellipsoid, in meters (input) f : Flattening of
         * ellipsoid (input) Origin_Latitude : Latitude of the local origin, in
         * radians (input) Origin_Longitude : Longitude of the local origin, in
         * radians (input) Origin_Height : Ellipsoid height of the local origin,
         * in meters (input) Orientation : Orientation angle of the local
         * cartesian coordinate system, in radians (input)
         */

        double N0;
        double inv_f = 1 / f;

        double val;
        // throw new IllegalArgumentException("");
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
        if ((Origin_Latitude < -PI_OVER_2) || (Origin_Latitude > PI_OVER_2))
        { /* origin latitude out of range */
            throw new IllegalArgumentException("origin latitude out of range");
        }
        if ((Origin_Longitude < -PI) || (Origin_Longitude > TWO_PI))
        { /* origin longitude out of range */
            throw new IllegalArgumentException("origin longitude out of range");
        }
        if ((Orientation < -PI) || (Orientation > TWO_PI))
        { /* orientation angle out of range */
            throw new IllegalArgumentException("orientation angle out of range");
        }

        LocalCart_a = a;
        LocalCart_f = f;
        LocalCart_Origin_Lat = Origin_Latitude;
        if (Origin_Longitude > PI)
            Origin_Longitude -= TWO_PI;
        LocalCart_Origin_Long = Origin_Longitude;
        LocalCart_Origin_Height = Origin_Height;
        if (Orientation > PI)
            Orientation -= TWO_PI;
        LocalCart_Orientation = Orientation;
        es2 = 2 * LocalCart_f - LocalCart_f * LocalCart_f;

        Sin_LocalCart_Origin_Lat = sin(LocalCart_Origin_Lat);
        Cos_LocalCart_Origin_Lat = cos(LocalCart_Origin_Lat);
        Sin_LocalCart_Origin_Lon = sin(LocalCart_Origin_Long);

        Cos_LocalCart_Origin_Lon = cos(LocalCart_Origin_Long);

        Sin_LocalCart_Orientation = sin(LocalCart_Orientation);
        Cos_LocalCart_Orientation = cos(LocalCart_Orientation);

        Sin_Lat_Sin_Orient = Sin_LocalCart_Origin_Lat
                * Sin_LocalCart_Orientation;
        Sin_Lat_Cos_Orient = Sin_LocalCart_Origin_Lat
                * Cos_LocalCart_Orientation;

        N0 = LocalCart_a
                / sqrt(1 - es2 * Sin_LocalCart_Origin_Lat
                        * Sin_LocalCart_Origin_Lat);

        val = (N0 + LocalCart_Origin_Height) * Cos_LocalCart_Origin_Lat;
        u0 = val * Cos_LocalCart_Origin_Lon;
        v0 = val * Sin_LocalCart_Origin_Lon;
        w0 = ((N0 * (1 - es2)) + LocalCart_Origin_Height)
                * Sin_LocalCart_Origin_Lat;

    } /* END OF Set_Local_Cartesian_Parameters */

    // void Get_Local_Cartesian_Parameters (double *a,
    // double *f,
    // double *Origin_Latitude,
    // double *Origin_Longitude,
    // double *Origin_Height,
    // double *Orientation)
    //
    // { /* BEGIN Get_Local_Cartesian_Parameters */
    // /*
    // * The function Get_Local_Cartesian_Parameters returns the ellipsoid
    // parameters
    // * and local origin parameters.
    // *
    // * a : Semi-major axis of ellipsoid, in meters (output)
    // * f : Flattening of ellipsoid (output)
    // * Origin_Latitude : Latitude of the local origin, in radians (output)
    // * Origin_Longitude : Longitude of the local origin, in radians (output)
    // * Origin_Height : Ellipsoid height of the local origin, in meters
    // (output)
    // * Orientation : Orientation angle of the local cartesian coordinate
    // system,
    // * in radians (output)
    // */
    //
    // *a = LocalCart_a;
    // *f = LocalCart_f;
    // *Origin_Latitude = LocalCart_Origin_Lat;
    // *Origin_Longitude = LocalCart_Origin_Long;
    // *Origin_Height = LocalCart_Origin_Height;
    // *Orientation = LocalCart_Orientation;
    //
    // } /* END OF Get_Local_Cartesian_Parameters */

    public Vector3 fromGeocentric(double u, double v, double w)
    { /*
         * BEGIN Convert_Geocentric_To_Local_Cartesian /* The function
         * Convert_Geocentric_To_Local_Cartesian converts geocentric coordinates
         * according to the current ellipsoid and local origin parameters.
         * 
         * u : Geocentric latitude, in meters (input) v : Geocentric longitude,
         * in meters (input) w : Geocentric height, in meters (input) X :
         * Calculated local cartesian X coordinate, in meters (output) Y :
         * Calculated local cartesian Y coordinate, in meters (output) Z :
         * Calculated local cartesian Z coordinate, in meters (output)
         * 
         */

        double X, Y, Z;
        double u_MINUS_u0, v_MINUS_v0, w_MINUS_w0;

        u_MINUS_u0 = u - u0;
        v_MINUS_v0 = v - v0;
        w_MINUS_w0 = w - w0;

        if (LocalCart_Orientation == 0.0)
        {

            double cos_lon_u_MINUS_u0 = Cos_LocalCart_Origin_Lon * u_MINUS_u0;
            double sin_lon_v_MINUS_v0 = Sin_LocalCart_Origin_Lon * v_MINUS_v0;

            X = -Sin_LocalCart_Origin_Lon * u_MINUS_u0
                    + Cos_LocalCart_Origin_Lon * v_MINUS_v0;
            Y = -Sin_LocalCart_Origin_Lat * cos_lon_u_MINUS_u0
                    + -Sin_LocalCart_Origin_Lat * sin_lon_v_MINUS_v0
                    + Cos_LocalCart_Origin_Lat * w_MINUS_w0;
            Z = Cos_LocalCart_Origin_Lat * cos_lon_u_MINUS_u0
                    + Cos_LocalCart_Origin_Lat * sin_lon_v_MINUS_v0
                    + Sin_LocalCart_Origin_Lat * w_MINUS_w0;
        }
        else
        {

            double cos_lat_w_MINUS_w0 = Cos_LocalCart_Origin_Lat * w_MINUS_w0;

            X = (-Cos_LocalCart_Orientation * Sin_LocalCart_Origin_Lon + Sin_Lat_Sin_Orient
                    * Cos_LocalCart_Origin_Lon)
                    * u_MINUS_u0
                    +

                    (Cos_LocalCart_Orientation * Cos_LocalCart_Origin_Lon + Sin_Lat_Sin_Orient
                            * Sin_LocalCart_Origin_Lon) * v_MINUS_v0 +

                    (-Sin_LocalCart_Orientation * cos_lat_w_MINUS_w0);

            Y = (-Sin_LocalCart_Orientation * Sin_LocalCart_Origin_Lon - Sin_Lat_Cos_Orient
                    * Cos_LocalCart_Origin_Lon)
                    * u_MINUS_u0
                    +

                    (Sin_LocalCart_Orientation * Cos_LocalCart_Origin_Lon - Sin_Lat_Cos_Orient
                            * Sin_LocalCart_Origin_Lon) * v_MINUS_v0 +

                    (Cos_LocalCart_Orientation * cos_lat_w_MINUS_w0);

            Z = (Cos_LocalCart_Origin_Lat * Cos_LocalCart_Origin_Lon)
                    * u_MINUS_u0 +

                    (Cos_LocalCart_Origin_Lat * Sin_LocalCart_Origin_Lon)
                    * v_MINUS_v0 +

                    Sin_LocalCart_Origin_Lat * w_MINUS_w0;

        }
        return new Vector3(X, Y, Z);
    } /* END OF Convert_Geocentric_To_Local_Cartesian */

    public Vector3 fromGeodetic(double Latitude,
            double Longitude, double Height)

    { /*
         * BEGIN Convert_Geodetic_TO_Local_Cartesian /* The function
         * Convert_Geodetic_To_Local_Cartesian converts geodetic coordinates
         * (latitude, longitude, and height) to local cartesian coordinates (X,
         * Y, Z), according to the current ellipsoid and local origin
         * parameters.
         * 
         * Latitude : Geodetic latitude, in radians (input) Longitude : Geodetic
         * longitude, in radians (input) Height : Geodetic height, in meters
         * (input) X : Calculated local cartesian X coordinate, in meters
         * (output) Y : Calculated local cartesian Y coordinate, in meters
         * (output) Z : Calculated local cartesian Z coordinate, in meters
         * (output)
         * 
         */

        if ((Latitude < -PI_OVER_2) || (Latitude > PI_OVER_2))
        { /* geodetic latitude out of range */
            throw new IllegalArgumentException("geodetic latitude out of range");
        }
        if ((Longitude < -PI) || (Longitude > TWO_PI))
        { /* geodetic longitude out of range */
            throw new IllegalArgumentException(
                    "geodetic longitude out of range");
        }

        Geocentric g = new Geocentric(LocalCart_a, LocalCart_f);
        Vector3 uvw = g.fromGeodetic(Latitude, Longitude,
                Height);

        return fromGeocentric(uvw.x, uvw.y, uvw.z);

    } /* END OF Convert_Geodetic_To_Local_Cartesian */

    public Vector3 toGeocentric(double X, double Y, double Z)

    { /* BEGIN Convert_Local_Cartesian_To_Geocentric */
        /*
         * The function Convert_Local_Cartesian_To_Geocentric converts local
         * cartesian coordinates (x, y, z) to geocentric coordinates (X, Y, Z)
         * according to the current ellipsoid and local origin parameters.
         * 
         * X : LocalSystem cartesian X coordinate, in meters (input) Y : LocalSystem
         * cartesian Y coordinate, in meters (input) Z : LocalSystem cartesian Z
         * coordinate, in meters (input) u : Calculated u value, in meters
         * (output) v : Calculated v value, in meters (output) w : Calculated w
         * value, in meters (output)
         */

        double u, v, w;
        if (LocalCart_Orientation == 0.0)
        {

            double sin_lat_y = Sin_LocalCart_Origin_Lat * Y;
            double cos_lat_z = Cos_LocalCart_Origin_Lat * Z;

            u = -Sin_LocalCart_Origin_Lon * X - sin_lat_y
                    * Cos_LocalCart_Origin_Lon + cos_lat_z
                    * Cos_LocalCart_Origin_Lon + u0;
            v = Cos_LocalCart_Origin_Lon * X - sin_lat_y
                    * Sin_LocalCart_Origin_Lon + cos_lat_z
                    * Sin_LocalCart_Origin_Lon + v0;
            w = Cos_LocalCart_Origin_Lat * Y + Sin_LocalCart_Origin_Lat * Z
                    + w0;
        }
        else
        {

            double rotated_x, rotated_y;

            double rotated_y_sin_lat, z_cos_lat;

            rotated_x = Cos_LocalCart_Orientation * X
                    + Sin_LocalCart_Orientation * Y;

            rotated_y = -Sin_LocalCart_Orientation * X
                    + Cos_LocalCart_Orientation * Y;

            rotated_y_sin_lat = rotated_y * Sin_LocalCart_Origin_Lat;

            z_cos_lat = Z * Cos_LocalCart_Origin_Lat;

            u = -Sin_LocalCart_Origin_Lon * rotated_x
                    - Cos_LocalCart_Origin_Lon * rotated_y_sin_lat
                    + Cos_LocalCart_Origin_Lon * z_cos_lat + u0;

            v = Cos_LocalCart_Origin_Lon * rotated_x - Sin_LocalCart_Origin_Lon
                    * rotated_y_sin_lat + Sin_LocalCart_Origin_Lon * z_cos_lat
                    + v0;

            w = Cos_LocalCart_Origin_Lat * rotated_y + Sin_LocalCart_Origin_Lat
                    * Z + w0;

        }
        return new Vector3(u, v, w);
    } /* END OF Convert_Local_Cartesian_To_Geocentric */

    public Geodetic.Point toGeodetic(double X, double Y,
            double Z)

    { /* BEGIN Convert_Local_Cartesian_To_Geodetic */
        /*
         * The function Convert_Local_Cartesian_To_Geodetic converts local
         * cartesian coordinates (X, Y, Z) to geodetic coordinates (latitude,
         * longitude, and height), according to the current ellipsoid and local
         * origin parameters.
         * 
         * X : LocalSystem cartesian X coordinate, in meters (input) Y : LocalSystem
         * cartesian Y coordinate, in meters (input) Z : LocalSystem cartesian Z
         * coordinate, in meters (input) Latitude : Calculated latitude value,
         * in radians (output) Longitude : Calculated longitude value, in
         * radians (output) Height : Calculated height value, in meters (output)
         */

        Vector3 uvw = toGeocentric(X, Y, Z);

        Geocentric g = new Geocentric(LocalCart_a, LocalCart_f);

        Geodetic.Point point = g.toGeodetic(uvw.x, uvw.y,
                uvw.z);

        if (point.longitude > PI)
            point.longitude -= TWO_PI;
        if (point.longitude < -PI)
            point.longitude += TWO_PI;

        return point;
    } /* END OF Convert_Local_Cartesian_To_Geodetic */

}
