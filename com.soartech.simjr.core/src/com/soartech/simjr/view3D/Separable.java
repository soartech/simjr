package com.soartech.simjr.view3D;

import java.util.List;

import de.jreality.scene.SceneGraphComponent;

/**
 * Interface implemented by 3D Constructs that should be separated into parts 
 * for bounding box sorting.  This is a helper method for properly rendering
 * transparent objects.  
 * 
 * @author Dan Silverglate
 */
public interface Separable
{
    List<SceneGraphComponent> getSubComponents();
}
