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
 * Created on Oct 28, 2007
 */
package com.soartech.simjr.ui.cheatsheets;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

import javax.swing.text.AbstractDocument.BranchElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soartech.simjr.scripting.ScriptRunSettings;
import com.soartech.simjr.scripting.ScriptRunner;
import com.soartech.simjr.util.WebBrowserTools;

/**
 * @author ray
 */
public class CheatSheet
{
    private static final Logger logger = LoggerFactory.getLogger(CheatSheet.class);
    
    private String name;
    private String location;
    private String javaScript;
    
    /**
     * @param location
     */
    public CheatSheet(String name, String location)
    {
        this.name = name;
        this.location = location;
    }
    
    public String getName()
    {
        return name;
    }
    
    void pageLoaded(HTMLDocument doc)
    {
        if(doc != null)
        {
            Pair script = null;
            int i = 0;
            for(Element e : doc.getRootElements())
            {
                script = getScriptElement(e, i++);
                if(script != null)
                {
                    break;
                }
            }
            
            if(script != null)
            {
                BranchElement parent = (BranchElement) script.element.getParentElement();
                Element next = parent.getElement(script.index + 1);
                AttributeSet attrs = next != null ? next.getAttributes() : null;
                if(attrs != null && attrs.isDefined(HTML.Attribute.COMMENT))
                {
                    Object o = attrs.getAttribute(HTML.Attribute.COMMENT);
                    javaScript = o.toString();
                }
                else
                {
                    javaScript = "";
                }
                logger.debug("javScript=" + javaScript);
            }
        }
        else
        {
            javaScript = "";
        }
    }
    
    public void activate(CheatSheetView view)
    {
        view.showPage(location);
    }

    private static class Pair
    {
        Element element;
        int index;
    }
    
    private Pair getScriptElement(Element root, int index)
    {
        String name = root.getName();
        if("script".equals(name))
        {
            Pair pair = new Pair();
            pair.element = root;
            pair.index = index;
            return pair;
        }
        
        if(root instanceof BranchElement)
        {
            BranchElement branch = (BranchElement) root;
            for(int i = 0; i < branch.getElementCount(); ++i)
            {
                Element e = branch.getElement(i);
                
                Pair script = getScriptElement(e, i);
                if(script != null)
                {
                    return script;
                }
            }
        }
        
        return null;
    }
    
    public void handleAction(CheatSheetView view, URI uri)
    {
        String scheme = uri.getScheme();
        if(scheme.equals("simjr"))
        {
            handleJavaScriptAction(view, uri);
        }
        else if(scheme.equals("file"))
        {
            view.showPage(uri.toString());
        }
        else if(scheme.equals("http"))
        {
            WebBrowserTools.openBrower(uri.toString());
        }
        else
        {
            view.setError("Unsupported scheme '" + scheme + "': " + uri);
        }
    }
 
    private void handleJavaScriptAction(CheatSheetView view, URI uri)
    {
        String code = uri.getSchemeSpecificPart();
        if(code == null)
        {
            return;
        }
        
        ScriptRunner runner = view.getServices().findService(ScriptRunner.class);
        if(runner == null)
        {
            logger.error("Scripting support (ScriptRunner) is not available");
            return;
        }
        
        final String finalCode = javaScript + "\n" + code;
        StringReader reader = new StringReader(finalCode);
        try
        {
            ScriptRunSettings.builder().reader(reader).path("CheatSheet").run(runner);
        }
        catch (Exception e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            view.setError(code + "\n\n" + e.getMessage() + "\n\n" + sw.toString());
        }
    }

//    private static Map<String, String> parseQuery(URI uri)
//    {
//        Map<String, String> result = new HashMap<String, String>();
//        String query = uri.getQuery();
//        if(query == null)
//        {
//            return result;
//        }
//        
//        List<String> parts = StringTools.split(query, "&");
//        for(String part : parts)
//        {
//            int i = part.indexOf('=');
//            if(i >= 0)
//            {
//                result.put(part.substring(0, i), part.substring(i+1));
//            }
//            else
//            {
//                result.put(part, "");
//            }
//        }
//        
//        return result;
//    }
}
