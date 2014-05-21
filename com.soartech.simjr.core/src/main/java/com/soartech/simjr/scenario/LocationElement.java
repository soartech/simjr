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

import java.text.DecimalFormat;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.jdom.Element;
import org.jdom.xpath.XPath;

import com.soartech.math.geotrans.Geodetic;
import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.scenario.model.ModelChangeEvent;
import com.soartech.simjr.scenario.model.ModelElement;

/**
 * @author ray
 */
public class LocationElement implements ModelElement
{
    public static final String LOCATION = LocationElement.class.getCanonicalName() + ".location";
    
    private final ModelElement parent;
    private final XPath latPath;
    private final XPath lonPath;
    private final XPath altPath;
    
    public static Element buildDefault(Model model)
    {
        Element root = model.newElement("location");
        Element lla = model.newElement("lla");
        root.addContent(lla);
        lla.setAttribute("latitude", "0.0", Model.NAMESPACE);
        lla.setAttribute("longitude", "0.0", Model.NAMESPACE);
        lla.setAttribute("altitude", "0.0", Model.NAMESPACE);
        
        return root;
    }
    
    /**
     * @param entity
     */
    public LocationElement(ModelElement entity)
    {
        this.parent = entity;
        this.latPath = this.parent.getModel().newXPath("simjr:location/simjr:lla/@simjr:latitude");
        this.lonPath = this.parent.getModel().newXPath("simjr:location/simjr:lla/@simjr:longitude");
        this.altPath = this.parent.getModel().newXPath("simjr:location/simjr:lla/@simjr:altitude");
    }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.scenario.ModelElement#getElement()
     */
    public Element getElement()
    {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.scenario.ModelElement#getModel()
     */
    public Model getModel()
    {
        return getParent().getModel();
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.scenario.ModelElement#getParent()
     */
    public ModelElement getParent()
    {
        return parent;
    }

    public double getLatitude()
    {
        return this.parent.getModel().getDouble(latPath, parent.getElement());
    }
    
    public UndoableEdit setLatitude(double degrees)
    {
        return setLocation(degrees, getLongitude(), getAltitude());
    }
    
    public double getLongitude()
    {
        return Double.valueOf(this.parent.getModel().getText(lonPath, parent.getElement()));
    }
    
    public UndoableEdit setLongitude(double degrees)
    {
        return setLocation(getLatitude(), degrees, getAltitude());
    }
    
    public double getAltitude()
    {
        return Double.valueOf(this.parent.getModel().getText(altPath, parent.getElement()));        
    }
    
    public UndoableEdit setAltitude(double meters)
    {
        return setLocation(getLatitude(), getLongitude(), meters);
    }
    
    public UndoableEdit setLocation(double latDegrees, double lonDegrees, double altitude)
    {
        final Model model = this.parent.getModel();
        final Element context = parent.getElement();
        
        final double oldLat = getLatitude();
        final double oldLon = getLongitude();
        final double oldAlt = getAltitude();
                
        boolean changed = model.setText(latPath, context, Double.toString(latDegrees), null);
        changed = model.setText(lonPath, context, Double.toString(lonDegrees), null) | changed;        
        changed = model.setText(altPath, context, Double.toString(altitude), null) | changed;
        
        UndoableEdit edit = null;
        if(changed)
        {
            model.fireChange(newEvent(LOCATION));
            edit = new ChangeLocationEdit(oldLat, oldLon, oldAlt);
        }
        return edit;
    }
    
    public UndoableEdit setLocation(Geodetic.Point lla)
    {
        return setLocation(Math.toDegrees(lla.latitude), Math.toDegrees(lla.longitude), lla.altitude);
    }
    
    public Geodetic.Point toRadians()
    {
        Geodetic.Point p = new Geodetic.Point();
        p.latitude = Math.toRadians(getLatitude());
        p.longitude = Math.toRadians(getLongitude());
        p.altitude = getAltitude();
        return p;
    }
    
    public String toString()
    {
        DecimalFormat df = new DecimalFormat("#.####");
        
        return '(' + df.format(getLatitude()) + ", " + df.format(getLongitude()) + ')';
    }
    
    private ModelChangeEvent newEvent(String prop)
    {
        return new ModelChangeEvent(this.parent.getModel(), this, prop);
    }
    
    private class ChangeLocationEdit extends AbstractUndoableEdit
    {
        private static final long serialVersionUID = -8050360351980227293L;

        private final double oldLat, oldLon, oldAlt;
        private final double newLat, newLon, newAlt;
        
        public ChangeLocationEdit(double oldLat, double oldLon, double oldAlt)
        {
            this.oldLat = oldLat;
            this.oldLon = oldLon;
            this.oldAlt = oldAlt;
            this.newLat = getLatitude();
            this.newLon = getLongitude();
            this.newAlt = getAltitude();
        }

        /* (non-Javadoc)
         * @see javax.swing.undo.AbstractUndoableEdit#redo()
         */
        @Override
        public void redo() throws CannotRedoException
        {
            super.redo();
            setLocation(newLat, newLon, newAlt);
        }

        /* (non-Javadoc)
         * @see javax.swing.undo.AbstractUndoableEdit#undo()
         */
        @Override
        public void undo() throws CannotUndoException
        {
            super.undo();
            setLocation(oldLat, oldLon, oldAlt);
        }
        
        
    }

}
