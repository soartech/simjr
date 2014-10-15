package com.soartech.simjr.ui.pvd;

import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.sim.Simulation;

public interface PvdViewFactory
{
    PvdView createPvdView(ServiceManager app, Simulation sim);
}
