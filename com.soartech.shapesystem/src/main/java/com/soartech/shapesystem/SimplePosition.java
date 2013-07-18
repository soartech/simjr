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
public class SimplePosition
{
    public double x = 0.0;
    public double y = 0.0;
    
    public static SimplePosition midPoint(SimplePosition a, SimplePosition b)
    {
        return new SimplePosition((a.x + b.x) / 2, (a.y + b.y) / 2);
    }
    
    public SimplePosition()
    {
    }
    
    public SimplePosition(SimplePosition other)
    {
        this.x = other.x;
        this.y = other.y;
    }
    
    public SimplePosition(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    
    public void translate(SimplePosition other)
    {
        x += other.x;
        y += other.y;
    }
    
    public void rotate(SimpleRotation r)
    {
        double x_orig = x;
        double y_orig = y;
        
        final double rad = r.inRadians();
        
        // The negative on the y-component is because y travels
        // down in screen coordinates vs. up in Cartesian
        x =  (x_orig*Math.cos(rad) + y_orig*Math.sin(rad));
        y = -(y_orig*Math.cos(rad) - x_orig*Math.sin(rad));
    }
    
    public double distance(double x, double y)
    {
        double dx = this.x - x;
        double dy = this.y - y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public double distance(SimplePosition other)
    {
        return distance(other.x, other.y);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ")";
    }
    
    
}
