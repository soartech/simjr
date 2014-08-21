package com.soartech.simjr.sim.entities;

import java.util.ArrayList;
import java.util.List;

import com.soartech.simjr.sim.EntityPrototype;

public class Container extends AbstractEntity
{
    protected List<AbstractEntity> containedEntities = new ArrayList<AbstractEntity>();
    protected EntityContainerCapability containerCapability;
    
    public Container(String name, EntityPrototype prototype)
    {
        super(name, prototype);
        containerCapability  = new EntityContainerCapability();
        containerCapability.attach(this);
    }
    
    public void add(AbstractEntity entity)
    {
        containedEntities.add(entity);
        containerCapability.add(entity);
    }
    
    public void remove(AbstractEntity entity)
    {
        if(containedEntities.remove(entity))
        {
            containerCapability.remove(entity, this.getPosition());
        }
    }

}
