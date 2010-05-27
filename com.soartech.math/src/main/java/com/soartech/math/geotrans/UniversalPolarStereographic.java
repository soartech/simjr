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
public class UniversalPolarStereographic extends GeoTransBase
{
    public static final class Point
    {
        char Hemisphere;

        double Easting;

        double Northing;
    }

    private static final double MAX_LAT = ((PI * 90) / 180.0); /* 90 degrees in radians */

    private static final double MAX_ORIGIN_LAT = ((81.114528 * PI) / 180.0);

    private static final double MIN_NORTH_LAT = (83.5 * PI / 180.0);

    private static final double MIN_SOUTH_LAT = (-79.5 * PI / 180.0);

    private static final double MIN_EAST_NORTH = 0;

    private static final double MAX_EAST_NORTH = 4000000;

    /* Ellipsoid Parameters, default to WGS 84  */
    private double UPS_a = GeoTransConstants.WGS84_SEMI_MAJOR_AXIS; /* Semi-major axis of ellipsoid in meters   */

    private double UPS_f = GeoTransConstants.WGS84_FLATTENING; /* Flattening of ellipsoid  */

    private final double UPS_False_Easting = 2000000;

    private final double UPS_False_Northing = 2000000;

    private double UPS_Origin_Latitude = MAX_ORIGIN_LAT; /*set default = North Hemisphere */

    private double UPS_Origin_Longitude = 0.0;

    private double false_easting = 0.0;

    private double false_northing = 0.0;

    private double UPS_Easting = 0.0;

    private double UPS_Northing = 0.0;

    /************************************************************************/
    /*                              FUNCTIONS
     *
     */

    public UniversalPolarStereographic()
    {

    }

    public UniversalPolarStereographic(double a, double f)
    {
        /*
         * The function SET_UPS_PARAMETERS receives the ellipsoid parameters and sets
         * the corresponding state variables. If any errors occur, the error code(s)
         * are returned by the function, otherwise UPS_NO_ERROR is returned.
         *
         *   a     : Semi-major axis of ellipsoid in meters (input)
         *   f     : Flattening of ellipsoid                          (input)
         */

        double inv_f = 1 / f;

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

        UPS_a = a;
        UPS_f = f;
    } /* END of Set_UPS_Parameters  */

    //    void Get_UPS_Parameters( double *a,
    //                             double *f)
    //    {
    //    /*
    //     * The function Get_UPS_Parameters returns the current ellipsoid parameters.
    //     *
    //     *  a      : Semi-major axis of ellipsoid, in meters (output)
    //     *  f      : Flattening of ellipsoid                           (output)
    //     */
    //
    //      *a = UPS_a;
    //      *f = UPS_f;
    //      return;
    //    } /* END OF Get_UPS_Parameters */

    public Point fromGeodetic(double Latitude, double Longitude)
    {
        /*
         *  The function Convert_Geodetic_To_UPS converts geodetic (latitude and
         *  longitude) coordinates to UPS (hemisphere, easting, and northing)
         *  coordinates, according to the current ellipsoid parameters. If any 
         *  errors occur, the error code(s) are returned by the function, 
         *  otherwide UPS_NO_ERROR is returned.
         *
         *    Latitude      : Latitude in radians                       (input)
         *    Longitude     : Longitude in radians                      (input)
         *    Hemisphere    : Hemisphere either 'N' or 'S'              (output)
         *    Easting       : Easting/X in meters                       (output)
         *    Northing      : Northing/Y in meters                      (output)
         */

        if ((Latitude < -MAX_LAT) || (Latitude > MAX_LAT))
        { /* latitude out of range */
            throw new IllegalArgumentException("latitude out of range");
        }
        if ((Latitude < 0) && (Latitude > MIN_SOUTH_LAT))
            throw new IllegalArgumentException("Invalid latitude");
        if ((Latitude >= 0) && (Latitude < MIN_NORTH_LAT))
            throw new IllegalArgumentException("Invalid latitude");
        if ((Longitude < -PI) || (Longitude > (2 * PI)))
        { /* slam out of range */
            throw new IllegalArgumentException("slam out of range");
        }

        Point point = new Point();
        if (Latitude < 0)
        {
            UPS_Origin_Latitude = -MAX_ORIGIN_LAT;
            point.Hemisphere = 'S';
        }
        else
        {
            UPS_Origin_Latitude = MAX_ORIGIN_LAT;
            point.Hemisphere = 'N';
        }

        PolarStereographic ps = new PolarStereographic(UPS_a, UPS_f,
                UPS_Origin_Latitude, UPS_Origin_Longitude, false_easting,
                false_northing);

        PolarStereographic.Point psPoint = ps
                .fromGeodetic(Latitude, Longitude);

        UPS_Easting = UPS_False_Easting + psPoint.Easting;
        UPS_Northing = UPS_False_Northing + psPoint.Northing;

        point.Easting = UPS_Easting;
        point.Northing = UPS_Northing;

        return point;
    } /* END OF Convert_Geodetic_To_UPS  */

    public Geodetic.Point toGeodetic(char Hemisphere,
            double Easting, double Northing)
    {
        /*
         *  The function Convert_UPS_To_Geodetic converts UPS (hemisphere, easting, 
         *  and northing) coordinates to geodetic (latitude and longitude) coordinates
         *  according to the current ellipsoid parameters.  If any errors occur, the 
         *  error code(s) are returned by the function, otherwise UPS_NO_ERROR is 
         *  returned.
         *
         *    Hemisphere    : Hemisphere either 'N' or 'S'              (input)
         *    Easting       : Easting/X in meters                       (input)
         *    Northing      : Northing/Y in meters                      (input)
         *    Latitude      : Latitude in radians                       (output)
         *    Longitude     : Longitude in radians                      (output)
         */

        if ((Hemisphere != 'N') && (Hemisphere != 'S'))
            throw new IllegalArgumentException("Invalid hemisphere");
        if ((Easting < MIN_EAST_NORTH) || (Easting > MAX_EAST_NORTH))
            throw new IllegalArgumentException("Invalid easting");
        if ((Northing < MIN_EAST_NORTH) || (Northing > MAX_EAST_NORTH))
            throw new IllegalArgumentException("Invalid northing");

        if (Hemisphere == 'N')
        {
            UPS_Origin_Latitude = MAX_ORIGIN_LAT;
        }
        if (Hemisphere == 'S')
        {
            UPS_Origin_Latitude = -MAX_ORIGIN_LAT;
        }

        PolarStereographic ps = new PolarStereographic(UPS_a, UPS_f,
                UPS_Origin_Latitude, UPS_Origin_Longitude, UPS_False_Easting,
                UPS_False_Northing);

        Geodetic.Point point = ps.toGeodetic(
                Easting, Northing);

        if ((point.latitude < 0) && (point.latitude > MIN_SOUTH_LAT))
            throw new IllegalArgumentException("Invalid latitude");
        if ((point.latitude >= 0) && (point.latitude < MIN_NORTH_LAT))
            throw new IllegalArgumentException("Invalid latitude");
        return point;
    } /*  END OF Convert_UPS_To_Geodetic  */

}
