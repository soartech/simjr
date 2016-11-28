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
 * A base class with constant and function definitions to simplify the 
 * C-to-Java conversion of Geotrans
 * 
 * @author ray
 */
class GeoTransBase
{
    
    protected static final double PI = Math.PI;
    protected static final double PI_OVER_2 = (Math.PI / 2.0e0);
    protected static final double  TWO_PI  =   (2.0 * PI);                    
    protected static final double PI_Over_4 = (PI / 4.0);

    protected static final double fabs(double v) { return Math.abs(v); }
    protected static final double tan(double v) { return Math.tan(v); }
    protected static final double sin(double v) { return Math.sin(v); }
    protected static final double cos(double v) { return Math.cos(v); }
    protected static final double sqrt(double v) { return Math.sqrt(v); }
    protected static final double atan2(double x, double y)
    {
        return Math.atan2(x, y);
    }
    protected static final double atan(double v) 
    {
        return Math.atan(v);
    }
    
    protected static final double pow(double r, double a)
    {
        return Math.pow(r, a);
    }
    protected static final double fmod(double a, double b)
    {
        return a % b;
    }
    
    
    /**
     * 
     */
    protected GeoTransBase()
    {
        super();
    }

}
