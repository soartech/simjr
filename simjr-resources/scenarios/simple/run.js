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

// This is an example configuration script for Sim Jr.

// First we have to import packages for any Java classes we want to use whose
// names are not fully qualified.
importPackage(Packages.com.soartech.simjr.controllers)
importPackage(Packages.com.soartech.simjr.ui.pvd)  // For MapImage
importPackage(Packages.com.soartech.simjr.ui.shapes)  // For ImageEntityShape

requireScript("polygons");
requireScript("ui");

// Wrap everything in an anonymous scope as a common courtesy.
(function() {
    // Set up the background map
    // The image is 2390x2070 pixels.
    var map = new MapImage(getRelativeFile("../maps/alamogordo.jpg"),
    				   new Vector3(1000, 1000, 0) /*center*/, 
    				   1.0 /*metersPerPixel*/);
    getActivePlanViewDisplay().setMapImage(map);
    
    var x = .5 * 2000;
    var y = .5 * 2000;
    
    // Create a new helicopter   
    var helicopter = simjr.entities.create({
    	name:"helicopter", 
    	prototype: "rwa",
    	x: x, 
    	y: y,
    	properties: {
    	    visible: false // Initially invisible (see timer below)
        }
    });
    
    ///////////////////////////////////////////////////////////////////////////////
    // Create a new helicopter, but give it a different appearance by setting its
    // shape property....
    var hello = simjr.entities.create({
    	name:"hello", 
    	prototype:"rwa",
    	x: x - 100,
    	y: y - 100,
    	properties: {
        	// Create an image shape that uses the two images, one for a body, and one for
        	// a shadow. The shape property must be set before the entity is added to the
        	// simulation. The shadow image is optional.
        	shape: ImageEntityShape.create("simjr/images/shapes/hello.png", "simjr/images/shapes/hello-shadow.png")
        }
    });
    
    setVisibleRange(hello, 150, 45);
    
    
    ///////////////////////////////////////////////////////////////////////////////
    
    
    var di = simjr.entities.create({
    	name:"di", 
    	prototype:"dismounted-infantry",
    	x: x - 200,
    	y: y + 200
    });
    
    // Here's how you can set the color of the route line
    var route = simjr.polygons.route({name:"route", properties: {"shape.line.color":java.awt.Color.PINK}},
        [{name:"alpha",   x: x - 400, y: y + 200},
         {name:"bravo",   x: x - 300, y: y - 200},
         {name:"charlie", x: x + 300, y: y - 300},
         {name:"delta",   x: x + 400, y: y + 300}]);
    
    var tank = simjr.entities.create({
    	name:"tank", 
    	prototype:"tank",
    	properties: {
            force: "opposing"
        },
    	x: x + 200,
    	y: y + 200,
    	capabilities: [
    	    // Create a route following behavior for the tank
    	    // This is a pair [capability, init function]
    	    [new RouteFollower(), function(follower){
    	    	follower.route = "route";
    	    	follower.speed = 15.0;
    	    	// Add a listener that prints information about waypoints as the tank reaches them
    	    	follower.addListener(new RouteFollowerListener({ 
    	    	    onWaypointAchieved: function(routeFollower, waypoint) {
    	    	        logger.info(routeFollower.getEntity() + " achieved " + waypoint.getName() + " at " + waypoint.getPosition()); 
    	    	    } 
    	    	}));	    	
    	    }]
    	]
    });
    
    setVisibleRange(tank, 100.0, 45);
    setRadarRange(tank, 200, 60);
    
    // Create an area on the screen with vertics at three objects.
    simjr.polygons.area({
    		name:"area", 
    		properties: {
    		    "shape.fill.color":new java.awt.Color(.5, 0, .25)
            }
    	},
        [helicopter, "tank", di] // vertices (objects or object names)
    ); 
    
    
    // Create an invisible route and a truck to follow it
    var truckRoute = simjr.polygons.route({
    		name:    "truckRoute", 
    		properties: {
    		    visible: false
            }
    	},
    	// List of points in route
        [{name:"truckRoute0", x: x + 200, y: y,       visible:false}, 
         {name:"truckRoute1", x: x,       y: y + 200, visible:false}, 
         {name:"truckRoute2", x: x - 200, y: y,       visible:false}, 
         {name:"truckRoute3", x: x,       y: y - 100, visible:false}]
    );
    
    var truck = simjr.entities.create({
    	name:"truck", 
    	prototype:"truck",
    	x: x + 100,
    	y: y,
    	properties: { 
            ccip: hello.getPosition() // Show CCIP circle pointing to "hello"'s position
        },
    	capabilities: [
    	    // Add a route following behavior. This is a pair [capability, initial props].
    	    [new RouteFollower(), { route: truckRoute, speed: 12.0 }]
    	]
    });
    
    var fwa = simjr.entities.create({
    	name: "F-18", 
    	prototype: "fa-18",
    	x: x + 200,
    	y: y
    });
    fwa.properties.get(EntityConstants.PROPERTY_FUEL).consumptionRate = 5.0;
    
    // Here's how to fire a weapon at a target from a script
    //helicopter.getWeapon("Autocannon-20mm").fire(1, tank)
    
    ///////////////////////////////////////////////////////////////////////////////
    // Setting a timer in the simulation...
    
    // Now set the runnable to run five seconds in the future without repeating.
    simjr.timers.oneshot(5.0, function() { 
        helicopter.setProperty(EntityConstants.PROPERTY_VISIBLE, true); 
    } );
    
    ///////////////////////////////////////////////////////////////////////////////
    
    ///////////////////////////////////////////////////////////////////////////////
    // Setting up the display
    
    // Create two PVDs next to each other, each locked on a particular entity.
    var pvd1 = getActivePlanViewDisplay();
    pvd1.setLockEntity(tank);
    
    var pvd2 = getMainFrame().createPlanViewDisplay(true);
    
    pvd1.showAll();
    pvd2.showAll();
    
    //Here's how to center the PVD on a location at startup rather than showing
    //all entities.
    //getActivePlanViewDisplay().showPosition(helicopter.position)
    
    createCheatSheet("Simple", "cheatsheet.html");
    
    //loadDockingLayout("layout.sjl");

})();
