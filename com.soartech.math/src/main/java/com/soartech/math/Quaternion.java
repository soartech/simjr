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
public class Quaternion
{
    public static final Quaternion IDENTITY = createRotation(0.0, Vector3.X_UNIT);

    public final double w;
    public final Vector3 v;

    private Quaternion conjugate = null;

    /**
     * Create a rotation quaternion around a particular axis
     *
     * @param theta Angle to rotate (radians)
     * @param axis Axis to rotate around (does not need to be normalized)
     * @return Quaternion
     */
    public static Quaternion createRotation(double theta, Vector3 axis)
    {
        double halfTheta = theta / 2.0;

        return new Quaternion(Math.cos(halfTheta),
                              axis.normalized().multiply(Math.sin(halfTheta)));
    }

    /**
     * Create a rotation quaternion that maps one vector onto another using the
     * smallest available rotation.
     *
     * <p>If {@code start} and {@code end} are colinear, then the rotation
     * will be about the axis normal to {@code start} and the X axis. If
     * {@code start} is colinear with the X axis then the axis normal to
     * {@code start} and the Y axis is used.
     *
     * @param start Start vector
     * @param end End vector that {@code start} is mapped to
     * @return Rotation quaternion
     */
    public static Quaternion createMappingRotation(Vector3 start, Vector3 end)
    {
        Vector3 axis = start.cross(end);
        if(Vector3.ZERO.epsilonEquals(axis))
        {
            axis = start.cross(Vector3.X_UNIT);
            if(Vector3.ZERO.epsilonEquals(axis))
            {
                axis = start.cross(Vector3.Y_UNIT);
            }
        }
        double angle = Math.acos(start.dot(end));
        return createRotation(angle, axis);
    }

    /**
     * Creates a quaternion from Euler angles.
     *
     * see {@link http://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles} for
     * details.
     *
     * @param roll (rotation around X axis in radians)
     * @param pitch (rotation around Y axis in radians)
     * @param heading (rotation around Z axis in radians)
     * @return resulting quaternion
     */
    public static Quaternion createFromEulerAngles(double roll, double pitch, double heading)
    {
        double sinphi2 = Math.sin(roll/2.);
        double cosphi2 = Math.cos(roll/2.);
        double sintheta2 = Math.sin(pitch/2.);
        double costheta2 = Math.cos(pitch/2.);
        double sinpsi2 = Math.sin(heading/2.);
        double cospsi2 = Math.cos(heading/2.);

        double w = cosphi2*costheta2*cospsi2 + sinphi2*sintheta2*sinpsi2;
        double x = sinphi2*costheta2*cospsi2 - cosphi2*sintheta2*sinpsi2;
        double y = cosphi2*sintheta2*cospsi2 + sinphi2*costheta2*sinpsi2;
        double z = cosphi2*costheta2*sinpsi2 - sinphi2*sintheta2*cospsi2;

        return new Quaternion(w, new Vector3(x, y, z));
    }

    public Quaternion(double w, Vector3 v)
    {
        this.w = w;
        this.v = v;
    }

    public Quaternion multiply(double s)
    {
        return new Quaternion(w * s, v.multiply(s));
    }

    public Quaternion multiply(Quaternion q)
    {
        // [w * q.w - v * q.v, w * q.v + q.v * v + v x q.v]
//        return new Quaternion(w * q.w - v.dot(q.v),
//                              q.v.multiply(w).add(v.multiply(q.w).add(v.cross(q.v))));
        return new Quaternion(
                w * q.w   - v.x * q.v.x - v.y * q.v.y - v.z * q.v.z,
                new Vector3(
                w * q.v.x + v.x * q.w   + v.y * q.v.z - v.z * q.v.y,
                w * q.v.y + v.y * q.w   + v.z * q.v.x - v.x * q.v.z,
                w * q.v.z + v.z * q.w   + v.x * q.v.y - v.y * q.v.x));
    }

    public Quaternion conjugate()
    {
        if(conjugate == null)
        {
            conjugate = new Quaternion(w, v.multiply(-1.0));
        }
        return conjugate;
    }

    public Quaternion inverse()
    {
        Quaternion conj = conjugate();
        return conj.multiply(1.0 / lengthSquared());
    }

    public Quaternion normalize()
    {
        double length = 1.0 / length();
        return new Quaternion(w * length, v.multiply(length));
    }

    public double length()
    {
        return Math.sqrt(lengthSquared());
    }

    public double lengthSquared()
    {
        return w * w + v.dot(v);
    }

    /**
     * Rotates a vector by the rotation that this quaternion represents.
     *
     * <p>If v is zero, or if the quaternions {@link #w} component is nearly
     * one, then {@code v} is simply returned because the rotation would
     * have no affect.
     *
     * @param v Input vector
     * @return Rotated vector
     */
    public Vector3 rotate(Vector3 v)
    {
        // If w is nearly 1 then this is an identity rotation. Don't bother
        // with all the stuff below, just give the vector back.
        if(Math.abs(w - 1.0) < 1e-6)
        {
            return v;
        }

        // If the vector is close to zero, do nothing. Nearly zero vectors
        // cause divide by zero issues.
        if(v.epsilonEquals(Vector3.ZERO))
        {
            return v;
        }

        // this * (0, v) * conj(this)
        Quaternion out = this.multiply(new Quaternion(0, v));
        out = out.multiply(this.conjugate());
        return out.v;
    }

    public double getRotationAngle()
    {
        return 2 * Math.acos(w);
    }

    public Vector3 getRotationAxis()
    {
        double s = Math.sqrt(1 - w * w);
        if(Math.abs(s) > 1e-5)
        {
            return v.multiply(1.0 / s);
        }
        return Vector3.Y_UNIT;
    }

    @Override
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

        Quaternion other = (Quaternion) o;

        return w == other.w && v.equals(other.v);

    }

    @Override
    public String toString()
    {
        return "[" + w + ", " + v + "]";
    }
}
