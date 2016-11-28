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
 * Created on Jun 6, 2007
 */
package com.soartech.simjr.weapons;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.math.Vector3;
import com.soartech.simjr.sim.Entity;

/**
 * @author ray
 */
public abstract class Weapon
{
    private static final Logger logger = LoggerFactory.getLogger(Weapon.class);
 
    private static Properties properties = new Properties();
    static
    {
        InputStream stream = Weapon.class.getResourceAsStream("/simjr.weapons.properties");
        if(stream != null)
        {
            try
            {
                properties.load(stream);
            }
            catch (IOException e)
            {
                logger.error(e.toString());
            }
            finally
            {
                try
                {
                    stream.close();
                }
                catch (IOException e)
                {
                }
            }
        }
        else
        {
            logger.error("Failed to load weapons.properties file");
        }
    }
    
    private Entity entity;
    private String name;
    private int count;
    private int maxCount;
    
    /**
     * Given the name of a weapon in the weapon "database", construct a new weapon
     * object of the appropriate type
     * 
     * @param name The weapon name, e.g. "hellfire"
     * @param count The number of rounds
     * @return New weapon object
     */
    public static Weapon load(String name, int count)
    {
        String klass = getProperty(name + ".class", null);
        if(klass == null)
        {
            throw new IllegalArgumentException("Unknown weapon '" + name + "'");
        }
        
        if("bomb".equals(klass))
        {
            return new BombWeapon(name, count, count);
        }
        else if(klass.contains("missile"))
        {
            return new MissileWeapon(name, count, count);
        }
        else if(klass.equals("cannon"))
        {
            return new CannonWeapon(name, count, count);
        }
        else if(klass.equals("rifle"))
        {
            return new RifleWeapon(name, count, count);
        }
        else if(klass.equals("laser-designator"))
        {
            return new CannonWeapon(name, count, count);
        }
        else
        {
            throw new IllegalStateException("Unknown weapon class '" + klass + "' for weapon '" + name + "'");
        }
    }
    
    /**
     * @param name
     * @param count
     * @param maxCount
     */
    public Weapon(String name, int count, int maxCount)
    {
        this.name = name;
        this.count = count;
        this.maxCount = maxCount;
    }

    /**
     * @return The owning entity
     */
    public Entity getEntity()
    {
        return entity;
    }

    /**
     * @param entity The owning entity, or null for none.
     */
    public void setEntity(Entity entity)
    {
        this.entity = entity;
    }

    /**
     * @return the count
     */
    public int getCount()
    {
        return count;
    }

    /**
     * @return the maxCount
     */
    public int getMaxCount()
    {
        return maxCount;
    }
    
    public void load(int count)
    {
        this.count += count;
        this.count = Math.min(this.count, maxCount);
        
        logger.info("Loaded " + count + " rounds into " + this);
    }
    
    protected boolean prepareToFire(int count, Object target)
    {
        int actual = Math.min(count, this.count);
        
        if(actual == 0)
        {
            logger.warn("Attempted to fire empty weapon " + this);
            return false;
        }
        
        this.count -= actual;
        logger.info("Fired " + actual + " rounds (requested " + count + ") from " + this + " at " + target);
        
        return true;
    }
    
    public abstract void fire(int count, Entity target);
    public abstract void fire(int count, Vector3 target);
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return (entity != null ? entity.getName() : "unknown") + ":" + 
                name + "(" + getClass().getSimpleName() + ", " + count + "/" + maxCount + ")";
    }
    
    
    protected static double getProperty(String name, double def)
    {
        String value = properties.getProperty(name);
        if(value == null)
        {
            logger.warn("Could not find weapon property '" + name + "' using default value " + def);
            return def;
        }
        try
        {
            return Double.valueOf(value);
        }
        catch (NumberFormatException e)
        {
            logger.error("Expected number for weapon property '" + name + "'. Got '" + value + "'. Using default value " + def);
        }
        return def;
    }
    
    protected static String getProperty(String name, String def)
    {
        String value = properties.getProperty(name, def);
        if(value == null)
        {
            logger.warn("Could not find weapon property '" + name + "' using default value " + def);
            return def;
        }
        return value;
    }
    
}
