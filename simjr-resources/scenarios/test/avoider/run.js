
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
    
    // Create a couple of waypoints to avoid
    simjr.polygons.waypoint({ name: "b", properties: { "shape.line.color" : java.awt.Color.RED }});
    simjr.polygons.waypoint({ name:"f", x: 0, y: 100, properties: { "shape.line.color" : java.awt.Color.RED } });
    
    // Create a truck to drive the route
    simjr.entities.create({
        name:      "avoider",
        prototype: "truck",
        x:         -100,     // start at point A
        capabilities: [
            new Packages.com.soartech.simjr.controllers.RouteFollower(),
            new Packages.com.soartech.simjr.controllers.Avoider()
        ],
        properties: {
            "routeFollower.route" : "route",
            // Set the points to avoid. Can be an entity, an entity name, or a location
            "avoider.points": createList(["b", new Vector3(100, 50, 0), simjr.entities("f")]),
            "avoider.radius": 20.0
        }
    });
    
    // reverse route
    simjr.polygons.route("route2", ["c", "a", "g", "e"]);
    
    // Create another truck that drives route in reverse and avoids first truck
    simjr.entities.create({
        name:      "avoider2",
        prototype: "truck",
        x:         100,     // start at point B
        capabilities: [
            new Packages.com.soartech.simjr.controllers.RouteFollower(),
            new Packages.com.soartech.simjr.controllers.Avoider()
        ],
        properties: {
            "routeFollower.route" : "route2",
            "routeFollower.speed" : 5,
            // Set the points to avoid. Can be an entity, an entity name, or a location
            "avoider.points": createList(["b", "avoider"]),
            "avoider.radius": 30.0
        }
    });
    
    getActivePlanViewDisplay().showAll();
})();
