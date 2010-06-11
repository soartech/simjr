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
/*
    Common script utilities for dealing with the UI
    
    Load with: requireScript("terrain");
*/

logger.info("loading simjr.ui.js ...");

/** Access the SelectionManager service */
function getSelectionManager()
{
    return Packages.com.soartech.simjr.ui.SelectionManager.findService(services);
}

/** Access the ActionManager service */
function getActionManager()
{
    return Packages.com.soartech.simjr.ui.actions.ActionManager.findService(services);
}

function getMainFrame()
{
    return Packages.com.soartech.simjr.ui.SimulationMainFrame.findService(services);
}

function getActivePlanViewDisplay()
{
    var pvdp = services.findService(java.lang.Class.forName("com.soartech.simjr.ui.pvd.PlanViewDisplayProvider"));
    return pvdp != null ? pvdp.getActivePlanViewDisplay() : null;
}

/**
    Center a PVD on an entity
    
    @param pvd the PVD. If null, the active PVD is used.
    @param name Name of the entity to show
    @param select Optional parameter defaults to true. If true, the entity is
        also selected.
*/
function showEntity(pvd, name, select)
{
    if(pvd == null)
    {
        pvd = getActivePlanViewDisplay();
        if(pvd == null)
        {
            logger.error("showEntity(null, " + name + "): No active PVD found");
            return;
        }
    }
    entity = simulation.getEntity(name);
    if(entity == null)
    {
        logger.error("showEntity(pvd, " + name + "): Entity not found");
        return;
    }
    pvd.showPosition(entity.getPosition());
    
    // typeof is undefined if the parameter is omitted.
    if(typeof(select) == 'undefined' || select == true)
    {
        getSelectionManager().setSelection(null, entity);        
    }
}

function selectEntity(name)
{
   entity = simulation.getEntity(name);
   if(entity != null)
   {
      getSelectionManager().setSelection(null, entity);
   } 
   return entity;
}

/**
    Load a docking layout from a file saved with File->Save window layout...
    
    @param file The name of the file to load. If not absolute, it is assumed 
            to be relative to the directory of the invoking script. If omitted,
            defaults to "layout.sjl" relative to location of calling script.
*/
function loadDockingLayout(file) {
    var frame = getMainFrame();
    if(frame == null) {
        return;
    }
    if(file === undefined) {
        file = "layout.sjl";
    }
    var absFile = new java.io.File(file);
    if(!absFile.isAbsolute()) {
        file = getRelativeFile(file);
    }
    frame.loadDockingLayoutFromFile(file);
}

/**
 * Create a cheat sheet.
 * 
 * @param name the name of the cheatsheet
 * @param file the file (relative to executing script), defaults to cheatsheet.html
 * @return the cheat sheet
 */
function createCheatSheet(name, file) {
   if(typeof(file) == 'undefined') file = 'cheatsheet.html';
   if(typeof(name) == 'undefined') name = 'CheatSheet';
   
   var csv = Packages.com.soartech.simjr.ui.cheatsheets.CheatSheetView.findService(services);
   return csv.createCheatSheet(name, runner.getRelativeFile(file).toString());
}

logger.info("finished loading simjr.ui.js");

