package com.soartech.simjr.sim;

public interface DebugProviderCapability extends EntityCapability
{
    /**
     * If the capability supports a debugger, this method should display it.
     */
    void openDebugger();
}
