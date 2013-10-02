/*
 * Copyright (c) 2010, Soar Technology, Inc. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * * Neither the name of Soar Technology, Inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * the specific prior written permission of Soar Technology, Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Created on Mar 29, 2009
 */
package com.soartech.simjr.sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.services.ServiceManager;

/**
 * A helper class to provide a list of prototypes filtered by user properties.
 * 
 * TODO: Consider generalizing the filter concept, allowing users of the API to provide
 *       their own filters
 */
public class EntityPrototypesFilter 
{
    private static final Logger logger = Logger.getLogger(EntityPrototypesFilter.class);
   
    /**
     * Populates the default type model based on built-in filters and those specified via properties.
     */
    public static List<EntityPrototype> getUserPrototypes(ServiceManager services)
    {
        final EntityPrototypeDatabase db = EntityPrototypeDatabase.findService(services);
        final List<EntityPrototype> prototypes = db.getPrototypes();
        
        applyPrototypeFilterDefault(prototypes);
        applyPrototypeFilterEnabled(prototypes);
        applyPrototypeFilterDisabled(prototypes);
        
        Collections.sort(prototypes, new Comparator<EntityPrototype>() {
            public int compare(EntityPrototype o1, EntityPrototype o2)
            {
                return o1.toString().compareTo(o2.toString());
            }
        });
        
        return prototypes;
    }
    
    /**
     * Filters abstract and flyout prototypes from the EntityPrototype list
     * @param prototypes A list of prototypes to filter, will be modified
     */
    private static void applyPrototypeFilterDefault(List<EntityPrototype> prototypes) 
    {
        // Filter out abstract prototypes and flyouts
        final Iterator<EntityPrototype> it = prototypes.iterator();
        while (it.hasNext())
        {
            final EntityPrototype p = it.next();
            if (p.isAbstract() || p.hasSubcategory("flyout"))
            {
                it.remove();
            }
        }
    }
    
    /**
     * Filters out prototypes that do not match anything in the enabled list
     * @param prototypes A list of prototypes to filter, will be modified
     */
    private static void applyPrototypeFilterEnabled(List<EntityPrototype> prototypes) 
    {
        List<String> enableFilters = loadFilters("simjr.editor.entitytypes.enabled");
        logger.debug("Applying prototype enabled filter: " + enableFilters);

        final Iterator<EntityPrototype> filterIt = prototypes.iterator();
        while (filterIt.hasNext())
        {
            final EntityPrototype p = filterIt.next();
            final String prototypePath = p.getDomain() + "." + p.getCategory() + "." + p.getSubcategory();
            boolean enabled = false;
            for(String enableFilter: enableFilters)
            {
                if(prototypePath.matches(enableFilter)) {
                    enabled = true;
                    break;
                }
            }
            if(!enabled) {
                filterIt.remove();
            }
        }
    }
    
    /**
     * Filters out prototypes that match anything in the disabled list
     * @param prototypes A list of prototypes to filter, will be modified
     */
    private static void applyPrototypeFilterDisabled(List<EntityPrototype> prototypes) 
    {
        List<String> disableFilters = loadFilters("simjr.editor.entitytypes.disabled");
        logger.debug("Applying prototype enabled filter: " + disableFilters);
        
        final Iterator<EntityPrototype> filterIt = prototypes.iterator();
        while (filterIt.hasNext())
        {
            final EntityPrototype p = filterIt.next();
            final String prototypePath = p.getDomain() + "." + p.getCategory() + "." + p.getSubcategory();
            
            for(String disableFilter: disableFilters)
            {
                if(prototypePath.matches(disableFilter)) {
                    filterIt.remove();
                    break;
                }
            }
        }
    }
    
    /**
     * Turns a human readable pattern of the form "foo.*.bar" into an appropriate regex, e.g. "foo\..*\.bar" 
     * @param pattern, e.g. "foo.*.bar"
     * @return A regex, e.g. "foo\..*\.bar"
     */
    private static String createRegex(String pattern)
    {
        return pattern.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*");
    }
    
    /**
     * Loads a set of filters from the given SimJr property.
     */
    private static List<String> loadFilters(String simJrProp)
    {
        final List<String> filters = new ArrayList<String>();
        
        String rawFilterList = SimJrProps.get(simJrProp);
        if(rawFilterList != null)
        {
            final String[] filterArray = rawFilterList.split(",");
            for(String filter: filterArray)
            {
                filters.add(createRegex(filter));
            }
        }
        
        return filters;
    }
}