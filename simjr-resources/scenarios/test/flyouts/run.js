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
// Test for weapon flyouts. Select targets and click links in cheatsheet.

requireScript("ui");
requireScript("weapons");

(function() {
    var fwa = simjr.entities.create({
        name:"fwa", 
        prototype: "generic-fighter",
        capabilities: [
            new Packages.com.soartech.simjr.controllers.FixedWingFlightController(),
           [new Packages.com.soartech.simjr.controllers.OrbitFlightController(), function(orbit){
               orbit.radius = 500.0
               orbit.speed = 100.0
               orbit.altitude = 100.0
           }]
        ]
    });
    addWeapon(fwa, "generic-missile", 4);
    addWeapon(fwa, "generic-bomb", 4);
    addWeapon(fwa, "generic-cannon", 400);
    
    simjr.entities.create({name:"target-a", prototype:"truck", y:2000.0, x:-1000.0});
    simjr.entities.create({name:"target-b", prototype:"tank", y:2000.0, x:-500.0});
    simjr.entities.create({name:"target-c", prototype:"truck", y:2000.0, x:500.0});
    simjr.entities.create({name:"target-d", prototype:"truck", y:2000.0, x:1000.0});
    
    createCheatSheet();
    
    getActivePlanViewDisplay().showAll();
})();