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
public class Position
{
    private final PositionType type;
    private final String parent;
    private final Scalar x;
    private final Scalar y;
    
    public static Position createWorldPixel(double x, double y)
    {
        return new Position(PositionType.WORLD, 
                            new Scalar(x, ScalarUnit.Pixels), 
                            new Scalar(y, ScalarUnit.Pixels));
    }
    
    public static Position createRelativePixel(double x, double y, String parent)
    {
        return new Position(PositionType.RELATIVE, 
                            new Scalar(x, ScalarUnit.Pixels), 
                            new Scalar(y, ScalarUnit.Pixels),
                            parent);
    }
    
    public static Position createWorldMeters(double x, double y)
    {
        return new Position(PositionType.WORLD, 
                new Scalar(x, ScalarUnit.Meters), 
                new Scalar(y, ScalarUnit.Meters));
    }
    
    public static Position createRelativeMeters(double x, double y, String parent)
    {
        return new Position(PositionType.RELATIVE, 
                new Scalar(x, ScalarUnit.Meters), 
                new Scalar(y, ScalarUnit.Meters), parent);
    }
    
    public Position()
    {
        this(PositionType.WORLD, Scalar.createPixel(0), Scalar.createPixel(0));
    }
    
    public Position(String parent)
    {
        this(PositionType.RELATIVE, Scalar.createPixel(0), Scalar.createPixel(0), parent);
    }
    
    public Position(PositionType type, Scalar x, Scalar y, String parent)
    {
        this.type = type;
        this.parent = parent;
        this.x = x;
        this.y = y;
    }
    
    public Position(PositionType type, Scalar x, Scalar y)
    {
        this(type, x, y, "");
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
    public PositionType getType()
    {
        return type;
    }

    /**
     * @return the x
     */
    public Scalar getX()
    {
        return x;
    }

    /**
     * @return the y
     */
    public Scalar getY()
    {
        return y;
    }
    
}
