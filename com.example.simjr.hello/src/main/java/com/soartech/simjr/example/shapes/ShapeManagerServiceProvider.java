/**
 * 
 */
package com.soartech.simjr.example.shapes;


import com.soartech.simjr.services.PluginServiceProvider;

/**
 * @author aron
 *
 */
public class ShapeManagerServiceProvider extends PluginServiceProvider
{
    {
        add("shapemanager", "com.soartech.simjr.example.shapes.ExampleShapeManager");
    }
}
