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
public class Scalar
{
    private static final Scalar PIXELS[] = new Scalar[21];
    static
    {
        for(int i = 0; i < PIXELS.length; ++i)
        {
            PIXELS[i] = new Scalar(i, ScalarUnit.Pixels);
        }
    }
    
    private final double value;
    private final ScalarUnit unit;
    
    public static Scalar createPixel(double value)
    {
        int v = (int) value;
        return v >= 0 && v< PIXELS.length ? PIXELS[v] :  new Scalar(value, ScalarUnit.Pixels);
    }
    
    public static Scalar createMeter(double value)
    {
        return new Scalar(value, ScalarUnit.Meters);
    }
    
    public Scalar(double value)
    {
        this(value, ScalarUnit.Pixels);
    }
    
    /**
     * @param value
     * @param unit
     */
    public Scalar(double value, ScalarUnit unit)
    {
        super();
        this.value = value;
        this.unit = unit;
    }

    public Scalar scale(double scale)
    {
        switch(unit)
        {
        case Pixels: return createPixel(value * scale);
        case Meters: return createMeter(value * scale);
        default:
            throw new IllegalStateException("Unknown unit: " + unit);
        }
    }
    /**
     * @return the unit
     */
    public ScalarUnit getUnit()
    {
        return unit;
    }

    /**
     * @return the value
     */
    public double getValue()
    {
        return value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return value + " (" + unit + ")";
    }
    
    
}
