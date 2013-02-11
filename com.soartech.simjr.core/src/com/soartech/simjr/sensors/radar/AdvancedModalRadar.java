package com.soartech.simjr.sensors.radar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.soartech.simjr.sensors.AbstractSensor;
import com.soartech.simjr.sensors.ContactManager;
import com.soartech.simjr.sensors.Detection;
import com.soartech.simjr.sensors.DetectionType;
import com.soartech.simjr.sensors.EntityFilter;
import com.soartech.simjr.sensors.NotchFilter;
import com.soartech.simjr.sensors.RadarSensor;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.util.ExtendedProperties;

public class AdvancedModalRadar extends AbstractSensor implements RadarSensor
{
    private static final Logger logger = Logger.getLogger(AdvancedModalRadar.class);
    
    private EntityFilter filter;
    private NotchFilter notchFilter;
    private RadarController controller = new RadarController(new RangeWhileSearch());
    private ContactManager contactManager;
    private List<Detection> detections = new ArrayList<Detection>();
    private final ExtendedProperties props;
    private final String name;

    public AdvancedModalRadar(String name, ExtendedProperties props)
    {
        super(name);
        this.props = props;
        this.name = name;
    }
    
    public RadarController getRadarController()
    {
        return controller;
    }
    
    public RadarMode getRadarMode()
    {
        return this.controller.getRadarMode();
    }
    
    public ContactManager getContactManager()
    {
        return this.contactManager;
    }
    
    public void setRadarMode(RadarMode newMode) 
    {
        this.controller.setRadarMode(newMode);
    }
    
    @Override
    public void setEntity(Entity entity)
    {
        super.setEntity(entity);
        filter = new EntityFilter(entity);
        notchFilter = new NotchFilter(entity);
        contactManager = new ContactManager(entity);
    }

    @Override
    public void tick(double dt)
    {
        detections.clear();
        if ( controller.getRadarMode() == null )
        {
            logger.warn("No radar mode set for AdvancedModalRadar for "+getEntity().getName());
            return;
        }
        
        int numDetectedTargets = 0;
        List<Entity> targets = this.getEntity().getSimulation().getEntitiesFast();
        for ( Entity target : targets ) 
        {
            if ( filter.isEntityOfInterest(target) ) 
            {
                if ( controller.getRadarMode().onlyShowTargets() ) 
                {
                    if ( controller.getRadarMode().isTarget(target) && canDetect(target) ) 
                    {
                        numDetectedTargets++;
                        detections.add(new Detection(this, target, DetectionType.RADAR)); 
                    }
                }
                else
                {
                    if ( canDetect(target) )
                    {
                        detections.add(new Detection(this, target, DetectionType.RADAR));
                    }
                }
            }
        }
        
        // Some radar modes switch back to RWS mode if there are no detected targets
        if ( controller.getRadarMode().switchToRWSWhenNoTargets() && numDetectedTargets == 0 )
        {
            controller.setRadarMode(new RangeWhileSearch());
        }
    }
        
    private boolean canDetect(Entity target)
    {
        if (props.getBoolean(name + ".alwaysDetect", false))
        {
            return true;
        }
        
        boolean inRadar = controller.isInRange(getEntity(), target.getPosition());
        if ( !inRadar ) return false;
        
        if (props.getBoolean(name + ".ignoreNotch", false))
        {
            return true;
        }
        
        boolean inNotch = notchFilter.inNotch(target.getPosition(), target.getVelocity());
        return !inNotch;
    }

    @Override
    public List<Detection> getDetections()
    {
        return Collections.unmodifiableList(detections);
    }

}
