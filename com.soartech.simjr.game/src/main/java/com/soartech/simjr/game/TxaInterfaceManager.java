package com.soartech.simjr.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.AbstractAdaptable;
import com.soartech.simjr.game.message.TxaDirectiveMessage;
import com.soartech.simjr.game.message.TxaGameMessage;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.services.SimulationService;
import com.soartech.simjr.sim.Simulation;
import com.soartech.switchboard.Publisher;
import com.soartech.switchboard.Switchboard;
import com.soartech.switchboard.message.MessageBuilder;
import com.soartech.txa.proto.TxaGame;

/**
 * SimulationService that maintains the TXA interface.
 * 
 * @author aron
 *
 */
public class TxaInterfaceManager extends AbstractAdaptable implements
        SimulationService {

    // Constants
    private static final Logger logger = LoggerFactory.getLogger(TxaInterfaceManager.class);
    

    // Grabbing a Publisher handle (usually a class will share a publisher)
    // The Publisher name can be anything you want to use to identify the sender, such as the class name.
    private static Publisher publisher = Switchboard.getInstance().requestPublisher("DirectiveUI");

    
    // Services
    private final ServiceManager services;
    private final Simulation simulation;

    /**
     * @param services
     * @return The active instance of the the TxaInterfaceManager service
     */
    public static TxaInterfaceManager findService(ServiceManager services) {
        return services.findService(TxaInterfaceManager.class);
    }

    /**
     * @param services
     */
    public TxaInterfaceManager(ServiceManager services) {
        this.services = services;
        this.simulation = Simulation.findService(services);
    }
    
    @Override
    public void start(ProgressMonitor progress) throws SimulationException {
        logger.info("Starting the TXA Interface manager");

       // For sending and receiving, you need to register the message meta-type.
       // You only need to do this once, but you need to do it before the message is sent or received.
       MessageBuilder.getInstance().registerMessage(TxaGameMessage.class);
       MessageBuilder.getInstance().registerMessage(TxaDirectiveMessage.class);
       
       Switchboard.getInstance().connect("localhost");
    }

    @Override
    public void shutdown() throws SimulationException {
        // TODO Auto-generated method stub

    }
    
    public void turnLeft(String player) {
        logger.info("Turn left : " + player);
        
        // Example of constructing a protocol buffer.
        // OUTER_CLASS is typically the name of the .proto or .java file containing the protocol buffers.
        // INNER_CLASS is, in the usual protocol buffer pattern, the name of the "container" class.
        // MESSAGE_TYPE is, in the usual protocol buffer pattern, the name of the "contained" message type.
        TxaGame.TxaGameMessage msg = TxaGame.TxaGameMessage.newBuilder()
            .setType(TxaGame.TxaGameMessage.Type.CHANGE_DIRECTION)
            .setChangeDirection(
                    TxaGame.ChangeDirection.newBuilder()
                    .setDegreeChange(-15.0)
                    .setEntityName(player)
        ).build();
        
        // Send the newly constructed message.
        publisher.publish(new TxaGameMessage(msg));
    }
    
    public void turnRight(String player) {
        logger.info("Turn right : " + player);
        

        // Example of constructing a protocol buffer.
        // OUTER_CLASS is typically the name of the .proto or .java file containing the protocol buffers.
        // INNER_CLASS is, in the usual protocol buffer pattern, the name of the "container" class.
        // MESSAGE_TYPE is, in the usual protocol buffer pattern, the name of the "contained" message type.
        TxaGame.TxaGameMessage msg = TxaGame.TxaGameMessage.newBuilder()
            .setType(TxaGame.TxaGameMessage.Type.CHANGE_DIRECTION)
            .setChangeDirection(
                    TxaGame.ChangeDirection.newBuilder()
                    .setDegreeChange(15.0)
                    .setEntityName(player)
        ).build();
        
        // Send the newly constructed message.
        publisher.publish(new TxaGameMessage(msg));
    }
    
    public void speedUp(String player) {
        logger.info("Speed Up : " + player);
        
        TxaGame.TxaGameMessage msg = TxaGame.TxaGameMessage.newBuilder()
                .setType(TxaGame.TxaGameMessage.Type.CHANGE_SPEED)
                .setChangeSpeed(
                        TxaGame.ChangeSpeed.newBuilder()
                        .setSpeedFactor(1)
                        .setEntityName(player)
            ).build();
            
            // Send the newly constructed message.
            publisher.publish(new TxaGameMessage(msg));
    }
    
    public void speedDown(String player) {
        logger.info("Speed Down : " + player);
        
        TxaGame.TxaGameMessage msg = TxaGame.TxaGameMessage.newBuilder()
                .setType(TxaGame.TxaGameMessage.Type.CHANGE_SPEED)
                .setChangeSpeed(
                        TxaGame.ChangeSpeed.newBuilder()
                        .setSpeedFactor(.2)
                        .setEntityName(player)
            ).build();
            
            // Send the newly constructed message.
            publisher.publish(new TxaGameMessage(msg));
    }

}
