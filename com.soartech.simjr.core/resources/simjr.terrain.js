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
/*
    Common script utilities for dealing terrain
    
    Load with: requireScript("terrain");
*/

logger.info("loading terrain.js ...");

/**
    Set the origin of the terrain of the simulation to a particular earth 
    coordinate.  This is only a helper function for setting up a simple,
    flat terrain. That is, it creates a new SimpleTerrain object and sets
    the sim's terrain to it.
    
    The argument is either an MGRS string, or a lat/lon degree coordinate stored
    in a property structure. For example:
    
    <pre>
    setOrigin("11SMS62559125");
    
    or
    
    setOrigin({"latitude":42.5, "longitude":-83.3});
    </pre>
    
    @param props An MGRS string, or a property structure with lat/lon degrees.
*/
function setOrigin(props)
{
    var sim = getSimulation();
    if(sim == null)
    {
        return;
    }
    
    if(typeof(props) == "string")
    {
        var origin = new Mgrs().toGeodetic(props);
        sim.setTerrain(new SimpleTerrain(origin));
    }
    else if(props.latitude !== undefined || props.longitude !== undefined)
    {
        var origin = new Geodetic.Point();
        if(props.latitude !== undefined )
        {
            origin.latitude = java.lang.Math.toRadians(props["latitude"]);
        }
        if(props.longitude !== undefined)
        {
            origin.longitude = java.lang.Math.toRadians(props["longitude"]);
        }
        sim.setTerrain(new SimpleTerrain(origin));
    }
    else
    {
        logger.error("simjr.terrain.js:setOrigin() expected either an MGRS string or a lat/lon struct.");
    }
}

function toGeocentric(props)
{
    var sim = getSimulation();
    if(sim == null)
    {
        logger.error("simjr.terrain.js:setOrigin() could not find simulation");
        return null;
    }
    if(typeof(props) == "string")
    {
        return sim.getTerrain().fromMgrs(props);
    }
    else if(props.latitude !== undefined || props.longitude !== undefined)
    {
        var origin = new Geodetic.Point();
        if(props.latitude !== undefined)
        {
            origin.latitude = java.lang.Math.toRadians(props["latitude"]);
        }
        if(props.longitude !== undefined)
        {
            origin.longitude = java.lang.Math.toRadians(props["longitude"]);
        }
        return sim.getTerrain().fromGeodetic(origin);
    }
    else
    {
        logger.error("simjr.terrain.js:setOrigin() expected either an MGRS string or a lat/lon struct.");
        return null;
    }

}

logger.info("finished loading terrain.js");

