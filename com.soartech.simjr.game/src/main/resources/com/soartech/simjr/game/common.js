
/**
 * Creates the various services used by the plugin and registers them with
 * the service manager
 * 
 * Initializing services somewhere like here called by the scenario is preferred 
 * if the scenario is running when Sim Jr loads.
 * 
 * This is because the Java SPI sometimes won't have a chance to create the services necessary
 * for the scenario before it starts loading its entities.
 * 
 * If that is the case, remove any Java SPI mappings for services created here, as the architecture
 * currently doesn't support creating services with Java SPI that have already been created.
 * 
 * @param services
 */
function initGameServices(services) {
	
	logger.info("Creating TXA Game services")
	
	var txaInterfaceManager = new TxaInterfaceManager(services);
	services.addService(txaInterfaceManager);
	txaInterfaceManager.start(null);
	
	
//	// an example of how to create a custom service
//	logger.info("Create the ExampleShapeManager service")
//	var shapeManager = ExampleShapeManager.findService(services);
	
	//add any other service initialization code here
}
