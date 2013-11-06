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
 * Created on Sep 15, 2009
 */
package com.soartech.simjr.startup;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.soartech.simjr.apps.SimJrApplication;
import com.soartech.simjr.scripting.ScriptProviderManager;
import com.soartech.simjr.ui.SimulationApplication;

/**
 * This is the main OSGi entry point for the Sim Jr simulation application.
 * It is registered with the "org.eclipse.core.runtime.applications" extension
 * point in plugin.xml.
 * 
 * @author ray
 */
public class DefaultApplication implements SimJrApplication
{
    private static final Logger logger = Logger.getLogger(DefaultApplication.class);
    
    public String getName() { return "sim"; }
    
    public void start(BundleContext bc, String[] args) throws Exception
    {
        logger.info("Sim application started with args: " + Arrays.asList(args));
        final SimulationApplication app = prepareApplication(bc);
        SimulationApplication.main(app, args);
    }

    private SimulationApplication prepareApplication(BundleContext bc)
    {
        final ScriptProviderManager scripts = getScriptProviderManager(bc);
        final SimulationApplication app = new SimulationApplication();
        
        if(scripts != null)
        {
            app.addService(scripts);
        }
        
        return app;
    }
    
    private ScriptProviderManager getScriptProviderManager(BundleContext bc)
    {
        // TODO: Try FrameworkUtil as described here: http://stackoverflow.com/questions/559989/how-do-i-get-the-osgi-bundlecontext-for-an-eclipse-rcp-application
        final ServiceReference ref = bc.getServiceReference(ScriptProviderManager.class.getName());
        return (ScriptProviderManager) bc.getService(ref);
    }
}
