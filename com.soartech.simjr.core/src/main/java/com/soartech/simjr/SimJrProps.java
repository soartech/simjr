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
 * Created on Jun 5, 2007
 */
package com.soartech.simjr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.simjr.util.ExtendedProperties;

/**
 * Central property repository for Sim Jr. system. Handles loading of default
 * and user property files as well as any properties set on the command line.
 * See simjr.default.properties for descriptions and default values of all
 * properties.
 * 
 * @author ray
 */
public class SimJrProps
{
    public static final String HOME = "simjr.home";
    public static final String CONFIG_PATH = "simjr.config.path";
    public static final String SCRIPTS_PATH = "simjr.scripts.path";
    
    public static final String ATE_ENABLED = "simjr.soar.ate.enabled";
    public static final String ATE_PATH = "simjr.soar.ate.path";
    public static final String ATE_EXE = "simjr.soar.ate.exe";
    public static final String ATE_DEBUG = "simjr.soar.ate.debug";
    public static final String SML_PATH = "simjr.soar.sml.path";
    public static final String ATE_SML_CLIENT_NAME = "simjr.soar.ate.smlClientName";
    public static final String ATE_TSI_ENABLED = "simjr.soar.ate.tsi.enabled";
    
    private static final Logger logger = LoggerFactory.getLogger(SimJrProps.class);
    
    private static ExtendedProperties defaults;
    private static ExtendedProperties user;
    static
    {
        // First load the default properties file
        defaults = loadProperties(null, "simjr.default.properties", false);
        
        // Next load additional default property files...
        defaults = loadAdditionalProperties(defaults);
        
        // Load user-specific properties
        user = loadProperties(defaults, "simjr.user.properties", true);
        
        // Any properties set on the command line will take final precendence
        user.putAll(System.getProperties());
        
        File home = new File(get(HOME));
        
        user.setProperty(HOME, home.getAbsolutePath());
    }
    
    /**
     * @return The default properties
     */
    public static ExtendedProperties getDefaults()
    {
        return defaults;
    }
    
    /**
     * @return The final property values
     */
    public static ExtendedProperties getProperties()
    {
        return user;
    }
    
    /**
     * @return The final property values, as a less-powerful Java Properties
     *         object.
     */
    public static Properties getJavaProperties()
    {
        return user.getJavaProperties();
    }
    
    /**
     * Retrieve a property
     * 
     * @param key The property name 
     * @return The property value, or null if not found
     */
    public static String get(String key)
    {
        return get(key, null);
    }
    
    /**
     * Retrieve a string property
     * 
     * @param key The property name
     * @param def The default value
     * @return The property value, or default if not found
     */
    public static String get(String key, String def)
    {
        return getProperties().getProperty(key, def);
    }
    
    /**
     * Retrieve an integer property
     * 
     * @param key The property name
     * @param def The default integer value
     * @return The property value, or default if not found or not an integer
     */
    public static int get(String key, int def)
    {
        return getProperties().getInteger(key, def);
    }
    
    /**
     * Retrieve a double property
     * 
     * @param key The property name
     * @param def The default double value
     * @return The property value, or default if not found or not a double
     */
    public static double get(String key, double def)
    {
        return getProperties().getDouble(key, def);
    }  
    
    /**
     * Retrieve a boolean property
     * 
     * @param key The property name
     * @param def The default boolean value
     * @return The property value, or default if not found
     */
    public static boolean get(String key, boolean def)
    {
        return getProperties().getBoolean(key, def);
    }
    
    /**
     * Loads additional default property files from the simjr.propertyFiles 
     * system property. simjr.propertyFiles is a comma-separated list of 
     * additional property files that are loaded between simjr.default.properties
     * and simjr.user.properties.
     * 
     * @param defaults Default properties to extend
     * @return Result properties, never null.
     */
    private static ExtendedProperties loadAdditionalProperties(ExtendedProperties defaults)
    {
        String fileList = System.getProperty("simjr.propertyFiles", "").trim();
        String[] toks = fileList.split(",");
        for(String fileName : toks)
        {
            fileName = fileName.trim();
            if(fileName.length() > 0)
            {
                defaults = loadProperties(defaults, fileName, false);
            }
        }
        return defaults;
    }
    
    /**
     * Load a property file.  First tries to load the file from the file system 
     * and then from the class path.
     * 
     * @param defaults Default properties to extend, or null.
     * @param fileName The file name
     * @param optional If true, errors will be ignored.
     * @return New properties object. Never null.
     */
    private static ExtendedProperties loadProperties(ExtendedProperties defaults, String fileName, boolean optional)
    {
        ExtendedProperties props = defaults != null ? new ExtendedProperties(defaults) : new ExtendedProperties();
        try
        {
            File file = new File(fileName);
            URL url = SimJrProps.class.getResource('/' + fileName);
            if(file.exists() && file.isFile())
            {
                logger.info("Loading properties from '" + fileName + "' (disk)");
                FileInputStream input = new FileInputStream(fileName);
                props.load(input);
                input.close();                
            }
            else if(url != null)
            {
                logger.info("Loading properties from '" + fileName + "' (class-path)");
                InputStream input = url.openStream(); 
                props.load(input);
                input.close();
            }
            else
            {
                if(!optional)
                {
                    throw new FileNotFoundException("Failed to load properties from '" + fileName + "'");
                }
            }
        }
        catch(FileNotFoundException e)
        {
            if(!optional)
            {
                logger.error("Failed to load properties from '" + fileName + "'", e);
                throw new RuntimeException(e);
            }
        }
        catch(IOException e)
        {
            logger.error("Failed to load properties from '" + fileName + "'", e);
            throw new RuntimeException(e);
        }
        return props;
    }
    
    /**
     * Loads default properties from a resource using the classpath.
     * 
     * <p>
     * Properties loaded with this function will not override any properties
     * loaded with loadPluginUserProperties, but will be used as defaults in
     * case a property isn't defined in the user properties.
     * 
     * @param loader
     *            Class loader to use to find resource
     * @param resourcePath
     *            Path to the resource to load properties from
     * @throws IOException
     *             Thrown if there was a problem reading the resource
     */
    public static void loadPluginProperties(ClassLoader loader, String resourcePath) throws IOException
    {
        logger.info("Loading plugin properties from '" + resourcePath + "'");
        loadPluginPropertiesInternal(getDefaults(), loader, resourcePath);
    }
    
    /**
     * Loads user properties from a resource using the classpath.
     * 
     * <p>
     * Properties loaded with this function override default properties.
     * 
     * @param loader
     *            Class loader to use to find resource
     * @param resourcePath
     *            Path to the resource to load properties from
     * @throws IOException
     *             Thrown if there was a problem reading the resource
     */    
    public static void loadPluginUserProperties(ClassLoader loader, String resourcePath) throws IOException
    {
        logger.info("Loading plugin user properties from '" + resourcePath + "'");
        loadPluginPropertiesInternal(getProperties(), loader, resourcePath);
    }
    
    private static void loadPluginPropertiesInternal(ExtendedProperties scope, ClassLoader loader, String resourcePath) throws IOException
    {
        final InputStream is = loader.getResourceAsStream(resourcePath);
        if(is != null)
        {
            try
            {
                scope.load(is);
                scope.putAll(System.getProperties()); //Command line properties must still take precedence
            }
            finally
            {
                is.close();
            }
        }
        else
        {
            logger.error("Could not find resource '" + resourcePath + "'");
        }
    }
    
    /**
     * Load properties (defaults) from a url.
     * 
     * @param url The URL from which to load properties.
     */
    public static void loadPluginProperties(URL url)
    {
        logger.info("Loading plugin properties from '" + url + "'");
        loadPluginPropertiesInternal(getDefaults(), url);
    }
    
    /**
     * Load user properties from a url.
     *
     * @param url The URL from which to load properties.
     */
    public static void loadPluginUserProperties(URL url)
    {
        logger.info("Loading plugin user properties from '" + url + "'");
        loadPluginPropertiesInternal(getProperties(), url);
    }
    
    private static void loadPluginPropertiesInternal(ExtendedProperties scope, URL url)
    {
        InputStream is = null;
        try 
        {
            try
            {
                is = url.openStream();
                scope.load(is);
                scope.putAll(System.getProperties()); //Command line properties must still take precedence
            }
            finally
            {
                is.close();
            }
        }
        catch (IOException e)
        {
            logger.error("Could not load resource '" + url.toString() + "'");
        }
    }
}
