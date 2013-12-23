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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.AbstractAdaptable;
import com.soartech.simjr.services.ConstructOnDemand;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.services.SimulationService;

/**
 * A database of entity prototypes. <b>Note that this class should not be constructed
 * directly. It should generally be retrieved with {@link #findService(ServiceManager)}
 * or {@link Simulation#getEntityPrototypes()}.</b>
 * 
 * @see EntityPrototype
 * @see EntityPrototypes
 * @author ray
 */
@ConstructOnDemand
public class EntityPrototypeDatabase extends AbstractAdaptable implements SimulationService
{
    private static final Logger logger = Logger.getLogger(EntityPrototypeDatabase.class);
    
    private Holder holder = new Holder();
    
    /**
     * Helper function to support scripting.
     * 
     * @param services
     * @return
     */
    public static EntityPrototypeDatabase findService(ServiceManager services)
    {
        return services.findService(EntityPrototypeDatabase.class);
    }
    
    public void load() throws IOException
    {
        final String resource = SimJrProps.get("simjr.simulation.entity.prototypes", "/simjr.entityprototypes.yaml");
        loadResource(resource, EntityPrototypeDatabase.class.getClassLoader());
        loadFragments(new InputStreamReader(getClass().getResourceAsStream("/simjr.ui.entityprototypes.yaml")), 
                      getClass().getClassLoader());
    }

    public void loadResource(final String resource, ClassLoader classLoader) throws IOException
    {
        logger.info("Loading entity prototypes from resource '" + resource + "' with class loader '" + classLoader + "' ...");
        final InputStream input = classLoader.getResourceAsStream(resource);
        if(input == null)
        {
            logger.error("Could not open resource '" + resource + "'");
            throw new IllegalArgumentException("Could not open resource '" + resource + "'");
        }
        try
        {
            load(new InputStreamReader(input), classLoader);
        }
        finally
        {
            input.close();
        }
    }
    
    /**
     * Load prototypes from the given reader and insert them into this database.
     * If any of the prototypes has an id that is already in use, an exception
     * will be thrown.
     * 
     * @param input the input reader (YAML)
     * @param classLoader Class loader used for resolving {@code !class} and @{code !!}
     *      directives in YAML. 
     */
    public void load(Reader input, ClassLoader classLoader)
    {
        final Yaml yaml = prepareLoader(classLoader);
        final Holder newHolder = (Holder) yaml.load(input);
        holder.setPrototypes(newHolder.getPrototypes());
        logger.info("Loaded " + newHolder.prototypes.size() + " prototypes");
    }
    
    /**
     * Load prototype fragments from the given reader and apply them to the 
     * database. Basically, the contents of the fragments overwrite or extend
     * the prototypes in the database. Look at the tests. 
     * 
     * @param input the input reader (YAML)
     * @param classLoader Class loader used for resolving {@code !class} and @{code !!}
     *      directives in YAML. 
     */
    public void loadFragments(Reader input, ClassLoader classLoader)
    {
        final Yaml yaml = prepareLoader(classLoader);
        final Holder newHolder = (Holder) yaml.load(input);
        final Map<String, EntityPrototype> fragments = newHolder.prototypes;
        applyFragments(fragments.values());
    }

    private void applyFragments(final Collection<EntityPrototype> fragments)
    {
        for(EntityPrototype fragment : fragments)
        {
            final EntityPrototype target = getPrototype(fragment.getId());
            if(target == null)
            {
                throw new IllegalArgumentException("Could not find target prototype '" + fragment.getId() + "'");
            }
            applyFragment((DefaultEntityPrototype) fragment, target);
        }
        logger.info("Applied " + fragments.size() + " fragments to entity prototypes");
    }

    private void applyFragment(DefaultEntityPrototype fragment, EntityPrototype target)
    {
        if(!(target instanceof DefaultEntityPrototype))
        {
            throw new IllegalArgumentException("Target of fragment '" + fragment.getId() + 
                    "' is not DefaultEntityPrototype, it's " + target.getClass());
        }
        logger.debug("Applying prototype fragment to '" + target.getId() + "'");
        final DefaultEntityPrototype extendableTarget = (DefaultEntityPrototype) target;
        extendableTarget.getBaseProperties().putAll(fragment.getBaseProperties());
    }

    private Yaml prepareLoader(ClassLoader classLoader)
    {
        final Constructor constructor = new MyConstructor(Holder.class, classLoader);
        final TypeDescription typeDescription = new TypeDescription(Holder.class);
        typeDescription.putListPropertyType("prototypes", DefaultEntityPrototype.class);
        constructor.addTypeDescription(typeDescription);
        
        final Loader loader = new Loader(constructor);
        final Yaml yaml = new Yaml(loader);
        return yaml;
    }
    
    public EntityPrototypeDatabase()
    {
    }
    
    /**
     * @return the prototypes
     */
    public List<EntityPrototype> getPrototypes()
    {
        return holder.getPrototypes();
    }
    
    public EntityPrototype getPrototype(String id)
    {
        return holder.prototypes.get(id);
    }
    
    public <T extends EntityPrototype> T addPrototype(T prototype)
    {
        final String id = prototype.getId();
        if(holder.prototypes.containsKey(id))
        {
            throw new IllegalArgumentException("Database already contains prototype with id '" + id + "'");
        }
        holder.prototypes.put(id, prototype);
        return prototype;
    }

    public String toString() 
    {
        return holder.prototypes.toString();
    }
    
    private class MyConstructor extends CustomClassLoaderConstructor {

        public MyConstructor(Class<Holder> rootClass, final ClassLoader classLoader)
        {
            super(rootClass, classLoader);
            
            yamlConstructors.put(new Tag("!class"), new Construct() {

                public Object construct(Node node)
                {
                    final String className = (String) constructScalar((ScalarNode) node);
                    try
                    {
                        return classLoader.loadClass(className);
                    }
                    catch (ClassNotFoundException e)
                    {
                        throw new IllegalArgumentException("Failed to find class '" + className + "'", e);
                    }
                }

                @Override
                public void construct2ndStep(Node arg0, Object arg1)
                {
                }});
            
            yamlConstructors.put(new Tag("!prototype"), new Construct() {

                public Object construct(Node node)
                {
                    final String prototypeId = (String) constructScalar((ScalarNode) node);
                    final EntityPrototype prototype = getPrototype(prototypeId);
                    if(prototype == null)
                    {
                        throw new IllegalArgumentException("Could not locate prototype for '!prototype " + prototypeId + "'");
                    }
                    return prototype;
                }

                @Override
                public void construct2ndStep(Node arg0, Object arg1)
                {
                }});
        }
        
    };

    public static class Holder
    {
        private Map<String, EntityPrototype> prototypes = new LinkedHashMap<String, EntityPrototype>();

        public Holder() 
        {
        }
        
        public List<EntityPrototype> getPrototypes()
        {
            return new ArrayList<EntityPrototype>(prototypes.values());
        }

        public void setPrototypes(List<EntityPrototype> prototypes)
        {
            for(EntityPrototype p : prototypes)
            {
                final EntityPrototype existing = this.prototypes.put(p.getId(), p);
                if(existing != null)
                {
                    throw new IllegalArgumentException("A prototype with id '" + p.getId() + "' already exists: " + existing);
                }
            }
        }
        
    }

    public void shutdown() throws SimulationException
    {
    }

    public void start(ProgressMonitor progress) throws SimulationException
    {
    }
}
