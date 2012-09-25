package com.soartech.simjr.sensors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.soartech.simjr.sim.EntityPrototypes;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.SimpleTerrain;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.entities.Vehicle;

public class ContactManagerTest extends TestCase
{
    // TODO: Mocking would probably be a better way to test this
    /**
     * Simple listener implementation for testing.
     * 
     * @author rdf
     */
    private static class TestListener implements ContactManagerListener
    {
        
        private static class StateChange {
            private Contact contact;
            private ContactState oldState;
            
            private StateChange(Contact contact, ContactState oldState) 
            {
                this.contact = contact;
                this.oldState = oldState;
            }
        }
        
        private List<Contact> createdContacts = new ArrayList<Contact>();
        private List<Contact> destroyedContacts = new ArrayList<Contact>();
        private List<StateChange> stateChanges = new ArrayList<StateChange>();

        @Override
        public void createdContact(Contact contact)
        {
            createdContacts.add(contact);
        }

        @Override
        public void destroyedContact(Contact contact)
        {
            destroyedContacts.add(contact);
        }

        @Override
        public void contactStateChanged(Contact contact, ContactState oldState)
        {
            stateChanges.add(new StateChange(contact,oldState));
        }
        
        public void reset() 
        {
            createdContacts.clear();
            destroyedContacts.clear();
            stateChanges.clear();
        }
        
    }
    
    /**
     * Simple sensor implementation used for testing that 
     * isn't much more than a pass through for detections.
     * 
     * @author Rich Frederiksen
     *
     */
    private static class TestSensor extends AbstractSensor implements VisionSensor, RadarSensor
    {
        private List<Detection> detections = new ArrayList<Detection>();

        public TestSensor(String name)
        {
            super(name);
        }

        @Override
        public void tick(double dt)
        {
        }

        @Override
        public List<Detection> getDetections()
        {
            // Usually would make this unmodifiable but in this case it makes to
            // change the sensors detections directly for testing.
            return detections;
        }
    }
    
    private Vehicle vehicle;
    private ContactManager contactManager;
    private TestListener contactListener;
    private TestSensor sensor;
    private Simulation simulation;
    
    private Detection createDetection(String entityName, DetectionType detectionType)
    {
        return new Detection(sensor, 
                new Vehicle(entityName,EntityPrototypes.NULL), 
                new HashMap<String,Object>(), 
                detectionType);
    }
    
    @Override
    public void setUp() 
    {
        vehicle = new Vehicle("test-vehicle", EntityPrototypes.NULL);
        contactManager = new ContactManager(vehicle);
        contactListener = new TestListener();
        contactManager.addContactManagerListener(contactListener);
        sensor = new TestSensor("test");
                
        SensorPlatform sensorPlatform = EntityTools.getSensorPlatform(vehicle);
        sensorPlatform.addSensor("test-sensor", sensor);
        
        simulation = new Simulation( SimpleTerrain.createExampleTerrain(), false);
        vehicle.setSimulation(simulation);
    }
    
    public void testBasicContactManagement() 
    {
        List<Detection> detections = sensor.getDetections();
        detections.clear();
        detections.add(createDetection("vehicle-1",DetectionType.RADAR));
        detections.add(createDetection("vehicle-1",DetectionType.VISIBLE));
        detections.add(createDetection("vehicle-2",DetectionType.RADAR));
        detections.add(createDetection("vehicle-3",DetectionType.VISIBLE));
        
        contactManager.update(1.);
        
        Map<String,Contact> contacts = contactManager.getContacts();
        assertEquals(3, contacts.size());
        
        assertEquals("vehicle-1", contacts.get("vehicle-1").getEntity().getName());
        assertEquals(Double.POSITIVE_INFINITY, contacts.get("vehicle-1").getExpirationTime());
        assertEquals(ContactState.VISIBLE, contacts.get("vehicle-1").getState());
        
        assertEquals("vehicle-2", contacts.get("vehicle-2").getEntity().getName());
        assertEquals(Double.POSITIVE_INFINITY, contacts.get("vehicle-2").getExpirationTime());
        assertEquals(ContactState.RADAR, contacts.get("vehicle-2").getState());

        assertEquals("vehicle-3", contacts.get("vehicle-3").getEntity().getName());
        assertEquals(Double.POSITIVE_INFINITY, contacts.get("vehicle-3").getExpirationTime());
        assertEquals(ContactState.VISIBLE, contacts.get("vehicle-3").getState());
    }

    public void testContactChangesOverTime()
    {
        List<Detection> detections = sensor.getDetections();
        detections.clear();
        detections.add(createDetection("vehicle-1",DetectionType.VISIBLE));
        detections.add(createDetection("vehicle-2",DetectionType.RADAR));
        
        contactManager.update(1.0);
        
        Map<String,Contact> contacts = contactManager.getContacts();
        assertEquals(2, contacts.size());

        assertEquals(ContactState.VISIBLE, contacts.get("vehicle-1").getState());
        assertEquals(ContactState.RADAR, contacts.get("vehicle-2").getState());

        simulation.setTime(1.0);
        detections.clear();
        detections.add(createDetection("vehicle-1",DetectionType.RADAR));
        detections.add(createDetection("vehicle-2",DetectionType.VISIBLE));
        
        contactManager.update(1.0);
        
        assertEquals(2, contacts.size());
        assertEquals(ContactState.RADAR, contacts.get("vehicle-1").getState());
        assertEquals(ContactState.VISIBLE, contacts.get("vehicle-2").getState());

        simulation.setTime(2.0);
        detections.clear();
        contactManager.update(1.0);

        assertEquals(2, contacts.size());        
        assertEquals(2.0 + Contact.projectionDuration, contacts.get("vehicle-1").getExpirationTime());
        assertEquals(ContactState.PROJECTED, contacts.get("vehicle-1").getState());
        assertEquals(2.0 + Contact.projectionDuration, contacts.get("vehicle-2").getExpirationTime());
        assertEquals(ContactState.PROJECTED, contacts.get("vehicle-2").getState());
        
        simulation.setTime(3.0);
        detections.clear();
        contactManager.update(1.0);

        assertEquals(2, contacts.size());        
        assertEquals(2.0 + Contact.projectionDuration, contacts.get("vehicle-1").getExpirationTime());
        assertEquals(ContactState.PROJECTED, contacts.get("vehicle-1").getState());
        assertEquals(2.0 + Contact.projectionDuration, contacts.get("vehicle-2").getExpirationTime());
        assertEquals(ContactState.PROJECTED, contacts.get("vehicle-2").getState());

        simulation.setTime(12.0);
        detections.clear();
        contactManager.update(9.0);
        
        assertEquals(2, contacts.size());        
        assertEquals(12.0 + Contact.disappearingDuration, contacts.get("vehicle-1").getExpirationTime());
        assertEquals(ContactState.PROJECTED_DISAPPEARING, contacts.get("vehicle-1").getState());        
        assertEquals(12.0 + Contact.disappearingDuration, contacts.get("vehicle-2").getExpirationTime());
        assertEquals(ContactState.PROJECTED_DISAPPEARING, contacts.get("vehicle-2").getState());
        
        simulation.setTime(13.0);
        detections.clear();
        contactManager.update(1.0);
        
        assertEquals(2, contacts.size());        
        assertEquals(12.0 + Contact.disappearingDuration, contacts.get("vehicle-1").getExpirationTime());
        assertEquals(ContactState.PROJECTED_DISAPPEARING, contacts.get("vehicle-1").getState());        
        assertEquals(12.0 + Contact.disappearingDuration, contacts.get("vehicle-2").getExpirationTime());
        assertEquals(ContactState.PROJECTED_DISAPPEARING, contacts.get("vehicle-2").getState());

        simulation.setTime(17.0);
        detections.clear();
        contactManager.update(4);
        
        assertEquals(0, contacts.size());
    }

    // TODO: These tests are kind of long winded see if they can be tightened up a little
    //
    // Create a helper function that does the sim setTime, contactManager.update and detection settting chores.
    // something with vargs like testTick(double dt, Detection... detections) might be cleanest
    public void testContactDisappearsAndThenReappears()
    {
        List<Detection> detections = sensor.getDetections();
        detections.clear();
        detections.add(createDetection("vehicle-1",DetectionType.VISIBLE));
        
        contactManager.update(1.0);
        
        Map<String,Contact> contacts = contactManager.getContacts();
        assertEquals(1, contacts.size());

        assertEquals(ContactState.VISIBLE, contacts.get("vehicle-1").getState());

        simulation.setTime(1.0);
        detections.clear();
        detections.add(createDetection("vehicle-1",DetectionType.RADAR));
        
        contactManager.update(1.0);
        
        assertEquals(1, contacts.size());
        assertEquals(ContactState.RADAR, contacts.get("vehicle-1").getState());

        simulation.setTime(2.0);
        detections.clear();
        contactManager.update(1.0);

        assertEquals(1, contacts.size());        
        assertEquals(2.0 + Contact.projectionDuration, contacts.get("vehicle-1").getExpirationTime());
        assertEquals(ContactState.PROJECTED, contacts.get("vehicle-1").getState());
        
        simulation.setTime(3.0);
        detections.clear();
        contactManager.update(1.0);

        assertEquals(1, contacts.size());        
        assertEquals(2.0 + Contact.projectionDuration, contacts.get("vehicle-1").getExpirationTime());
        assertEquals(ContactState.PROJECTED, contacts.get("vehicle-1").getState());

        simulation.setTime(4.0);
        detections.clear();
        detections.add(createDetection("vehicle-1", DetectionType.RADAR));
        contactManager.update(1.0);

        assertEquals(1, contacts.size());        
        assertEquals(Double.POSITIVE_INFINITY, contacts.get("vehicle-1").getExpirationTime());
        assertEquals(ContactState.RADAR, contacts.get("vehicle-1").getState());

        simulation.setTime(5.0);
        detections.clear();
        contactManager.update(1.0);

        assertEquals(1, contacts.size());        
        assertEquals(5.0 + Contact.projectionDuration, contacts.get("vehicle-1").getExpirationTime());
        assertEquals(ContactState.PROJECTED, contacts.get("vehicle-1").getState());

        simulation.setTime(15.0);
        detections.clear();
        contactManager.update(10.0);

        assertEquals(1, contacts.size());        
        assertEquals(15.0 + Contact.disappearingDuration, contacts.get("vehicle-1").getExpirationTime());
        assertEquals(ContactState.PROJECTED_DISAPPEARING, contacts.get("vehicle-1").getState());

        simulation.setTime(16.0);
        detections.clear();
        detections.add(createDetection("vehicle-1", DetectionType.VISIBLE));
        contactManager.update(1.0);

        assertEquals(1, contacts.size());        
        assertEquals(Double.POSITIVE_INFINITY, contacts.get("vehicle-1").getExpirationTime());
        assertEquals(ContactState.VISIBLE, contacts.get("vehicle-1").getState());
    }

    public void testContactManagerListener()
    {
        List<Detection> detections = sensor.getDetections();
        detections.clear();
        detections.add(createDetection("vehicle-1",DetectionType.VISIBLE));
        detections.add(createDetection("vehicle-1",DetectionType.RADAR));
        
        contactManager.update(1.0);
        
        assertEquals(1, contactListener.createdContacts.size());
        assertEquals("vehicle-1", contactListener.createdContacts.get(0).getEntity().getName());
        assertEquals(ContactState.VISIBLE, contactListener.createdContacts.get(0).getState());
        assertEquals(0, contactListener.destroyedContacts.size());
        assertEquals(0, contactListener.stateChanges.size());
        contactListener.reset();

        simulation.setTime(1.0);
        detections.clear();
        detections.add(createDetection("vehicle-1",DetectionType.RADAR));        
        contactManager.update(1.0);
        
        assertEquals(0, contactListener.createdContacts.size());
        assertEquals(0, contactListener.destroyedContacts.size());
        assertEquals(1, contactListener.stateChanges.size());
        assertEquals("vehicle-1", contactListener.stateChanges.get(0).contact.getEntity().getName());
        assertEquals(ContactState.RADAR, contactListener.stateChanges.get(0).contact.getState());
        assertEquals(ContactState.VISIBLE, contactListener.stateChanges.get(0).oldState);
        contactListener.reset();

        simulation.setTime(2.0);
        detections.clear();
        contactManager.update(1.0);

        assertEquals(0, contactListener.createdContacts.size());
        assertEquals(0, contactListener.destroyedContacts.size());
        assertEquals(1, contactListener.stateChanges.size());
        assertEquals("vehicle-1", contactListener.stateChanges.get(0).contact.getEntity().getName());
        assertEquals(ContactState.PROJECTED, contactListener.stateChanges.get(0).contact.getState());
        assertEquals(ContactState.RADAR, contactListener.stateChanges.get(0).oldState);
        contactListener.reset();

        simulation.setTime(3.0);
        detections.clear();
        contactManager.update(1.0);

        assertEquals(0, contactListener.createdContacts.size());
        assertEquals(0, contactListener.destroyedContacts.size());
        assertEquals(0, contactListener.stateChanges.size());
        contactListener.reset();

        simulation.setTime(4.0);
        detections.clear();
        detections.add(createDetection("vehicle-1", DetectionType.RADAR));
        contactManager.update(1.0);

        assertEquals(0, contactListener.createdContacts.size());
        assertEquals(0, contactListener.destroyedContacts.size());
        assertEquals(1, contactListener.stateChanges.size());
        assertEquals("vehicle-1", contactListener.stateChanges.get(0).contact.getEntity().getName());
        assertEquals(ContactState.RADAR, contactListener.stateChanges.get(0).contact.getState());
        assertEquals(ContactState.PROJECTED, contactListener.stateChanges.get(0).oldState);
        contactListener.reset();

        simulation.setTime(5.0);
        detections.clear();
        contactManager.update(1.0);

        assertEquals(0, contactListener.createdContacts.size());
        assertEquals(0, contactListener.destroyedContacts.size());
        assertEquals(1, contactListener.stateChanges.size());
        assertEquals("vehicle-1", contactListener.stateChanges.get(0).contact.getEntity().getName());
        assertEquals(ContactState.PROJECTED, contactListener.stateChanges.get(0).contact.getState());
        assertEquals(ContactState.RADAR, contactListener.stateChanges.get(0).oldState);
        contactListener.reset();

        simulation.setTime(15.0);
        detections.clear();
        contactManager.update(10.0);

        assertEquals(0, contactListener.createdContacts.size());
        assertEquals(0, contactListener.destroyedContacts.size());
        assertEquals(1, contactListener.stateChanges.size());
        assertEquals("vehicle-1", contactListener.stateChanges.get(0).contact.getEntity().getName());
        assertEquals(ContactState.PROJECTED_DISAPPEARING, contactListener.stateChanges.get(0).contact.getState());
        assertEquals(ContactState.PROJECTED, contactListener.stateChanges.get(0).oldState);
        contactListener.reset();

        simulation.setTime(20.0);
        detections.clear();
        contactManager.update(5.0);

        assertEquals(0, contactListener.createdContacts.size());
        assertEquals(1, contactListener.destroyedContacts.size());
        assertEquals("vehicle-1", contactListener.destroyedContacts.get(0).getEntity().getName());
        assertEquals(0, contactListener.stateChanges.size());
        contactListener.reset();
    }

}
