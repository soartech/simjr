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
 * Created on May 22, 2006
 */
package com.soartech.math;



/**
 * @author ray
 */
public class Vector3
{
    public static final Vector3 ZERO = new Vector3(0.0, 0.0, 0.0);
    public static final Vector3 X_UNIT = new Vector3(1.0, 0.0, 0.0);
    public static final Vector3 Y_UNIT = new Vector3(0.0, 1.0, 0.0);
    public static final Vector3 Z_UNIT = new Vector3(0.0, 0.0, 1.0);
    
    public final double x, y, z;
    
    /**
     * Parse a vector from a string. The format of the string is three comma-
     * delimited numeric strings surrounded by optional parentheses.
     * 
     * @param inputObject Input object. If it's a vector, it is returned. Otherwise,
     *      the result of its toString() method is parsed.
     * @return Parsed vector
     * @throws IllegalArgumentException If there is an error in the input
     */
    public static Vector3 parseVector(Object inputObject)
    {
        if(inputObject instanceof Vector3)
        {
            return (Vector3) inputObject;
        }
        
        String input = inputObject.toString().trim();
        if(input.length() < 2)
        {
            throw new IllegalArgumentException("Invalid vector string '" + input + "'");
        }
        
        char start = input.charAt(0);
        char end = input.charAt(input.length() - 1);
        if(start == '(' && end == ')')
        {
            input = input.substring(1, input.length() - 1).trim();
        }
        else if(start == '(' || end == ')')
        {
            throw new IllegalArgumentException("Unbalanced parentheses in '" + input + "'");
        }
        
        final String[] parts = input.split("\\s*,\\s*", -1);
        if(parts.length != 3)
        {
            throw new IllegalArgumentException("Expected three vector components, got " + parts.length);
        }
        
        try
        {
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            
            return new Vector3(x, y, z);
        }
        catch(NumberFormatException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Linearly interpolate between two vectors
     * 
     * @param v0 Start vector, returned when {@code u} == 0.
     * @param v1 End vector, returned when {@code u} == 1.
     * @param u The interpolation parameter
     * @return Interpolated vector.
     */
    public static Vector3 interpolate(Vector3 v0, Vector3 v1, double u)
    {
        return v0.multiply(1 - u).add(v1.multiply(u));
    }
    
    /**
     * Calculate XY distance between two points
     * 
     * @param a First point 
     * @param b Second point
     * @return Distance between points ignoring Z component.
     */
    public static double getLateralDistance(Vector3 a, Vector3 b)
    {
        return new Vector3(a.x, a.y, 0.0).distance(new Vector3(b.x, b.y, 0.0));
    }
    
    /**
     * @param x
     * @param y
     * @param z
     */
    public Vector3(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Retrieve a component of the vector by index where 0 returns x, 1 returns
     * y and 2 returns z.
     * 
     * @param i The index of the component to retrieve
     * @return The component at the desired index
     * @throws IndexOutOfBoundsException if {@code i} is not 0, 1, or 2
     */
    public double get(int i)
    {
        if(i == 0) return x;
        else if(i == 1) return y;
        else if(i == 2) return z;
        
        throw new IndexOutOfBoundsException("i must be 0, 1, or 2");
    }

    public double lengthSquared()
    {
        return x * x + y * y + z * z;
    }
    
    public double length()
    {
        return Math.sqrt(lengthSquared());
    }
    
    public double distanceSquared(Vector3 other)
    {
        Vector3 d = new Vector3(x - other.x, y - other.y, z - other.z);
        return d.lengthSquared();
    }

    public double distance(Vector3 other)
    {
        return Math.sqrt(distanceSquared(other));
    }
    
    public Vector3 add(Vector3 other)
    {
        return this != ZERO ? 
                new Vector3(x + other.x, y + other.y, z + other.z) : 
                other;
    }
    
    public Vector3 subtract(Vector3 other)
    {
        return other != ZERO ? 
                new Vector3(x - other.x, y - other.y, z - other.z) : 
                this;
    }
    
    
    public Vector3 multiply(double s)
    {
        return this != ZERO ? new Vector3(x * s, y * s, z * s) : this;
    }
    
    public double dot(Vector3 other)
    {
        return x * other.x + y * other.y + z * other.z;
    }
    
    public Vector3 cross(Vector3 other)
    {
       return new Vector3(y * other.z - z * other.y,
                          z * other.x - x * other.z,
                          x * other.y - y * other.x);
    }
    
    public Vector3 normalized()
    {
        double length = length();
        if(Math.abs(length) < 1e-6)
        {
            return Vector3.ZERO;
        }
        return multiply(1/length);
    }
    
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(o == null || o.getClass() != this.getClass())
        {
            return false;
        }
        
        Vector3 other = (Vector3) o;
        
        return x == other.x && y == other.y && z == other.z;
    }
    
    public boolean epsilonEquals(Vector3 other, double epsilon)
    {
        return Math.abs(x - other.x) < epsilon &&
               Math.abs(y - other.y) < epsilon &&
               Math.abs(z - other.z) < epsilon;
    }
    
    public boolean epsilonEquals(Vector3 other)
    {
        return epsilonEquals(other, 1e-6);
    }
    
    /**
     * Project this vector onto plane defined by normal vector
     * {@code normal}
     * 
     * @param normal The normal vector
     * @return The projection of this vector onto plane defined by {@code normal}
     */
    public Vector3 projectOntoPlane(Vector3 normal)
    {
        // V - (V dot N)N
        return subtract(normal.multiply(dot(normal)));
    }
    
    /**
     * @return the angle between this vector and otherVector, given in radians in the interval [0.0, Pi].
     */
    public double cosAngle(Vector3 otherVector)
    {
        Double angle = Math.acos((this.dot(otherVector)/(this.length() * otherVector.length())));
        return !angle.equals(Double.NaN) ? angle : 0.0;
    }
    
    public String toString()
    {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
