/*
 * Copyright (c) 2010, Soar Technology, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * * Neither the name of Soar Technology, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without the specific prior written permission of Soar Technology, Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on May 25, 2007
 */
package com.soartech.simjr.sim;

import java.awt.Color;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.lang.reflect.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.math.Angles;
import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geodetic.Point;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.sensors.Sensor;
import com.soartech.simjr.sensors.SensorPlatform;
import com.soartech.simjr.sim.entities.DamageStatus;
import com.soartech.simjr.sim.entities.EntityVisibleRange;
import com.soartech.simjr.sim.entities.FuelModel;
import com.soartech.simjr.weapons.WeaponPlatform;

/**
 * Various tools and utility fucntions for dealing with entities
 * 
 * @author ray
 */
public class EntityTools
{
    private static final Logger logger = LoggerFactory.getLogger(EntityTools.class);
    
    public static final Comparator<Entity> NAME_COMPARATOR = new NameComparator();
    
    /**
     * Generate an unused name for an entity using the given prefix as a starting point.
     * 
     * @param sim the simulation
     * @param prefix the prefix
     * @return a unused name with the given prefix
     */
    public static String getUnusedName(Simulation sim, String prefix)
    {
        synchronized(sim.getLock())
        {
            int suffix = 0;
            while(sim.getEntity(prefix + suffix) != null)
            {
                suffix++;
            }
            return prefix + suffix;
        }
    }
    
    /**
     * Retrieve a property from the given property set, returning a default
     * value if it's not there. This method also takes {@link LazyEntityPropertyValue}
     * into account.
     * 
     * @param props The property set
     * @param name The name of the property
     * @param def The default value to return if the property is not present
     * @return The property value or the default value if now present
     */
    public static Object getProperty(Map<String, Object> props, String name, Object def)
    {
        final Object v = props.get(name);
        if(v instanceof LazyEntityPropertyValue)
        {
            return ((LazyEntityPropertyValue) v).getValue();
        }
        return v != null ? v : def;
    }
    
    /**
     * Set a property on all entities of a certain type.
     * 
     * @param sim the simulation
     * @param prototype the name of the type
     * @param property the property to set
     * @param value the new value
     */
    public static void setPropertyOnType(Simulation sim, String prototype, String property, Object value)
    {
        synchronized(sim.getLock())
        {
            for(Entity e : sim.getEntitiesFast())
            {
                if(e.getPrototype().hasSubcategory(prototype))
                {
                    e.setProperty(property, value);
                }
            }
        }
    }
    
    /**
     * Find an entity with a specific property and value of the property.
     * 
     * @param sim the simulation
     * @param property the property to find
     * @param value the value to find
     * @return First entity with that property and value, or null
     */
    public static Entity findFirstWithProperty(Simulation sim, String property, Object value)
    {
        synchronized(sim.getLock())
        {
            for(Entity e : sim.getEntitiesFast())
            {
                Object theValue = e.getProperty(property);
                if (theValue != null && theValue.equals(value))
                {
                    return e;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the speed of an entity in m/s
     * 
     * @param e The entity
     * @return The speed of the entity in m/s
     */
    public static double getSpeed(Entity e)
    {
        return e.getVelocity().length();
    }
    
    /**
     * Returns the ground (x/y) speed of an entity in m/s
     * @param e The entity
     * @return The ground speed of the entity in m/s
     */
    public static double getGroundSpeed(Entity e)
    {
        Vector3 p = e.getVelocity();
        return new Vector3(p.x, p.y, 0.0).length();
    }
    
    public static double getAltitude(Entity e)
    {
        Vector3 p = e.getPosition();
        Terrain terrain = e.getSimulation().getTerrain();
        
        return terrain.toGeodetic(p).altitude;
    }
    
    public static void setAltitude(Entity e, double newAltitude)
    {
        Simulation sim = e.getSimulation();
        if(sim == null)
        {
            return;
        }
        
        synchronized(sim.getLock())
        {
            Terrain terrain = sim.getTerrain();
            Vector3 p = e.getPosition();
            
            Point seaLevelLla = terrain.toGeodetic(p);
            seaLevelLla.altitude = newAltitude;
            
            e.setPosition(terrain.fromGeodetic(seaLevelLla));
        }
    }
    
    public static double getAboveGroundLevel(Entity e)
    {
        Vector3 p = e.getPosition();
        Terrain terrain = e.getSimulation().getTerrain();
        
        return getAltitude(e) - terrain.getElevationAtPoint(p);
    }
    
    public static void setAboveGroundLevel(Entity e, double newAgl)
    {
        Simulation sim = e.getSimulation();
        if(sim == null)
        {
            return;
        }
        
        synchronized(sim.getLock())
        {
            e.setPosition(sim.getTerrain().clampPointToGround(e.getPosition(), newAgl));
        }
    }

    public static Double getEnforcedAboveGroundLevel(Entity entity)
    {
        Simulation sim = entity.getSimulation();
        Object forceAglObject = entity.getProperty(EntityConstants.PROPERTY_ENFORCE_AGL);
        if(forceAglObject == null || sim == null)
        {
            return null;
        }

        double forceAgl = 0.0;
        if(forceAglObject instanceof Number)
        {
            Number forceAglNum = (Number) forceAglObject;
            forceAgl = forceAglNum.doubleValue();
        }
        else
        {
            try
            {
                forceAgl = Double.parseDouble(forceAglObject.toString());
            }
            catch(NumberFormatException e)
            {
                logger.error("Invalid value for '" + EntityConstants.PROPERTY_ENFORCE_AGL + "' property: " + forceAglObject);
            }
        }
        
        return forceAgl;
    }
    
    /**
     * Calculate the bearing of an entity in radians
     * 
     * @param e Entity
     * @return Bearing of the entity in radians.
     */
    public static double getBearing(Entity e)
    {
        return Angles.getBearing(e.getVelocity());
    }
    
    /**
     * Calculate the heading of an entity in radians
     * 
     * @param e The entity
     * @return Heading of the entity in radians
     */
    public static double getHeading(Entity e)
    {
        return Angles.mathRadiansToNavRadians(e.getHeading());
    }
    
    public static String getForce(Entity e)
    {
        Object force = e.getProperty(EntityConstants.PROPERTY_FORCE);
        return force != null ? force.toString() : EntityConstants.FORCE_OTHER;
    }
    
    public static DamageStatus getDamage(Entity e)
    {
        final DamageStatus damage = (DamageStatus) e.getProperty(EntityConstants.PROPERTY_DAMAGE);
        return damage != null ? damage : DamageStatus.intact;
    }
    
    public static DamageStatus getDamage(Map<String, Object> props)
    {
        return (DamageStatus) getProperty(props, EntityConstants.PROPERTY_DAMAGE, DamageStatus.intact);
    }
    
    /**
     * Set the visible property of the given entity. This is a convenience method
     * for scripting.
     * 
     * @param e The entity
     * @param visible The visibility value
     */
    public static void setVisible(Entity e, boolean visible)
    {
        e.setProperty(EntityConstants.PROPERTY_VISIBLE, visible);
    }
    
    /**
     * Set the "label visible" property of the given entity. This is a convenience method for scripting.
     * 
     * @param e The entity
     * @param labelVisible True if the label should be visible, false if not
     */
    public static void setLabelVisible(Entity e, boolean labelVisible)
    {
        e.setProperty(EntityConstants.PROPERTY_SHAPE_LABEL_VISIBLE, labelVisible);
    }
    
    /**
     * Returns true if the given entity is visible. This is a convenience 
     * method for scripting.
     * 
     * @param e The entity
     * @return True if the entity is visible.
     */
    public static boolean isVisible(Entity e)
    {
        return (Boolean) getProperty(e.getProperties(), EntityConstants.PROPERTY_VISIBLE, true);
    }
    
    /**
     * Remove all non-visible entities from the given list. The list is 
     * modified.
     * 
     * @param entities List of entities to filter
     */
    public static void removeInvisibleEntities(List<Entity> entities)
    {
        Iterator<Entity> it = entities.iterator();
        while(it.hasNext())
        {
            Entity e = it.next();
            if(!EntityTools.isVisible(e))
            {
                it.remove();
            }
        }        
    }
    
    /**
     * Retrieve an entity's weapon platform. This is mostly for use by scripts
     * so DO NOT REMOVE IT even if it appears to not be used by any Java code.
     * 
     * @param entity The entity
     * @return The weapon platform or null if there is none.
     */
    public static WeaponPlatform getWeaponPlatform(Entity entity)
    {
        return Adaptables.adapt(entity, WeaponPlatform.class);
    }
    
    /**
     * Retrieve an entity's sensor platform. This is mostly for use by scripts
     * so DO NOT REMOVE IT even if it appears to not be used by any Java code.
     * 
     * @param entity The entity
     * @return The sensor platform or null if there is none.
     */
    public static SensorPlatform getSensorPlatform(Entity entity)
    {
        return Adaptables.adapt(entity, SensorPlatform.class);
    }

    /**
     * Retrieve an entity's fuel model. This is mostly for use by scripts
     * so DO NOT REMOVE IT even if it appears to not be used by any Java code.
     * 
     * @param entity The entity
     * @return The fuel model or null if there is none.
     */
    public static FuelModel getFuelModel(Entity entity)
    {
        return Adaptables.adapt(entity, FuelModel.class);
    }

    public static EntityVisibleRange getVisibleRange(Entity entity) {
        return (EntityVisibleRange) entity.getProperty(EntityConstants.PROPERTY_VISIBLE_RANGE);
    }
    
    /**
     * Comparator for sorting entities by name
     */
    private static class NameComparator implements Comparator<Entity>
    {
        public int compare(Entity o1, Entity o2)
        {
            return o1.getName().compareTo(o2.getName());
        }
    }
    
    /**
     * Returns the line color or default line color of an entity as a Color.
     * 
     * @param e The entity, def The default color
     * @return The Color of the entity.
     */
    public static Color getLineColor(Entity e, Color def)
    {
        String color = (String)e.getPrototype().getProperty(EntityConstants.PROPERTY_SHAPE_LINE_COLOR);

        Color entityColor = getColorByName(color);
        if(entityColor != null) { 
            return entityColor; 
        }
        
        entityColor = getColorByCode(color);
        if(entityColor != null) { 
            return entityColor; 
        }
        
        return def;
    }
    
    /**
     * Returns the fill color or default fill  color of an entity as a Color.
     * 
     * @param e The entity, def The default color
     * @return The Color of the entity.
     */
    public static Color getFillColor(Entity e, Color def)
    {
        String color = (String)e.getPrototype().getProperty(EntityConstants.PROPERTY_SHAPE_FILL_COLOR);
        Color entityColor;
        try{
            Field field = Color.class.getField(color);
            entityColor = (Color)field.get(null);
        }catch (Exception E){
            entityColor = def;
        }
        return entityColor;
    }
    
    /**
     * Attempt to parse the given color string as a java.awt.Color
     * @param colorName, e.g. "red"
     * @return the Color object associated with the name, or null if not found
     */
    private static Color getColorByName(String colorName) 
    {
        Color c = null;
        try {
            Field field = Color.class.getField(colorName);
            c = (Color)field.get(null);
        } catch (Exception E){
            //Color not found
        }
        return c;
    }
    
    /**
     * Attempt to parse the given color string as a java.awt.Color
     * @param colorCode, e.g. #00FF00
     * @return the Color object associated with the code, or null if not found
     */
    private static Color getColorByCode(String colorCode)
    {
        Color c = null;
        try {
            c = new Color(Integer.parseInt(colorCode, 16));
        } catch (Exception E) {
            // Color not found
        }
        return c;
    }

    public static <T extends Sensor> T getSensorOfType(Entity entity, Class<T> sensorClass)
    {
        SensorPlatform platform = EntityTools.getSensorPlatform(entity);
        if ( platform != null )
        {
            for (Sensor s : platform.getSensors() ) 
            {
                if ( sensorClass.isInstance(s) ) 
                {
                    return sensorClass.cast(s);
                }
            }
        }
        return null;
    }
    
    // TODO: Roll
    // TODO: Pitch
    // TODO: Yaw
    // TODO: Heading
    // TODO: Bearing
}
