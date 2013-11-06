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
 * Created on Jan 29, 2010
 */
package com.soartech.simjr.apps;

import org.osgi.framework.BundleContext;

/**
 * Interface implemented by Sim Jr application entry points.
 * 
 * <p>This interface should be implemented and registered as a service
 * with <code>BundleContext.registerService()</code>. The name of the 
 * desired entry point is specified by the <code>simjr.app</code>
 * system property at startup. The <code>com.soartech.simjr.startup</code>
 * plugin watches for a service with the desired name. When it is 
 * discovered, its {@link #start(BundleContext, String[])} method is
 * called.
 * 
 * @author ray
 */
public interface SimJrApplication
{
    /**
     * @return the name of this application. Must match value passed in
     *  with <code>simjr.app</code> system property
     */
    String getName();
    
    
    /**
     * Called to start an application. The arguments are parsed out of the
     * <code>simjr.args</code> system property.
     * 
     * @param bc the bundle context
     * @param args the args, parsed from <code>simjr.args</code> system property
     * @throws Exception
     */
    void start(BundleContext bc, String[] args) throws Exception;
}
