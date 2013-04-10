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
logger.info("loading entities.js ...");

importPackage(Packages.com.soartech.simjr.sim.entities)

if(typeof(simjr) == 'undefined') 
{
    simjr = {};
}

/** Access the entity prototype database */
function getEntityPrototypes()
{
	return Packages.com.soartech.simjr.sim.EntityPrototypeDatabase.findService(services);
}

function getEntityPrototype(name)
{
    if(name instanceof Packages.com.soartech.simjr.sim.EntityPrototype) {
        return name;
    }
	return getEntityPrototypes().getPrototype(String(name));
}

/**
 * Create a new entity using the given properties. 
 * 
 * <p>options is a map of properties for the new entity. It should at least have
 * "name" and "prototype" properties setting the name and prototype of the new
 * entity. prototype may be a prototype name, or an instance of EntityPrototype.
 * Additional special properties are x, y, and z for the initial position.
 * Also orientation sets the initial orientation in degrees.
 * The "capabilities" property is a list of capabilities that will be added to the
 * entity.
 * The "properties" property lists additional entity properties to set. For example:
 * 
 * <pre>
 * var e = simjr.entities.create({
 *          name:"Rebel20", 
 *          prototype:"ah-64", 
 *          x:1000, 
 *          properties: { "shape.label.visible":false} });
 * </pre>
 * @param options properties
 * @param doNotAdd if false, or omitted, the entity is automatically added to the sim
 * @return the new entity
 */
function createEntity(options, doNotAdd)
{
    // Convert to prototype object
	var prototypeObj = simjr.entities.prototypes(options.prototype);
	var name = options.name;
	logger.debug("creating " + name + " from prototype " + prototypeObj.getId());
	var e = prototypeObj.createEntity(name);
	
	if(options.hasOwnProperty("position")) {
	    e.setPosition(options.position);
	}
	else {
        var x = 0.0;
        var y = 0.0;
        var z = 0.0;
        if(options.hasOwnProperty("x")) {
            x = options["x"];
        }
        if(options.hasOwnProperty("y")) {
            y = options["y"];
        }
        if(options.hasOwnProperty("z")) {
            z = options["z"];
        }
        e.setPosition(new Vector3(x, y, z));
	}
    
    if(options.hasOwnProperty("orientation"))
    {
        e.setOrientation(java.lang.Math.toRadians(options["orientation"]));
    }
    
	if(options.properties){
	    var props = options.properties;
        for(var prop in props) {
            if(props.hasOwnProperty(prop)){
                e.setProperty(prop, props[prop]);
            }
        }
	}
    
    if(typeof(doNotAdd) == 'undefined' || !doNotAdd) 
    {
    	simulation.addEntity(e);
    }
    
    // Do this after props since some caps may get initial values from props
    // Do this after entity added because some caps may assume the entity is
    // part of the sim :(
    for each(var cap in options.capabilities) {
        if(cap instanceof Array){
            cap = simjr.entities.capabilities.create(cap[0], cap[1]);
        } else {
            cap = simjr.entities.capabilities.create(cap);
        }
        e.addCapability(cap);
    }
        
    return e;
}
 
function setRangeProperty(entity, property, range, angle) {
	var vr = entity.getProperties().get(property);
	if(vr == null)
	{
	    vr = new EntityVisibleRange(entity, property);
	    entity.setProperty(property, vr);
	}
	vr.setVisibleRange(range);
	vr.setVisibleAngle(java.lang.Math.toRadians(angle));
}
 
/**
 Set the visible range for an entity
 
 @param entity The entity
 @param range The range in meters
 @param angle The visible angle in degrees
*/
function setVisibleRange(entity, range, angle) {
	setRangeProperty(entity, EntityConstants.PROPERTY_VISIBLE_RANGE, range, angle);
}

/**
 Set the radar range for an entity
 
 @param entity The entity
 @param range The range in meters
 @param angle The radar angle in degrees
*/
function setRadarRange(entity, range, angle){
	setRangeProperty(entity, EntityConstants.PROPERTY_RADAR, range, angle);
}

simjr.entities = new (function() {
    /**
     * Retrieve an entity by name, e.g. simjr.entities("foo")
     */
	var thisFunc = function(name) {
	    return simulation.getEntity(name);
	};
	
	thisFunc.create = createEntity;
	
	thisFunc.factoryForPrototype = function(prototype) {
		return function(options) {
			if(typeof(options) == "string") {
				options = {
					name: options
				};
			}
			if(!options.prototype) {
				options.prototype = prototype;
			}
			return simjr.entities.create(options);
		}
	};
	
	return thisFunc;
})();

simjr.entities.prototypes = new (function() {
	function database() { return getEntityPrototypes(); }
	
	function specialize(parent, options) {
		function nullable(x) { return x !== undefined ? x : null; }
		
		var simPkg = Packages.com.soartech.simjr.sim;
		if(!(parent instanceof simPkg.EntityPrototype)){
			parent = simjr.entities.prototypes(parent);
		}
		var builder = new simPkg.DefaultEntityPrototype.newBuilder();
		builder.id(options.id).
		        parent(parent).
				domain(nullable(options.domain)).
				category(nullable(options.category)).
				subcategory(nullable(options.subcategory)).
				factory(nullable(options.factory));
		for(var k in options.properties) {
			builder.property(k, options.properties[k]);
		}
		var caps = options.capabilities;
		if(caps !== undefined) {
			var capsList = new java.util.ArrayList();
			for each(var c in caps) {
				capsList.add(c);
			}
			builder.property("capabilities", capsList);
		}
		var spec = builder.build();
		return database().addPrototype(spec);
	}
	
	var thisFunc = function(name) {
	    return getEntityPrototype(name);
	};
	thisFunc.database = database;
	thisFunc.specialize = specialize;
	
	return thisFunc;
})();

simjr.entities.capabilities = new (function() {
	
	return {
		create: function(cap, options) {
			if(typeof(cap) == "function") {
				cap = cap();
			}
			else if(typeof(cap) == "string") {
				cap = classForName(cap).newInstance();
			}
			
			if(typeof(options) != "function") {
				for(var key in options) {
					logger.debug("key=" + key );
					cap[key] = options[key];
				}
			}
			else {
				options(cap);
			}
			return cap;
		},
		add: function(entity, cap, options) {
			var c = simjr.entities.capabilities.create(cap, options);
			entity.addCapability(c);
			return c;
		}
	};	
})();

logger.info("finished loading entities.js");
