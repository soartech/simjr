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
 * Created on May 4, 2008
 */
package com.soartech.simjr.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.AbstractAdaptable;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.services.ConstructOnDemand;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.services.SimulationService;
import com.soartech.simjr.ui.SelectionManager;
import com.soartech.simjr.ui.SelectionManagerListener;

/**
 * @author ray
 */
@ConstructOnDemand
public class ActionManager extends AbstractAdaptable implements SimulationService
{
    private static final Logger logger = Logger.getLogger(ActionManager.class);
    
    private ServiceManager services;
    private List<AbstractSimulationAction> actions = new ArrayList<AbstractSimulationAction>();
    private Map<String, AbstractSimulationAction> actionCache = new HashMap<String, AbstractSimulationAction>();
    private Object context;
    
    private static class ObjectActionPair
    {
        AbstractSimulationAction action;
        Class<?> klass;
        public boolean adapt;
    };
    
    private List<ObjectActionPair> objectActions = new ArrayList<ObjectActionPair>();
    
    public static ActionManager findService(ServiceManager services)
    {
        return services.findService(ActionManager.class);
    }
    
    public static void update(ServiceManager services)
    {
        ActionManager am = findService(services);
        if(am != null)
        {
            am.updateActions();
        }
    }
    
    /**
     * Constructed on demand by ServiceManager.findService()
     * 
     * @param services The owning service manager
     */
    public ActionManager(ServiceManager services)
    {
        this.services = services;
        
        SelectionManager selectionManager = this.services.findService(SelectionManager.class);
        if(selectionManager != null)
        {
            selectionManager.addListener(new SelectionManagerListener() {

                public void selectionChanged(Object source)
                {
                    updateActions();
                }});
        }
        else
        {
            // TODO: maybe we should automatically create the selection manager?
            // Or maybe the ServiceManager should create simple objects automatically using a
            // default constructor or something?
            logger.warn("No selection manager found. Actions will not update with selection changes.");
        }
    }
    
    /**
     * @return The owning service manager
     */
    public ServiceManager getServices()
    {
        return services;
    }

    public AbstractSimulationAction getAction(String id)
    {
        AbstractSimulationAction r = actionCache.get(id);
        if(r != null)
        {
            return r;
        }
        
        for(AbstractSimulationAction action : actions)
        {
            if(id.equals(action.getId()))
            {
                r = action;
                break;
            }
        }
        
        if(r != null)
        {
            actionCache.put(r.getId(), r);
        }
        
        return r;
    }
    
    /**
     * Add an action that is managed by the application
     * 
     * @param action The action to add
     */
    public void addAction(AbstractSimulationAction action)
    {
        if(!actionCache.containsKey(action.getId()))
        {
            actionCache.put(action.getId(), action);
        }
        actions.add(action);
    }
    
    public void updateActions()
    {
        for(AbstractSimulationAction action : actions)
        {
            action.update();
        }
    }

    public void executeAction(String id)
    {
        AbstractSimulationAction action = getAction(id);
        if(action != null)
        {
            action.actionPerformed(null);
        }
        else
        {
            logger.error("No action found with id '" + id + "'");
        }
    }
    
    /**
     * Register an action associated with a particular object class.
     * 
     * @param action The action
     * @param klass The class of object this action is associated with.
     * @param adapt If true, the class is located through adapters in addition to the usual
     *      instanceof test.
     */
    public void addObjectAction(AbstractSimulationAction action, Class<?> klass, boolean adapt)
    {
        addAction(action);
        
        ObjectActionPair pair = new ObjectActionPair();
        pair.action = action;
        pair.klass = klass;
        pair.adapt = adapt;
        
        objectActions.add(pair);
    }
    
    /**
     * Return a list of actions applicable to the given object. These are 
     * actions previously installed with a call to {@link #addObjectAction(AbstractSimulationAction, Class, boolean)}.
     * 
     * @param o The object
     * @return The list of applicable actions.
     */
    public List<AbstractSimulationAction> getActionsForObject(Object o)
    {
        List<AbstractSimulationAction> result = new ArrayList<AbstractSimulationAction>();
        for(ObjectActionPair pair : objectActions)
        {
            if(pair.adapt)
            {
                if(Adaptables.adapt(o, pair.klass) != null)
                {
                    result.add(pair.action);
                }
            }
            else if(pair.klass.isInstance(o))
            {
                result.add(pair.action);
            }
        }
        return result;
    }
    

    /**
     * @return the context
     */
    public Object getActionContext()
    {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setActionContext(Object context)
    {
        this.context = context;
    }

    public void shutdown() throws SimulationException
    {
        // Do nothing
    }


    public void start(ProgressMonitor progress) throws SimulationException
    {
        // Do nothing
    }
}
