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
 * Created on Aug 16, 2007
 */
package com.soartech.simjr.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

/**
 * @author ray
 */
public class SwingTools
{
    public static JPanel createLabeledComponent(String label, Component component)
    {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(label), BorderLayout.WEST);
        p.add(component, BorderLayout.CENTER);
        return p;
    }
    
    public static void enableAntiAliasing(Graphics g)
    {
        if(g instanceof Graphics2D)
        {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }
    
    public static FileFilter createFileFilter(Object extension, final String desc)
    {
        final Collection<?> extensions;
        if(extension instanceof Collection<?>)
        {
            extensions = (Collection<?>) extension;
        }
        else
        {
            extensions = Arrays.asList(extension);
        }
        return new FileFilter() {

            @Override
            public boolean accept(File f)
            {
                final String ext = FileTools.getExtension(f);
                return !f.getName().startsWith(".") && 
                       (f.isDirectory() || extensions.contains(ext));
            }

            @Override
            public String getDescription()
            {
                final StringBuilder extList = new StringBuilder();
                boolean first = true;
                for(Object o : extensions)
                {
                    if(!first)
                    {
                        extList.append(", ");
                    }
                    extList.append("*." + o);
                    first = false;
                }
                return desc + " (" + extList + ")";
            }};
    }
    
    /**
     * Add a list of file filters to a file chooser. The input is a list of 
     * extension/description pairs, for example:
     * 
     * <pre>{@code
     * addFileFilters(chooser, "Text Files", "txt", "Web Pages", "html");
     * }</pre>
     * 
     * @param chooser the file chooser
     * @param pairs list of extension/description pairs
     */
    public static void addFileFilters(JFileChooser chooser, Object... pairs)
    {
        if(pairs.length % 2 != 0)
        {
            throw new IllegalArgumentException("pairs must have even length");
        }
        for(int i = 0; i < pairs.length; i += 2)
        {
            chooser.addChoosableFileFilter(createFileFilter(pairs[i], pairs[i+1].toString()));
        }
    }
}
