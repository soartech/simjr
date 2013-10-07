package com.soartech.simjr.sim.entities;

public enum Direction
{

    NORTH("North"),
    SOUTH("South"),
    EAST("East"), 
    WEST("West");
    
    private String value;
    
    private Direction(String text)
    {
        value = text;
    }
    
    public String ToString()
    {
        return value;
    }
    
    public static Direction getOpposite(Direction dir)
    {
        switch(dir)
        {
        case EAST:
            return WEST;
        case NORTH:
            return SOUTH;
        case SOUTH:
            return NORTH;
        case WEST:
            return EAST;
        }
        return NORTH;
    }
    
    public Direction getOpposite()
    {
        switch(this)
        {
        case EAST:
            return WEST;
        case NORTH:
            return SOUTH;
        case SOUTH:
            return NORTH;
        case WEST:
            return EAST;
        }
        return NORTH;
    }
}
