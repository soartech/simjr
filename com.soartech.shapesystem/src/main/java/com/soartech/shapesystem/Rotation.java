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
 * Created on May 17, 2007
 */
package com.soartech.shapesystem;


/**
 * @author ray
 */
public class Rotation
{
    public static final Rotation IDENTITY = fromDegrees(0.0, RotationType.WORLD);

    private final RotationType type;
    private final String parent;
    private final double degrees;
    
    public static Rotation fromDegrees(double degrees, RotationType type, String parent)
    {
        return new Rotation(type, parent, degrees);
    }
    
    public static Rotation fromDegrees(double degrees, RotationType type)
    {
        return fromDegrees(degrees, type, "");
    }
    
    public static Rotation fromRadians(double radians, RotationType type, String parent)
    {
        return new Rotation(type, parent, Math.toDegrees(radians));
    }
    
    public static Rotation fromRadians(double radians, RotationType type)
    {
        return fromRadians(radians, type, "");
    }
    
    public static Rotation createRelative(String parent)
    {
        return fromDegrees(0, RotationType.RELATIVE, parent);
    }
    
    public static Rotation createPointAt(String id)
    {
        return new Rotation(RotationType.POINT_AT, id, 0.0);
    }
    
    private Rotation(RotationType type, String parent, double degrees)
    {
        super();
        this.type = type;
        this.parent = parent;
        this.degrees = degrees;
    }

    /**
     * @return the degrees
     */
    public double getDegrees()
    {
        return degrees;
    }
    
    public double getRadians()
    {
        return Math.toRadians(degrees);
    }

    /**
     * @return the parent
     */
    public String getParent()
    {
        return parent;
    }
    public boolean hasParent()
    {
        return parent.length() > 0;
    }

    /**
     * @return the type
     */
    public RotationType getType()
    {
        return type;
    }
    
    
}
