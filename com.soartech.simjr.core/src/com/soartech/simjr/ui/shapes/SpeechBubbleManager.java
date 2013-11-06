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

import com.soartech.shapesystem.FillStyle;
import com.soartech.shapesystem.Position;
import com.soartech.shapesystem.Rotation;
import com.soartech.shapesystem.Scalar;
import com.soartech.shapesystem.ShapeStyle;
import com.soartech.shapesystem.TextStyle;
import com.soartech.shapesystem.shapes.ArrowLine;
import com.soartech.shapesystem.shapes.Frame;
import com.soartech.shapesystem.shapes.ImageShape;
import com.soartech.shapesystem.shapes.Text;
import com.soartech.simjr.radios.RadioHistory;
import com.soartech.simjr.radios.RadioHistoryListener;
import com.soartech.simjr.radios.RadioMessage;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.ui.SimulationImages;

/**
 * Manages display of speech bubbles in the UI
 * 
 * @author ray
 */
public class SpeechBubbleManager
{
    private static final Logger logger = Logger.getLogger(SpeechBubbleManager.class);
    private static final ShapeStyle speechBubbleStyle = new ShapeStyle().setOpacity(0.8f);
    
    private Simulation sim;
    private RadioHistory history;
    private EntityShapeManager shapes;
    private final List<RadioMessage> pendingRadioMessages = Collections.synchronizedList(new ArrayList<RadioMessage>());
    private RadioListener radioListener = new RadioListener();
    
    /**
     * @param sim
     * @param timeShapes
     */
    public SpeechBubbleManager(Simulation sim, RadioHistory history, EntityShapeManager shapes)
    {
        this.sim = sim;
        this.history = history;
        this.shapes = shapes;
        
        this.history.addListener(radioListener);
    }

    public void update()
    {
        synchronized (pendingRadioMessages)
        {
            for(RadioMessage message : pendingRadioMessages)
            {
                Entity sourceEntity = sim.getEntity(message.getSource());
                Entity targetEntity = sim.getEntity(message.getTarget());
                if(sourceEntity != null)
                {
                    EntityShape es = shapes.getEntityShape(sourceEntity);
                    if(es != null)
                    {
                        createRadioMessageIndicator(message, es, shapes.getEntityShape(targetEntity));
                    }
                }
            }
            pendingRadioMessages.clear();
        }
    }
    
    public void dispose()
    {
        history.removeListener(radioListener);
    }
    
    private void createRadioMessageIndicator(RadioMessage message, EntityShape source, EntityShape target)
    {
        final String name = source.getRootFrame().getName();
        if(shapes.getShapeSystem().getShape(name + ".speechFrame") != null)
        {
            return;
        }
        Frame frame = new Frame(name + ".speechFrame", EntityConstants.LAYER_LABELS, 
                Position.createRelativePixel(0, 0, source.getPrimaryDisplayShape()),
                Rotation.IDENTITY);
        
        ImageShape image = new ImageShape(name + ".speech", 
                                EntityConstants.LAYER_LABELS,
                                Position.createRelativePixel(0, 20, frame.getName()),
                                Rotation.IDENTITY,
                                Scalar.createPixel(SimulationImages.SPEECH_BUBBLE.getIconWidth()),
                                Scalar.createPixel(SimulationImages.SPEECH_BUBBLE.getIconHeight()),
                                "speechBubble",
                                speechBubbleStyle);
        
        double time = sim.getTime() + 5.0;
        shapes.getTimedShapes().addShape(time, frame, image);
        
        if(target != null)
        {
            showRadioMessageTargetIndicator(message, source, target, time);
            
            // TODO: I think we should show this all the time
            showMessageText(message, source, time, name + ".speech.label");
        }
        
    }
    private void showRadioMessageTargetIndicator(RadioMessage message, EntityShape source, EntityShape target, double time)
    {
        if(target == null)
        {
            logger.warn("Don't know how to make detonation graphic when target is null");
            return;
        }
        
        // Add a dashed red line from the detonation source to the detonation location.
        ShapeStyle style = new ShapeStyle();
        style.setFillStyle(FillStyle.FILLED);
        style.setFillColor(Color.YELLOW);
        style.setLineColor(Color.YELLOW);
        //style.lineStyle = LineStyle.LineDashed;
        style.setLineThickness(Scalar.createPixel(4.0));
        style.setOpacity(0.7f);
        
        String name = source.getEntity().getName() + "." + target.getEntity().getName() + ".speechLine" + "." + time;
        ArrowLine line = new ArrowLine(name, 
                             EntityConstants.LAYER_AREA,
                             style,
                             source.getPrimaryDisplayShape(),
                             target.getPrimaryDisplayShape());
        shapes.getTimedShapes().addShape(time, line);
    }

    /**
     * @param message
     * @param source
     * @param time
     * @param style
     * @param name
     */
    private void showMessageText(RadioMessage message, EntityShape source,
            double time, String name)
    {
        //        labelFrame = new Frame(name + ".labelFrame", EntityConstants.LAYER_LABELS, 
        //                Position.createRelativePixel(0, 0, relative),
        //                Rotation.IDENTITY);
                TextStyle labelStyle = new TextStyle();
                labelStyle.setFillStyle(FillStyle.FILLED);
                labelStyle.setFillColor(Color.YELLOW);
                labelStyle.setOpacity(0.7f);
                Text label = new Text(name + ".label", EntityConstants.LAYER_LABELS,
                         Position.createRelativePixel(32, 20, source.getPrimaryDisplayShape()),
                         Rotation.IDENTITY,
                         labelStyle,
                         message.getContent());
                
                shapes.getTimedShapes().addShape(time, label);
    } 

    
    private class RadioListener implements RadioHistoryListener
    {
        public void onRadioMessage(RadioMessage message)
        {
            // RadioMessage is immutable, so it's safe to access from any thread
            pendingRadioMessages.add(message);
        }
    }
}
