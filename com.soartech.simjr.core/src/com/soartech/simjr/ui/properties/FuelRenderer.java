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
 * Created on Sep 18, 2007
 */
package com.soartech.simjr.ui.properties;

/* 
 * ColorRenderer.java (compiles with releases 1.2, 1.3, and 1.4) is used by 
 * TableDialogEditDemo.java.
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import com.soartech.simjr.sim.entities.FuelModel;

public class FuelRenderer extends JPanel implements TableCellRenderer 
{
    private static final long serialVersionUID = -2346391769903882818L;
    
    Border unselectedBorder = null;
    Border selectedBorder = null;
    boolean isBordered = true;
    FuelModel fuelModel = null;

    public FuelRenderer() {
        setOpaque(true); //MUST do this for background to show up.
    }

    public Component getTableCellRendererComponent(
                            JTable table, Object value,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) 
    {
        fuelModel = (FuelModel) value;
        
        setBackground(Color.WHITE);
        if (isBordered) {
            if (isSelected) {
                if (selectedBorder == null) {
                    selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                              table.getSelectionBackground());
                }
                setBorder(selectedBorder);
            } else {
                if (unselectedBorder == null) {
                    unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                              table.getBackground());
                }
                setBorder(unselectedBorder);
            }
        }
        
        setToolTipText(fuelModel.toString());
        
        return this;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        
        double percent = fuelModel.getLevel() / fuelModel.getCapacity();
        if(percent > .30)
        {
            g.setColor(Color.GREEN);
        }
        else if(percent > .15)
        {
            g.setColor(Color.YELLOW);
        }
        else
        {
            g.setColor(Color.RED);
        }
        
        final int pad = 2;
        int width = getWidth();
        int height = getHeight();
        int total = (int) (percent * (width - 2 * pad));
        g.fillRect(pad, pad, total, height - 2 * pad);
        
        boolean empty = fuelModel.isEmpty();
        g.setColor(empty ? Color.GRAY : Color.BLACK);
        g.drawRect(pad, pad, width - 2 * pad, height - 2 * pad);
        
        FontMetrics fm = g.getFontMetrics();
        
        if(!empty)
        {
            g.drawString("e", pad + 2, pad + fm.getAscent() - 1);
            g.drawString("f", width - (pad + fm.charWidth('f')), pad + fm.getAscent());
        }
        else
        {
            g.drawString("empty", pad + 2, pad + fm.getAscent() - 1);
        }
    }
    
    
}
