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
public class UniversalTransverseMercator extends GeoTransBase
{
    public static class Point
    {
        public int   Zone;
        public char   Hemisphere;
        public double Easting;
        public double Northing;
    }
    
    private static final double MIN_LAT     = ( (-80.5 * PI) / 180.0 ); /* -80.5 degrees in radians    */
    private static final double MAX_LAT     = ( (84.5 * PI) / 180.0 ) ; /* 84.5 degrees in radians     */
    private static final double MIN_EASTING = 100000;
    private static final double MAX_EASTING = 900000;
    private static final double MIN_NORTHING= 0;
    private static final double MAX_NORTHING =10000000;

    /***************************************************************************/
    /*
     *                              GLOBAL DECLARATIONS
     */

    private double UTM_a = GeoTransConstants.WGS84_SEMI_MAJOR_AXIS;         /* Semi-major axis of ellipsoid in meters  */
    private double UTM_f = GeoTransConstants.WGS84_FLATTENING; /* Flattening of ellipsoid                 */
    private int   UTM_Override = 0;          /* Zone override flag                      */


    /***************************************************************************/
    /**
     * 
     */
    public UniversalTransverseMercator()
    {
        super();
    }
    
    /**
     * 
     */
    public UniversalTransverseMercator(double a,      
                            double f,
                            int   override)
    {
    /*
     * The function Set_UTM_Parameters receives the ellipsoid parameters and
     * UTM zone override parameter as inputs, and sets the corresponding state
     * variables.  If any errors occur, the error code(s) are returned by the 
     * function, otherwise UTM_NO_ERROR is returned.
     *
     *    a                 : Semi-major axis of ellipsoid, in meters       (input)
     *    f                 : Flattening of ellipsoid                                   (input)
     *    override          : UTM override zone, zero indicates no override (input)
     */

      double inv_f = 1 / f;

      if (a <= 0.0)
      { /* Semi-major axis must be greater than zero */
          throw new IllegalArgumentException("Semi-major axis must be greater than zero");
      }
      if ((inv_f < 250) || (inv_f > 350))
      { /* Inverse flattening must be between 250 and 350 */
          throw new IllegalArgumentException("Inverse flattening must be between 250 and 350");
      }
      if ((override < 0) || (override > 60))
      {
          throw new IllegalArgumentException("Invalid UTM zone override");
      }
        UTM_a = a;
        UTM_f = f;
        UTM_Override = override;
    } /* END OF Set_UTM_Parameters */


//    void Get_UTM_Parameters(double *a,
//                            double *f,
//                            long   *override)
//    {
//    /*
//     * The function Get_UTM_Parameters returns the current ellipsoid
//     * parameters and UTM zone override parameter.
//     *
//     *    a                 : Semi-major axis of ellipsoid, in meters       (output)
//     *    f                 : Flattening of ellipsoid                                   (output)
//     *    override          : UTM override zone, zero indicates no override (output)
//     */
//
//      *a = UTM_a;
//      *f = UTM_f;
//      *override = UTM_Override;
//    } /* END OF Get_UTM_Parameters */


    public Point fromGeodetic (double Latitude,
                                  double Longitude)
    { 
    /*
     * The function Convert_Geodetic_To_UTM converts geodetic (latitude and
     * longitude) coordinates to UTM projection (zone, hemisphere, easting and
     * northing) coordinates according to the current ellipsoid and UTM zone
     * override parameters.  If any errors occur, the error code(s) are returned
     * by the function, otherwise UTM_NO_ERROR is returned.
     *
     *    Latitude          : Latitude in radians                 (input)
     *    Longitude         : Longitude in radians                (input)
     *    Zone              : UTM zone                            (output)
     *    Hemisphere        : North or South hemisphere           (output)
     *    Easting           : Easting (X) in meters               (output)
     *    Northing          : Northing (Y) in meters              (output)
     */

      long Lat_Degrees;
      long Long_Degrees;
      int temp_zone;
      double Origin_Latitude = 0;
      double Central_Meridian = 0;
      double False_Easting = 500000;
      double False_Northing = 0;
      double Scale = 0.9996;

      if ((Latitude < MIN_LAT) || (Latitude > MAX_LAT))
      { /* Latitude out of range */
          throw new IllegalArgumentException("Latitude out of range");
      }
      if ((Longitude < -PI) || (Longitude > (2*PI)))
      { /* Longitude out of range */
          throw new IllegalArgumentException("Longitude out of range");
      }
      Point point = new Point();
        if (Longitude < 0)
          Longitude += (2*PI) + 1.0e-10;
        Lat_Degrees = (long)(Latitude * 180.0 / PI);
        Long_Degrees = (long)(Longitude * 180.0 / PI);

        if (Longitude < PI)
          temp_zone = (int)(31 + ((Longitude * 180.0 / PI) / 6.0));
        else
          temp_zone = (int)(((Longitude * 180.0 / PI) / 6.0) - 29);
        if (temp_zone > 60)
          temp_zone = 1;
        /* UTM special cases */
        if ((Lat_Degrees > 55) && (Lat_Degrees < 64) && (Long_Degrees > -1)
            && (Long_Degrees < 3))
          temp_zone = 31;
        if ((Lat_Degrees > 55) && (Lat_Degrees < 64) && (Long_Degrees > 2)
            && (Long_Degrees < 12))
          temp_zone = 32;
        if ((Lat_Degrees > 71) && (Long_Degrees > -1) && (Long_Degrees < 9))
          temp_zone = 31;
        if ((Lat_Degrees > 71) && (Long_Degrees > 8) && (Long_Degrees < 21))
          temp_zone = 33;
        if ((Lat_Degrees > 71) && (Long_Degrees > 20) && (Long_Degrees < 33))
          temp_zone = 35;
        if ((Lat_Degrees > 71) && (Long_Degrees > 32) && (Long_Degrees < 42))
          temp_zone = 37;

        if (UTM_Override != 0)
        {
          if ((temp_zone == 1) && (UTM_Override == 60))
            temp_zone = UTM_Override;
          else if ((temp_zone == 60) && (UTM_Override == 1))
            temp_zone = UTM_Override;
          else if (((temp_zone-1) <= UTM_Override) && (UTM_Override <= (temp_zone+1)))
            temp_zone = UTM_Override;
          else
          {
              throw new IllegalArgumentException("Invalid zone override");
          }
        }
          if (temp_zone >= 31)
            Central_Meridian = (6 * temp_zone - 183) * PI / 180.0;
          else
            Central_Meridian = (6 * temp_zone + 177) * PI / 180.0;
          point.Zone = temp_zone;
          if (Latitude < 0)
          {
            False_Northing = 10000000;
            point.Hemisphere = 'S';
          }
          else
              point.Hemisphere = 'N';
          TransverseMercator tm = new TransverseMercator(UTM_a, UTM_f, Origin_Latitude,
                                             Central_Meridian, False_Easting, False_Northing, Scale);
          TransverseMercator.Point tmPoint = tm.fromGeodetic(Latitude, Longitude);
          point.Easting = tmPoint.easting;
          point.Northing = tmPoint.northing;
          if ((point.Easting < MIN_EASTING) || (point.Easting > MAX_EASTING))
          {
              throw new IllegalArgumentException("Invalid UTM easting");
          }
          if ((point.Northing < MIN_NORTHING) || (point.Northing > MAX_NORTHING))
          {
              throw new IllegalArgumentException("Invalid UTM northing");
          }
          return point;
    } /* END OF Convert_Geodetic_To_UTM */


    public Geodetic.Point toGeodetic(long   Zone,
                                 char   Hemisphere,
                                 double Easting,
                                 double Northing)
    {
    /*
     * The function Convert_UTM_To_Geodetic converts UTM projection (zone, 
     * hemisphere, easting and northing) coordinates to geodetic(latitude
     * and  longitude) coordinates, according to the current ellipsoid
     * parameters.  If any errors occur, the error code(s) are returned
     * by the function, otherwise UTM_NO_ERROR is returned.
     *
     *    Zone              : UTM zone                               (input)
     *    Hemisphere        : North or South hemisphere              (input)
     *    Easting           : Easting (X) in meters                  (input)
     *    Northing          : Northing (Y) in meters                 (input)
     *    Latitude          : Latitude in radians                    (output)
     *    Longitude         : Longitude in radians                   (output)
     */
      double Origin_Latitude = 0;
      double Central_Meridian = 0;
      double False_Easting = 500000;
      double False_Northing = 0;
      double Scale = 0.9996;

      if ((Zone < 1) || (Zone > 60))
          throw new IllegalArgumentException("Invalid zone");
      if ((Hemisphere != 'S') && (Hemisphere != 'N'))
          throw new IllegalArgumentException("Invalid hemisphere");
      if ((Easting < MIN_EASTING) || (Easting > MAX_EASTING))
          throw new IllegalArgumentException("Invalid easting");
      if ((Northing < MIN_NORTHING) || (Northing > MAX_NORTHING))
          throw new IllegalArgumentException("Invalid northing");

        if (Zone >= 31)
          Central_Meridian = ((6 * Zone - 183) * PI / 180.0 /*+ 0.00000005*/);
        else
          Central_Meridian = ((6 * Zone + 177) * PI / 180.0 /*+ 0.00000005*/);
        if (Hemisphere == 'S')
          False_Northing = 10000000;
        TransverseMercator tm = new TransverseMercator(UTM_a, UTM_f, Origin_Latitude,
                                           Central_Meridian, False_Easting, False_Northing, Scale);
        Geodetic.Point point = tm.toGeodetic(Easting, Northing);

        if ((point.latitude < MIN_LAT) || (point.latitude > MAX_LAT))
        { /* Latitude out of range */
            throw new IllegalArgumentException("Invalid latitude");
        }
      return point;
    } /* END OF Convert_UTM_To_Geodetic */

}
