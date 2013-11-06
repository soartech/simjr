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
importPackage(Packages.com.soartech.simjr)
importPackage(Packages.com.soartech.simjr.controllers)
importPackage(Packages.com.soartech.simjr.sim)
importPackage(Packages.com.soartech.simjr.sim.entities)

requireScript("ui");
requireScript("polygons");

var rwaOrbitPoint = simjr.polygons.waypoint({name:"rwaPoint", x: 0, y: -2000 });
var rwa = simjr.entities.create({
	name:"rwa", 
	prototype:"rwa",
	x: -5000, y: 2000,
	capabilities: [
	    new RotaryWingFlightController(),
	   [new OrbitFlightController(), { 
		   centerEntity: rwaOrbitPoint,
		   altitude: 400, speed: 50, radius: 500 
		}]
	]
});

var fwaOrbitPoint = simjr.polygons.waypoint({name:"fwaPoint", x: 0, y: 2000 });
var fwa = simjr.entities.create({
	name:"fwa", 
	prototype:"fwa",
	x: 5000, y: -2000,
	capabilities: [
	    new FixedWingFlightController(),
	   [new OrbitFlightController(), { 
		   centerEntity: fwaOrbitPoint,
		   altitude: 400, speed: 50, radius: 500 
		}]
	]
});

createCheatSheet();

var pvd = getActivePlanViewDisplay();
pvd.getDistanceTools().addDistanceTool(rwa, rwaOrbitPoint);
pvd.getDistanceTools().addDistanceTool(fwa, fwaOrbitPoint);
pvd.showAll();