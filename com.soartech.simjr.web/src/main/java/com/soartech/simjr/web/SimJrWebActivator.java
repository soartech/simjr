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
 */
package com.soartech.simjr.web;

import org.apache.log4j.Logger;
import org.ops4j.pax.web.extender.whiteboard.ResourceMapping;
import org.ops4j.pax.web.extender.whiteboard.runtime.DefaultResourceMapping;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.soartech.simjr.SimJrCoreActivator;
import com.soartech.simjr.scripting.ResourceScriptProvider;
import com.soartech.simjr.scripting.ScriptProvider;

public class SimJrWebActivator implements BundleActivator
{
    private static final Logger logger = Logger.getLogger(SimJrCoreActivator.class);
    
    private static SimJrWebActivator instance;
    
    private BundleContext bundleContext;
    
    public static SimJrWebActivator getDefault() { return instance; }
    
    /**
     * @return this plugin's BundleContext
     */
    public BundleContext getBundleContext()
    {
        return bundleContext;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext context) throws Exception
    {
        logger.info("Sim Jr web plugin activated");
        instance = this;
        this.bundleContext = context;

        // Register script for requireScript("tas"); support
        bundleContext.registerService(ScriptProvider.class.getName(), new ResourceScriptProvider("web", "/com/soartech/simjr/web/common.js", getClass().getClassLoader()), null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception
    {
        this.bundleContext = null;
        instance = null;
        logger.info("Sim Jr web plugin deactivated");
    }

    public void registerResource(String urlPath, String classPath)
    {
        final DefaultResourceMapping resourceMapping = new DefaultResourceMapping();
        resourceMapping.setPath(classPath);
        resourceMapping.setAlias(urlPath);
        getBundleContext().registerService(ResourceMapping.class.getName(), resourceMapping, null);
    }
}
