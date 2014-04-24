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

import org.jdom.Element;
import org.jdom.xpath.XPath;

import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.scenario.model.ModelChangeEvent;

/**
 * @author ray
 */
public class OrientationElement
{
    public static final String ORIENTATION = OrientationElement.class.getCanonicalName() + ".orientation";
    
    private final EntityElement entity;
    private final XPath headingPath;
    private final XPath rollPath;
    private final XPath pitchPath;
    
    public static Element buildDefault(Model model)
    {
        Element root = model.newElement("orientation");
        root.setAttribute("heading", "0.0", Model.NAMESPACE);
        root.setAttribute("roll", "0.0", Model.NAMESPACE);
        root.setAttribute("pitch", "0.0", Model.NAMESPACE);
        
        return root;
    }
    
    /**
     * @param entity
     */
    public OrientationElement(EntityElement entity)
    {
        this.entity = entity;
        this.headingPath = this.entity.getModel().newXPath("simjr:orientation/@simjr:heading");
        this.rollPath = this.entity.getModel().newXPath("simjr:orientation/@simjr:roll");
        this.pitchPath = this.entity.getModel().newXPath("simjr:orientation/@simjr:pitch");
    }
    
    public EntityElement getEntity()
    {
        return entity;
    }

    public double getHeading()
    {
        return this.entity.getModel().getDouble(headingPath, entity.getElement());
    }
    
    public UndoableEdit setHeading(double degrees)
    {
        return setOrientation(degrees, getRoll(), getPitch());
    }
    
    public double getRoll()
    {
        return Double.valueOf(this.entity.getModel().getText(rollPath, entity.getElement()));
    }
    
    public UndoableEdit setRoll(double degrees)
    {
        return setOrientation(getHeading(), degrees, getPitch());
    }
    
    public double getPitch()
    {
        return Double.valueOf(this.entity.getModel().getText(pitchPath, entity.getElement()));        
    }
    
    public UndoableEdit setPitch(double meters)
    {
        return setOrientation(getHeading(), getRoll(), meters);
    }
    
    public UndoableEdit setOrientation(double headingDegrees, double rollDegrees, double pitchDegrees)
    {
        final Model model = this.entity.getModel();
        final Element context = entity.getElement();
        
        final double oldHeading = getHeading();
        final double oldRoll = getRoll();
        final double oldPitch = getPitch();
        
        boolean changed = model.setText(headingPath, context, Double.toString(headingDegrees), null);
        changed = model.setText(rollPath, context, Double.toString(rollDegrees), null) | changed;        
        changed = model.setText(pitchPath, context, Double.toString(pitchDegrees), null) | changed;
        
        UndoableEdit edit = null;
        if(changed)
        {
            model.fireChange(newEvent(ORIENTATION));
            edit = new Edit(oldHeading, oldRoll, oldPitch);
        }
        return edit;
    }
    
    private ModelChangeEvent newEvent(String prop)
    {
        return new ModelChangeEvent(this.entity.getModel(), this, prop);
    }
    
    private class Edit extends AbstractUndoableEdit
    {
        private static final long serialVersionUID = -8050360351980227293L;

        private final double oldHeading, oldRoll, oldPitch;
        private final double newHeading, newRoll, newPitch;
        
        public Edit(double oldHeading, double oldRoll, double oldPitch)
        {
            this.oldHeading = oldHeading;
            this.oldRoll = oldRoll;
            this.oldPitch = oldPitch;
            this.newHeading = getHeading();
            this.newRoll = getRoll();
            this.newPitch = getPitch();
        }

        /* (non-Javadoc)
         * @see javax.swing.undo.AbstractUndoableEdit#redo()
         */
        @Override
        public void redo() throws CannotRedoException
        {
            super.redo();
            setOrientation(newHeading, newRoll, newPitch);
        }

        /* (non-Javadoc)
         * @see javax.swing.undo.AbstractUndoableEdit#undo()
         */
        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();
            setOrientation(oldHeading, oldRoll, oldPitch);
        }
    }

}
