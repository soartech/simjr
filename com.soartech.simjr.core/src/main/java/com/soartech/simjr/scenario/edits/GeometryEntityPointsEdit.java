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
 * Created on Mar 29, 2009
 */
package com.soartech.simjr.scenario.edits;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.soartech.simjr.scenario.EntityElement;

/**
 * @author ray
 */
public class GeometryEntityPointsEdit extends AbstractUndoableEdit
{
    private static final long serialVersionUID = -1233404857977400436L;
    
    private final EntityElement geometryEntity;
    private final int index;
    
    /**
     * 
     */
    public GeometryEntityPointsEdit(EntityElement geometryEntity)
    {
        this.geometryEntity = geometryEntity;
        this.index = geometryEntity.getModel().getEntities().getEntities().indexOf(geometryEntity);
    }

    /* (non-Javadoc)
     * @see javax.swing.undo.AbstractUndoableEdit#redo()
     */
    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();
        
        this.geometryEntity.getModel().getEntities().addEntity(geometryEntity, index);
    }

    /* (non-Javadoc)
     * @see javax.swing.undo.AbstractUndoableEdit#undo()
     */
    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();
        
        this.geometryEntity.getModel().getEntities().removeEntity(geometryEntity);
    }

    public EntityElement getEntity()
    {
        return geometryEntity;
    }   
}