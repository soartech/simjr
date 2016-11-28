/*
 * Copyright (c) 2010, Soar Technology, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * * Neither the name of Soar Technology, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without the specific prior written permission of Soar Technology, Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on Jun 28, 2013
 */
package com.soartech.simjr.sim;

import com.soartech.math.Vector3;

/**
 * Default motion integrator, this implements RK4 integration with no fixed or minimum timestep.
 * This is better than Euler integration, but may be improved further by interpolating the timestep to
 * a smaller time.
 * 
 * @author pdehaan
 */
public class DefaultEntityMotionIntegrator implements EntityMotionIntegrator
{
    private static final EntityMotionIntegrator instance = new DefaultEntityMotionIntegrator();
    
    public static final EntityMotionIntegrator getInstance() { return instance; }
    
    /**
     * Helper class for passing/returning multiple Vector3s
     */
    private static class PositionVelocity
    {
        public static final PositionVelocity ZERO = new PositionVelocity(Vector3.ZERO, Vector3.ZERO);
        
        public final Vector3 position;
        public final Vector3 velocity;
        
        public PositionVelocity(Vector3 position, Vector3 velocity)
        {
            this.position = position;
            this.velocity = velocity;
        }
    }
    
    @Override
    public void updateEntity(Entity e, EntityAccelerationProvider accel, double dt)
    {
        if (EntityAccelerationProvider.NO_ACCELERATION_MODEL == accel)
        {
            // with no acceleration model, no extra work is needed
            e.setPosition(e.getPosition().add(e.getVelocity().multiply(dt)));
        }
        else
        {
            // there is an acceleration model, so run it through RK4 integration to
            // approximate new position and velocity under possibly dynamic acceleration
            rk4(e, dt, accel);
        }
    }
    
    private void rk4(Entity e, double dt, EntityAccelerationProvider accel)
    {
        double t = e.getSimulation().getTime();
        PositionVelocity initial = new PositionVelocity(e.getPosition(), e.getVelocity());
        
        // sample derivatives out to dt
        PositionVelocity a = computeDeriv(accel, initial, t, 0.0, PositionVelocity.ZERO);
        PositionVelocity b = computeDeriv(accel, initial, t, dt * 0.5, a);
        PositionVelocity c = computeDeriv(accel, initial, t, dt * 0.5, b);
        PositionVelocity d = computeDeriv(accel, initial, t, dt, c);
        
        // weighted average of derivatives, doing: (a + 2 * (b + c) + d)/6
        Vector3 dPos = b.position.add(c.position).multiply(2.0).add(a.position).add(d.position).multiply(1.0/6.0);
        Vector3 dVel = b.velocity.add(c.velocity).multiply(2.0).add(a.velocity).add(d.velocity).multiply(1.0/6.0);
        
        // update entity based on new derivatives
        e.setPosition(e.getPosition().add(dPos.multiply(dt)));
        e.setVelocity(e.getVelocity().add(dVel.multiply(dt)));
    }
    
    /**
     * @param accel    acceleration provider; used to find appropriate acceleration at particular time
     * @param initial  state of the system at time t
     * @param t        start of the timestep
     * @param dt       the timestep to sample over
     * @param deriv    derivative of position and velocity (e.g. velocity and acceleration)
     */
    private PositionVelocity computeDeriv(EntityAccelerationProvider accel, PositionVelocity initial, double t, double dt, PositionVelocity deriv)
    {
        // first compute new state based on Euler method
        Vector3 pos = initial.position.add(deriv.position.multiply(dt));
        Vector3 vel = initial.velocity.add(deriv.velocity.multiply(dt));
        
        // compute derivative at new state
        PositionVelocity outputDeriv = new PositionVelocity(
                vel,
                accel.getAcceleration(pos, vel, t + dt));
        
        return outputDeriv;
    }
}
