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
 * An example of implementing a custom entity behavior in pure JavaScript.
 * Drag around "first" to see how "second" follows it.
 */
requireScript("polygons");
requireScript("ui");

(function(){
    
    // Create a rectangular route
    simjr.polygons.route("route",[
       { name: "a", x: -100 },
       { name: "c", x: 100 },
       { name: "e", x: 100, y: 100 },
       { name: "g", x: -100, y: 100 }
    ]);
    
    // Create a truck to drive the route
    simjr.entities.create({
        name:      "first",
        prototype: "truck",
        x:         -100,     // start at point A
        capabilities: [
            new Packages.com.soartech.simjr.controllers.RouteFollower()
        ],
        properties: {
            "routeFollower.route" : "route"
        }
    });
    
    // Create another truck
    var second = simjr.entities.create({
        name:      "second",
        prototype: "truck",
        x:         100,
        y:         100,
        capabilities: [],
        properties: {
            "target" : "first"
        }
    });
    
    // Implement a simple following behavior for the second truck. It will
    // follow the first truck wherever it goes. We use EntityController 
    // because it has Tickable built-in.
    second.addCapability(new EntityController({
        getEntity: function () { return second; },
        attach: function(e) {},
        detach: function() {},
        
        tick: function (dt) { 
        	var e = this.getEntity();
        	// Look at the target property to see what we should follow
            var target = e.getSimulation().getEntity(e.getProperty("target"));
            
            // Calculate new velocity vector to the target
            var dir = target.position.subtract(e.position);
            dir = dir.normalized().multiply(10.0);
            e.velocity = dir;
            
            // Set the orientation to point at the target
            e.orientation = java.lang.Math.atan2(dir.y, dir.x);
        },
        
        openDebugger: function() { 
            logger.info("JavaScript debugging not supported");
        },
        getAdapter: function(klass) { return null; }
    }));
    
    getActivePlanViewDisplay().showAll();
})();
