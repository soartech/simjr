/**
 * 
 */
package com.soartech.simjr.game;


import com.soartech.simjr.services.PluginServiceProvider;

/**
 * @author aron
 *
 */
public class ShapeManagerServiceProvider extends PluginServiceProvider
{
    {
        add("shapemanager", "com.soartech.simjr.game.GameShapeManager");
    }
}
