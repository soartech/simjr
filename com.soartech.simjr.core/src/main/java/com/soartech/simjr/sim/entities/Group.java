package com.soartech.simjr.sim.entities;

import java.util.LinkedHashMap;
import java.util.Map;

import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPrototype;
import com.soartech.simjr.sim.entities.AbstractEntity;

public class Group extends Container
{
    public Group(String name, EntityPrototype prototype)
    {
        super(name, prototype);
    }

    /**
     * A container for entities to transform them into one unit
     * @author Jordan Lampi
     *
     */
    public enum Type
    {
        fireteam,
        squad,
        platoon,
        company,
        batallion,
        regiment,
        division;
        
        private static Map<Type, Integer> groupMaxSize = new LinkedHashMap<Type, Integer>();
        static
        {//the maximum number of units to still be classified as the type
            groupMaxSize.put(fireteam, 4);
            groupMaxSize.put(squad, 13);
            groupMaxSize.put(platoon, 64);
            groupMaxSize.put(company, 225);
            groupMaxSize.put(batallion, 1300);
            groupMaxSize.put(regiment, 5000);
            groupMaxSize.put(division, 15000);
        }
        
        public static Type getTypeByCount(int numUnits)
        {
            Type groupType = Type.fireteam;
            for(Type t : groupMaxSize.keySet())
            {
                groupType = t;
                if(groupMaxSize.get(t) >= numUnits)
                {
                    break;
                }
            }
            return groupType;
        }
    }
    
    public Type getType()
    {
        return Type.getTypeByCount(containedEntities.size());
    }
    
    @Override 
    public void add(AbstractEntity entity)
    {
        super.add(entity);
        this.setProperty(EntityConstants.PROPERTY_DIAMETER, containedEntities.size());
    }
    
}