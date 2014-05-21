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

var actionsPkg = Packages.com.soartech.simjr.ui.actions;

new actionsPkg.LoadScenarioAction(actionManager);
new actionsPkg.ExportScenarioAction(actionManager);
new actionsPkg.ExitAction(actionManager);
new actionsPkg.RunAction(actionManager);
new actionsPkg.PauseAction(actionManager);
new actionsPkg.CenterViewOnEntityAction(actionManager);
new actionsPkg.AdjustMapOpacityAction(actionManager);
new actionsPkg.ShowAllAction(actionManager);
new actionsPkg.OpenDebuggerAction(actionManager);
new actionsPkg.AddDistanceToolAction(actionManager);
new actionsPkg.ClearDistanceToolsAction(actionManager);
new actionsPkg.NewPlanViewDisplayAction(actionManager);
new actionsPkg.ZoomInAction(actionManager);
new actionsPkg.ZoomOutAction(actionManager);
new actionsPkg.RestoreDefaultLayoutAction(actionManager);

new actionsPkg.LoadDockingLayoutAction(actionManager);
new actionsPkg.SaveDockingLayoutAction(actionManager);
   
new actionsPkg.LoadContainerAction(actionManager);
new actionsPkg.UnloadContainerAction(actionManager);
   
new actionsPkg.CreateManualControllerAction(actionManager);
new actionsPkg.ShowHideEntityAction(actionManager);
new actionsPkg.AddToPolygonAction(actionManager);
new actionsPkg.RemoveFromPolygonAction(actionManager);
new actionsPkg.EditRouteAction(actionManager);
new actionsPkg.ToggleCategoryLabelsAction(actionManager);
