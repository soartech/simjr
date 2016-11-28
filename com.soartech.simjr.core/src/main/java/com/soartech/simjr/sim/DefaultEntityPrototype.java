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
 * Created on Mar 26, 2009
 */
package com.soartech.simjr.sim;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.soartech.simjr.SimulationException;

/**
 * Default implementation of {@link EntityPrototype}. Note that all setters
 * are "write once" since prototypes should be immutable once they're created.
 * 
 * @see EntityPrototypes
 * @author ray
 */
public class DefaultEntityPrototype implements EntityPrototype
{
    private EntityPrototype parent;
    private String id;
    private String domain;
    private String category;
    private String subcategory;
    private Object factory;
    private boolean abstrakt;
    private Map<String, Object> properties = new LinkedHashMap<String, Object>();
    
    /**
     * Builder for constructing new prototypes
     * 
     * @author ray
     */
    public static class Builder
    {
        private String id, domain, category, subcategory;
        private EntityPrototype parent;
        private Map<String, Object> properties = new LinkedHashMap<String, Object>();
        private Object factory;
        
        public DefaultEntityPrototype build()
        {
            DefaultEntityPrototype result = new DefaultEntityPrototype();
            result.setId(id);
            result.setParent(parent);
            result.setDomain(domain);
            result.setCategory(category);
            result.setSubcategory(subcategory);
            result.setProperties(properties);
            result.setFactory(factory);
            return result;
        }
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder domain(String domain) { this.domain = domain; ; return this; }
        public Builder category(String cat) { this.category = cat; return this; }
        public Builder subcategory(String sc) { this.subcategory = sc; return this; }
        public Builder parent(EntityPrototype parent) { this.parent = parent; return this; }
        public Builder property(String name, Object value)
        {
            properties.put(name, value);
            return this;
        }
        public Builder factory(Object f) { this.factory = f; return this; }
    };
    
    /**
     * @return a new builder
     */
    public static Builder newBuilder() { return new Builder(); }
    
    /**
     * Default constructor, for use with YAML. Java and JavaScript code should use
     * {@link Builder}.
     */
    public DefaultEntityPrototype() {}
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.EntityPrototype#getParent()
     */
    public EntityPrototype getParent()
    {
        return parent;
    }
    
    /**
     * Set the parent of this prototype.
     * 
     * @param parent the new parent
     * @throws IllegalStateException if parent has already been set
     */
    public void setParent(EntityPrototype parent)
    {
        if(this.parent != null)
        {
            throw new IllegalStateException("parent is already set");
        }
        this.parent = parent;
    }
    
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.EntityPrototype#getId()
     */
    public String getId()
    {
        return id;
    }

    /**
     * Set the id of this prototype
     * 
     * @param id the new id
     * @throws IllegalStateException if id has already been set
     */
    public void setId(String id)
    {
        if(this.id != null)
        {
            throw new IllegalStateException("id is already set");
        }
        this.id = id;
    }

    public String getDomain()
    {
        return domain != null ? domain : (parent != null ? parent.getDomain() : null);
    }
    public void setDomain(String domain)
    {
        if(this.domain != null)
        {
            throw new IllegalStateException("domain is already set");
        }
        this.domain = domain;
    }
    public String getCategory()
    {
        return category != null ? category : (parent != null ? parent.getCategory() : null);
    }
    public void setCategory(String category)
    {
        if(this.category != null)
        {
            throw new IllegalStateException("category is already set");
        }
        this.category = category;
    }
    
    public String getSubcategory()
    {
        return subcategory != null ? subcategory : getId();
    }
    
    public void setSubcategory(String subcategory)
    {
        if(this.subcategory != null)
        {
            throw new IllegalStateException("subcategory is already set");
        }
        this.subcategory = subcategory;
    }
    
    public boolean hasSubcategory(String subcategory)
    {
        if(subcategory.equals(getSubcategory()))
        {
            return true;
        }
        return parent != null ? parent.hasSubcategory(subcategory) : false;
    }
    
    public Object getFactory()
    {
        return factory != null ? factory : (parent != null ? parent.getFactory() : null);
    }

    public void setFactory(Object factory)
    {
        this.factory = factory;
    }

    public boolean isAbstract()
    {
        return abstrakt;
    }

    public void setAbstract(boolean abstrakt)
    {
        this.abstrakt = abstrakt;
    }

    public Map<String, Object> getProperties()
    {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        addProperties(result);
        return result;
    }
    
    Map<String, Object> getBaseProperties()
    {
        return properties;
    }
    
    public void addProperties(Map<String, Object> target)
    {
        if(parent != null)
        {
            parent.addProperties(target);
        }
        target.putAll(properties);
    }
    
    public void setProperties(Map<String, Object> properties)
    {
        this.properties = properties;
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.EntityPrototype#getProperty(java.lang.String)
     */
    public Object getProperty(String name)
    {
        Object value = properties.get(name);
        if(value != null)
        {
            return value;
        }
        return parent != null ? parent.getProperty(name) : null;
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.EntityPrototype#getProperty(java.lang.String, java.lang.String)
     */
    public String getProperty(String name, String defVal)
    {
        final Object value = getProperty(name);
        return value != null ? value.toString() : defVal;
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.EntityPrototype#getProperty(java.lang.String, java.lang.Double)
     */
    public Double getProperty(String name, Double defVal)
    {
        String v = getProperty(name, defVal != null ? defVal.toString() : null);
        try
        {
            return v != null ? Double.valueOf(v) : defVal;
        }
        catch(NumberFormatException e)
        {
            return defVal;
        }
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.EntityPrototype#getProperty(java.lang.String, java.lang.Integer)
     */
    public Integer getProperty(String name, Integer defVal)
    {
        String v = getProperty(name, defVal != null ? defVal.toString() : null);
        try
        {
            return v != null ? Integer.valueOf(v) : defVal;
        }
        catch(NumberFormatException e)
        {
            return defVal;
        }
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.EntityPrototype#createEntity(java.lang.String)
     */
    public Entity createEntity(String name) throws SimulationException
    {
        final Object factory = getFactory();
        if(factory == null)
        {
            throw new SimulationException("No factory for entity prototype '" + this + "'");
        }
        if(!(factory instanceof Class<?>))
        {
            throw new SimulationException("Expected Class<?> factory, got '" + factory + "'");
        }
        
        Class<?> klass = (Class<?>) factory;
        try
        {
            Constructor<?> c = klass.getConstructor(String.class, EntityPrototype.class);
            Entity entity = (Entity) c.newInstance(name, this);
            configureNewEntity(entity);
            return entity;
        }
        catch (SecurityException e)
        {
            throw new SimulationException("Failed to instantiate entity from prototype '" + this + "'", e);
        }
        catch (NoSuchMethodException e)
        {
            throw new SimulationException("Failed to instantiate entity from prototype '" + this + "'", e);
        }
        catch (IllegalArgumentException e)
        {
            throw new SimulationException("Failed to instantiate entity from prototype '" + this + "'", e);
        }
        catch (InstantiationException e)
        {
            throw new SimulationException("Failed to instantiate entity from prototype '" + this + "'", e);
        }
        catch (IllegalAccessException e)
        {
            throw new SimulationException("Failed to instantiate entity from prototype '" + this + "'", e);
        }
        catch (InvocationTargetException e)
        {
            throw new SimulationException("Failed to instantiate entity from prototype '" + this + "'", e);
        }
    }
    
    private void configureNewEntity(Entity e) throws SimulationException
    {
        @SuppressWarnings("unchecked")
        final List<Object> capabilities = (List<Object>) getProperty("capabilities");
        if(capabilities == null)
        {
            return;
        }
        
        for(Object capFactory : capabilities)
        {
            if(capFactory instanceof Class)
            {
                final Class<?> klass = (Class<?>) capFactory;
                try
                {
                    EntityCapability cap = (EntityCapability) klass.newInstance();
                    e.addCapability(cap);
                }
                catch (InstantiationException e1)
                {
                    throw new SimulationException("Failed to instantiate entity capability prototype '" + this + "'", e1);
                }
                catch (IllegalAccessException e1)
                {
                    throw new SimulationException("Failed to instantiate entity capability prototype '" + this + "'", e1);
                }
            }
        }
    }
    
    public String toString()
    {
        return "{" + getDomain() + "/" + getCategory() + "/" + getSubcategory() + "}";
    }

}
