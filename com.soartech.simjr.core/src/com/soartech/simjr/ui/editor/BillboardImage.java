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
 * Created on June 27, 2011
 */
package com.soartech.simjr.ui.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.SwingConstants;

import com.soartech.math.Vector3;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;
import com.soartech.simjr.sim.EntityPropertyListener;

import de.jreality.backends.label.LabelUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;

/**
 * 3D Construct to represent an entity.  Implemented as a single square 
 * polygon which constantly faces the camera but maintains a constant heading 
 * screen size. 
 * 
 * @author Dan Silverglate
 */
public class BillboardImage extends AbstractConstruct implements TransformationListener, EntityPropertyListener
{
    SceneGraphComponent billboardComp;
    SceneGraphComponent imageComp;
    Viewer viewer;
    double xPos = 0.0;
    double yPos = 0.0;
    double zPos = 0.0;
    double correctionAngle = 0.0;
    double width = 1.0;
    double height = 1.0;
    double scale = 10.0;
    ImageData imageData = null;
    double[] zero = new double[]{0.0, 0.0, 0.0, 1.0};
    double[] negZAxis = new double[]{0.0, 0.0, -1.0, 1.0};
    Matrix camMatrix = new Matrix();
    Matrix matrix = new Matrix();
    
    /**
     * Main constructor.  Needs to monitor the main camera to adjust billboard, 
     * heading, position, and scale.
     * 
     * @param cameraPath
     * @param viewer
     * @param type
     */
    public BillboardImage(SceneGraphPath cameraPath, Viewer viewer, String type)    {
        super("Billboard Image");
        
        this.viewer = viewer;
        
        IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
        
        double[][] vc = new double[][]
        {
              {-1, -1, 0},
              {1, -1, 0},
              {1, 1, 0},
              {-1, 1, 0},
              
/*              {-1, 0, 1},
              {1, 0, 1},
              {1, 0, -1},
              {-1, 0, -1},*/
        };

        double[][] tc = new double[][]{
            {0.0, 1.0},
            {1.0, 1.0},
            {1.0, 0.0},
            {0.0, 0.0},

/*                {0.0, 0.0},
                {0.0, 1.0},
                {1.0, 1.0},
                {1.0, 0.0},*/
        
        };
        
        int[][] fi = new int[][]{
          {0, 1, 2, 3}      
        };
       
        ifsf.setVertexCount(vc.length);
        ifsf.setVertexCoordinates(vc);
        ifsf.setFaceCount(fi.length);
        ifsf.setFaceIndices(fi);
        ifsf.setVertexTextureCoordinates(tc);
        ifsf.setGenerateFaceNormals(true);
        ifsf.setGenerateEdgesFromFaces(true);
        ifsf.update();
        
        billboardComp = new SceneGraphComponent("billboard");
        addChild(billboardComp);
        
        imageComp = new SceneGraphComponent("image");
        imageComp.setGeometry(ifsf.getIndexedFaceSet());        
        billboardComp.addChild(imageComp);
        
        // monitor camera's transform
        cameraPath.getLastComponent().getTransformation().addTransformationListener(this);
        
        setEntityType(type, type, "other");

        rebuild();
    }
    
    /**
     * Attempts to replace image based on a change of force or type.
     * 
     * @param type
     * @param parentType
     * @param forceType
     */
    private void setEntityType(String type, String parentType, String forceType)
    {
        Input input = null;
        
        // try grabbing image for type
        try
        {
            input = Input.getInput("resources//simjr//images//shapes//entities//"+type+"//"+forceType+".png");
            setImageFile(input);
            return;
        }
        catch (IOException ioe) { }

        // if no image exists for type try the parent type
        try
        {
            input = Input.getInput("resources//simjr//images//shapes//entities//"+parentType+"//"+forceType+".png");
            setImageFile(input);
            return;
        }
        catch (IOException ioe) { }
        
        System.out.println("no image found for parentType(" + parentType + ") type("+type+") forceType("+forceType+")");
    }
    
    /**
     * Applies loads the image data and applies to the square polygon.  Then 
     * adjusts scale of polygon based on image dimensions.
     * 
     * @param imageFile
     */
    public void setImageFile(Input imageFile)
    {
        Appearance ap = new Appearance();
        ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
        ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
        ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(1f, 1f, 1f));
        ap.setAttribute(CommonAttributes.AMBIENT_COLOR, new Color(1f, 1f, 1f));
        ap.setAttribute(CommonAttributes.AMBIENT_COEFFICIENT, 1.0);
        
        if (imageFile != null)
        {
            try
            {
                imageData = ImageData.load(imageFile);
                TextureUtility.createTexture(ap, de.jreality.shader.CommonAttributes.POLYGON_SHADER, imageData);
                width = imageData.getWidth();
                height = imageData.getHeight();
                if (width > height)
                {
                    height = height/width;
                    width = 1.0;
                }
                else
                {
                    width = width/height;
                    height = 1.0;
                }
            }
            catch (IOException ioe)
            {
                imageData = null;
                ioe.printStackTrace();
            }
        }
        
        else
        {
            xPos = 0.0;
            zPos = 0.0;
            width = 0.0;
            height = 0.0;
            scale = 1.0;
            imageData = null;
        }
        
        setAppearance(ap);
        
        rebuild();
    }
    
    /**
     * Called to rebuild the 3D construct whenever a field has changed. 
     */
    private void rebuild()
    {
        if (imageData != null)
        {
            MatrixBuilder.euclidean().translate(xPos, yPos, zPos).assignTo(this);
            MatrixBuilder.euclidean().rotateZ(correctionAngle).scale(width*scale, height*scale, 1.0).assignTo(imageComp);
        }
        else
        {
            MatrixBuilder.euclidean().scale(0.0).assignTo(imageComp);
        }
      
    }
    
    /* (non-Javadoc)
     * @see de.jreality.scene.event.TransformationListener#transformationMatrixChanged(de.jreality.scene.event.TransformationEvent)
     */
    public void transformationMatrixChanged(TransformationEvent ev)
    {
        if (entity == null) return;
        
        // determine current transform of camera
        camMatrix.assignFrom(ev.getTransformationMatrix());

        // calculate the direction the camera is pointing
        double[] camNormal = Rn.subtract(null, camMatrix.multiplyVector(negZAxis), camMatrix.multiplyVector(zero));

        // calculate the transform matrix from this billboard to the camera
        try
        {
            matrix.assignFrom(this.getTransformation().getMatrix());
        }
        catch (NullPointerException npe)
        {
            return;
        }
        matrix.invert();
        camMatrix.multiplyOnLeft(matrix.getArray());
        
        // calculate the rotation needed to billboard toward the camera
        FactoredMatrix lookAtMatrix = new FactoredMatrix(
                LabelUtility.calculateBillboardMatrix(
                        null,
                        1.0,
                        1.0,
                        zero,
                        SwingConstants.CENTER,
                        camMatrix.getArray(),
                        zero,
                        Pn.EUCLIDEAN
                )
        );
        
        MatrixBuilder.euclidean().rotate(lookAtMatrix.getRotationAngle(), lookAtMatrix.getRotationAxis()).assignTo(billboardComp);
        
        // calculate the distance from the billboard to the plane of the camera's view (screen) to scale the billboard image
        double distance = Rn.innerProduct(camNormal, camMatrix.multiplyVector(zero));
        if (distance < 0) distance = -distance;

        this.scale = distance/15;

        // get the current position and heading of entity
        Vector3 position = entity.getPosition();
        xPos = position.x;
        yPos = position.z;
        zPos = -position.y;
        
        double heading = entity.getHeading();

        // get the screen aspect ratio
        Dimension dim = viewer.getViewingComponentSize();
        double ratio  = dim.width / dim.height;

        // determine 3D coordinates of the entity and its heading vector 
        Graphics3D g = new Graphics3D(viewer);
        Matrix w2ndc = new Matrix(g.getWorldToNDC());
        double[] o = new double[]{xPos, yPos, zPos, 1};
        double[] x = new double[]{100*Math.cos(heading)+xPos, yPos, -100*Math.sin(heading)+zPos, 1};
        
        // project 3D coordinates to screen coordinates
        o = w2ndc.multiplyVector(o);
        x = w2ndc.multiplyVector(x);

        // calculate the screen angle (roll angle in the billboard's plane) needed to match the 3D angle 
        double deltaX = (x[0]/x[3] - o[0]/o[3]) * ratio;
        double deltaY = (x[1]/x[3] - o[1]/o[3]);
        
        this.correctionAngle = Math.atan2(deltaY, deltaX);
        
        rebuild();
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.editor.AbstractConstruct#updateFromEntity(com.soartech.simjr.sim.Entity)
     */
    public void updateFromEntity(Entity entity)
    {
        if (this.entity != entity)
        {
            if (entity != null)
            {
                entity.removePropertyListener(this);
            }
            this.entity = entity;
            entity.addPropertyListener(this);
        }
        Vector3 pos = entity.getPosition();
        xPos = pos.x;
        yPos = pos.z;
        zPos = -pos.y;

        String type = entity.getPrototype().getId();
        String parentType = entity.getPrototype().getCategory();
        String forceType = (String)entity.getProperty(EntityConstants.PROPERTY_FORCE);
        setEntityType(type, parentType, forceType);
        
        setupLabel(entity.getName(), 0.0, 0.0, .01);
        
        rebuild();
    }

    public void testAndUpdateFromEntity(Entity entity) { }
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.sim.EntityPropertyListener#onPropertyChanged(com.soartech.simjr.sim.Entity, java.lang.String)
     */
    public void onPropertyChanged(Entity entity, String propertyName)
    {
        //System.out.println("onPropertyChanged("+entity.getClass().getName()+", "+propertyName+")");
        
        if (propertyName.equals(EntityConstants.PROPERTY_FORCE))
        {
            String type = entity.getPrototype().getId();
            String parentType = entity.getPrototype().getCategory();
            String forceType = entity.getProperty(EntityConstants.PROPERTY_FORCE).toString();
            setEntityType(type, parentType, forceType);
            
            rebuild();
        }
    }
}
