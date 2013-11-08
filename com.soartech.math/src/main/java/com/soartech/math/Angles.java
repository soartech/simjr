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
 * Created on Apr 22, 2010
 */
package com.soartech.math;

public class Angles
{
    /**
     * Bound an angle in degrees to the range (-180, 180].
     * 
     * @param v An angle in degrees
     * @return angle in the range (-180, 180].
     */
    public static double boundedAngleDegrees(double v)
    {
        while(v <= -180) v += 360;
        while(v > 180) v -= 360;
        return v;
    }
    
    /**
     * Bound an angle in degrees to the range (0, 360].
     * 
     * @param v An angle in degrees
     * @return angle in the range (0, 360].
     */
    public static double boundedPositiveAngleDegrees(double v)
    {
        while(v <= 0) v += 360;
        while(v > 360) v -= 360;
        return v;
    }
    
    /**
     * Bound an angle in radians to the range (-pi, pi].
     * 
     * @param v An angle in radians
     * @return angle in the range (-pi, pi].
     */
    public static double boundedAngleRadians(double v)
    {
        while(v <= -Math.PI) v += 2 * Math.PI;
        while(v > Math.PI) v -= 2 * Math.PI;
        return v;
    }
    
    /**
     * Bound an angle in radians to the range (0, 2*pi].
     * 
     * @param v An angle in radians
     * @return angle in the range (0, 2*pi].
     */
    public static double boundedPositiveAngleRadians(double v)
    {
        while(v <= 0.0) v += 2 * Math.PI;
        while(v > 2*Math.PI) v -= 2 * Math.PI;
        return v;
    }
    
    /**
     * Calculate bearing from a velocity vector in radians
     * 
     * @param velocity Input velocity vector
     * @return The bearing in radians. Returns 0 if velocity is zero.
     */
    public static double getBearing(Vector3 velocity)
    {
        // Y and X are intentionally switched here because we want 0 degrees to
        // be pointing north.
        return Math.atan2(velocity.x, velocity.y);
    }
    
    public static double mathRadiansToNavRadians(double angle)
    {
        // Convert from Math angle (CCW from east) to navigational angle (CW from north)
        return boundedAngleRadians(Math.PI / 2.0 - angle);
    }
    
    public static double navRadiansToMathRadians(double angle)
    {
        // Convert from navigational angle (CW from north) to Math angle (CCW from east)
        return boundedAngleRadians(Math.PI / 2.0 - angle);
    }
    
    /**
     * Safely calculate the difference between two radian angles
     * 
     * @param angle First angle in radians
     * @param otherAngle Second angle in radians
     * @return Difference in radians in range (-pi, pi)
     */
    public static double angleDifference(double angle, double otherAngle)
    {
        return boundedAngleRadians(angle - otherAngle);
    }
    
    /**
     * Safely calculate the difference between two radian angles, with an optional specification
     * of whether to go left or right from the second angle to the first angle.  Careful here.
     * This method is using "math" angles, so left/right signs are switched from "navigation" angles.
     * 
     * @param angle First angle in radians
     * @param otherAngle Second angle in radians
     * @param turnDir the string "left" or "right".  If this has some other value, just use the shortest direction.
     * @return Difference in radians in range (-pi, pi)
     */
    public static double angleDifference(double angle, double otherAngle, String turnDir)
    {
        if (turnDir != null)
        {
            if (turnDir.equals("right"))
            {
                return -boundedPositiveAngleRadians(otherAngle - angle);
            } else if (turnDir.equals("left"))
            {
                return boundedPositiveAngleRadians(angle - otherAngle);
            } 
        }
        return angleDifference(angle, otherAngle);
    }
    

}
