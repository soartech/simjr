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
 * Created on Oct 8, 2009
 */
package com.soartech.simjr.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.AbstractAdaptable;

public class DefaultServiceManagerTest extends TestCase
{
    @ConstructOnDemand
    public static class OnDemandTestService extends AbstractAdaptable implements SimulationService
    {
        boolean started = false;
        
        @Override
        public void shutdown() throws SimulationException
        {
        }

        @Override
        public void start(ProgressMonitor progress) throws SimulationException
        {
            started = true;
        }
    }
    
    public static class NotOnDemandTestService extends AbstractAdaptable implements SimulationService
    {
        boolean started = false;
        
        @Override
        public void shutdown() throws SimulationException
        {
        }

        @Override
        public void start(ProgressMonitor progress) throws SimulationException
        {
            started = true;
        }
    }
    
    public void testFindServiceConstructsDefaultInstanceOnDemand()
    {
        final ServiceManager manager = new DefaultServiceManager();
        final OnDemandTestService service = manager.findService(OnDemandTestService.class);
        assertNotNull(service);
        assertTrue(service.started);
        assertSame(service, manager.findService(OnDemandTestService.class));
    }
    
    public void testFindServiceDoesntConstructDefaultInstanceWithoutAnnotation()
    {
        final ServiceManager manager = new DefaultServiceManager();
        final NotOnDemandTestService service = manager.findService(NotOnDemandTestService.class);
        assertNull(service);
    }
    
    public void testAddService()
    {
        final ServiceManager manager = new DefaultServiceManager();
        final NotOnDemandTestService service = new NotOnDemandTestService();
        assertNull(manager.findService(NotOnDemandTestService.class));
        manager.addService(service);
        assertSame(service, manager.findService(NotOnDemandTestService.class));
    }
    
    public static class ShutdownTestService extends AbstractAdaptable implements SimulationService
    {
        final List<SimulationService> shutdownList;
        
        public ShutdownTestService(List<SimulationService> shutdownList)
        {
            this.shutdownList = shutdownList;
        }
        
        @Override
        public void shutdown() throws SimulationException
        {
            this.shutdownList.add(this);
        }

        @Override
        public void start(ProgressMonitor progress) throws SimulationException
        {
        }
    }
    
    public void testServicesAreShutdownInReverseOrder() throws Exception
    {
        final ServiceManager manager = new DefaultServiceManager();
        final List<SimulationService> shutdownList = new ArrayList<SimulationService>();
        final SimulationService a = new ShutdownTestService(shutdownList);
        final SimulationService b = new ShutdownTestService(shutdownList);
        manager.addService(a);
        manager.addService(b);
        manager.shutdownServices();
        assertEquals(Arrays.asList(b, a), shutdownList);
    }
}
