package com.soartech.simjr.sensors.radar;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.soartech.simjr.sim.Entity;

public abstract class RadarMode
{
    @SuppressWarnings("serial")
    private static HashMap<String, Class<? extends RadarMode>> radarModes = new HashMap<String, Class<? extends RadarMode>>() {{
        put(RangeWhileSearch.getRadarName(), RangeWhileSearch.class);
        put(SAM.getRadarName(), SAM.class);
        put(SingleTarget.getRadarName(), SingleTarget.class);
        put(TrackWhileScan.getRadarName(), TrackWhileScan.class);
        put(Full.getRadarName(), Full.class);
    }};
    
    // Preserves insertion ordering which is used if a target
    // is added when the mode is already at its maximum allowed number of targets
    private LinkedHashSet<Entity> targets = new LinkedHashSet<Entity>();
    
    public static Set<String> getModes()
    {
        return radarModes.keySet();
    }
    
    public static RadarMode factory(String mode)
    {
        try
        {
            Constructor<? extends RadarMode> c = radarModes.get(mode).getConstructor();
            return c.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public RadarMode()
    {
        // Intentionally left blank
    }

    public void addTarget(Entity e)
    {
        if (getAllowedTargetCount() <= 0)
        {
            return;
        }
        
        // If we've already got the max number of targets
        // then remove one form the front of the queue
        while (targets.size() >= getAllowedTargetCount())
        {
            // Removing something with an iterator is kind of ugly
            Iterator<Entity> it = targets.iterator();
            it.next();
            it.remove();
        }
        
        targets.add(e);
    }
    
    public void addTargets(RadarMode mode)
    {
        targets.addAll(mode.targets);
    }
    
    public boolean isTarget(Entity e)
    {
        return targets.contains(e);
    }
    
    public void removeTarget(Entity e)
    {
        targets.remove(e);
    }
    
    public abstract int getAllowedTargetCount();
    public abstract DetectionMode getTargetDetectionMode();
    public abstract DetectionMode getRegularDetectionMode();

    public void clearTargets()
    {
        targets.clear();
    }
    
    public abstract boolean onlyShowTargets();
    public abstract boolean switchToRWSWhenNoTargets();

    public Set<Entity> getTargets()
    {
        return Collections.unmodifiableSet(targets);
    }

    
    public abstract RadarBound getRangeBounds();
    public abstract RadarDegreeBound getAzimuthBounds();
    public abstract RadarDegreeBound getInclinationBounds();
}
