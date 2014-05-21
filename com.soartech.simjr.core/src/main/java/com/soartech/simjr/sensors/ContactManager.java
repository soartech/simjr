package com.soartech.simjr.sensors;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityTools;

public class ContactManager
{
    private List<ContactManagerListener> listeners = new CopyOnWriteArrayList<ContactManagerListener>();
    private Entity entity;
    
    private HashMap<String,Contact> contactMap = new HashMap<String,Contact>();
    
    public ContactManager(Entity entity) 
    {
        this.entity = entity;
    }
    
    public void addContactManagerListener(ContactManagerListener listener) 
    {
        listeners.add(listener);
    }
    
    public void removeContactManagerListener(ContactManagerListener listener)
    {
        listeners.remove(listener);
    }
    
    public Contact getContact(String name)
    {
        return contactMap.get(name);
    }
    
    public Map<String,Contact> getContacts() 
    {
        return Collections.unmodifiableMap(contactMap);
    }
    
    public void update(double dt) 
    {
        SensorPlatform sensorPlatform = EntityTools.getSensorPlatform(entity);
        if ( sensorPlatform == null ) 
        {
            return;
        }
        
        HashMap<String,Contact> undetectedContacts = new HashMap<String,Contact>(contactMap);
        for (Sensor sensor : sensorPlatform.getSensors() ) 
        {
            if ( sensor instanceof RadarSensor || sensor instanceof VisionSensor ) 
            {
                processDetections( sensor.getDetections(), undetectedContacts );
            }
        }
        
        // TODO: Try to pull out simulation references if possible
        double currentTime = entity.getSimulation().getTime();
        for (Contact contact : undetectedContacts.values() ) 
        {
            contact.updatePosition( dt );
            boolean hasExpired = contact.getExpirationTime() <= currentTime;
            
            if ( contact.getState() == ContactState.PROJECTED_DISAPPEARING ) 
            {
                if ( hasExpired )
                {
                    removeContact(contact);
                }
            }
            else if ( contact.getState() == ContactState.PROJECTED )
            {
                if ( hasExpired )
                {
                    ContactState oldState = contact.getState();
                    contact.updateState( ContactState.PROJECTED_DISAPPEARING, entity.getSimulation().getTime() );
                    fireContactStateChange(contact, oldState);
                }
            }
            else
            {
                ContactState oldState = contact.getState();
                contact.updateState( ContactState.PROJECTED, entity.getSimulation().getTime() );
                fireContactStateChange(contact, oldState);
            }
        }
    }
    
    private void removeContact(Contact contact)
    {
        Contact removedContact = this.contactMap.remove(contact.getEntity().getName());
        for ( ContactManagerListener listener : listeners ) 
        {
            listener.destroyedContact(removedContact);
        }
    }

    private void processDetections(List<Detection> detections, HashMap<String,Contact> undetectedContacts)
    {
        for (Detection detection : detections) 
        {
            String targetName = detection.getTargetEntity().getName();
            undetectedContacts.remove(targetName);
            Contact contact = contactMap.get(targetName);
            if ( contact == null )
            {
                createContactWithDetection(detection);
            }
            else
            {
                ContactState oldState = contact.getState();
                updateContactWithDetection(contact, detection);
                fireContactStateChange(contact, oldState); 
            }
        }
    }

    private void fireContactStateChange(Contact contact, ContactState oldState)
    {
        // If state hasn't changed then this shouldn't be fired
        if ( contact.getState() == oldState ) 
        {
            return;
        }
        
        for ( ContactManagerListener listener : listeners )
        {
            listener.contactStateChanged(contact, oldState);
        }
    }

    private void updateContactWithDetection(Contact contact, Detection detection)
    {
        ContactState state = ContactState.UNKNOWN;
        if ( detection.getType() == DetectionType.VISIBLE ) 
        {
            state = ContactState.VISIBLE;
        } 
        else if ( detection.getType() == DetectionType.RADAR )
        {
            state = ContactState.RADAR;
        }
        
        contact.updateState(state, entity.getSimulation().getTime());
    }

    private void createContactWithDetection(Detection detection)
    {
        Contact contact = new Contact(detection.getTargetEntity());
        this.contactMap.put(contact.getEntity().getName(),contact);
        updateContactWithDetection(contact, detection);
        
        for ( ContactManagerListener listener : listeners )
        {
            listener.createdContact(contact);
        }
    }
    
    public void addContact(Contact contact) 
    {
        this.contactMap.put(contact.getEntity().getName(), contact);
        contact.updateState(ContactState.PROJECTED, entity.getSimulation().getTime());
    }

}
