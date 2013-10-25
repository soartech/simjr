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
 * Created on Oct 13, 2009
 */
package com.soartech.simjr.util;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Simple dialog that shows a list with checkboxes that allows multiple objects
 * to be selected.
 * 
 * @author ray
 */
public class MultiSelectDialog extends JDialog
{
    private static final long serialVersionUID = -2750983942866294579L;
    
    private JList jList;
    
    private Object[] result;
    
    
    public static Object[] select(Frame owner, String title, Object[] objects)
    {
        return select(owner, title, objects, new Object[]{});
    }
    
    public static Object[] select(Frame owner, String title, Object[] objects, Object[] selected)
    {
        final Object[] sorted = Arrays.copyOf(objects, objects.length);
        Arrays.sort(sorted, new Comparator<Object>()
        {
            public int compare(Object o1, Object o2)
            {
                return o1.toString().compareTo(o2.toString());
            }
        });
        
        final MultiSelectDialog dialog = new MultiSelectDialog(owner, title, sorted, selected);
        dialog.setSize(320, 320);
        if(owner != null)
        {
            final Rectangle frame = owner.getBounds();
            dialog.setLocation(frame.x + (frame.width - dialog.getWidth()) / 2, frame.y + (frame.height - dialog.getHeight()) / 2);
        }
        dialog.setVisible(true);
        return dialog.result;
    }
    
    private MultiSelectDialog(Frame owner, String title, Object[] objects, Object[] selected)
    {
        super(owner, title, true);
        
        jList = new JList(objects);

        List<Object> selectedList = new ArrayList<Object>();
        for(int i = 0; i < selected.length; i++)
        {
            selectedList.add(selected[i]);
        }
        
        int[] indices = new int[selected.length];
        Arrays.fill(indices, -1);
        int index = 0;
        for (int i = 0; i < objects.length; i++) 
        {
            Object elementAt = objects[i];
            if(selectedList.contains(elementAt)) 
            {
                indices[index++] = i;
            }
        }
        
        jList.setSelectedIndices(indices);
        
        final JPanel panel = new JPanel(new BorderLayout());
        setContentPane(panel);
        
        getContentPane().add(new JScrollPane(jList), BorderLayout.CENTER);
        
        final JPanel buttons = new JPanel();
        final JButton ok = new JButton(new AbstractAction("Ok")
        {
            private static final long serialVersionUID = -159062856422098646L;

            public void actionPerformed(ActionEvent e)
            {
                result = jList.getSelectedValues();
                MultiSelectDialog.this.dispose();
            }
        });
        buttons.add(ok);
        buttons.add(new JButton(new AbstractAction("Cancel")
        {
            private static final long serialVersionUID = -3581759102146366333L;

            public void actionPerformed(ActionEvent e)
            {
                result = null;
                MultiSelectDialog.this.dispose();
            }
        }));
        
        getContentPane().add(buttons, BorderLayout.SOUTH);
        
        getRootPane().setDefaultButton(ok);
    }
    
    public static void main(String[] args)
    {
        final Object[] result = select(null, "Hello", new Object[] {"hi", "bye"}, new Object[] {"bye"});
        System.out.println(result != null ? Arrays.asList(result) : "cancel");
    }
}
