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
 * Created on Nov 13, 2008
 */
package com.soartech.simjr.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.soartech.simjr.radios.RadioHistory;
import com.soartech.simjr.radios.RadioHistoryListener;
import com.soartech.simjr.radios.RadioMessage;

/**
 * @author ray
 */
public class RadioMessageTableModel extends AbstractTableModel
{
    private static final long serialVersionUID = -7075665087263753820L;

    private static enum Columns { Icon {

        @Override
        public String toString()
        {
            return "";
        }}, Time, From, To, Freq, Content };
    
    private final RadioHistory history;
    private final List<RadioMessage> messages = new ArrayList<RadioMessage>();
    private final Listener listener = new Listener();
    
    /**
     * 
     */
    public RadioMessageTableModel(RadioHistory history)
    {
        this.history = history;
        this.messages.addAll(this.history.getMessages());
        this.history.addListener(listener);
    }

    public void clear()
    {
        this.messages.clear();
        fireTableDataChanged();
    }
    
    public void dispose()
    {
        this.history.removeListener(listener);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int column)
    {
        return Columns.values()[column].toString();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return Columns.values().length;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
        return messages.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        final RadioMessage m = messages.get(rowIndex);
        final Columns c = Columns.values()[columnIndex];
        switch(c)
        {
        case Icon:    return "";
        case Time:    return Math.round(m.getTime() * 100) / 100.0;
        case Content: return m.getContent();
        case Freq:    return m.getFrequency();
        case From:    return m.getSource();
        case To:      return m.getTarget();
        default:      throw new IllegalStateException("Unknown column: " + c);
        }
    }

    private void handleRadioMessage(RadioMessage message)
    {
        messages.add(message);
        fireTableRowsInserted(messages.size() - 1, messages.size() -  1);
    }
    
    private class Listener implements RadioHistoryListener
    {
        public void onRadioMessage(final RadioMessage message)
        {
            SwingUtilities.invokeLater(new Runnable() { public void run() { handleRadioMessage(message); }});
        }
    }
}
