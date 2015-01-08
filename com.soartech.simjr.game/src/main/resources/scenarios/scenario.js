// Soar Technology Proprietary, Restricted Rights

// Must set terrain BEFORE running common.js!

importPackage(Packages.com.soartech.simjr.controllers)
importPackage(Packages.com.soartech.simjr.scripting)
importPackage(Packages.com.soartech.simjr.sim)
importPackage(Packages.com.soartech.simjr.sim.entities)
importPackage(Packages.com.soartech.simjr.jsoar)

importPackage(Packages.com.soartech.math.geotrans)
importPackage(Packages.com.soartech.math)
importPackage(Packages.com.soartech.simjr)
importPackage(Packages.com.soartech.simjr.ui) // For SimulationMainFrame
importPackage(Packages.com.soartech.simjr.ui.pvd)  // For MapImage
importPackage(Packages.com.soartech.simjr.ui.shapes)  // For MapImage

requireScript("polygons");
requireScript("ui");
requireScript("terrain");
requireScript("dis");
requireScript("weapons");
requireScript("fwasp");

// Set up the background map
// Note that the origin is also set in TinyNiceServer#createDummySim()
setOrigin({"latitude": 37.188943, "longitude": -115.019989});
mapFile = getRelativeFile("nellis-area-map.png");
mapOrigin = new Vector3(0, 0, 0) // Center of map in meters
metersPerPixel = 485.933
//metersPerPixel = 50;
map = new MapImage(mapFile, mapOrigin, metersPerPixel);
map.setOpacity(0.5);
getActivePlanViewDisplay().setMapImage(map)

scenario = {}

/**
 * Creates a friendly generic fighter. 
 */
function createFWASPFighter(name, force)
{
    var friendly = createEntity({
        name:name, 
        prototype:"generic-fighter", 
        properties: {
            "force": force,
            "radios": "a, b, guard",
            "fwasp.mission.path": getRelativeFile("."),
            "fwasp.vista.enabled": true, 
            "DIS_TYPE": "1.2.225.2.4.1"
        }
    });
    EntityTools.getFuelModel(friendly).setConsumptionRate(1.0);
    //setVisibleRange(friendly, 8000, 360);
    //setRadarRange(friendly, 15000, 270);
    setVisibleRange(friendly, 11000, 360);
    setRadarRange(friendly, 140000, 360);
    
    friendly.setProperty(EntityConstants.PROPERTY_RADAR_VISIBLE, true);
    friendly.setProperty(EntityConstants.PROPERTY_VISIBLE_RANGE_VISIBLE, false);
    
    addWeapon(friendly, "generic-missile", 4);
    addWeapon(friendly, "generic-bomb", 4);
    addWeapon(friendly, "generic-cannon", 4);
    addWeapon(friendly, "maverick", 4);
    addWeapon(friendly, "gbu12", 4);
    addWeapon(friendly, "mk-82", 4);
    addWeapon(friendly, "laser-dsg", 4);
    
    return friendly;
}

function createFWASPEntity(name, force) {
	var entity=createFWASPFighter(name, force);
	entity.setProperty("vehicle-parameters",getRelativeFile("./vehicle-parameters.xml"));
	entity.setProperty("a2a-weapon-parameters",getRelativeFile("./a2a-weapon-parameters.xml"));
	entity.setProperty("sap","true");
	return entity;
}

//function setupSoarSpeakIO (fwaspagent) {
//	var sjpkg = Packages.com.soartech.simjr;
//	var agent = fwaspagent.getSoarAgent();
//    var ssi = new sjpkg.swarm.SoarSpeakInput(agent.getAgent(), "radio-messages", "radios");
//    var crbc = sjpkg.soarspeak.CustomRadioBridgeClient
//            .newCustomRadioBridgeClient()
//            .id(agent.getName())
//            .type("agent")
//            .callsign(agent.getName())
//            .grammar("grammar.dll")
//            .semantic("grammar_ss.xml")
//            .listener(ssi)
//            .build();
//
//    var rms = {
//    	sendMessage : function(to, message) {
//    		return crbc.sendMessage(to, message);
//    	},
//    };
//    
//    var rmsi = new Packages.com.soartech.simjr.jsoar.RadioMessageSink(rms);
//    
//    new sjpkg.fwasp.commands.RadioMessageByFrequencyCommand(fwaspagent.getOutputManager(), rmsi);
//}

function createFWASPAgent(entity) {
	runner.subTask("Creating FWASP Agent " + entity.getName() + " ...");
	//var dir = SimJrProps.get("simjr.fwasp.agent.home")
	//var knowledge = new java.io.File(dir, "knowledge.xml")
	entity.setProperty("knowledge", "agent/knowledge.xml");
	var agent1 = new Packages.com.soartech.simjr.fwasp.FWASPAgent(services, entity);
	//setupSoarSpeakIO(agent1);
	agent1.loadDefaultProductions();
	
	//Control agent debugger opening with properties. See resources/../user.properties.readme
	
	// This makes the agent start running when play is pressed in the simulation (and stop when it is paused)
	agent1.runWithSimulation(true);
	return agent1;
}

/**
 * Creates a scripted fighter aircraft.
 */

function createGenericFighter(name)
{
    var entity = createEntity({
        name:name, 
        prototype:"generic-fighter", 
        properties: {
            force:"friendly",
            "radios": "a, b, guard",
            "DIS_TYPE": "1.2.225.2.4.1"
        }
    });
    entity.setProperty(EntityConstants.PROPERTY_RADAR_VISIBLE, false);
    entity.setProperty(EntityConstants.PROPERTY_VISIBLE_RANGE_VISIBLE, false);
    return entity;
}

/**
 * Creates a fueler aircraft.
 */

function createPatrolFueler(name, points, speed)
{
    var entity = createEntity({
        name:name, 
        prototype:"generic-fueler", 
        properties: {
            force:"friendly",
            "radios": "a, b, guard",
            "DIS_TYPE": "1.2.225.2.4.1"
        }
    });
    entity.setProperty(EntityConstants.PROPERTY_RADAR_VISIBLE, false);
    entity.setProperty(EntityConstants.PROPERTY_VISIBLE_RANGE_VISIBLE, false);
    entity.setProperty(EntityConstants.PROPERTY_SHAPE_LABEL_VISIBLE, false);
    
	var route = createRoute(name + "-rt", points);
	EntityTools.setVisible(route, false);
	
    simjr.entities.capabilities.add(entity, new RouteFollower(), { route: name + "-rt", speed: speed});
    
    return entity;
}

/**
 * Creates a tank that patrols between the given points.
 */
function createPatrolTank(tankName, points, speed)
{
	var route = createRoute(tankName + "-rt", points);
	EntityTools.setVisible(route, false);
	
	var tank = createEntity({name:tankName, prototype:"tank"});
	tank.setProperty(EntityConstants.PROPERTY_FORCE, EntityConstants.FORCE_OPPOSING);
	tank.setProperty(EntityConstants.PROPERTY_SHAPE_LABEL_VISIBLE, false);
	simjr.entities.capabilities.add(tank, new RouteFollower(), { route: tankName + "-rt", speed: speed});
	
	return tank;
}
 /**
 *	Create solider for arming station.
 */
function createGroundSoldier(name)
{
    var entity = createEntity({
        name:name, 
        prototype:"dismounted-infantry", 
        properties: {
            force:"friendly",
            "DIS_TYPE": "1.2.225.2.4.2"
        }
    });
    return entity;
}

/**
 * Creates a scripted version of the viper21 lead aircraft.
 */
function createTestLead()
{
	var leader = createGenericFighter("Viper21");
	// Give the FWASP agents time to initialize
	//var slowTaxiSpeed = 1;
	//var taxiSpeed = 1;
	//var takeoffSpeed = 250;
	//var cruisingSpeed = 250;
	var follower = new SegmentFollower();
	follower.setSegments( new SegmentInfo("viper21-marker", slowTaxiSpeed, 
						  new SegmentInfo("viper-lane-marker", taxiSpeed, 5.0,
						  new SegmentInfo("3L-arming-1-entry", taxiSpeed,
						  new SegmentInfo("3L-arming-station-1", slowTaxiSpeed,
						  new SegmentInfo("3L-arming-1-exit", taxiSpeed,
						  new SegmentInfo("3-hold-west-spot", taxiSpeed,
						  new SegmentInfo("3L-runway-start", taxiSpeed, 
					      new SegmentInfo("3L-runway-end", takeoffSpeed, 30.0,
					      new SegmentInfo("apex", cruisingSpeed, 
					      new SegmentInfo("dry-lake", cruisingSpeed, 
					      new SegmentInfo("junno", cruisingSpeed, null))))))))))));
								  
	simjr.entities.capabilities.add(leader, follower, null);
}

runner.subTask("Loading waypoints ...");
runner.evalFile("waypoints.js");
runner.subTask("Loading positions ...");
runner.evalFile("positions.js");

createCheatSheet("GBE Sweep Scenario", "new-script-table.html");

// The FWASP agents and other non-scenario configuration is handled in run.js
var centerPvd = new java.lang.Runnable({run : function() {
   var pvd1 = getActivePlanViewDisplay();
   pvd1.showAll();
//   pvd1.zoom(-70);
//   showEntity(pvd1, "Viper22");
}});
// Run the PVD code in the swing thread
javax.swing.SwingUtilities.invokeAndWait(centerPvd);
//Google Earth support. 
//requireScript("web");
//simjr.web.install();
//simjr.web.openBrowser();
