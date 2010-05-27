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
logger.info("Loading simjr.common.js");

importPackage(Packages.com.soartech.simjr)
importPackage(Packages.com.soartech.simjr.sim)
importPackage(Packages.com.soartech.math.geotrans)
importPackage(Packages.com.soartech.math)

function requireScript(name)
{
	return runner.requireScript(name);
}

requireScript("timers");
requireScript("entities");

///////////////////////////////////////////////////////////////////////////////
// Common Service Accessors
// These functions serve two purposes. First, they're a convenient way of 
// accessing services from JavaScript. Second, they leave package binding until
// as late as possible.


/** Access the Simulation service */
function getSimulation()
{
    return Packages.com.soartech.simjr.sim.Simulation.findService(services);
}

// Set global simulation variable. At some point, I'd like to get rid of this.
if(services != null)
{
    simulation = getSimulation();
}

/**
 * Helper function for looking up a class object. This should be used wherever
 * a Java method would usually expect a class literal, e.g. "java.lang.String.class".
 * 
 * @param name the fully qualified name of the class
 * @return the class object
 */
function classForName(name) {
	return java.lang.Class.forName(name);
}

/**
 * @return true if their is no simulation UI
 */
function isHeadless()
{
    return Packages.com.soartech.simjr.ui.SimulationMainFrame.findService(services) == null;
}

/**
   Convert a path relative to the current scripts location to an absolute path
   
   @param f The relative file path
   @return Absolute path
*/
function getRelativeFile(f)
{
    return runner.getRelativeFile(f);
}

/**
    Construct a Java map from an object's properties
*/
function createMap(propObject) 
{
    var map = new java.util.HashMap();
    for(var p in propObject) 
    {
        map.put(p, propObject[p]);
    }
    return map;
}

function createList(array)
{
    var list = new java.util.ArrayList();
    for(var i = 0; i < array.length; i++)
    {
        list.add(array[i]);
    }
    return list;
}
