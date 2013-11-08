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
import java.io.IOException;

import com.soartech.math.Vector3;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;

/**
 * 3D Construct to represent a Terrain Image.  Implemented as a textured 
 * rectangular polygon.  
 * 
 * @author Dan Silverglate
 */
public class ImagePoly extends SceneGraphComponent 
{
    double xPos = 0.0;
    double zPos = 0.0;
    double width = 1.0;
    double height = 1.0;
    double metersPerPixel = 1.0;
    ImageData imageData = null;
    
    /**
     * Main constuctor.
     * 
     */
    public ImagePoly()
    {
        super("Terrain Image");
        
        IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
        
        double[][] vc = new double[][]
        {
            {-.5, 0.0, .5},
            {.5, 0.0, .5},
            {.5, 0.0, -.5},
            {-.5, 0.0, -.5},
        };

        double[][] tc = new double[][]{
            {0.0, 1.0},
            {1.0, 1.0},
            {1.0, 0.0},
            {0.0, 0.0},
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
        setGeometry(ifsf.getIndexedFaceSet());
        
        //this.element = element;
        
        //rebuild();
    }
    
    /**
     * Scales the polygon when the 2D's metersPerPixel parameter changes
     * 
     * @param metersPerPixel
     */
    public void setMetersPerPixel(double metersPerPixel)
    {
        this.metersPerPixel = metersPerPixel;
        if (imageData != null)
        {
            width = imageData.getWidth() * metersPerPixel;
            height = imageData.getHeight() * metersPerPixel;
            rebuild();
        }
    }
    
    /**
     * Sets the position based on the 2D position.
     * 
     * @param pos
     */
    public void setPosition(Vector3 pos)
    {
        //System.out.println("setPosition("+pos+")");
        xPos = pos.x;
        zPos = -pos.y;
        
        rebuild();
    }
    
    /**
     * Attempts to load the image file and apply it to the polygon. Then 
     * adjusts scale of polygon based on image dimensions.
     * 
     * @param imageFile
     */
    public void setImageFile(String imageFile)
    {
        Appearance ap = new Appearance();
        //DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
        //dgs.setShowLines(false);
        //dgs.setShowPoints(false);
        //DefaultPolygonShader dps = (DefaultPolygonShader)dgs.createPolygonShader("default");
        //dps.setDiffuseColor(Color.white);
        //dps.setAmbientColor(Color.white);
        //dps.setAmbientCoefficient(1.0);
        ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
        ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
        ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(1f, 1f, 1f));
        ap.setAttribute(CommonAttributes.AMBIENT_COLOR, new Color(1f, 1f, 1f));
        ap.setAttribute(CommonAttributes.AMBIENT_COEFFICIENT, 1.0);
        
        // 
        if (imageFile != null)
        {
            try
            {
                imageData = ImageData.load(Input.getInput(imageFile));
                TextureUtility.createTexture(ap, de.jreality.shader.CommonAttributes.POLYGON_SHADER, imageData);
                width = imageData.getWidth() * metersPerPixel;
                height = imageData.getHeight() * metersPerPixel;
                //System.out.println(imageFile+"("+width+", "+height+") X "+metersPerPixel);
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
            metersPerPixel = 1.0;
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
            MatrixBuilder trans = MatrixBuilder.euclidean();
            trans.scale(width, 1, height);
            trans.translate(xPos/width, -1, zPos/height);
            setTransformation(new Transformation(trans.getArray()));
        }
        else
        {
            MatrixBuilder trans = MatrixBuilder.euclidean();
            trans.scale(0.0);
            setTransformation(new Transformation(trans.getArray()));
        }
      
    }
}
