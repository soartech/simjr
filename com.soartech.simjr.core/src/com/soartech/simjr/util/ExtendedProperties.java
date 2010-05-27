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
 * Created on Aug 7, 2007
 */
package com.soartech.simjr.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * A replacement for Java's Properties class that includes support for 
 * ANT-style variables in properties. For example,
 * 
 * <pre>
 * simjr.home=.
 * 
 * simjr.config.path=${simjr.home}/config
 * </pre>
 * 
 * <p>Variables may be nested. They are expanded at the time they are 
 * retrieved. If a variable cannot be found, an IllegalArgumentException
 * is thrown.
 * 
 * @author ray
 */
public class ExtendedProperties
{
    private static final Logger logger = Logger.getLogger(ExtendedProperties.class);
    
    /**
     * Parent properties
     */
    private ExtendedProperties parent;
    
    /**
     * The map of property values. 
     */
    private Map<String, String> map = new HashMap<String, String>();
    
    /**
     * Construct an empty property set with no parent 
     */
    public ExtendedProperties()
    {
        this(null);
    }
    
    /**
     * Construct a property set with the given parent. If a property is not
     * found in this property set, the request will be forwarded to the
     * parent.
     * 
     * @param parent Parent property set.
     */
    public ExtendedProperties(ExtendedProperties parent)
    {
        this.parent = parent;
    }
    
    /**
     * @return True if this property set (and its parents) is empty.
     */
    public boolean isEmpty()
    {
        return map.isEmpty() && (parent != null ? parent.isEmpty() : true);
    }
    
    /**
     * Look up a property value by key
     * 
     * @param key The key
     * @return The value, or null if not found.
     */
    public String getProperty(String key)
    {
        return getProperty(key, null);
    }
    
    public String getProperty(String key, String defaultValue)
    {
        return getProperty(key, defaultValue, this);
    }
    
    public void setProperty(String key, String value)
    {
        map.put(key, value);
    }
    
    public int getInteger(String key, int def)
    {
        try
        {
            return Integer.parseInt(getProperty(key, Integer.toString(def)));
        }
        catch(NumberFormatException e)
        {
            return def;
        }
    }
    
    public double getDouble(String key, double def)
    {
        try
        {
            return Double.parseDouble(getProperty(key, Double.toString(def)));
        }
        catch(NumberFormatException e)
        {
            return def;
        }
    }
    
    public boolean getBoolean(String key, boolean def)
    {
        return getProperty(key, Boolean.toString(def)).toLowerCase().equals("true");
    }
    
    /**
     * @return The set of all keys in this property set
     */
    public Set<String> getKeys()
    {
        Set<String> keys = new HashSet<String>(map.keySet());
        if(parent != null)
        {
            keys.addAll(parent.getKeys());
        }
        return keys;
    }
    
    /**
     * Expand all properties in this set. Any properties that contain unknown variables
     * will be ignored, but a warning will be logged.
     * 
     * @return All properties in this object, fully expanded.
     */
    public Map<String, String> getExpandedProperties()
    {
        final Map<String, String> expanded = new HashMap<String, String>();
        for(String key : getKeys())
        {
            try
            {
                final String value = getProperty(key);
                expanded.put(key, value);
            }
            catch (IllegalArgumentException e)
            {
                logger.warn("Ignoring invalid property '" + key + "': " + e.getMessage());
            }
        }
        return expanded;
    }
    
    /**
     * Clear all properties from this property set. Does not clear properties in
     * parent sets.
     */
    public void clear()
    {
        map.clear();
    }
    
    /**
     * Load the properties from the given input stream into this property set. Existing
     * properties are not cleared.
     * 
     * @param inStream stream to read from
     * @throws IOException
     */
    public void load(InputStream inStream) throws IOException
    {
        Properties props = new Properties();
        props.load(inStream);
        putAll(props);
    }
        
    /**
     * Put all properties from the given Java property set into this one.
     * 
     * @param props The property set to copy.
     */
    public void putAll(Properties props)
    {
        for(Map.Entry<Object, Object> e : props.entrySet())
        {
            map.put(e.getKey().toString(), e.getValue().toString());
        }
    }
    
    /**
     * Extract a resource to a temp file and store the resulting location in a property. 
     * This is a fairly common operation for plugins, so here's a utility for it. The
     * temp file is set to be deleted automatically on exit.
     * 
     * @param loader The class loader to use to locate the resource
     * @param path the path to the resource (e.g. "/com/soartech/simjr/foo.txt")
     * @param property the name of the property to store resulting path in
     * @throws IOException
     */
    public void extractTempFileForProperty(ClassLoader loader, String path, String property) throws IOException
    {
        final InputStream is = loader.getResourceAsStream(path);
        if(is != null)
        {
            try
            {
                final File tempFile = FileTools.copyToTempFile(is, property, ".tmp");
                tempFile.deleteOnExit();
                setProperty(property, tempFile.getAbsolutePath());
                logger.info("Extracted '" + path + "' to '" + tempFile + "' and stored in '" + property + "'");
            }
            finally
            {
                is.close();
            }
        }
    }
    
    private String getProperty(String key, String defaultValue, ExtendedProperties child)
    {
        if(map.containsKey(key))
        {
            return expandProperty(map.get(key), child);
        }
        else if(parent != null)
        {
            return parent.getProperty(key, defaultValue, child);
        }
        else if(defaultValue == null)
        {
            return null;
        }
        String value = expandProperty(defaultValue, child);
        
        return value != null ? value.trim() : null;
    }
    
    private String expandProperty(String value, ExtendedProperties child)
    {
        Variable v = findVariable(value);
        while(v != null)
        {
            String varValue = child.getProperty(v.name);
            if(varValue == null)
            {
                throw new IllegalArgumentException("Undefined variable '" + v.name + "' in '" + value + "'");
            }
            value = value.substring(0, v.start) + varValue + value.substring(v.end);
            
            v = findVariable(value);
        }
        return value;
    }
    
    private static class Variable
    {
        public String name;
        public int start = -1;
        public int end = -1;
    }
    
    private Variable findVariable(String value)
    {
        Variable v = new Variable();
        char previous = 0, current = 0, next = 0;
        
        for(int i = 0; i < value.length(); ++i)
        {
            previous = current;
            current = value.charAt(i);
            next = i + 1 < value.length() ? value.charAt(i + 1) : 0;
            
            if(v.start == -1 && current == '$' && next == '{' && previous != '\\')
            {
                v.start = i;
            }
            else if(v.end == -1 && v.start != -1 && current == '}'  && previous != '\\')
            {
                v.end = i + 1;
            }
        }
        if(v.start != -1 && v.end != -1)
        {
            assert v.end >= v.start + 3;
            
            v.name = value.substring(v.start + 2, v.end - 1);
            return v;
        }
        return null;
    }
}
