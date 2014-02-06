package com.soartech.simjr.sim.entities;

public enum Direction
{

    NORTH("N"),
    SOUTH("S"),
    EAST("E"), 
    WEST("W"),
    NORTHWEST("NW"),
    SOUTHWEST("SW"),
    NORTHEAST("NE"), 
    SOUTHEAST("SE"),
    UNKNOWN("?");
    
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
        case NORTHEAST:
            return SOUTHWEST;
        case NORTHWEST:
            return SOUTHEAST;
        case SOUTHEAST:
            return NORTHWEST;
        case SOUTHWEST:
            return NORTHEAST;
        }
        return UNKNOWN;
    }
    
    public static Direction parse(String string)
    {
        for(Direction dir : Direction.values())
        {
            if(string.equalsIgnoreCase(dir.ToString()))
            {
                return dir;
            }
        }
        return null;
    }
    
    public Direction getOpposite()
    {
        return Direction.getOpposite(this);
    }
}
