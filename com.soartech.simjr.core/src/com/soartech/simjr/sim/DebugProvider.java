package com.soartech.simjr.sim;

/**
 * Adds a "Debug" menu entry to the right-click context menu (in the simulation view)
 * of this entity.
 * 
 * @author charles.newton
 */
public interface DebugProvider extends EntityCapability
{
    /**
     * If the entity supports a debugger, this method should display it.
     */
    void openDebugger();
}