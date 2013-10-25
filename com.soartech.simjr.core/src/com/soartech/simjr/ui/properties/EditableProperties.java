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
 * Created on Oct 5, 2007
 */
package com.soartech.simjr.ui.properties;

import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;

import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityTools;
import com.soartech.simjr.sim.entities.DamageStatus;

/**
 * @author ray
 */
public class EditableProperties
{
    private static final EditableProperty ALL_PROPS[] = 
    {
        new AboveGroundLevel(),
        new DefaultEditableProperty(EntityConstants.PROPERTY_VISIBLE),
        new DefaultEditableProperty(EntityConstants.PROPERTY_LOCKED),
        new DefaultComboBoxProperty(EntityConstants.PROPERTY_DAMAGE, DamageStatus.values()),
        new DefaultEditableProperty(EntityConstants.PROPERTY_SHAPE_LABEL_VISIBLE),
        new DefaultEditableProperty(EntityConstants.PROPERTY_SHAPE_LABEL_DISPLAYIFPARENT),
        new DefaultEditableProperty(EntityConstants.PROPERTY_VISIBLE_RANGE_VISIBLE),
        new DefaultEditableProperty(EntityConstants.PROPERTY_RADAR_VISIBLE),
        new DefaultComboBoxProperty(EntityConstants.PROPERTY_FORCE, EntityConstants.ALL_FORCES),
        new DefaultEditableProperty(EntityConstants.PROPERTY_MGRS),
        new DefaultEditableProperty(EntityConstants.PROPERTY_POSITION),
        new DefaultEditableProperty(EntityConstants.PROPERTY_VELOCITY),
    };
    
    public static void install(Map<String, EditableProperty> props)
    {
        for(EditableProperty prop : ALL_PROPS)
        {
            props.put(prop.getProperty(), prop);
        }
    }
    
    private static class DefaultEditableProperty implements EditableProperty
    {
        private String propertyName;
        
        public DefaultEditableProperty(String propertyName)
        {
            this.propertyName = propertyName;
        }
        
        public String getProperty()
        {
            return propertyName;
        }

        public Object setValue(Entity entity, Object newValue, Object oldValue)
        {
            try
            {
                entity.setProperty(getProperty(), newValue);
                return newValue;
            }
            catch(IllegalArgumentException e)
            { 
            }
            return oldValue;
        }

        public TableCellEditor getEditor()
        {
            return null;
        }
    }
    
    private static class DefaultComboBoxProperty extends DefaultEditableProperty
    {
        private Object[] values;

        public DefaultComboBoxProperty(String propertyName, Object[] values)
        {
            super(propertyName);
            
            this.values = values;
        }
        
        public TableCellEditor getEditor()
        {
            JComboBox cb = new JComboBox(values);
            return new DefaultCellEditor(cb);
        }
    }
    
    public static class AboveGroundLevel implements EditableProperty
    {
        public String getProperty()
        {
            return EntityConstants.PROPERTY_AGL;
        }

        public Object setValue(Entity entity, Object newValue, Object oldValue)
        {
            try
            {
                double agl = Double.parseDouble(newValue.toString());
                agl = Math.max(agl, 0.0);
                EntityTools.setAboveGroundLevel(entity, agl);
                return agl;
            }
            catch(NumberFormatException ex)
            {
            }
            return oldValue;
        }

        public TableCellEditor getEditor()
        {
            return null;
        }
    }
}
