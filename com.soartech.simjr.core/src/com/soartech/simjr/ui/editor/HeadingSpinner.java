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
 * Created on Apr 7, 2009
 */
package com.soartech.simjr.ui.editor;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.UndoableEdit;

import com.soartech.simjr.scenario.OrientationElement;
import com.soartech.simjr.scenario.model.ModelChangeEvent;
import com.soartech.simjr.scenario.model.ModelChangeListener;

/**
 * @author ray
 */
public class HeadingSpinner extends JPanel implements ModelChangeListener, ChangeListener
{
    private static final long serialVersionUID = 5213871459070396772L;

    private final UndoService undoService;
    private final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, -180, 180, 1);
    private final JSpinner spinner = new JSpinner(spinnerModel);
    private OrientationElement element;
    
    public HeadingSpinner(UndoService undoService)
    {
        super(new BorderLayout());
        
        this.undoService = undoService;
        
        add(spinner, BorderLayout.CENTER);
        
        spinnerModel.addChangeListener(this);
    }
    
    public void setElement(OrientationElement element)
    {
        if(this.element != null)
        {
            this.element.getEntity().getModel().removeModelChangeListener(this);
        }
        this.element = element;
        if(this.element != null)
        {
            this.spinner.setEnabled(true);
            this.spinnerModel.setValue(this.element.getHeading());
            this.element.getEntity().getModel().addModelChangeListener(this);
        }
        else
        {
            this.spinner.setEnabled(false);
        }
    }
    
    public OrientationElement getElement()
    {
        return element;
    }

    public void onModelChanged(ModelChangeEvent e)
    {
        if(e.source == element && e.property.equals(OrientationElement.ORIENTATION))
        {
            this.spinnerModel.setValue(this.element.getHeading());
        }
    }

    public void stateChanged(ChangeEvent e)
    {
        if(this.element != null)
        {
            final UndoableEdit edit = this.element.setHeading(((Number) spinnerModel.getValue()).doubleValue());
            undoService.addEdit(edit);
        }
    }
    
    
}
