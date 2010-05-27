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

/**
 * @author ray
 */
public class MetadataElement
{
    public static final String DEFAULT_NAME = "Untitled";
    public static final String NAME = MetadataElement.class.getCanonicalName() + ".name";
    public static final String DESC = MetadataElement.class.getCanonicalName() + ".desc";
    private final Model model;
    private final XPath nameXPath;
    private final XPath descXPath;
    
    public static Element buildDefault(Model model)
    {
        final Element root = model.newElement("metadata");
        root.addContent(model.newElement("name").setText(DEFAULT_NAME));
        root.addContent(model.newElement("description").setText(""));
        return root;
    }

    public MetadataElement(Model model)
    {
        this.model = model;
        this.nameXPath = this.model.newXPath("/simjr:scenario/simjr:metadata/simjr:name");
        this.descXPath = this.model.newXPath("/simjr:scenario/simjr:metadata/simjr:description");
    }
    
    public String getName()
    {
        return model.getText(nameXPath, null);
    }
    
    public UndoableEdit setName(String name)
    {
        final String oldName = getName();
        if(model.setText(nameXPath, null, name, new ModelChangeEvent(model, this, NAME)))
        {
            return new ChangeNameEdit(oldName);
        }
        return null;
    }
    
    public String getDescription()
    {
        return model.getText(descXPath, null);
    }
    
    public UndoableEdit setDescription(String d)
    {
        final String oldDesc = getDescription();
        if(model.setText(descXPath, null, d, new ModelChangeEvent(model, this, DESC)))
        {
            return new ChangeDescEdit(oldDesc);
        }
        return null;
    }
    
    private class ChangeNameEdit extends AbstractUndoableEdit
    {
        private static final long serialVersionUID = -3675152085436331048L;
        private final String oldName;
        private final String newName;
        
        public ChangeNameEdit(String oldName)
        {
            this.oldName = oldName;
            this.newName = getName();
        }

        @Override
        public void redo() throws CannotRedoException
        {
            super.redo();
            setName(newName);
        }

        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();
            setName(oldName);
        }
    }
    private class ChangeDescEdit extends AbstractUndoableEdit
    {
        private static final long serialVersionUID = -3675152085436331048L;
        private final String oldName;
        private final String newName;
        
        public ChangeDescEdit(String oldName)
        {
            this.oldName = oldName;
            this.newName = getDescription();
        }

        @Override
        public void redo() throws CannotRedoException
        {
            super.redo();
            setDescription(newName);
        }

        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();
            setDescription(oldName);
        }
    }
}
