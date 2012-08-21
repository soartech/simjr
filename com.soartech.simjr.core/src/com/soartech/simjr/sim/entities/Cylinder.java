package com.soartech.simjr.sim.entities;

import com.soartech.simjr.sim.EntityPrototype;

public class Cylinder extends DefaultEntity
{
    
    public Cylinder(EntityPrototype prototype)
    {
        super("", prototype);
    }
    
    public Cylinder(String defaultName, EntityPrototype prototype)
    {
        super(defaultName, prototype);
    }
}
