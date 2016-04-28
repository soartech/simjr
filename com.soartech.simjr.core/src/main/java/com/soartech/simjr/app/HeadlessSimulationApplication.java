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
 * Created on May 22, 2007
 */
package com.soartech.simjr.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.simjr.NullProgressMonitor;
import com.soartech.simjr.SimJrProps;

/**
 * Application entry point for running Sim Jr with no UI.
 * 
 * @author ray
 */
public class HeadlessSimulationApplication extends AbstractSimulationApplication//DefaultServiceManager
{
    private static final Logger logger = LoggerFactory.getLogger(HeadlessSimulationApplication.class);

    public HeadlessSimulationApplication() { }

    /**
     * @param args
     */
    public static void main(final String[] args)
    {
        main(new HeadlessSimulationApplication(), args);
    }

    public static void main(final HeadlessSimulationApplication app, final String[] args)
    {
        logger.info("Sim Jr (headless) started");
        logger.info("   simjr.home=" + SimJrProps.get(SimJrProps.HOME));
        logger.info("   current directory = " + System.getProperty("user.dir"));
        
        app.initialize(new NullProgressMonitor(), args);
        while(true) {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }
}
