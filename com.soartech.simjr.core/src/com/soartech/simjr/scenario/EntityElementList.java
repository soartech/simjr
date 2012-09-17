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
 * Created on Mar 28, 2009
 */
package com.soartech.simjr.scenario;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import com.soartech.simjr.scenario.edits.DeleteEntityEdit;
import com.soartech.simjr.scenario.edits.NewEntityEdit;
import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.scenario.model.ModelChangeEvent;

/**
 * @author ray
 */
public class EntityElementList
{
    public static final String ENTITY_ADDED = EntityElementList.class.getCanonicalName() + "entityAdded";
    public static final String ENTITY_REMOVED = EntityElementList.class.getCanonicalName() + "entityRemoved";
    
    private final Model model;
    private final XPath entitySetXPath;
    private final List<EntityElement> entities = new ArrayList<EntityElement>();
    
    private int nameIndex = 1;
    
    public static Element buildDefault(Model model)
    {
        return model.newElement("entities");
    }
    
    public static EntityElementList attach(Model model)
    {
        return new EntityElementList(model);
    }
    
    private EntityElementList(Model model)
    {
        this.model = model;
        this.entitySetXPath = this.model.newXPath("/simjr:scenario/simjr:entities/simjr:entity");
     
        try
        {
            List<?> nodes = this.entitySetXPath.selectNodes(this.model.getDocument());
            for(Object o : nodes)
            {
                final Element e = (Element) o;
                entities.add(EntityElement.attach(model, e));
            }
        }
        catch (JDOMException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public List<EntityElement> getEntities()
    {
        return entities;
    }
    
    public EntityElement getEntity(String name)
    {
        for(EntityElement e : entities)
        {
            if(name.equals(e.getName()))
            {
                return e;
            }
        }
        return null;
    }
    
    public NewEntityEdit addEntity(String baseName, String prototype)
    {
        return addEntity(baseName, prototype, false);
    }
    
    /**
     * Add a new entity with the give (base) name and prototype.
     * 
     * @param baseName the base name, from which a new name will be generated
     * @param prototype the prototype
     * @param forceName if {@code true}, {@code baseName} will be used directly rather
     *      than generating a new unique name
     * @return the edit for the entity creation
     */
    public NewEntityEdit addEntity(String baseName, String prototype, boolean forceName)
    {
        final String finalName = forceName ? baseName : generateName(baseName);
        
        final Element element = EntityElement.build(model, finalName, prototype);
        
        final EntityElement entity = EntityElement.attach(model, element);
        getEntitiesElement().addContent(element);
        entities.add(entity);
        
        model.fireChange(new ModelChangeEvent(model, entity, ENTITY_ADDED));
        return new NewEntityEdit(entity);
    }
    
    public void addEntity(EntityElement entity, int before)
    {
        getEntitiesElement().addContent(before, entity.getElement());
        entities.add(before, entity);
        model.fireChange(new ModelChangeEvent(model, entity, ENTITY_ADDED));
    }
    
    public DeleteEntityEdit removeEntity(EntityElement entity)
    {
        final int index = entities.indexOf(entity);
        getEntitiesElement().removeContent(entity.getElement());
        entities.remove(entity);

        model.fireChange(new ModelChangeEvent(model, entity, ENTITY_REMOVED));
        return new DeleteEntityEdit(entity, index);
    }
    
    public NewEntityEdit cloneEntity(EntityElement entityToCopy)
    {
        final Element element = (Element) entityToCopy.getElement().clone();
        final String newName = generateName(entityToCopy.getName());
        EntityElement.setName(model, element, newName);
        
        final EntityElement entity = EntityElement.attach(model, element);
        getEntitiesElement().addContent(element);
        entities.add(entity);
        
        model.fireChange(new ModelChangeEvent(model, entity, ENTITY_ADDED));
        return new NewEntityEdit(entity);
    }
    
    private Element getEntitiesElement()
    {
        final XPath xpath = model.newXPath("/simjr:scenario/simjr:entities");
        try
        {
            return (Element) xpath.selectSingleNode(model.getDocument());
        }
        catch (JDOMException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private String generateName(String baseName)
    {
        String finalName = baseName;
        while(getEntity(finalName) != null)
        {
            finalName = baseName + (nameIndex++);
        }
        return finalName;
    }
}
