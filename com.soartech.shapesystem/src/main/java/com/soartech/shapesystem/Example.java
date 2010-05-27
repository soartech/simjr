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
 * Created on May 21, 2007
 */
package com.soartech.shapesystem;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.soartech.math.Vector3;
import com.soartech.shapesystem.shapes.Arc;
import com.soartech.shapesystem.shapes.Box;
import com.soartech.shapesystem.shapes.Circle;
import com.soartech.shapesystem.shapes.Frame;
import com.soartech.shapesystem.shapes.ImageShape;
import com.soartech.shapesystem.shapes.Line;
import com.soartech.shapesystem.shapes.Text;
import com.soartech.shapesystem.swing.SwingPrimitiveRendererFactory;

public class Example extends JPanel
{
    private static final long serialVersionUID = 7601974801407434992L;

    private SwingPrimitiveRendererFactory factory = new SwingPrimitiveRendererFactory(new Transformer());
    private ShapeSystem system = new ShapeSystem();
    private Box box = new Box("box", "",
            new Position("boxFrame"), 
            Rotation.createRelative("boxFrame"), new ShapeStyle().setFillStyle(FillStyle.FILLED), 
             new Scalar(50.0), new Scalar(50.0));
    private Text label = new Text("box.label", "",
            Position.createRelativePixel(25, 50, "box"), 
             Rotation.IDENTITY, new TextStyle(), "A Box");
    private Frame boxFrame = new Frame("boxFrame", "", 
            Position.createWorldPixel(1, 1),
            Rotation.IDENTITY);
    private ImageShape shadow = new ImageShape("shadow", "top", 
                Position.createRelativePixel(0, 0, "boxFrame"),
                Rotation.createRelative("boxFrame"),
                Scalar.createPixel(20), Scalar.createPixel(20), "shadow", null);
    private Arc arc = new Arc("arc", "", Position.createWorldPixel(300, 50), 
                              Rotation.createPointAt("boxFrame"),
                              new ShapeStyle(),
                              new Scalar(140.0),
                              Rotation.fromDegrees(45, RotationType.WORLD),
                              Rotation.fromDegrees(-45, RotationType.WORLD));
    
    private void updateFrame(double theta)
    {
        double x = 100.0 * Math.cos(theta) + 300.0;
        double y = 100.0 * Math.sin(theta) + 300.0;
        boxFrame.setPosition(Position.createWorldPixel(x, y));
        boxFrame.setRotation(Rotation.fromRadians(theta, RotationType.WORLD));
        
        repaint();
    }
    
    public Example()
    {
        setBackground(Color.WHITE);
        
        system.getLayer("top").setZorder(5);
        factory.loadImage(getClass().getClassLoader(), "shadow", "images/explosion.gif");
        
        Circle circle = new Circle("circle", "", 
                                new Position(PositionType.WORLD, new Scalar(400), new Scalar(300)), 
                                                Rotation.IDENTITY, new ShapeStyle(), 
                                                new Scalar(50.0));
        
        
        system.addShape(shadow);
        system.addShape(circle);
        system.addShape(boxFrame);
        system.addShape(box);
        system.addShape(label);
        system.addShape(arc);
        
        Line line = new Line("line", "", new ShapeStyle(), "circle", "box");
        
        system.addShape(line);
        
        new Thread(new Runnable() {

            public void run()
            {
                double theta = 0.0;
                while(true)
                {
                    theta += Math.toRadians(5.0);
                    if(theta > 2 * Math.PI)
                    {
                        theta -= 2 * Math.PI;
                    }
                    
                    final double finalTheta = theta;
                    try
                    {
                    SwingUtilities.invokeAndWait(new Runnable() {
    
                        public void run()
                        {
                            updateFrame(finalTheta);
                            
                        }});
                    
                        Thread.sleep(50);
                    }
                    catch (InterruptedException e)
                    {
                    }
                    catch (InvocationTargetException e)
                    {
                        e.printStackTrace();
                    }
                }
                
            }}).start();
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        factory.setGraphics2D(g2d, getWidth(), getHeight()); 
        
        system.update(factory.getTransformer());
        system.draw(factory);
        system.displayDebugging(factory, factory.getTransformer());
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Shape System Example");
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setSize(800, 600);
        frame.getContentPane().add(new Example());
        
        frame.setVisible(true);
    }

    private static class Transformer implements CoordinateTransformer
    {
        public SimplePosition metersToScreen(double x, double y)
        {
            return new SimplePosition(y, x);
        }

        public double metersYToScreen(double y)
        {
            return y;
        }

        public double scalarToPixels(Scalar x)
        {
            return x.getValue();
        }

        public boolean supportsSingleWorldCoordinates()
        {
            return true;
        }

        public double metersXToScreen(double x)
        {
            return x;
        }

        public Vector3 screenToMeters(double x, double y)
        {
            return null;
        }
        
    }
}
