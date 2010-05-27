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
 * Created on May 1, 2008
 */
package com.soartech.simjr.util;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author ray
 */
public class SystemTools
{
    public static File getCurrentDirectory()
    {
        return new File(System.getProperty("user.dir"));
    }
    
    /**
     * Retrieve an unused port to listen on.
     * 
     * @return An unused port
     * @throws IOException If there is a networking error.
     */
    public static int getUnusedPort() throws IOException
    {
        ServerSocket s = null;
        try
        {
            s = new ServerSocket(0);
            
            return s.getLocalPort();
        }
        finally
        {
            if(s != null)
            {
                try
                {
                    s.setReuseAddress(true);
                    s.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static int waitForProcessToExit(Process process, long timeout)
    {
        long endTime = System.currentTimeMillis() + 5000;
        while(System.currentTimeMillis() < endTime)
        {
            try
            {
                return process.exitValue();
            }
            catch(IllegalThreadStateException e)
            {
            }
        }
        throw new IllegalThreadStateException("Process '" + process + "' did not exit as expected.");
    }
}
