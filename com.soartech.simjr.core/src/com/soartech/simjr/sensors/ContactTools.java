package com.soartech.simjr.sensors;

import com.soartech.simjr.sensors.radar.AdvancedModalRadar;
import com.soartech.simjr.sensors.radar.DetectionMode;

public class ContactTools
{

    public static DetectionMode getDetectionMode(DetectionMode currentMode, Contact contact, AdvancedModalRadar radar)
    {
        ContactState cstate = contact.getState();
        if ( cstate == ContactState.VISIBLE ) {
            return DetectionMode.FULL;
        } else if ( cstate == ContactState.RADAR ) {
            boolean isTarget = radar.getRadarMode().isTarget(contact.getEntity());
            if ( isTarget ) {
                return radar.getRadarMode().getTargetDetectionMode();
            } else {
                return radar.getRadarMode().getRegularDetectionMode();
            }
        }
        return currentMode;
    }

}
