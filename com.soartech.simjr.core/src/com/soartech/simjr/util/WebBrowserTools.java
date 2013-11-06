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
 * Created on Oct 30, 2007
 */
package com.soartech.simjr.util;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * @author ray
 */
public final class WebBrowserTools
{
    private static final Logger logger = Logger.getLogger(WebBrowserTools.class);

    public static void openBrower(String url)
    {

        // open the browser
        try
        {
            String operatingSystem = System.getProperty("os.name");
            if (operatingSystem.startsWith("Windows"))
            {
                Runtime.getRuntime().exec(
                        "rundll32 url.dll,FileProtocolHandler " + url);
            }
            else
            {
                String[] browsers = // Assume Unix/Linux
                { "firefox", "opera", "konqueror", "epiphany", "mozilla",
                        "netscape" };
                String browser = null;
                for (int i = 0; i < browsers.length && browser == null; i++)
                    if (Runtime.getRuntime().exec(
                            new String[] { "which", browsers[i] }).waitFor() == 0)
                        browser = browsers[i];
                if (browser != null)
                {
                    Runtime.getRuntime().exec(new String[] { browser, url });
                }
                else
                {
                    logger.error("No browser found");
                }
            }
        }
        catch (IOException e)
        {
            logger.error(e);
        }
        catch (InterruptedException e)
        {
            logger.error(e);
        }

    }
}
