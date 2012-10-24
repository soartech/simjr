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

import java.util.Vector;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;

/**
 * @author Dan Silverglate
 */
public abstract class ExtrudedPolygon extends AbstractConstruct
{
    SceneGraphComponent endCapComp;
    double minAltitude = 0.0;
    double maxAltitude = 100.0;
    
    public ExtrudedPolygon(String name)
    {
        super(name);
    }

    public double[][] cleanPath(double[][] path)
    {
       //This check fixes a race condition where sometimes the area is constructed before points are actually added.  ~ Josh Haley
        if(path.length ==0)
            return new double[0][0];
        
        int numSides;
        if (path[0][0] == path[path.length-1][0]
         && path[0][1] == path[path.length-1][1])
        {
            numSides = path.length - 1;
        }
        else
        {
            numSides = path.length; 
        }
        
        double[][] newPath = new double[numSides][2];
        for (int i=0; i<newPath.length; i++)
        {
            newPath[i][0] = path[i][0];
            newPath[i][1] = path[i][1];
        }
        return newPath;
    }
    
    public IndexedFaceSetFactory buildSides(double[][] path, double minHeight, double maxHeight)
    {
        return buildSides(path, minHeight, maxHeight, false);
    }

    public IndexedFaceSetFactory buildSides(double[][] path, double minHeight, double maxHeight, boolean smooth)
    {   
        IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
        
        int numSides = path.length;
        
        double[][] vc = new double[numSides * 2][3];
   
        for (int i=0; i<numSides; i++)
        {     
            vc[i][0] = path[i][0];
            vc[i][1] = minHeight;
            vc[i][2] = path[i][1];
            
            vc[i + numSides][0] = path[i][0];
            vc[i + numSides][1] = maxHeight;
            vc[i + numSides][2] = path[i][1];
        }

        int[][] fi = new int[numSides][];
        for (int i=0; i<numSides-1; i++)
        {     
            fi[i] = new int[4];
            fi[i][0] = i;
            fi[i][1] = i + 1;
            fi[i][2] = i + numSides + 1;
            fi[i][3] = i + numSides;
        }
        
        //last side
        fi[numSides-1] = new int[4];
        fi[numSides-1][0] = numSides - 1;
        fi[numSides-1][1] = 0;
        fi[numSides-1][2] = numSides;
        fi[numSides-1][3] = numSides + numSides - 1;
        
        ifsf.setVertexCount(vc.length);
        ifsf.setVertexCoordinates(vc);
        ifsf.setFaceCount(fi.length);
        ifsf.setFaceIndices(fi);
        ifsf.setGenerateFaceNormals(true);
        if (smooth) ifsf.setGenerateVertexNormals(true);
        ifsf.setGenerateEdgesFromFaces(true);
        ifsf.update();
        
        return ifsf;
    }
    
    public IndexedFaceSetFactory buildEndCapFaces(double[][] path, double minHeight, double maxHeight)
    {
        return buildEndCapFaces(path, minHeight, maxHeight, true);
    }

    public IndexedFaceSetFactory buildEndCapFaces(double[][] path, double minHeight, double maxHeight, boolean triangulate)
    {   
        IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();

        int numSides = path.length;
        double[][] vc = new double[numSides*2][3];
     
        for (int i=0; i<numSides; i++)
        {     
            vc[i][0] = path[i][0];
            vc[i][1] = minHeight;
            vc[i][2] = path[i][1];
            
            int j = i + numSides;
            vc[j][0] = path[i][0];
            vc[j][1] = maxHeight;
            vc[j][2] = path[i][1];
        }
        
        Vector<int[]> triangles = triangulate(path);
        
        int[][] fi;
        
        if (triangulate)
        {
            fi = new int[triangles.size()*2][];

            for (int i=0; i<triangles.size(); i++)
            {     
                fi[i] = triangles.get(i);
                
                int j = i + triangles.size();
                fi[j] = new int[3];
                fi[j][0] = fi[i][0] + numSides;
                fi[j][1] = fi[i][1] + numSides;
                fi[j][2] = fi[i][2] + numSides;
            }
        }
        else
        {
            fi = new int[2][numSides];

            for (int i=0; i<numSides; i++)
            {
                fi[0][i] = i;
                fi[1][i] = i + numSides;
            }
        }
        
        ifsf.setVertexCount(vc.length);
        ifsf.setVertexCoordinates(vc);
        ifsf.setFaceCount(fi.length);
        ifsf.setFaceIndices(fi);
        ifsf.setGenerateFaceNormals(true);
        ifsf.setGenerateEdgesFromFaces(true);
        ifsf.update();
        
        return ifsf;
    }
    
    public IndexedFaceSetFactory buildTop(double[][] path, double height)
    {   
        IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();

        double[][] vc = new double[path.length][3];
     
        for (int i=0; i<vc.length; i++)
        {     
            vc[i][0] = path[i][0];
            vc[i][1] = height;
            vc[i][2] = path[i][1];
        }
        Vector<int[]> triangles = triangulate(path);
        
        int[][] fi = new int[triangles.size()][];
        for (int i=0; i<fi.length; i++)
        {     
            fi[i] = triangles.get(i);
        }
        
        ifsf.setVertexCount(vc.length);
        ifsf.setVertexCoordinates(vc);
        ifsf.setFaceCount(fi.length);
        ifsf.setFaceIndices(fi);
        ifsf.setGenerateFaceNormals(true);
        ifsf.setGenerateEdgesFromFaces(true);
        ifsf.update();
        
        return ifsf;
    }
    
    private Vector<double[]> findNonConvexPoints(double[][] points, boolean ccw)
    {
        Vector<double[]> nonConvexPoints = new Vector<double[]>();
        
        if (points.length > 3)
        {
            double[] a;
            double[] b;
            double[] c;

            int before, after;
            for (int i=0; i<points.length; i++)
            {
                before = i - 1;
                if (before < 0) before = points.length - 1;
                after = i + 1;
                if (after == points.length) after = 0;
                
                a = points[before];
                b = points[i];
                c = points[after];
                
                if (!isConvex(a, b, c, ccw))
                {
                    nonConvexPoints.add(b);
                }
            }
        }
        
        return nonConvexPoints;
    }
    
    public boolean isCCW(double[][] points)
    {
        if (points.length < 3) return true;
        
        int index = 0;
        double[] b = points[0];
        for (int i=1; i<points.length; i++)
        {
            if (points[i][0] < b[0] || (points[i][0] == b[0] && points[i][1] > b[1]))
            {
                b = points[i];
                index = i;
            }
        }
        
        double[] a;
        if (index == 0)
        {
            a =  points[points.length-1];
        }
        else
        {
            a =  points[index-1];
        }
        
        double[] vec = new double[2];
        Rn.subtract(vec, b, a);

        double[] c;
        if (index == points.length - 1)
        {
            c = points[0];
        }
        else
        {
            c = points[index+1];
        }
        
        double result = c[0] * vec[1] - c[1] * vec[0] + vec[0] * a[1] - vec[1] * a[0];

        return (result > 0);
    }
   
    public boolean isInside(double[] a, double[] b, double[] c, double[] p)
    {
        double[] vec1 = new double[2];
        double[] vec2 = new double[2];
        double[] vec3 = new double[2];
        
        Rn.subtract(vec1, b, a);
        Rn.subtract(vec2, c, a);
        Rn.subtract(vec3, p, a);
        
        
        double det = vec1[0] * vec2[1] - vec2[0] * vec1[1];
        double lambda = (vec3[0] * vec2[1] - vec2[0] * vec3[1]) / det;
        double mue = (vec1[0] * vec3[1] - vec3[0] * vec1[1]) / det;
        
        return (lambda > 0 && mue > 0 && (lambda+mue) < 1);
    }

    public boolean isEar(Vector<double[]>nonConvexPoints, double[] a, double[] b, double[] c, boolean ccw)
    {
        //System.out.println("isConvex[("+a[0]+", "+a[1]+"), ("+b[0]+", "+b[1]+"), ("+c[0]+", "+c[1]+")]("+isConvex(a, b, c, ccw)+")");
        if (!isConvex(a, b, c, ccw)) return false;
        
        for (int i=0; i<nonConvexPoints.size(); i++)
        {
            //System.out.println(nonConvexPoints.get(i)[0]+", "+nonConvexPoints.get(i)[1]+" in ("+a[0]+","+a[1]+"), ("+b[0]+","+b[1]+"), ("+c[0]+","+c[1]+") ?");
            //System.out.println("isInside["+i+"]("+isInside(a, b, c, nonConvexPoints.get(i))+")");
            if (isInside(a, b, c, nonConvexPoints.get(i)))
            {
                return false;
            }
        }
        return true;
    }
    
    private boolean isConvex(double[] a, double[] b, double[] c, boolean ccw)
    {
        double[] vec = new double[2];
        Rn.subtract(vec, b, a);
        double result = c[0] * vec[1] - c[1] * vec[0] + vec[0] * a[1] - vec[1] * a[0];
        return !((result > 0 && !ccw) || result <=0 && ccw);
    }
    
    public Vector<int[]> triangulate(double[][] points)
    {
        /*for (int i=0; i<points.length; i++) {
            System.out.println("["+i+"]("+points[i][0]+", "+points[i][1]+")");
        }*/
        
        Vector<int[]> triangles = new Vector<int[]>();
        
        if (points.length < 3) return triangles;
        
        boolean ccw =  isCCW(points);
        //System.out.println("ccw("+ccw+")");
        Vector<double[]> nonConvexPoints = findNonConvexPoints(points, ccw);
        /*System.out.println("nonConvexPoints("+nonConvexPoints.size()+")");
        for (int i=0; i<nonConvexPoints.size(); i++) {
            double[] temp = nonConvexPoints.get(i);
            System.out.println("["+i+"]("+temp[0]+", "+temp[1]+")");
        }*/
        
        Vector<Integer> v = new Vector<Integer>();
        for (int i=0; i<points.length; i++)
        {
            v.addElement(new Integer(i));
        }
        
        int index = 1;
        int before, after;
        int a, b, c;
        
        while (v.size() > 3)
        {
            before = index - 1;
            if (before < 0) before = v.size()-1;
            
            after = index + 1;
            if (after == v.size()) after = 0;
   
            a = v.get(before);
            b = v.get(index);
            c = v.get(after);
            //System.out.println("isEar["+a+", "+b+", "+c+"]");
            
            if (isEar(nonConvexPoints, points[a], points[b], points[c], ccw))
            {
                triangles.add(new int[]{a, b, c});
                //System.out.println("addTriangle("+a+", "+b+", "+c+")");
                v.removeElementAt(index);

                --index;
                if (index < 0) index = v.size()-1;
                
                //System.out.println("isEar[true]");
            }
            else
            {
                ++index;
                if (index == v.size()) index = 0;
                //System.out.println("isEar[false]");
            }
        }
        
        triangles.add(new int[]{v.get(0), v.get(1), v.get(2)});
        
        return triangles;
    }
}
