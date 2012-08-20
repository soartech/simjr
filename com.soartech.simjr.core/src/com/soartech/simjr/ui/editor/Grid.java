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

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;

/**
 * @author Dan Silverglate
 */
public class Grid extends SceneGraphComponent 
{
    public Grid(int xWidth, int zDepth, double spacing)    {
        super("Grid");

        IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
        
        double[][] vc = new double[(xWidth+1)*(zDepth+1)][3];
        
        int v = 0;
        
        for (int z=0; z<zDepth+1; z++)
        {
            double zValue = (zDepth/2.0 - z) * spacing;
            for (int x=0; x<xWidth+1; x++)
            {
                vc[v][0] = (x - xWidth/2.0) * spacing;
                vc[v][1] = 0;
                vc[v][2] = zValue;
                ++v;
            }
        }
        
        int[][] fi = new int[xWidth * zDepth][4];
        
        v = 0;
        int f = 0;
        for (int z=0; z<zDepth; z++)
        {
            for (int x=0; x<xWidth; x++)
            {
                v = f + z;
                fi[f][0] = v;
                fi[f][1] = v+1;
                fi[f][2] = v + xWidth + 2;
                fi[f][3] = v + xWidth + 1;
                ++f;
            }
        }
        
        ifsf.setVertexCount(vc.length);
        ifsf.setVertexCoordinates(vc);
        ifsf.setFaceCount(fi.length);
        ifsf.setFaceIndices(fi);
        ifsf.setGenerateFaceNormals(true);
        ifsf.setGenerateEdgesFromFaces(true);
        ifsf.update();
        setGeometry(ifsf.getIndexedFaceSet());
        
        Appearance ap = new Appearance();
        ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
        ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(1f, 1f, 1f));
        ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
        ap.setAttribute(CommonAttributes.TRANSPARENCY, .5);
        ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
        ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
        ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.black);
        
        setAppearance(ap);
    }
}
