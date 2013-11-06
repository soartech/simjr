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

logger.info("loading simjr.web.js ...");

if(typeof(simjr) == 'undefined') 
{
    simjr = {};
}

function SimJrWeb()
{
    /**
     * Install the Sim Jr web interface
     */
    this.install = function()
    {
        var sm = Packages.com.soartech.simjr.web.sim.WebApplication.findService(services);
        if(sm == null)
        {
            runner.subTask("Initializing Simulation web app ...");
            var sm = new Packages.com.soartech.simjr.web.sim.WebApplication(services);
            sm.start(runner);
            services.addService(sm);
        }
        if(sm == null)
        {
            logger.warn("No simulation web app was found.");
        }
        return sm;
    },
    
    /**
     * Open a browser pointing at the web interface. If install() has not
     * been called yet, it will be.
     */
    this.openBrowser = function()
    {
        this.install();
        
        var url = "http://localhost:8080/simjr/ui/index.html";
        logger.info("Opening Sim Jr web app in browser at '" + url + "'");
        
        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
    }
}

simjr.web = new SimJrWeb();

logger.info("finished loading simjr.web.js");

