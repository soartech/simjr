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
 * Created on Jun 19, 2007
 */
package com.soartech.simjr.radios;

/**
 * @author ray
 */
public class RadioMessage
{
    private final String source;
    private final String target;
    private final String content;
    private final double time;
    private final String frequency;
    
    /**
     * @param source
     * @param content
     */
    public RadioMessage(String source, String target, String content, double time, String frequency)
    {
        this.source = source;
        this.target = target;
        this.content = content;
        this.time = time;
        this.frequency = frequency;
    }
    /**
     * @return the content
     */
    public String getContent()
    {
        return content;
    }
    
    /**
     * @return the source
     */
    public String getSource()
    {
        return source;
    }
    
    /**
     * @return the target of the message, possibly <code>null</code>
     */
    public String getTarget()
    {
        return target;
    }
    /**
     * @return the time
     */
    public double getTime()
    {
        return time;
    }
    
    /**
     * @return the frequency
     */
    public String getFrequency()
    {
        return frequency;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("(%.3f) ", time) +  source + " " + target + " " + frequency + ": " + content;
    }
}
