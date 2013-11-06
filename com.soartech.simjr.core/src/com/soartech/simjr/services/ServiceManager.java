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
 * Created on May 3, 2008
 */
package com.soartech.simjr.services;

import com.soartech.simjr.SimulationException;


/**
 * An interface for an object that manages a set of services.
 * 
 * @author ray
 */
public interface ServiceManager
{
    /**
     * Add a service to the manager. The service's start() method is not called.
     * 
     * @param service the service to add
     */
    public void addService(SimulationService service);
    
    /**
     * Find the first service of a particular type in the service manager.
     * 
     * <p>If the service is not found and the requested class has the {@link ConstructOnDemand}
     * annotation, then the manager will attempt to construct an instance of
     * the object. The new instance's start() method will be called and the
     * service will be added before it returns.
     * 
     * @param <T> The requested type of service
     * @param klass the class of the type of service
     * @return The service, or {@code null} if the service couldn't be found
     */
    public <T extends SimulationService> T findService(Class<T> klass);

    /**
     * Shutdown all of the services in the manager. The shutdown method of
     * each service is called in the reverse order that they were added.
     * 
     * @throws SimulationException
     */
    public void shutdownServices() throws SimulationException;
}
