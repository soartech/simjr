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

import org.jdom.Element;
import org.jdom.xpath.XPath;

import com.soartech.simjr.scenario.edits.ChangeScriptBlockEdit;
import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.scenario.model.ModelChangeEvent;

/**
 * @author ray
 */
public class ScriptBlockElement
{
    public static final String TYPE = ScriptBlockElement.class + ".type";
    public static final String TEXT = ScriptBlockElement.class + ".text";
    
    private final Model model;
    private final Element parentElement;
    private final XPath textPath;
    private final XPath typePath;

    public static Element buildDefault(Model model, String name, String defaultValue)
    {
        final Element e = model.newElement(name);
        e.setAttribute("type", "text/javascript", Model.NAMESPACE);
        if(defaultValue.length() > 0)
        {
            e.setText(defaultValue);
        }
        return e;
    }
    
    public static ScriptBlockElement attach(Model model, Element parentElement, String elementName)
    {
        return new ScriptBlockElement(model, parentElement, elementName);
    }
    
    private ScriptBlockElement(Model model, Element parentElement, String elementName)
    {
        this.model = model;
        this.parentElement = parentElement;
        this.typePath = model.newXPath("simjr:" + elementName + "/@simjr:type");
        this.textPath = model.newXPath("simjr:" + elementName);
    }
    
    /**
     * @return the model
     */
    public Model getModel()
    {
        return model;
    }

    public String getType()
    {
        return model.getText(typePath, parentElement);
    }
    
    public void setType(String type)
    {
        model.setText(typePath, parentElement, type, new ModelChangeEvent(model, this, TYPE));
    }
    
    public String getText()
    {
        return model.getText(textPath, parentElement);
    }
    public ChangeScriptBlockEdit setText(String text)
    {
        final String oldText = getText();
        if(model.setText(textPath, parentElement, text, new ModelChangeEvent(model, this, TEXT)))
        {
            return new ChangeScriptBlockEdit(this, oldText);
        }
        return null;
    }
}
