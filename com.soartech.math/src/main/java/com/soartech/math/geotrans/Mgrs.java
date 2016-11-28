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

import java.text.DecimalFormat;

/**
 * @author ray
 */
public class Mgrs extends GeoTransBase
{
    private static final double DEG_TO_RAD = 0.017453292519943295; /* PI/180 */

    private static final double RAD_TO_DEG = 57.29577951308232087; /* 180/PI */

    private static final int LETTER_A = 0; /* ARRAY INDEX FOR LETTER A */

    private static final int LETTER_B = 1; /* ARRAY INDEX FOR LETTER B */

    private static final int LETTER_C = 2; /* ARRAY INDEX FOR LETTER C */

    private static final int LETTER_D = 3; /* ARRAY INDEX FOR LETTER D */

    private static final int LETTER_E = 4; /* ARRAY INDEX FOR LETTER E */

    private static final int LETTER_F = 5; /* ARRAY INDEX FOR LETTER E */

    private static final int LETTER_G = 6; /* ARRAY INDEX FOR LETTER H */

    private static final int LETTER_H = 7; /* ARRAY INDEX FOR LETTER H */

    private static final int LETTER_I = 8; /* ARRAY INDEX FOR LETTER I */

    private static final int LETTER_J = 9; /* ARRAY INDEX FOR LETTER J */

    private static final int LETTER_K = 10; /* ARRAY INDEX FOR LETTER J */

    private static final int LETTER_L = 11; /* ARRAY INDEX FOR LETTER L */

    private static final int LETTER_M = 12; /* ARRAY INDEX FOR LETTER M */

    private static final int LETTER_N = 13; /* ARRAY INDEX FOR LETTER N */

    private static final int LETTER_O = 14; /* ARRAY INDEX FOR LETTER O */

    private static final int LETTER_P = 15; /* ARRAY INDEX FOR LETTER P */

    private static final int LETTER_Q = 16; /* ARRAY INDEX FOR LETTER Q */

    private static final int LETTER_R = 17; /* ARRAY INDEX FOR LETTER R */

    private static final int LETTER_S = 18; /* ARRAY INDEX FOR LETTER S */

    private static final int LETTER_T = 19; /* ARRAY INDEX FOR LETTER S */

    private static final int LETTER_U = 20; /* ARRAY INDEX FOR LETTER U */

    private static final int LETTER_V = 21; /* ARRAY INDEX FOR LETTER V */

    private static final int LETTER_W = 22; /* ARRAY INDEX FOR LETTER W */

    private static final int LETTER_X = 23; /* ARRAY INDEX FOR LETTER X */

    private static final int LETTER_Y = 24; /* ARRAY INDEX FOR LETTER Y */

    private static final int LETTER_Z = 25; /* ARRAY INDEX FOR LETTER Z */

    private static final int MGRS_LETTERS = 3; /* NUMBER OF LETTERS IN MGRS */

    private static final double ONEHT = 100000.e0; /* ONE HUNDRED THOUSAND */

    private static final double TWOMIL = 2000000.e0; /* TWO MILLION */

    private static final double MIN_EASTING = 100000;

    private static final double MAX_EASTING = 900000;

    private static final double MIN_NORTHING = 0;

    private static final double MAX_NORTHING = 10000000;

    private static final double MAX_PRECISION = 5; /*
     * Maximum precision of
     * easting & northing
     */

    private static final double MIN_UTM_LAT = ((-80 * PI) / 180.0); /*
     * -80
     * degrees
     * in
     * radians
     */

    private static final double MAX_UTM_LAT = ((84 * PI) / 180.0); /*
     * 84
     * degrees
     * in
     * radians
     */

    private static final double MIN_EAST_NORTH = 0;

    private static final double MAX_EAST_NORTH = 4000000;

    /*
     * CLARKE_1866 : Ellipsoid code for CLARKE_1866 CLARKE_1880 : Ellipsoid code
     * for CLARKE_1880 BESSEL_1841 : Ellipsoid code for BESSEL_1841
     * BESSEL_1841_NAMIBIA : Ellipsoid code for BESSEL 1841 (NAMIBIA)
     */
    private static final String CLARKE_1866 = "CC";

    private static final String CLARKE_1880 = "CD";

    private static final String BESSEL_1841 = "BR";

    private static final String BESSEL_1841_NAMIBIA = "BN";

    private static class Latitude_Band
    {
        int letter; /* letter representing latitude band */

        double min_northing; /* minimum northing for latitude band */

        double north; /* upper latitude for latitude band */

        double south; /* lower latitude for latitude band */

        public Latitude_Band(int letter, double min_northing, double north,
                double south)
        {
            this.letter = letter;
            this.min_northing = min_northing;
            this.north = north;
            this.south = south;
        }
    };

    private static final Latitude_Band Latitude_Band_Table[] = {
            new Latitude_Band(LETTER_C, 1100000.0, -72.0, -80.5),
            new Latitude_Band(LETTER_D, 2000000.0, -64.0, -72.0),
            new Latitude_Band(LETTER_E, 2800000.0, -56.0, -64.0),
            new Latitude_Band(LETTER_F, 3700000.0, -48.0, -56.0),
            new Latitude_Band(LETTER_G, 4600000.0, -40.0, -48.0),
            new Latitude_Band(LETTER_H, 5500000.0, -32.0, -40.0),
            new Latitude_Band(LETTER_J, 6400000.0, -24.0, -32.0),
            new Latitude_Band(LETTER_K, 7300000.0, -16.0, -24.0),
            new Latitude_Band(LETTER_L, 8200000.0, -8.0, -16.0),
            new Latitude_Band(LETTER_M, 9100000.0, 0.0, -8.0),
            new Latitude_Band(LETTER_N, 0.0, 8.0, 0.0),
            new Latitude_Band(LETTER_P, 800000.0, 16.0, 8.0),
            new Latitude_Band(LETTER_Q, 1700000.0, 24.0, 16.0),
            new Latitude_Band(LETTER_R, 2600000.0, 32.0, 24.0),
            new Latitude_Band(LETTER_S, 3500000.0, 40.0, 32.0),
            new Latitude_Band(LETTER_T, 4400000.0, 48.0, 40.0),
            new Latitude_Band(LETTER_U, 5300000.0, 56.0, 48.0),
            new Latitude_Band(LETTER_V, 6200000.0, 64.0, 56.0),
            new Latitude_Band(LETTER_W, 7000000.0, 72.0, 64.0),
            new Latitude_Band(LETTER_X, 7900000.0, 84.5, 72.0) };

    private static final class UPS_Constant
    {
        int letter; /* letter representing latitude band */

        int ltr2_low_value; /* 2nd letter range - high number */

        int ltr2_high_value; /* 2nd letter range - low number */

        int ltr3_high_value; /* 3rd letter range - high number (UPS) */

        double false_easting; /* False easting based on 2nd letter */

        double false_northing; /* False northing based on 3rd letter */

        private UPS_Constant(int letter, int ltr2_low_value,
                int ltr2_high_value, int ltr3_high_value, double false_easting,
                double false_northing)
        {
            this.letter = letter;
            this.ltr2_low_value = ltr2_low_value;
            this.ltr2_high_value = ltr2_high_value;
            this.ltr3_high_value = ltr3_high_value;
            this.false_easting = false_easting;
            this.false_northing = false_northing;
        }

    }

    private static final UPS_Constant UPS_Constant_Table[] = {
            new UPS_Constant(LETTER_A, LETTER_J, LETTER_Z, LETTER_Z, 800000.0,
                    800000.0),
            new UPS_Constant(LETTER_B, LETTER_A, LETTER_R, LETTER_Z, 2000000.0,
                    800000.0),
            new UPS_Constant(LETTER_Y, LETTER_J, LETTER_Z, LETTER_P, 800000.0,
                    1300000.0),
            new UPS_Constant(LETTER_Z, LETTER_A, LETTER_J, LETTER_P, 2000000.0,
                    1300000.0) };

    /** ************************************************************************ */

    /* Ellipsoid parameters, default to WGS 84 */
    private double MGRS_a = GeoTransConstants.WGS84_SEMI_MAJOR_AXIS; /*
     * Semi-major axis of ellipsoid in
     * meters
     */

    private double MGRS_f = GeoTransConstants.WGS84_FLATTENING; /* Flattening of ellipsoid */

    //private double MGRS_recpf = 298.257223563;

    private String MGRS_Ellipsoid_Code = "WE";

    public Mgrs()
    {

    }

    public Mgrs(double a, double f, String Ellipsoid_Code)
    /*
     * The function SET_MGRS_PARAMETERS receives the ellipsoid parameters and
     * sets the corresponding state variables. If any errors occur, the error
     * code(s) are returned by the function, otherwise MGRS_NO_ERROR is
     * returned.
     * 
     * a : Semi-major axis of ellipsoid in meters (input) f : Flattening of
     * ellipsoid (input) Ellipsoid_Code : 2-letter code for ellipsoid (input)
     */
    { /* Set_MGRS_Parameters */

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
        MGRS_a = a;
        MGRS_f = f;
        MGRS_Ellipsoid_Code = Ellipsoid_Code;
    } /* Set_MGRS_Parameters */

    private double Get_Latitude_Band_Min_Northing(int letter)
    /*
     * The function Get_Latitude_Band_Min_Northing receives a latitude band
     * letter and uses the Latitude_Band_Table to determine the minimum northing
     * for that latitude band letter.
     * 
     * letter : Latitude band letter (input) min_northing : Minimum northing for
     * that letter (output)
     */
    { /* Get_Latitude_Band_Min_Northing */

        if ((letter >= LETTER_C) && (letter <= LETTER_H))
            return Latitude_Band_Table[letter - 2].min_northing;
        else if ((letter >= LETTER_J) && (letter <= LETTER_N))
            return Latitude_Band_Table[letter - 3].min_northing;
        else if ((letter >= LETTER_P) && (letter <= LETTER_X))
            return Latitude_Band_Table[letter - 4].min_northing;
        else
            throw new IllegalArgumentException("Invalid MGRS letter");

    } /* Get_Latitude_Band_Min_Northing */

    private static final class LatitudeRange
    {
        double north;

        double south;
    }

    private LatitudeRange Get_Latitude_Range(int letter)
    /*
     * The function Get_Latitude_Range receives a latitude band letter and uses
     * the Latitude_Band_Table to determine the latitude band boundaries for
     * that latitude band letter.
     * 
     * letter : Latitude band letter (input) north : Northern latitude boundary
     * for that letter (output) north : Southern latitude boundary for that
     * letter (output)
     */
    { /* Get_Latitude_Range */
        LatitudeRange range = new LatitudeRange();
        if ((letter >= LETTER_C) && (letter <= LETTER_H))
        {
            range.north = Latitude_Band_Table[letter - 2].north * DEG_TO_RAD;
            range.south = Latitude_Band_Table[letter - 2].south * DEG_TO_RAD;
        }
        else if ((letter >= LETTER_J) && (letter <= LETTER_N))
        {
            range.north = Latitude_Band_Table[letter - 3].north * DEG_TO_RAD;
            range.south = Latitude_Band_Table[letter - 3].south * DEG_TO_RAD;
        }
        else if ((letter >= LETTER_P) && (letter <= LETTER_X))
        {
            range.north = Latitude_Band_Table[letter - 4].north * DEG_TO_RAD;
            range.south = Latitude_Band_Table[letter - 4].south * DEG_TO_RAD;
        }
        else
            throw new IllegalArgumentException("Invalid MGRS letter");

        return range;
    } /* Get_Latitude_Range */

    private int Get_Latitude_Letter(double latitude)
    /*
     * The function Get_Latitude_Letter receives a latitude value and uses the
     * Latitude_Band_Table to determine the latitude band letter for that
     * latitude.
     * 
     * latitude : Latitude (input) letter : Latitude band letter (output)
     */
    { /* Get_Latitude_Letter */
        int letter;
        double temp = 0.0;
        double lat_deg = latitude * RAD_TO_DEG;

        if (lat_deg >= 72 && lat_deg < 84.5)
            letter = LETTER_X;
        else if (lat_deg > -80.5 && lat_deg < 72)
        {
            temp = ((latitude + (80.0 * DEG_TO_RAD)) / (8.0 * DEG_TO_RAD)) + 1.0e-12;
            letter = Latitude_Band_Table[(int) temp].letter;
        }
        else
            throw new IllegalArgumentException("Invalid MGRS latitude");

        return letter;
    } /* Get_Latitude_Letter */

    private boolean Check_Zone(String MGRS)
    /*
     * The function Check_Zone receives an MGRS coordinate string. If a zone is
     * given, true is returned. Otherwise, false is returned.
     * 
     * MGRS : MGRS coordinate string (input) zone_exists : true if a zone is
     * given, false if a zone is not given (output)
     */
    { /* Check_Zone */
        int i = 0;
        int j = 0;
        int num_digits = 0;

        /* skip any leading blanks */
        while (i < MGRS.length() && MGRS.charAt(i) == ' ')
            i++;
        j = i;
        while (i < MGRS.length() && Character.isDigit(MGRS.charAt(i)))
            i++;
        num_digits = i - j;
        if (num_digits <= 2)
            if (num_digits > 0)
                return true;
            else
                return false;
        else
            throw new IllegalArgumentException("Invalid MGRS string");
    } /* Check_Zone */

    private long Round_MGRS(double value)
    /*
     * The function Round_MGRS rounds the input value to the nearest integer,
     * using the standard engineering rule. The rounded integer value is then
     * returned.
     * 
     * value : Value to be rounded (input)
     */
    { /* Round_MGRS */
        double ivalue = Math.floor(value);
        double fraction = value - ivalue;
        long ival = (long) ivalue;
        if ((fraction > 0.5) || ((fraction == 0.5) && (ival % 2 == 1)))
            ival++;
        return (ival);
    } /* Round_MGRS */

    static String format(long value, long precision)
    {
        StringBuffer b = new StringBuffer();
        for (long i = 0; i < precision; ++i)
        {
            b.append('0');
        }
        DecimalFormat f = new DecimalFormat(b.toString());
        return f.format(value);
    }

    private String Make_MGRS_String(long Zone, int Letters[], double Easting,
            double Northing, long Precision)
    /*
     * The function Make_MGRS_String constructs an MGRS string from its
     * component parts.
     * 
     * MGRS : MGRS coordinate string (output) Zone : UTM Zone (input) Letters :
     * MGRS coordinate string letters (input) Easting : Easting value (input)
     * Northing : Northing value (input) Precision : Precision level of MGRS
     * string (input)
     */
    { /* Make_MGRS_String */
        int j;
        double divisor;
        long east;
        long north;
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        StringBuffer MGRS = new StringBuffer();
        if (Zone != 0)
        {
            MGRS.append(format(Zone, 2));
            // i = sprintf (MGRS+i,"%2.2ld",Zone);
        }
        else
        {
            MGRS.append("  "); // 2 spaces
        }

        for (j = 0; j < 3; j++)
        {
            MGRS.append(alphabet.charAt(Letters[j]));
        }
        divisor = pow(10.0, (5 - Precision));

        Easting = fmod(Easting, 100000.0);
        if (Easting >= 99999.5)
            Easting = 99999.0;
        east = (long) (Easting / divisor);
        MGRS.append(format(east, Precision));
        // i += sprintf (MGRS+i, "%*.*ld", Precision, Precision, east);
        Northing = fmod(Northing, 100000.0);
        if (Northing >= 99999.5)
            Northing = 99999.0;
        north = (long) (Northing / divisor);
        MGRS.append(format(north, Precision));
        // i += sprintf (MGRS+i, "%*.*ld", Precision, Precision, north);

        return MGRS.toString();
    } /* Make_MGRS_String */

    private static class Point
    {
        int Zone;

        int Letters[] = new int[MGRS_LETTERS];

        double Easting;

        double Northing;

        long Precision;
    }

    private Point Break_MGRS_String(String MGRS)
    /*
     * The function Break_MGRS_String breaks down an MGRS coordinate string into
     * its component parts.
     * 
     * MGRS : MGRS coordinate string (input) Zone : UTM Zone (output) Letters :
     * MGRS coordinate string letters (output) Easting : Easting value (output)
     * Northing : Northing value (output) Precision : Precision level of MGRS
     * string (output)
     */
    { /* Break_MGRS_String */
        int num_digits;
        int num_letters;
        int i = 0;
        int j = 0;

        Point point = new Point();
        while (i < MGRS.length() && MGRS.charAt(i) == ' ')
            i++; /* skip any leading blanks */
        j = i;
        while (i < MGRS.length() && Character.isDigit(MGRS.charAt(i)))
            i++;
        num_digits = i - j;
        if (num_digits <= 2)
            if (num_digits > 0)
            {
                String zone_string = MGRS.substring(j, j + 2);
                point.Zone = Integer.valueOf(zone_string);
                if ((point.Zone < 1) || (point.Zone > 60))
                    throw new IllegalArgumentException("Invalid MGRS zone '"
                            + zone_string + "'");
            }
            else
                point.Zone = 0;
        else
            throw new IllegalArgumentException("Invalid MGRS string");
        j = i;

        while (i < MGRS.length() && Character.isLetter(MGRS.charAt(i)))
            i++;
        num_letters = i - j;
        if (num_letters == 3)
        {
            /* get letters */
            point.Letters[0] = (Character.toUpperCase(MGRS.charAt(j)) - 'A');
            if ((point.Letters[0] == LETTER_I)
                    || (point.Letters[0] == LETTER_O))
                throw new IllegalArgumentException("Invalid MGRS string");
            point.Letters[1] = (Character.toUpperCase(MGRS.charAt(j + 1)) - 'A');
            if ((point.Letters[1] == LETTER_I)
                    || (point.Letters[1] == LETTER_O))
                throw new IllegalArgumentException("Invalid MGRS string");
            point.Letters[2] = (Character.toUpperCase(MGRS.charAt(j + 2)) - 'A');
            if ((point.Letters[2] == LETTER_I)
                    || (point.Letters[2] == LETTER_O))
                throw new IllegalArgumentException("Invalid MGRS string");
        }
        else
            throw new IllegalArgumentException("Invalid MGRS string");
        j = i;
        while (i < MGRS.length() && Character.isDigit(MGRS.charAt(i)))
            i++;
        num_digits = i - j;
        if ((num_digits <= 10) && (num_digits % 2 == 0))
        {
            int n;
            long east;
            long north;
            double multiplier;
            /* get easting & northing */
            n = num_digits / 2;
            point.Precision = n;
            if (n > 0)
            {
                String east_string = MGRS.substring(j, j + n);
                east = Long.valueOf(east_string);

                String north_string = MGRS.substring(j + n, j + n + n);
                north = Long.valueOf(north_string);

                multiplier = pow(10.0, 5 - n);
                point.Easting = east * multiplier;
                point.Northing = north * multiplier;
            }
            else
            {
                point.Easting = 0.0;
                point.Northing = 0.0;
            }
        }
        else
            throw new IllegalArgumentException("Invalid MGRS string");

        return point;
    } /* Break_MGRS_String */

    private static class GridValues
    {
        int ltr2_low_value;

        int ltr2_high_value;

        double false_northing;
    }

    private GridValues Get_Grid_Values(long zone)
    /*
     * The function Get_Grid_Values sets the letter range used for the 2nd
     * letter in the MGRS coordinate string, based on the set number of the utm
     * zone. It also sets the false northing using a value of A for the second
     * letter of the grid square, based on the grid pattern and set number of
     * the utm zone.
     * 
     * zone : Zone number (input) ltr2_low_value : 2nd letter low number
     * (output) ltr2_high_value : 2nd letter high number (output) false_northing :
     * False northing (output)
     */
    { /* BEGIN Get_Grid_Values */
        GridValues values = new GridValues();
        long set_number; /* Set number (1-6) based on UTM zone number */
        boolean aa_pattern; /* Pattern based on ellipsoid code */

        set_number = zone % 6;

        if (set_number == 0)
            set_number = 6;

        if (MGRS_Ellipsoid_Code.equals(CLARKE_1866)
                || MGRS_Ellipsoid_Code.equals(CLARKE_1880)
                || MGRS_Ellipsoid_Code.equals(BESSEL_1841)
                || MGRS_Ellipsoid_Code.equals(BESSEL_1841_NAMIBIA))
            aa_pattern = false;
        else
            aa_pattern = true;

        if ((set_number == 1) || (set_number == 4))
        {
            values.ltr2_low_value = LETTER_A;
            values.ltr2_high_value = LETTER_H;
        }
        else if ((set_number == 2) || (set_number == 5))
        {
            values.ltr2_low_value = LETTER_J;
            values.ltr2_high_value = LETTER_R;
        }
        else if ((set_number == 3) || (set_number == 6))
        {
            values.ltr2_low_value = LETTER_S;
            values.ltr2_high_value = LETTER_Z;
        }

        /* False northing at A for second letter of grid square */
        if (aa_pattern)
        {
            if ((set_number % 2) == 0)
                values.false_northing = 1500000.0;
            else
                values.false_northing = 0.0;
        }
        else
        {
            if ((set_number % 2) == 0)
                values.false_northing = 500000.0;
            else
                values.false_northing = 1000000.00;
        }
        return values;
    } /* END OF Get_Grid_Values */

    public String UTM_To_MGRS(long Zone, double Latitude, double Easting,
            double Northing, long Precision)
    /*
     * The function UTM_To_MGRS calculates an MGRS coordinate string based on
     * the zone, latitude, easting and northing.
     * 
     * Zone : Zone number (input) Latitude : Latitude in radians (input) Easting :
     * Easting (input) Northing : Northing (input) Precision : Precision (input)
     * MGRS : MGRS coordinate string (output)
     */
    { /* BEGIN UTM_To_MGRS */
        double grid_easting; /* Easting used to derive 2nd letter of MGRS */
        double grid_northing; /* Northing used to derive 3rd letter of MGRS */
        int letters[] = new int[MGRS_LETTERS]; /*
         * Number location of 3 letters
         * in alphabet
         */
        double divisor;

        /* Round easting and northing values */
        divisor = pow(10.0, (5 - Precision));
        Easting = Round_MGRS(Easting / divisor) * divisor;
        Northing = Round_MGRS(Northing / divisor) * divisor;
        GridValues values = Get_Grid_Values(Zone);

        letters[0] = Get_Latitude_Letter(Latitude);

        grid_northing = Northing;
        if (grid_northing == 1.e7)
            grid_northing = grid_northing - 1.0;

        while (grid_northing >= TWOMIL)
        {
            grid_northing = grid_northing - TWOMIL;
        }
        grid_northing = grid_northing - values.false_northing;

        if (grid_northing < 0.0)
            grid_northing = grid_northing + TWOMIL;

        letters[2] = (int) (grid_northing / ONEHT);
        if (letters[2] > LETTER_H)
            letters[2] = letters[2] + 1;

        if (letters[2] > LETTER_N)
            letters[2] = letters[2] + 1;

        grid_easting = Easting;
        if (((letters[0] == LETTER_V) && (Zone == 31))
                && (grid_easting == 500000.0))
            grid_easting = grid_easting - 1.0; /* SUBTRACT 1 METER */

        letters[1] = values.ltr2_low_value + ((int) (grid_easting / ONEHT) - 1);
        if ((values.ltr2_low_value == LETTER_J) && (letters[1] > LETTER_N))
            letters[1] = letters[1] + 1;

        return Make_MGRS_String(Zone, letters, Easting, Northing, Precision);
    } /* END UTM_To_MGRS */

    // void Get_MGRS_Parameters (double *a,
    // double *f,
    // char* Ellipsoid_Code)
    // /*
    // * The function Get_MGRS_Parameters returns the current ellipsoid
    // * parameters.
    // *
    // * a : Semi-major axis of ellipsoid, in meters (output)
    // * f : Flattening of ellipsoid (output)
    // * Ellipsoid_Code : 2-letter code for ellipsoid (output)
    // */
    // { /* Get_MGRS_Parameters */
    // *a = MGRS_a;
    // *f = MGRS_f;
    // strcpy (Ellipsoid_Code, MGRS_Ellipsoid_Code);
    // return;
    // } /* Get_MGRS_Parameters */

    public String fromGeodetic(double Latitude, double Longitude,
            long Precision)
    /*
     * The function Convert_Geodetic_To_MGRS converts Geodetic (latitude and
     * longitude) coordinates to an MGRS coordinate string, according to the
     * current ellipsoid parameters. If any errors occur, the error code(s) are
     * returned by the function, otherwise MGRS_NO_ERROR is returned.
     * 
     * Latitude : Latitude in radians (input) Longitude : Longitude in radians
     * (input) Precision : Precision level of MGRS string (input) MGRS : MGRS
     * coordinate string (output)
     * 
     */
    { /* Convert_Geodetic_To_MGRS */
        if ((Latitude < -PI_OVER_2) || (Latitude > PI_OVER_2))
        { /* Latitude out of range */
            throw new IllegalArgumentException("Latitude out of range");
        }
        if ((Longitude < -PI) || (Longitude > (2 * PI)))
        { /* Longitude out of range */
            throw new IllegalArgumentException("Longitude out of range");
        }
        if ((Precision < 0) || (Precision > MAX_PRECISION))
            throw new IllegalArgumentException("Invalid prevision");

        if ((Latitude < MIN_UTM_LAT) || (Latitude > MAX_UTM_LAT))
        {
            UniversalPolarStereographic ups = new UniversalPolarStereographic(MGRS_a, MGRS_f);
            UniversalPolarStereographic.Point upsPoint = ups
                    .fromGeodetic(Latitude, Longitude);
            return fromUps(upsPoint.Hemisphere, upsPoint.Easting,
                    upsPoint.Northing, Precision);
        }
        else
        {
            UniversalTransverseMercator utm = new UniversalTransverseMercator(MGRS_a, MGRS_f, 0);
            UniversalTransverseMercator.Point utmPoint = utm
                    .fromGeodetic(Latitude, Longitude);
            return UTM_To_MGRS(utmPoint.Zone, Latitude, utmPoint.Easting,
                    utmPoint.Northing, Precision);
        }

    } /* Convert_Geodetic_To_MGRS */

    public Geodetic.Point toGeodetic(String MGRS)
    /*
     * The function Convert_MGRS_To_Geodetic converts an MGRS coordinate string
     * to Geodetic (latitude and longitude) coordinates according to the current
     * ellipsoid parameters. If any errors occur, the error code(s) are returned
     * by the function, otherwise UTM_NO_ERROR is returned.
     * 
     * MGRS : MGRS coordinate string (input) Latitude : Latitude in radians
     * (output) Longitude : Longitude in radians (output)
     * 
     */
    { /* Convert_MGRS_To_Geodetic */
        boolean zone_exists;

        Geodetic.Point point;
        zone_exists = Check_Zone(MGRS);
        if (zone_exists)
        {
            UniversalTransverseMercator.Point utmPoint = toUtm(MGRS);
            UniversalTransverseMercator utm = new UniversalTransverseMercator(MGRS_a, MGRS_f, 0);
            point = utm.toGeodetic(utmPoint.Zone,
                    utmPoint.Hemisphere, utmPoint.Easting, utmPoint.Northing);
        }
        else
        {
            UniversalPolarStereographic.Point upsPoint = toUps(MGRS);
            UniversalPolarStereographic ups = new UniversalPolarStereographic(MGRS_a, MGRS_f);
            point = ups.toGeodetic(upsPoint.Hemisphere,
                    upsPoint.Easting, upsPoint.Northing);
        }
        return point;
    } /* END OF Convert_MGRS_To_Geodetic */

    public String fromUtm(long Zone, char Hemisphere,
            double Easting, double Northing, long Precision)
    /*
     * The function Convert_UTM_To_MGRS converts UTM (zone, easting, and
     * northing) coordinates to an MGRS coordinate string, according to the
     * current ellipsoid parameters. If any errors occur, the error code(s) are
     * returned by the function, otherwise MGRS_NO_ERROR is returned.
     * 
     * Zone : UTM zone (input) Hemisphere : North or South hemisphere (input)
     * Easting : Easting (X) in meters (input) Northing : Northing (Y) in meters
     * (input) Precision : Precision level of MGRS string (input) MGRS : MGRS
     * coordinate string (output)
     */
    { /* Convert_UTM_To_MGRS */

        if ((Zone < 1) || (Zone > 60))
            throw new IllegalArgumentException("Invalid MGRS zone");
        if ((Hemisphere != 'S') && (Hemisphere != 'N'))
            throw new IllegalArgumentException("Invalid hemisphere");
        if ((Easting < MIN_EASTING) || (Easting > MAX_EASTING))
            throw new IllegalArgumentException("Invalid easting");
        if ((Northing < MIN_NORTHING) || (Northing > MAX_NORTHING))
            throw new IllegalArgumentException("Invalid northing");
        if ((Precision < 0) || (Precision > MAX_PRECISION))
            throw new IllegalArgumentException("Invalid precision");

        UniversalTransverseMercator utm = new UniversalTransverseMercator(MGRS_a, MGRS_f, 0);
        Geodetic.Point llaPoint = utm.toGeodetic(Zone, Hemisphere,
                Easting, Northing);

        /* Special check for rounding to (truncated) eastern edge of zone 31V */
        if ((Zone == 31) && (llaPoint.latitude >= 56.0 * DEG_TO_RAD)
                && (llaPoint.latitude < 64.0 * DEG_TO_RAD)
                && (llaPoint.longitude >= 3.0 * DEG_TO_RAD))
        { /* Reconvert to UTM zone 32 */
            utm = new UniversalTransverseMercator(MGRS_a, MGRS_f, 32);
            UniversalTransverseMercator.Point utmPoint = utm
                    .fromGeodetic(llaPoint.latitude,
                            llaPoint.longitude);
            Zone = utmPoint.Zone;
            Easting = utmPoint.Easting;
            Northing = utmPoint.Northing;
            Hemisphere = utmPoint.Hemisphere;
        }

        return UTM_To_MGRS(Zone, llaPoint.latitude, Easting, Northing,
                Precision);
    } /* Convert_UTM_To_MGRS */

    public UniversalTransverseMercator.Point toUtm(String MGRS)
    /*
     * The function Convert_MGRS_To_UTM converts an MGRS coordinate string to
     * UTM projection (zone, hemisphere, easting and northing) coordinates
     * according to the current ellipsoid parameters. If any errors occur, the
     * error code(s) are returned by the function, otherwise UTM_NO_ERROR is
     * returned.
     * 
     * MGRS : MGRS coordinate string (input) Zone : UTM zone (output) Hemisphere :
     * North or South hemisphere (output) Easting : Easting (X) in meters
     * (output) Northing : Northing (Y) in meters (output)
     */
    { /* Convert_MGRS_To_UTM */
        double scaled_min_northing;
        double min_northing;
        double grid_easting; /* Easting for 100,000 meter grid square */
        double grid_northing; /* Northing for 100,000 meter grid square */
        int letters[];
        double divisor = 1.0;

        Point point = Break_MGRS_String(MGRS);
        letters = point.Letters;
        UniversalTransverseMercator.Point utmPoint = new UniversalTransverseMercator.Point();
        utmPoint.Easting = point.Easting;
        utmPoint.Northing = point.Northing;
        utmPoint.Zone = point.Zone;

        if (point.Zone == 0)
            throw new IllegalArgumentException("MGRS string error");

        if ((letters[0] == LETTER_X)
                && ((point.Zone == 32) || (point.Zone == 34) || (point.Zone == 36)))
            throw new IllegalArgumentException("MGRS string error");

        if (letters[0] < LETTER_N)
            utmPoint.Hemisphere = 'S';
        else
            utmPoint.Hemisphere = 'N';

        GridValues gridValues = Get_Grid_Values(point.Zone);

        /*
         * Check that the second letter of the MGRS string is within the range
         * of valid second letter values Also check that the third letter is
         * valid
         */
        if ((letters[1] < gridValues.ltr2_low_value)
                || (letters[1] > gridValues.ltr2_high_value)
                || (letters[2] > LETTER_V))
            throw new IllegalArgumentException("MGRS string error");

        grid_northing = (double) (letters[2]) * ONEHT
                + gridValues.false_northing;
        grid_easting = (double) ((letters[1]) - gridValues.ltr2_low_value + 1)
                * ONEHT;
        if ((gridValues.ltr2_low_value == LETTER_J) && (letters[1] > LETTER_O))
            grid_easting = grid_easting - ONEHT;

        if (letters[2] > LETTER_O)
            grid_northing = grid_northing - ONEHT;

        if (letters[2] > LETTER_I)
            grid_northing = grid_northing - ONEHT;

        if (grid_northing >= TWOMIL)
            grid_northing = grid_northing - TWOMIL;

        min_northing = Get_Latitude_Band_Min_Northing(letters[0]);
        scaled_min_northing = min_northing;
        while (scaled_min_northing >= TWOMIL)
        {
            scaled_min_northing = scaled_min_northing - TWOMIL;
        }

        grid_northing = grid_northing - scaled_min_northing;
        if (grid_northing < 0.0)
            grid_northing = grid_northing + TWOMIL;

        grid_northing = min_northing + grid_northing;

        utmPoint.Easting = grid_easting + utmPoint.Easting;
        utmPoint.Northing = grid_northing + utmPoint.Northing;

        /* check that point is within Zone Letter bounds */
        UniversalTransverseMercator utm = new UniversalTransverseMercator(MGRS_a, MGRS_f, point.Zone);
        Geodetic.Point llaPoint = utm.toGeodetic(point.Zone,
                utmPoint.Hemisphere, utmPoint.Easting, utmPoint.Northing);
        divisor = pow(10.0, point.Precision);
        LatitudeRange latRange = Get_Latitude_Range(letters[0]);
        if (!(((latRange.south - DEG_TO_RAD / divisor) <= llaPoint.latitude) && (llaPoint.latitude <= (latRange.north + DEG_TO_RAD
                / divisor))))
        {
            throw new IllegalArgumentException("MGRS latitude error");
        }

        return utmPoint;
    } /* Convert_MGRS_To_UTM */

    public String fromUps(char Hemisphere, double Easting,
            double Northing, long Precision)
    /*
     * The function Convert_UPS_To_MGRS converts UPS (hemisphere, easting, and
     * northing) coordinates to an MGRS coordinate string according to the
     * current ellipsoid parameters. If any errors occur, the error code(s) are
     * returned by the function, otherwise UPS_NO_ERROR is returned.
     * 
     * Hemisphere : Hemisphere either 'N' or 'S' (input) Easting : Easting/X in
     * meters (input) Northing : Northing/Y in meters (input) Precision :
     * Precision level of MGRS string (input) MGRS : MGRS coordinate string
     * (output)
     */
    { /* Convert_UPS_To_MGRS */
        double false_easting; /* False easting for 2nd letter */
        double false_northing; /* False northing for 3rd letter */
        double grid_easting; /* Easting used to derive 2nd letter of MGRS */
        double grid_northing; /* Northing used to derive 3rd letter of MGRS */
        int ltr2_low_value; /* 2nd letter range - low number */
        double divisor;
        int index = 0;
        int letters[] = new int[MGRS_LETTERS];

        if ((Hemisphere != 'N') && (Hemisphere != 'S'))
            throw new IllegalArgumentException("Invalid hemisphere");
        if ((Easting < MIN_EAST_NORTH) || (Easting > MAX_EAST_NORTH))
            throw new IllegalArgumentException("Iinvalid easting");
        if ((Northing < MIN_EAST_NORTH) || (Northing > MAX_EAST_NORTH))
            throw new IllegalArgumentException("Invalid northing");
        if ((Precision < 0) || (Precision > MAX_PRECISION))
            throw new IllegalArgumentException("Invalid precision");

        divisor = pow(10.0, (5 - Precision));
        Easting = Round_MGRS(Easting / divisor) * divisor;
        Northing = Round_MGRS(Northing / divisor) * divisor;

        if (Hemisphere == 'N')
        {
            if (Easting >= TWOMIL)
                letters[0] = LETTER_Z;
            else
                letters[0] = LETTER_Y;

            index = letters[0] - 22;
            ltr2_low_value = UPS_Constant_Table[index].ltr2_low_value;
            false_easting = UPS_Constant_Table[index].false_easting;
            false_northing = UPS_Constant_Table[index].false_northing;
        }
        else
        {
            if (Easting >= TWOMIL)
                letters[0] = LETTER_B;
            else
                letters[0] = LETTER_A;

            ltr2_low_value = UPS_Constant_Table[letters[0]].ltr2_low_value;
            false_easting = UPS_Constant_Table[letters[0]].false_easting;
            false_northing = UPS_Constant_Table[letters[0]].false_northing;
        }

        grid_northing = Northing;
        grid_northing = grid_northing - false_northing;
        letters[2] = (int) (grid_northing / ONEHT);

        if (letters[2] > LETTER_H)
            letters[2] = letters[2] + 1;

        if (letters[2] > LETTER_N)
            letters[2] = letters[2] + 1;

        grid_easting = Easting;
        grid_easting = grid_easting - false_easting;
        letters[1] = ltr2_low_value + ((int) (grid_easting / ONEHT));

        if (Easting < TWOMIL)
        {
            if (letters[1] > LETTER_L)
                letters[1] = letters[1] + 3;

            if (letters[1] > LETTER_U)
                letters[1] = letters[1] + 2;
        }
        else
        {
            if (letters[1] > LETTER_C)
                letters[1] = letters[1] + 2;

            if (letters[1] > LETTER_H)
                letters[1] = letters[1] + 1;

            if (letters[1] > LETTER_L)
                letters[1] = letters[1] + 3;
        }

        return Make_MGRS_String(0, letters, Easting, Northing, Precision);

    } /* Convert_UPS_To_MGRS */

    public UniversalPolarStereographic.Point toUps(String MGRS)
    /*
     * The function Convert_MGRS_To_UPS converts an MGRS coordinate string to
     * UPS (hemisphere, easting, and northing) coordinates, according to the
     * current ellipsoid parameters. If any errors occur, the error code(s) are
     * returned by the function, otherwide UPS_NO_ERROR is returned.
     * 
     * MGRS : MGRS coordinate string (input) Hemisphere : Hemisphere either 'N'
     * or 'S' (output) Easting : Easting/X in meters (output) Northing :
     * Northing/Y in meters (output)
     */
    { /* Convert_MGRS_To_UPS */
        long ltr2_high_value; /* 2nd letter range - high number */
        long ltr3_high_value; /* 3rd letter range - high number (UPS) */
        long ltr2_low_value; /* 2nd letter range - low number */
        double false_easting; /* False easting for 2nd letter */
        double false_northing; /* False northing for 3rd letter */
        double grid_easting; /* easting for 100,000 meter grid square */
        double grid_northing; /* northing for 100,000 meter grid square */
        int index = 0;

        Point mgrsPoint = Break_MGRS_String(MGRS);
        if (mgrsPoint.Zone != 0)
            throw new IllegalArgumentException("Invalid MGRS string");

        UniversalPolarStereographic.Point upsPoint = new UniversalPolarStereographic.Point();
        if (mgrsPoint.Letters[0] >= LETTER_Y)
        {
            upsPoint.Hemisphere = 'N';

            index = mgrsPoint.Letters[0] - 22;
            ltr2_low_value = UPS_Constant_Table[index].ltr2_low_value;
            ltr2_high_value = UPS_Constant_Table[index].ltr2_high_value;
            ltr3_high_value = UPS_Constant_Table[index].ltr3_high_value;
            false_easting = UPS_Constant_Table[index].false_easting;
            false_northing = UPS_Constant_Table[index].false_northing;
        }
        else
        {
            upsPoint.Hemisphere = 'S';

            ltr2_low_value = UPS_Constant_Table[mgrsPoint.Letters[0]].ltr2_low_value;
            ltr2_high_value = UPS_Constant_Table[mgrsPoint.Letters[0]].ltr2_high_value;
            ltr3_high_value = UPS_Constant_Table[mgrsPoint.Letters[0]].ltr3_high_value;
            false_easting = UPS_Constant_Table[mgrsPoint.Letters[0]].false_easting;
            false_northing = UPS_Constant_Table[mgrsPoint.Letters[0]].false_northing;
        }

        /*
         * Check that the second letter of the MGRS string is within the range
         * of valid second letter values Also check that the third letter is
         * valid
         */
        if ((mgrsPoint.Letters[1] < ltr2_low_value)
                || (mgrsPoint.Letters[1] > ltr2_high_value)
                || ((mgrsPoint.Letters[1] == LETTER_D)
                        || (mgrsPoint.Letters[1] == LETTER_E)
                        || (mgrsPoint.Letters[1] == LETTER_M)
                        || (mgrsPoint.Letters[1] == LETTER_N)
                        || (mgrsPoint.Letters[1] == LETTER_V) || (mgrsPoint.Letters[1] == LETTER_W))
                || (mgrsPoint.Letters[2] > ltr3_high_value))
        {
            throw new IllegalArgumentException("Invalid MGRS string");
        }

        grid_northing = (double) mgrsPoint.Letters[2] * ONEHT + false_northing;
        if (mgrsPoint.Letters[2] > LETTER_I)
            grid_northing = grid_northing - ONEHT;

        if (mgrsPoint.Letters[2] > LETTER_O)
            grid_northing = grid_northing - ONEHT;

        grid_easting = (double) ((mgrsPoint.Letters[1]) - ltr2_low_value)
                * ONEHT + false_easting;
        if (ltr2_low_value != LETTER_A)
        {
            if (mgrsPoint.Letters[1] > LETTER_L)
                grid_easting = grid_easting - 300000.0;

            if (mgrsPoint.Letters[1] > LETTER_U)
                grid_easting = grid_easting - 200000.0;
        }
        else
        {
            if (mgrsPoint.Letters[1] > LETTER_C)
                grid_easting = grid_easting - 200000.0;

            if (mgrsPoint.Letters[1] > LETTER_I)
                grid_easting = grid_easting - ONEHT;

            if (mgrsPoint.Letters[1] > LETTER_L)
                grid_easting = grid_easting - 300000.0;
        }

        upsPoint.Easting = grid_easting + mgrsPoint.Easting;
        upsPoint.Northing = grid_northing + mgrsPoint.Northing;

        return upsPoint;
    } /* Convert_MGRS_To_UPS */

}
