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
 * Created on Mar 25, 2009
 */
package com.soartech.simjr.sim;

import java.util.Map;

import com.soartech.simjr.SimulationException;


/**
 * @author ray
 */
public interface EntityPrototype
{
    /**
     * Returns the parent of this prototype. When a property is requested, 
     * the request is delegated to the parent if the property is not set
     * in this prototype.
     * 
     * @return the parent of this prototype
     */
    EntityPrototype getParent();
    
    /**
     * Returns this prototype's unique identifier.
     * 
     * @return unique identifier
     */
    String getId();
    /**
     * @return the domain of the entity
     */
    String getDomain();
    /**
     * @return the category of the entity
     */
    String getCategory();
    
    /**
     * Returns the most specific type of this entity. If this is not provided, it defaults
     * to the value of {@link #getId()}
     * 
     * @return the sub-category of the entity, i.e. the specific type of entity, e.g. f-16
     */
    String getSubcategory();
    
    /**
     * Returns true if this prototype, or any of its ancestors has the given
     * subcategory. This should be used rather than instanceof tests in code.
     * 
     * @param subcategory the subcategory to test
     * @return true if this prototype or any of its ancestors has the given
     *  subcategory.
     */
    boolean hasSubcategory(String subcategory);
    
    /**
     * @return a factory for creating entities from this prototype
     */
    Object getFactory();
    
    /**
     * Return true if this prototype is abstract. Abstract prototypes shouldn't
     *      be instantiated and won't be visible in the scenario editor.
     *      
     * @return true if this prototype is abstract.
     */
    boolean isAbstract();
    
    /**
     * @return a copy of this (and its ancestor's) prototype's properties
     */
    Map<String, Object> getProperties();
    /**
     * Add the properties in this prototype (and its ancestors) to the target
     * map.
     * 
     * @param target map to put properties in
     */
    void addProperties(Map<String, Object> target);

    /**
     * Retrieve a property by name. If not set in this prototype, then
     * parents are checked.
     * 
     * @param name the name of the property
     * @return the value of the property or null if not set.
     */
    Object getProperty(String name);
    
    /**
     * Get a custom property.
     * 
     * @param name the name of the property, e.g. "maxSpeed"
     * @param defVal default value to return if the property is not present
     * @return the property value, or the default value
     */
    String getProperty(String name, String defVal);
    /**
     * Get a custom double property on this entity type.
     *  
     * @param name the name of the property, e.g. "maxSpeed"
     * @param defVal default value to return if the property is not present or
     *     not a double.
     * @return the property value, or the default value
     */
    Double getProperty(String name, Double defVal);
    
    /**
     * Get a custom int property on this entity type.
     *  
     * @param name the name of the property, e.g. "maxSpeed"
     * @param defVal default value to return if the property is not present or 
     *      not an integer
     * @return the property value, or the default value
     */
    Integer getProperty(String name, Integer defVal);
        
    /**
     * Construct a new entity from this prototype using its current factory
     * 
     * @param name the name of the new entity
     * @return a new entity
     * @throws SimulationException
     */
    Entity createEntity(String name) throws SimulationException;
}
