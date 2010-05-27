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
 */
package com.soartech.simjr;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import com.soartech.simjr.apps.SimJrApplication;
import com.soartech.simjr.scripting.DefaultScriptProviderManager;
import com.soartech.simjr.scripting.ScriptProvider;
import com.soartech.simjr.scripting.ScriptProviderManager;
import com.soartech.simjr.startup.DefaultApplication;
import com.soartech.simjr.startup.EditorApplication;
import com.soartech.simjr.startup.HeadlessApplication;

public class SimJrCoreActivator implements BundleActivator
{
    private static final Logger logger = Logger.getLogger(SimJrCoreActivator.class);
    
    private static final String SCRIPT_PROVIDER_SERVICE_FILTER = "(objectclass=" + ScriptProvider.class.getName() + ")";
    
    private static SimJrCoreActivator instance;
    
    private BundleContext bundleContext;
    private final ScriptProviderManager scripts = new DefaultScriptProviderManager();
    
    public static SimJrCoreActivator getDefault() { return instance; }
     
    /**
     * @return this plugin's BundleContext
     */
    public BundleContext getBundleContext()
    {
        return bundleContext;
    }
    
    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(final BundleContext context) throws Exception
    {
        logger.info("Sim Jr core plugin activated");
        instance = this;
        this.bundleContext = context;
        context.registerService(ScriptProviderManager.class.getName(), scripts, null);
        context.registerService(SimJrApplication.class.getName(), new DefaultApplication(), null);
        context.registerService(SimJrApplication.class.getName(), new HeadlessApplication(), null);
        context.registerService(SimJrApplication.class.getName(), new EditorApplication(), null);
        
        listenForScriptProviders(context);
    }
    
    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception
    {
        this.bundleContext = null;
        instance = null;
        logger.info("Sim Jr core plugin deactivated");
    }


    private void listenForScriptProviders(final BundleContext context)
            throws InvalidSyntaxException
    {
        context.addServiceListener(new ServiceListener() {

            public void serviceChanged(ServiceEvent e)
            {
                if(e.getType() == ServiceEvent.REGISTERED)
                {
                    scriptProviderRegistered(context, e.getServiceReference());
                }
                else if(e.getType() == ServiceEvent.UNREGISTERING)
                {
                    scriptProviderUnregistered(context, e.getServiceReference());
                }
            }}, SCRIPT_PROVIDER_SERVICE_FILTER);
        
        final ServiceReference[] serviceReferences = context.getServiceReferences(null, SCRIPT_PROVIDER_SERVICE_FILTER);
        if(serviceReferences != null)
        {
            for(ServiceReference sr : serviceReferences)
            {
                scriptProviderRegistered(context, sr);
            }
        }
    }


    private void scriptProviderRegistered(BundleContext context, ServiceReference sr)
    {
        final ScriptProvider provider = (ScriptProvider) context.getService(sr);
        scripts.addProvider(provider);
    }

    protected void scriptProviderUnregistered(BundleContext context, ServiceReference sr)
    {
        final ScriptProvider provider = (ScriptProvider) context.getService(sr);
        scripts.removeProvider(provider);
        context.ungetService(sr);
    }
}
