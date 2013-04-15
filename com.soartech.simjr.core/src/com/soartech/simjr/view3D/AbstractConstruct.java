/*
 * Copyright (c) 2012, Soar Technology, Inc.
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
 */
package com.soartech.simjr.view3D;

import java.awt.Color;

import javax.swing.SwingConstants;

import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.Simulation;

import de.jreality.geometry.PointSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ShaderUtility;

/**
 * Abstract class to represent the 3D SceneGraphComponent that will 
 * correspond to 2D entities. 
 * 
 * @author Dan Silverglate
 */
public abstract class AbstractConstruct extends SceneGraphComponent
{
    Simulation sim;
    Entity entity = null;
    SceneGraphComponent label = null;
    
    public AbstractConstruct(String name)
    {
        super(name); 
    }
    
    public void setPosition()
    {
        
    }
    
    /**
     * Method to display the text label for this construct.  Takes the display 
     * text and relative coordinates as parameters.  Uses jRealitity's labeled 
     * vertex.
     * 
     * @param text
     * @param x
     * @param y
     * @param z
     */
    public void setupLabel(String text, double x, double y, double z)
    {
        // if the label SceneGraphComponent has not yet been created, create one.
        if (label == null)
        {
            label = new SceneGraphComponent(this.getName()+"_"+label);
            addChild(label);
            
            Appearance ap = new Appearance();
            DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, false);
            DefaultPointShader ps = (DefaultPointShader)dgs.getPointShader();
            ps.setSpheresDraw(true);
            DefaultTextShader pts = (DefaultTextShader)ps.getTextShader();
            pts.setScale(1.0);
            pts.setOffset(new double[]{0,0,0});
            pts.setAlignment(SwingConstants.LEFT);
            pts.setDiffuseColor(Color.black);
            label.setAppearance(ap);
        }
        
        PointSetFactory lf = new PointSetFactory();
        lf.setVertexCount(1);
        lf.setVertexCoordinates(new double[]{x, y, z});
        lf.setVertexLabels(new String[]{text});
        lf.update();
        label.setGeometry(lf.getPointSet());
        
        //MatrixBuilder.euclidean().translate(x, y, z).assignTo(label);
    }

    /**
     * Called when some property of the 2D entity has changed to give the 3D 
     * construct a chance to update.
     * 
     * @param entity
     */
    abstract public void updateFromEntity(Entity entity);
    
    /**
     * Called when a 2D entity changes that does not have a direct 3D construct 
     * counterpart.  The 3D construct needs to test if it is affected by this 
     * change.  For example the moving of a waypoint affect any routes and/or 
     * areas that reference that waypoint.
     * 
     * @param entity
     */
    abstract public void testAndUpdateFromEntity(Entity entity);
}
