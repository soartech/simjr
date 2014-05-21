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
 * Created on Sep 20, 2007
 */
package com.soartech.simjr.adaptables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Various tools for dealing with Adaptable objects
 * 
 * @author ray
 */
public class Adaptables
{

    /**
     * Adapt an object to the given class. This is equivalent to o.getAdapter(klass)
     * but o may be null or not adaptable and casting is handled automatically with 
     * generics.
     * 
     * @param <T> The desired type
     * @param o The object to adapt. If not Adaptable, then a simple instanceof test
     *      is performed. May be null.
     * @param klass The desired class. May not be null
     * @return An object of the desired type, or null.
     */
    public static <T> T adapt(Object o, Class<T> klass)
    {
        return adapt(o, klass, true);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T adapt(Object o, Class<T> klass, boolean recurse)
    {
        if(o == null)
        {
            return null;
        }
        else if(klass.isInstance(o))
        {
            return (T) o;
        }
        else if(recurse && (o instanceof Adaptable))
        {
            return (T) ((Adaptable) o).getAdapter(klass);
        }
        return null;
    }
    
    public static Object adaptUnchecked(Object o, Class<?> klass, boolean recurse)
    {
        if(o == null)
        {
            return null;
        }
        else if(klass.isInstance(o))
        {
            return o;
        }
        else if(recurse && (o instanceof Adaptable))
        {
            return ((Adaptable) o).getAdapter(klass);
        }
        return null;
    }
    
    /**
     * Filter a collection of objects to those that are adaptable to a particular
     * type.
     * 
     * @param <T> Desired type
     * @param collection Collection to filter
     * @param klass Desired type class
     * @return List of objects that are adaptable to T.
     */
    public static <T> List<T> adaptCollection(Collection<?> collection, Class<T> klass)
    {
        List<T> r = new ArrayList<T>();
        
        for(Object o : collection)
        {
            T t = adapt(o, klass);
            if(t != null)
            {
                r.add(t);
            }
        }
        
        return r;
    }

    /**
     * Search a collection for the first object that is adaptable to a particular
     * type.
     * 
     * @param <T> Desired type
     * @param collection Collection to search
     * @param klass Desired type class
     * @return First object in collection that is adaptable to T, or null if
     *  none was found.
     */
    public static <T> T findAdapter(Collection<?> collection, Class<T> klass)
    {
        for(Object o : collection)
        {
            T t = adapt(o, klass);
            if(t != null)
            {
                return t;
            }
        }
        return null;
    }
}
