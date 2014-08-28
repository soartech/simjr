importPackage(Packages.com.example.simjr.shapes)

/**
 * Creates the various services used by the example plugin and registers them with
 * the service manager
 * 
 * @param services
 */
function initExample(services) {
	
	var shapeManager = new ExampleShapeManager(services);
	services.addService(shapeManager);
	shapeManager.start(null);
	
}
