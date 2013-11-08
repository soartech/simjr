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
 * Created on Aug 2, 2007
 */
package com.soartech.simjr.ui.shapes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.soartech.math.Vector3;
import com.soartech.shapesystem.LineStyle;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.shapes.Frame;
import com.soartech.shapesystem.shapes.ImageShape;
import com.soartech.shapesystem.shapes.Line;
import com.soartech.simjr.sim.Detonation;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.SimulationListenerAdapter;

/**
 * Manages display of detonation shapes in the UI.
 * 
 * @author ray
 */
public class DetonationShapeManager
{
    private static final Logger logger = Logger.getLogger(DetonationShapeManager.class);
    
    private Simulation sim;
    private TimedShapeManager timedShapes;
    private List<Detonation> pendingDetonations =
        Collections.synchronizedList(new ArrayList<Detonation>());
    private int nextId = 0;
    private Listener listener = new Listener();
    
    /**
     * @param sim
     * @param timeShapes
     */
    public DetonationShapeManager(Simulation sim, TimedShapeManager timeShapes)
    {
        this.sim = sim;
        this.timedShapes = timeShapes;
        
        sim.addListener(listener);
    }

    public void update()
    {
        synchronized(pendingDetonations)
        {
            for(Detonation d : pendingDetonations)
            {
                handleDetonation(d);
            }
            pendingDetonations.clear();
        }
    }
    
    public void dispose()
    {
        this.sim.removeListener(listener);
    }
    
    private void handleDetonation(Detonation d)
    {
        Vector3 pos = d.getLocation();
        pos = EntityShape.adjustPositionForShadow(pos, sim.getTerrain().toGeodetic(pos).altitude);
        Frame frame = new Frame("__detonation" + ++nextId, 
                                EntityConstants.LAYER_AIR,
                                Position.createWorldMeters(pos.x, pos.y),
                                Rotation.IDENTITY);
        ImageShape image = new ImageShape(frame.getName() + ".explosion", 
                EntityConstants.LAYER_AIR, 
                Position.createRelativeMeters(0, 0, frame.getName()),
                Rotation.IDENTITY, 
                Scalar.createPixel(EntityShapeManager.explosion.getIconWidth()),
                Scalar.createPixel(EntityShapeManager.explosion.getIconHeight()), 
                "explosion", null);
        
        double time = sim.getTime() + 5.0;
        timedShapes.addShape(time, frame);
        timedShapes.addShape(time, image);
        
        showDetonationSourceIndicator(d, time);
        
    }
    
    private void showDetonationSourceIndicator(Detonation d, double time)
    {
        if(d.getTarget() == null)
        {
            logger.warn("Don't know how to make detonation source indicator when target is null");
            return;
        }
        
        // Add a dashed red line from the detonation source to the detonation location.
        ShapeStyle style = new ShapeStyle();
        style.setLineColor(Color.RED);
        style.setLineStyle(LineStyle.DASHED);
        style.setLineThickness(Scalar.createPixel(3));
        style.setOpacity(0.5f);
        
        Line line = new Line(d.getWeapon().getName() + "." + d.getTarget().getName() + ++nextId, 
                             EntityConstants.LAYER_AREA,
                             style,
                             EntityShape.getBodyFrameName(d.getWeapon().getEntity()),
                             EntityShape.getBodyFrameName(d.getTarget()));
        
        timedShapes.addShape(time, line);
    } 
    
    private class Listener extends SimulationListenerAdapter
    {
        /* (non-Javadoc)
         * @see com.soartech.simjr.SimulationAdapter#onDetonation(com.soartech.simjr.Detonation)
         */
        @Override
        public void onDetonation(Detonation detonation)
        {
            pendingDetonations.add(detonation);
        }
    }
}
