package com.soartech.simjr.sensors;

public enum ContactState
{
    VISIBLE(4), 
    RADAR(3), 
    PROJECTED(2), 
    PROJECTED_DISAPPEARING(1),
    UNKNOWN(0);
    
    private final int priority;
    
    private ContactState(int priority) {
        this.priority = priority;
    }
    
    /**
     * Priority is used by the Contact class to determine
     * which state takes precedence when multiple updates
     * are called in the same tick.
     * 
     * @return the priority of the state w.r.t. the other states
     */
    public int getPriority()
    {
        return this.priority;
    }
}
