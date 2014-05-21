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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.services.ServiceManager;

/**
 * A helper class to provide a list of prototypes filtered by user properties.
 */
public class FilterableEntityPrototypes 
{
    private final List<EntityPrototype> prototypes;
    
    public FilterableEntityPrototypes(List<EntityPrototype> prototypes)
    {
        this.prototypes = prototypes;
    }
    
    public List<EntityPrototype> getPrototypes() { return Collections.unmodifiableList(prototypes); }
    
    /**
     * Populates the default type model based on built-in filters and those specified via properties.
     */
    public static FilterableEntityPrototypes getUserPrototypes(ServiceManager services)
    {
        final EntityPrototypeDatabase db = EntityPrototypeDatabase.findService(services);
        final List<EntityPrototype> defaultPrototypes = db.getPrototypes();
        
        FilterableEntityPrototypes filteredPrototypes = new FilterableEntityPrototypes(defaultPrototypes);
        filteredPrototypes.excludeAbstract()
                          .exclude("*.flyout.*")
                          .include(loadFilters("simjr.editor.entitytypes.enabled"))
                          .exclude(loadFilters("simjr.editor.entitytypes.disabled"))
                          .sort();
        
        return filteredPrototypes;
    }
    
    /**
     * Filters out all but prototypes matching the given filter string. 
     * @param filter
     * @return
     */
    public FilterableEntityPrototypes include(String filter)
    {
        return include(Arrays.asList(new String[] { filter }));
    }
    
    /**
     * Filters all prototypes not matching one of the given filter strings. 
     * @param whitelist
     * @return
     */
    public FilterableEntityPrototypes include(List<String> whitelist)
    {
        final Iterator<EntityPrototype> filterIt = prototypes.iterator();
        while (filterIt.hasNext())
        {
            final EntityPrototype p = filterIt.next();
            final String prototypePath = p.getDomain() + "." + p.getCategory() + "." + p.getSubcategory();
            boolean enabled = false;
            for(String inclusion: createRegexes(whitelist))
            {
                if(prototypePath.matches(inclusion)) {
                    enabled = true;
                    break;
                }
            }
            if(!enabled) {
                filterIt.remove();
            }
        }
        return this;
    }
    
    /**
     * Filters any prototype matching the given filter.
     * @param filter
     * @return
     */
    public FilterableEntityPrototypes exclude(String filter) 
    {
        return exclude(Arrays.asList(new String[] { filter }));
    }
    
    /**
     * Filters all the prototypes matching one of the filter strings. 
     * @param blacklist
     * @return
     */
    public FilterableEntityPrototypes exclude(List<String> blacklist) 
    {
        final Iterator<EntityPrototype> filterIt = prototypes.iterator();
        while (filterIt.hasNext())
        {
            final EntityPrototype p = filterIt.next();
            final String prototypePath = p.getDomain() + "." + p.getCategory() + "." + p.getSubcategory();
            
            for(String exclusion: createRegexes(blacklist))
            {
                if(prototypePath.matches(exclusion)) { 
                    filterIt.remove();
                    break;
                }
            }
        }
        
        return this;
    }
    
    /**
     * Excludes abstract entity prototypes.
     * @return
     */
    public FilterableEntityPrototypes excludeAbstract()
    {
        final Iterator<EntityPrototype> it = prototypes.iterator();
        while (it.hasNext())
        {
            final EntityPrototype p = it.next();
            if (p.isAbstract()) {
                it.remove();
            }
        }
        
        return this;
    }
    
    /**
     * Sorts the prototype list in lexicographic order 
     * @return
     */
    public FilterableEntityPrototypes sort()
    {
        Collections.sort(prototypes, new Comparator<EntityPrototype>() {
            public int compare(EntityPrototype o1, EntityPrototype o2)
            {
                return o1.toString().compareTo(o2.toString());
            }
        });
        
        return this;
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
     * See createRegex
     * @param patterns
     * @return
     */
    private static List<String> createRegexes(List<String> patterns)
    {
        List<String> regexes = new ArrayList<String>();
        for(String pattern: patterns) {
            regexes.add(createRegex(pattern));
        }
        return regexes;
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
                filters.add(filter);
            }
        }
        
        return filters;
    }
}