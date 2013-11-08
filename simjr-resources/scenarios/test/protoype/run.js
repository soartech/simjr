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
requireScript("entities");
requireScript("polygons");
requireScript("ui");

// Create a truck prototype that drives along "route" at 100 m/s
var myTruckPrototype = simjr.entities.prototypes.specialize("truck", { 
		id: "route-truck", 
		capabilities: [
		    classForName("com.soartech.simjr.controllers.RouteFollower")
		],
		properties: { 
			force:"opposing",
			"routeFollower.route": "route",
			"routeFollower.speed": 100
		} 
	}
);

// Create the route
simjr.polygons.route("route", [{ name: "start", x: 1000, y: 1000}, { name: "end", x: -1000, y: -1000}]);
simjr.polygons.route("route2", ["start", { name: "end2", x: -1000, y: 1000}]);

// Create one truck using prototype defaults
simjr.entities.create({name: "a", prototype: myTruckPrototype });

// Create another truck that overrides the desired-speed from the prototype
// Here the prototype is referenced by id "route-truck"
simjr.entities.create({name: "b", prototype: "route-truck", x:500, y:-500, properties: {"routeFollower.speed": 150 }});

//And another truck that overrides the route and force from the prototype
//Here the prototype is referenced by id "route-truck"
simjr.entities.create({
	name: "c", prototype: "route-truck", 
	x:-500, y:-500, 
	properties: {
    	force: "neutral", 
    	"routeFollower.route": "route2" 
    }
});

createCheatSheet();