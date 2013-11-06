// An example that creates a 1000 trucks and gives them random routes to follow.

requireScript("polygons");

(function(){
    // Create a 50x50 grid of waypoints
    var gridSize = 50;
    var points = [];
    for(var row = 0; row < gridSize; row++){
        for(var col = 0; col < gridSize; col++) {
            points.push(simjr.polygons.waypoint({
                name:"p-"+row+"-"+col, 
                x: col * 100, 
                y: row * 100,
                properties: {
                    visible:false  // waypoints obscure everything
                }
            }));
        }
    }
    
    function pick(a) {return a[Math.floor(Math.random()*a.length)];}
    
    // Pick a random waypoint
    function randomPoint() { return pick(points); }
    
    // Build a random list of points to travel through
    function randomRoute() {
        var result = [];
        var size = 2 + Math.floor(Math.random()* 4); // 2 to 6 segments long
        for(var i = 0; i < size; i++){
            result.push(randomPoint());
        }
        return result;
    }
    
    var vehicleCount = 1000;
    for(var i = 0; i < vehicleCount; i++) {
        var v = simjr.entities.create({
            name:       "v-" + i,
            prototype:  pick(["truck", "tank", "mobile-sam"]),
            position:   randomPoint().getPosition(),
            properties: {
                force:      pick(["friendly", "opposing", "neutral", "unknown"]),
                "shape.label.visible": false, // labels obscure everything
            },
            capabilities: [
                // Follow a random route
                [new Packages.com.soartech.simjr.controllers.SegmentFollower(), function(follower) {
                    follower.setRouteVisible(false); // routes obscure everything
                    follower.setSegments(simjr.polygons.segments(randomRoute()));
                }]
            ]
        });
    }
})();