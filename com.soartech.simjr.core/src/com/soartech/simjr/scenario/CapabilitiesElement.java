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
 * Created on Mar 27, 2009
 */
package com.soartech.simjr.scenario;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.scenario.model.ModelChangeEvent;

/**
 * A CapabilitiesElement contains data for recreating entity capabilities, such as controllers.
 * Currently only supports the EntityFollower controller.
 * 
 * See ScenarioLoader for where the capabilities are added to the entity.
 */
public class CapabilitiesElement
{
    private static final Logger logger = Logger.getLogger(CapabilitiesElement.class);
    
    public static final String CAPABILITIES = CapabilitiesElement.class.getCanonicalName() + ".capabilities";
    
    private final EntityElement entity;
    
    private final XPath capabilitiesPath;
    private final XPath followTargetPath;
    
    public static Element buildDefault(Model model)
    {
        Element root = model.newElement("capabilities");
        return root;
    }
    
    /**
     * @param model
     */
    public CapabilitiesElement(EntityElement entity)
    {
        this.entity = entity;
        this.capabilitiesPath = this.entity.getModel().newXPath("simjr:capabilities");
        this.followTargetPath = this.entity.getModel().newXPath("simjr:capabilities/@simjr:followTarget");
    }
    
    public EntityElement getEntity()
    {
        return entity;
    }
    
    public String getFollowTarget()
    {
        String value = this.entity.getModel().getText(followTargetPath, entity.getElement());
        //For some reason, XPath.valueOf returns "" instead of null
        if(value != null && value.isEmpty()) {
            return null;
        }
        return value;
    }
    
    public UndoableEdit setFollowTarget(String newFollowTarget) 
    {
        logger.debug("Setting follow target: " + newFollowTarget);
        
        final Model model = this.entity.getModel();
        final String oldFollowTarget = this.getFollowTarget();
        
        boolean changed = updateFollowTarget(newFollowTarget);
        
        UndoableEdit edit = null;
        if(changed) {
            model.fireChange(newEvent(CAPABILITIES));
            edit = new ChangeCapabilitiesEdit(oldFollowTarget, newFollowTarget);
        }
        return edit;
    }
    
    private boolean updateFollowTarget(String newFollowTarget)
    {
        logger.debug("Updating follow target to: " + newFollowTarget);
        final Element context = entity.getElement();
        boolean changed = false;
        
        try {
            //Check for existing attribute
            Object o = followTargetPath.selectSingleNode(context);
            if(o != null && o instanceof Attribute)
            {
                //followTarget attr exists
                Attribute followTargetAttr = (Attribute)o;
                if(newFollowTarget != null) //Update it 
                {
                    logger.debug("Changing existing follow target attribute to: " + newFollowTarget + " from: " + followTargetAttr.getValue());
                    changed = !followTargetAttr.getValue().equals(newFollowTarget);
                    followTargetAttr.setValue(newFollowTarget);
                }
                else //Remove it 
                {
                    logger.debug("Removing existing follow target attribute: " + followTargetAttr.getValue());
                    if(followTargetAttr.getParent() != null) {
                        followTargetAttr.detach();
                        changed = true;
                    }
                }
            }
            else //followTarget attribute does not exist 
            {
                if(newFollowTarget != null) //create it
                {
                    logger.debug("Creating new follow target attribute: " + newFollowTarget);
                    o = capabilitiesPath.selectSingleNode(context);
                    Element capabilitiesElement;
                    if(o != null && o instanceof Element) {
                        capabilitiesElement = (Element)o;
                    }
                    else { //capabilities element doesn't exist
                        logger.debug("Creating new capabilities element");
                        capabilitiesElement = CapabilitiesElement.buildDefault(entity.getModel());
                        entity.getElement().addContent(capabilitiesElement);
                    }
                    capabilitiesElement.setAttribute("followTarget", newFollowTarget, Model.NAMESPACE);
                    changed = true;
                }
                else {
                    logger.debug("No follow target attribute to remove.");
                }
            }
        }
        catch (JDOMException e) {
            logger.error("Unable to update follow target: " + e);
        }
        
        return changed;
    }
    
    public UndoableEdit removeFollowTarget()
    {
        logger.debug("Removing follow target.");
        return setFollowTarget(null);
    }

    private ModelChangeEvent newEvent(String prop)
    {
        return new ModelChangeEvent(this.entity.getModel(), this, prop);
    }
    
    private class ChangeCapabilitiesEdit extends AbstractUndoableEdit
    {
        private static final long serialVersionUID = -8050360351980227293L;

        private final String oldFollowTarget;
        private final String newFollowTarget;
        
        public ChangeCapabilitiesEdit(String oldFollowTarget, String newFollowTarget)
        {
            this.oldFollowTarget = oldFollowTarget;
            this.newFollowTarget = newFollowTarget;
        }

        /* (non-Javadoc)
         * @see javax.swing.undo.AbstractUndoableEdit#redo()
         */
        @Override
        public void redo() throws CannotRedoException
        {
            super.redo();
            setFollowTarget(newFollowTarget);
        }

        /* (non-Javadoc)
         * @see javax.swing.undo.AbstractUndoableEdit#undo()
         */
        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();
            setFollowTarget(oldFollowTarget);
        }
    }
}
