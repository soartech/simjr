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
 */ 
// This script is run at application startup to install the default application
// actions. It can be overridden (perhaps to limit the set of available actions)
// by setting the "simjr.actions.path" property or by putting your own version
// of simjr.actions.js earlier on the classpath.

runner.subTask("Installing default actions ...");
logger.info("Installing default actions from simjr.actions.js")
var actionManager = Packages.com.soartech.simjr.ui.actions.ActionManager.findService(services);

new Packages.com.soartech.simjr.ui.actions.LoadScenarioAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.ExportScenarioAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.ExitAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.RunAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.PauseAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.CenterViewOnEntityAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.AdjustMapOpacityAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.ShowAllAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.OpenDebuggerAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.AddDistanceToolAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.ClearDistanceToolsAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.NewPlanViewDisplayAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.ZoomInAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.ZoomOutAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.RestoreDefaultLayoutAction(actionManager);

new Packages.com.soartech.simjr.ui.actions.LoadDockingLayoutAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.SaveDockingLayoutAction(actionManager);
   
new Packages.com.soartech.simjr.ui.actions.LoadContainerAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.UnloadContainerAction(actionManager);
   
new Packages.com.soartech.simjr.ui.actions.CreateManualControllerAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.ShowHideEntityAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.AddToPolygonAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.RemoveFromPolygonAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.EditRouteAction(actionManager);
new Packages.com.soartech.simjr.ui.actions.ToggleCategoryLabelsAction(actionManager);
