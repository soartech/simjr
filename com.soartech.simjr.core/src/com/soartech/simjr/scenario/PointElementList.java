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
 * Created on Mar 30, 2009
 */
package com.soartech.simjr.scenario;

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

/**
 * @author ray
 */
public class PointElementList
{
    public static final String POINTS = PointElementList.class.getCanonicalName() + ".points";
    
    public static final Element buildDefault(Model model)
    {
        return model.newElement("points");
    }

    public static PointElementList attach(Model model, EntityElement parent)
    {
        return new PointElementList(model, parent);
    }
    
    private final Model model;
    private final EntityElement parent;
    private Element element;
    private final XPath namesPath;
    
    /**
     * @param model
     * @param parent
     */
    public PointElementList(Model model, EntityElement parent)
    {
        this.model = model;
        this.parent = parent;
        this.element = parent.getElement().getChild("points", Model.NAMESPACE);
        this.namesPath = model.newXPath("simjr:point/@simjr:name");
    }
    
    public EntityElement getParent()
    {
        return parent;
    }
    
    private Element createPointElement(String name)
    {
        final Element pointElement = model.newElement("point");
        pointElement.setAttribute("name", name, Model.NAMESPACE);
        return pointElement;
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getPoints()
    {
        List<String> result = new ArrayList<String>();
        if(element == null)
        {
            return result;
        }
        
        try
        {
            for(Attribute a : (List<Attribute>)namesPath.selectNodes(element))
            {
                result.add(a.getValue());
            }
            return result;
        }
        catch (JDOMException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public UndoableEdit setPoints(List<String> points)
    {
        final List<String> oldPoints = getPoints();
        if(points.equals(oldPoints))
        {
            return null;
        }
        
        initPointsElement();
        element.removeContent();
        for(String p : points)
        {
            element.addContent(createPointElement(p));
        }
        model.fireChange(new ModelChangeEvent(model, this, POINTS));
        
        return new SetPointsEdit(oldPoints);
    }
    
    private void initPointsElement()
    {
        if(element == null)
        {
            element = model.newElement("points");
            parent.getElement().addContent(element);
        }
    }
    
    private class SetPointsEdit extends AbstractUndoableEdit
    {
        private static final long serialVersionUID = -4284510748433377867L;
        
        final List<String> oldPoints;
        final List<String> newPoints = getPoints();
        
        public SetPointsEdit(List<String> oldPoints)
        {
            this.oldPoints = oldPoints;
        }

        @Override
        public void redo() throws CannotRedoException
        {
            super.redo();
            setPoints(newPoints);
        }

        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();
            setPoints(oldPoints);
        }
        
    }
}
