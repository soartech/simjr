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
 * Created on January, 29, 2010
 */
package com.soartech.simjr.startup;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import com.soartech.simjr.apps.SimJrApplication;

/**
 * This activator is what actually runs Sim Jr. Using OSGi run levels, it should 
 * be the last plugin activated. It will pick up the desired app (@{code simjr.app})
 * and command-line args (@{code simjr.args}) and will run the registered application.
 * 
 * @author ray
 */
public class SimJrStartupActivator implements BundleActivator
{
    private static final Logger logger = Logger.getLogger(SimJrStartupActivator.class);
    
    private static SimJrStartupActivator instance;
    
    private BundleContext bundleContext;
    
    private HashMap<String, Boolean> startedApps = new HashMap<String, Boolean>();
    
    public static SimJrStartupActivator getDefault() { return instance; }
    
    public BundleContext getBundleContext()
    {
        return bundleContext;
    }
    
    /**
     * Start up a Sim Jr instance with an app name and arguments.
     */
    public void startup(final BundleContext context, final String appName, final String rawArgs) throws Exception
    {
        logger.info("Started with app '" + appName + "' and args '" + rawArgs + "'");
        
        final String[] parsedArgs = parseArgs(rawArgs);
        
        final ServiceReference[] refs = context.getServiceReferences(SimJrApplication.class.getName(), null);
        if(refs != null)
        {
            for(ServiceReference ref : refs)
            {
                final SimJrApplication app = (SimJrApplication) context.getService(ref);
                try
                {
                    if(appName.equals(app.getName()))
                    {
                        startedApps.put(appName, true);
                        app.start(context, parsedArgs);
                        break;
                    }
                }
                finally
                {
                    context.ungetService(ref);
                }
            }
        }
        else
        {
            context.addServiceListener(new ServiceListener()
            {
                
                @Override
                public void serviceChanged(ServiceEvent e)
                {
                    if(e.getType() == ServiceEvent.REGISTERED)
                    {
                        final ServiceReference ref = e.getServiceReference();
                        if(ref.isAssignableTo(context.getBundle(), SimJrApplication.class.getName()))
                        {
                            final SimJrApplication app = (SimJrApplication) context.getService(ref);
                            try
                            {
                                if(appName.equals(app.getName()))
                                {
                                    app.start(context, parsedArgs);
                                    context.removeServiceListener(this);
                                }
                            }
                            catch(Exception ex)
                            {
                                ex.printStackTrace();
                            }
                            finally
                            {
                                context.ungetService(ref);
                            }
                        }
                        
                    }
                }
            });
        }
    }
    
    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(final BundleContext context) throws Exception
    {
        instance = this;
        this.bundleContext = context;
        
        String appName = System.getProperty("simjr.app", "none");
        String rawArgs = System.getProperty("simjr.args", "");
        
        startup(context, appName, rawArgs);
    }
    
    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception
    {
        instance = null;
    }

    private String[] parseArgs(String rawArgs)
    {
        rawArgs = rawArgs.trim();
        if(rawArgs.length() == 0)
        {
            return new String[0];
        }
        return rawArgs.split(";");
    }

    public boolean isStarted(String appName)
    {
        if (!startedApps.containsKey(appName))
        {
            return false;
        }
        
        return startedApps.get(appName);
    }
    
}
