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
 * Created on Oct 30, 2007
 */
package com.soartech.simjr.ui.cheatsheets;

import java.awt.Graphics;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.apache.log4j.Logger;

import com.soartech.simjr.SimJrProps;
import com.soartech.simjr.util.SwingTools;

/**
 * @author ray
 */
public class CheatSheetHtmlPane extends JEditorPane
{
    private static final String STYLE_SHEET = "/com/soartech/simjr/ui/cheatsheets/cheatsheet.css";

    private static final Logger logger = Logger.getLogger(CheatSheetHtmlPane.class);
    
    private static final long serialVersionUID = 9099310711048906323L;

    public CheatSheetHtmlPane()
    {
        setEditable(false);
        
        String styleSheetFile = SimJrProps.get("simjr.cheatsheet.styleSheet");
        if(styleSheetFile != null)
        {
            addStyleSheet(styleSheetFile);
        }
    }
    
    public void addStyleSheet(String file)
    {
        final URL source = CheatSheetHtmlPane.class.getResource(STYLE_SHEET);
        if(source == null)
        {
            logger.warn("Could not locate cheatsheet stylesheet at '" + STYLE_SHEET + "'");
            return;
        }
        
        final HTMLEditorKit editorKit = new HTMLEditorKit();
        final StyleSheet styleSheet = new StyleSheet();
        styleSheet.addStyleSheet(editorKit.getStyleSheet());
        styleSheet.importStyleSheet(source);
        editorKit.setStyleSheet(styleSheet);
        setEditorKit(editorKit);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        SwingTools.enableAntiAliasing(g);
        super.paint(g);
    }

}
