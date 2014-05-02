package com.soartech.simjr.sim.entities;

import com.soartech.math.Angles;
import com.soartech.math.Vector3;
import com.soartech.simjr.sim.AbstractEntityCapability;
import com.soartech.simjr.sim.Entity;
import com.soartech.simjr.sim.EntityConstants;

/**
 * An EntityCapability that allows for checking if a location is within the
 * visible range of the entity associated with this capability. The visible
 * range used is the value of the property defined by
 * {@link EntityConstants#PROPERTY_BEHOLDER_RANGE} (defaults to 1000 m). The visible
 * angle (field of view) used is the value of the property defined by
 * {@link EntityConstants.PROPERTY_BEHOLDER_ARC} (defaults to 2*PI).
 * 
 * @see EntityConstants
 * 
 */
public class BeholderCapability extends AbstractEntityCapability
{
    private double visibleRange = 1000.0;
    private double visibleAngle = 2 * Math.PI;
    
    @Override
    public void attach(Entity entity)
    {
        super.attach(entity);
        
        Double visibleRange = (Double)entity.getProperty(EntityConstants.PROPERTY_BEHOLDER_RANGE);
        if (visibleRange != null)
        {
            this.visibleRange = visibleRange;
        }
        
        Double visibleAngle = (Double)entity.getProperty(EntityConstants.PROPERTY_BEHOLDER_ARC);
        if (visibleAngle != null)
        {
            this.visibleAngle = visibleAngle;
        }
    }
    
    /**
     * Tests whether a position is within this visible range
     * 
     * @param otherPos The position to test. Z is ignored.
     * @return True if otherPos is within this visible range.
     */
    public boolean beholds(Vector3 otherPos)
    {
        if(visibleRange < 0) return true;
        
        Vector3 agentPos = getEntity().getPosition();
        Vector3 displacement = otherPos.subtract(agentPos);
        Vector3 xyDisplacement = new Vector3(displacement.x, displacement.y, 0.0);
        double xyRange = xyDisplacement.length();
        double xyAngle = Math.atan2(xyDisplacement.y, xyDisplacement.x);
        double agentAngle = Angles.boundedAngleRadians(getEntity().getHeading());
        
        if(xyRange > visibleRange || Math.abs(Angles.angleDifference(xyAngle, agentAngle)) > 0.5* visibleAngle)
        {
            return false;
        }
        
        return true;
    }
    
    @Override
    public Object getAdapter(Class<?> klass)
    {
        if (klass == VeilCapability.class)
        {
            return this;
        }
        return super.getAdapter(klass);
    }
}
