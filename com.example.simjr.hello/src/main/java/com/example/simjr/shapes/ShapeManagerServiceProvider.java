/**
 * 
 */
package com.example.simjr.shapes;


import com.soartech.simjr.services.PluginServiceProvider;

/**
 * @author aron
 *
 */
public class ShapeManagerServiceProvider extends PluginServiceProvider
{
    {
        add("shapemanager", "com.example.simjr.shapes.ExampleShapeManager");
    }
}
