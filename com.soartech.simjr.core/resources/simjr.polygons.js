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
    Common script utilities for dealing with Waypoints, Routes, and Areas
    
    Load with: requireScript("polygons");
*/

logger.info("loading polygons.js ...");

if(typeof(simjr) == "undefined") {
	simjr = {};
}

/**
 * simjr.polygons "namespace".
 */
simjr.polygons = new (function() {
	function factoryForPrototypePolygon(prototypeName) {
		return function(props, points) {
			if(typeof(props) == "string") {
				props = {
					name: props
				};
			}
			if(!props.prototype) {
				props.prototype = prototypeName;
			}
			return simjr.polygons.polygon(props, points);
		};
	}
	function factoryForPrototypeCompoundArea(prototypeName) {
		return function(props, areas) {
			if(typeof(props) == "string") {
				props = {
					name: props
				};
			}
			if(!props.prototype) {
				props.prototype = prototypeName;
			}
			return simjr.polygons.compoundAreaConstructor(props, areas);
		};
	}
	return {
		/**
		 * Adapt the given object, e.g. an entity, into an AbstractPolygon object.
		 */
		asPolygon: function(o) { return AbstractPolygon.adapt(o) },
		
		/**
		 * Construct a new polygon such as a route, or area.
		 * 
		 * @param props A property struct. See simjr.entities.create. Assumes
		 * 		that an entity convertible to AbstractPolygon is created.
		 * @param points array of point structures. See simjr.polygons.waypoints.
		 * @return the resulting polygon entity.
		 */
		polygon: function(props, points) {
		    var poly = simjr.entities.create(props);
		    simjr.polygons.addPoints(poly, simjr.polygons.waypoints(points));
		    return poly;
		},

		/**
		 * Adapt the given object, e.g. an entity, into an DefaultCompoundPolygon object.
		 */
		asCompoundArea: function(o) { return DefaultCompoundPolygon.adapt(o) },
		
		/**
		 * Construct a new compound area.
		 * 
		 * @param props A property struct. See simjr.entities.create. Assumes
		 * 		that an entity convertible to DefaultCompoundPolygon is created.
		 * @param areas array of entities representing areas. See simjr.polygons.waypoints.
		 * @return the resulting compound area entity.
		 */
		compoundAreaConstructor: function(props, areas) {
		    var poly = simjr.entities.create(props);
		    var compoundArea = simjr.polygons.asCompoundArea(poly);
		    var areas = simjr.polygons.waypoints(areas);
			for each(var area in areas){
				compoundArea.addPolygon(area);
			}
		    return poly;
		},
		
		/**
		 * Construct a waypoint. This is a shortcut for simjr.entities.create
		 * with a prototype of "waypoint".
		 */
		waypoint: simjr.entities.factoryForPrototype("waypoint"),
		
		/**
		 * Construct a list of waypoints from a list of point descriptions.
		 * The input array can consist of any of the following:
		 * <ul>
		 * <li>An instance of com.soartech.simjr.sim.Entity. Passes through
		 *  unchanged.
		 * <li>A point structure, passed to simjr.polygons.waypoint.
		 * <li>A string. Retrieves the entity with the name stored in the string.
		 *   If no entity is found, the string is passed to simjr.polygons.waypoint
		 *   to create a new default waypoint.
		 * </ul>
		 * 
		 * @param points Array of points. See above.
		 * @return List of waypoint entities
		 */
		waypoints: function(points){
			var result = []
		    for each(var p in points)
		    {
		        var wp = p;
		        if(p instanceof Packages.com.soartech.simjr.sim.Entity) {
		            // Nothing to do
		        }
		        else if(typeof(p) == "object"){
		            wp = simjr.polygons.waypoint(p);
		        }
		        else if(typeof(p) == "string")
		        {
		            wp = simulation.getEntity(p);
		            if(wp == null) {
		                wp = simjr.polygons.waypoint(p);
		            }
		        }
		        result.push(wp);
		    }
			return result;
		},
		
		route: factoryForPrototypePolygon("route"),
		
		area: factoryForPrototypePolygon("area"),
		
		complexArea: factoryForPrototypePolygon("complex-area"),

		complexAreaStatic: factoryForPrototypePolygon("complex-area-static"),

		compoundArea: factoryForPrototypeCompoundArea("compound-area"),
		
		segments: function createSegments(points) {
		    var lastSegment = null;
			for(var i = points.length - 1; i >= 0; i--)	{
			    var point = points[i];
			    var name = point;
			    var speed = 10.0;
			    if(!(point instanceof java.lang.String) && typeof(point) != "string") {
			        name = point.name;
			        if(point.speed) {
			            speed = point.speed;
			        }
			    }
			    lastSegment = new Packages.com.soartech.simjr.controllers.SegmentInfo(name, speed, lastSegment);
			}
			return lastSegment;
		},
		
		addPoints: function(container, points) {
			if(typeof(container) == "string"){
				container = simulation.getEntity(container);
			}
			var poly = asPolygon(container);
			for each(var p in points){
				poly.addPoint(p);
			}
		}
	};	
});

// Legacy functions...
asPolygon = simjr.polygons.asPolygon;
createWaypoint = simjr.polygons.waypoint;
createWaypoints = simjr.entities.waypoints;
createRoute = simjr.polygons.route;
createArea = simjr.polygons.area;
createSegments = simjr.polygons.segments;

logger.info("finished loading polygons.js");

