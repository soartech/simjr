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
 * Created on Dec 6, 2009
 */
package com.soartech.simjr.web.sim;

import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.services.SimulationService;
import com.soartech.simjr.web.SimJrWebActivator;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * 
 * @author ray
 */
@Path("/")
public class WebApplication extends Application implements
        SimulationService
{
    private ServiceManager services;
    
    public static WebApplication findService(ServiceManager services)
    {
        return services.findService(WebApplication.class);
    }
    
    public WebApplication(ServiceManager services)
    {
        this.services = services;
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#shutdown()
     */
    public void shutdown() throws SimulationException
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#start(com.soartech.simjr.ProgressMonitor)
     */
    @SuppressWarnings("unchecked")
    public void start(ProgressMonitor progress) throws SimulationException
    {
        SimJrWebActivator.getDefault().registerResource("/simjr/ui", "/com/soartech/simjr/web/ui");
//        SimJrWebActivator.getDefault().registerResource("/simjr/ui/stylesheets", "/com/soartech/simjr/web/ui/stylesheets");
//        SimJrWebActivator.getDefault().registerResource("/simjr/ui/images", "/com/soartech/simjr/web/ui/images");
//        SimJrWebActivator.getDefault().registerResource("/simjr/ui/javascripts", "/com/soartech/simjr/web/ui/javascripts");
        
        final Hashtable props = new Hashtable();
        props.put("servlet-name", "Sim Jr Simulation Servlet");
        props.put("alias", "/simjr/sim");
        
        SimJrWebActivator.getDefault().getBundleContext().registerService(
                HttpServlet.class.getName(), 
                new ServletContainer(this), 
                props);
    }
    
    /* (non-Javadoc)
     * @see javax.ws.rs.core.Application#getSingletons()
     */
    @Override
    public Set<Object> getSingletons()
    {
        final Set<Object> result = new LinkedHashSet<Object>();
        result.add(new RootResource(services));
        return result;
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.adaptables.Adaptable#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class klass)
    {
        return Adaptables.adaptUnchecked(this, klass, false);
    }
    
}
