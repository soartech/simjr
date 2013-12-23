package com.soartech.simjr.sensors;

import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.entities.Person;
import com.soartech.simjr.sim.entities.Vehicle;

public class EntityFilter
{
    private Entity source;
    
    public EntityFilter(Entity source)
    {
        this.source = source;
    }
    
    public boolean isEntityOfInterest(Entity entity)
    {
        // Don't report self as contact
        if(entity == source)
        {
            return false;
        }
        
        // This refers to visibility through the ui, its
        // common to add entities to the scenario and set their visibility
        // to false until they are needed.
        if ( !EntityTools.isVisible(entity) )
        {
            return false;
        }
        
        return entity instanceof Person || entity instanceof Vehicle;
    }
}
