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
 * Created on May 3, 2008
 */
package com.soartech.simjr.scripting;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.mozilla.javascript.NativeJavaObject;

import com.soartech.simjr.services.DefaultServiceManager;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.services.SimulationService;
import com.soartech.simjr.sim.EntityPrototypeDatabase;
import com.soartech.simjr.sim.EntityPrototypes;
import com.soartech.simjr.sim.SimpleTerrain;
import com.soartech.simjr.sim.Simulation;
import com.soartech.simjr.sim.entities.NonEntity;
import com.soartech.simjr.ui.SelectionManager;
import com.soartech.simjr.ui.actions.ActionManager;

public class ScriptRunnerTest extends TestCase
{
    private ServiceManager services;
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.SimJrTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        services = new DefaultServiceManager();
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimJrTestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        Thread.sleep(500); // Give radio bridge a chance to come up so it can be shut down
        services.shutdownServices();
        super.tearDown();
    }

    public void testClassForName() throws Exception
    {
        ScriptRunner runner = new ScriptRunner(new DefaultServiceManager());
        
        String script = "c = java.lang.Class.forName(\"java.lang.Integer\")";
        NativeJavaObject result = (NativeJavaObject)
            ScriptRunSettings.builder().reader(new StringReader(script)).path("testClassForName").run(runner);
        assertSame(java.lang.Integer.class, result.unwrap());
    }
    
    public void testGetSimulation() throws Exception
    {
        services.addService(new Simulation(SimpleTerrain.createExampleTerrain(), false));
        validateCommonServiceGetter("getSimulation()", Simulation.class);
    }
    public void testGetActionManager() throws Exception
    {
        services.addService(new ActionManager(services));
        validateCommonServiceGetter("requireScript('ui');\ngetActionManager()", ActionManager.class);
    }
    public void testGetSelectionManager() throws Exception
    {
        services.addService(new SelectionManager());
        validateCommonServiceGetter("requireScript('ui');\ngetSelectionManager()", SelectionManager.class);
    }
    
    public void testGetEntityPrototypes() throws Exception
    {
        services.addService(new EntityPrototypeDatabase());
        validateCommonServiceGetter("requireScript('entities');\ngetEntityPrototypes()", EntityPrototypeDatabase.class);
    }
    
    public void testGetEntityPrototype() throws Exception
    {
        final EntityPrototypeDatabase db = new EntityPrototypeDatabase();
        db.load();
        services.addService(db);
        final ScriptRunner runner = new ScriptRunner(services);
        final String script = "getEntityPrototype(\"any\")";
        final NativeJavaObject result = (NativeJavaObject)
            ScriptRunSettings.builder().reader(new StringReader(script)).path("testGetEntityPrototype").run(runner);
        assertNotNull("Null result from " + script, result);
        assertSame(db.getPrototype("any"), result.unwrap());
    }
    
    // TODO Move this test to Soar plugin
    /*
    public void testGetSoarManager() throws Exception
    {
        validateCommonServiceGetter("getSoarManager()", SoarManager.class);
    }
    */
    /*
     * TODO: Move this test to SoarSpeak plugin
    public void testGetSoarSpeak() throws Exception
    {
        // Disable SoarSpeak plugins since we don't really care about them at this point 
        SimJrProps.getProperties().setProperty("simjr.soarspeak.plugins", "udpplugin");
        String script = "configureSoarSpeak(null, null, null); getSoarSpeak()";
        validateCommonServiceGetter(script, SoarSpeakManager.class);
    }
    */
    
    private <T extends SimulationService> void  validateCommonServiceGetter(String script, Class<T> klass) throws Exception
    {
        final ScriptRunner runner = new ScriptRunner(services);
        NativeJavaObject result = (NativeJavaObject) 
                ScriptRunSettings.builder().reader(new StringReader(script)).path(klass.getName()).run(runner);
        assertNotNull("Null result from " + script, result);
        assertSame(services.findService(klass), result.unwrap());
    }
    
    public void testCreateMap() throws Exception
    {
        final ScriptRunner runner = new ScriptRunner(services);
        final String script = "createMap({\"x\":\"hello\", \"y\":\"goodbye\"})";
        NativeJavaObject result = (NativeJavaObject) 
                ScriptRunSettings.builder().reader(new StringReader(script)).path("testCreateMap").run(runner);
        assertNotNull(result);
        Map<String, String> map = new HashMap<String, String>();
        map.put("x", "hello");
        map.put("y", "goodbye");
        assertEquals(map, result.unwrap());
    }
    
    public void testSelectEntity() throws Exception
    {
        final Simulation sim = new Simulation(SimpleTerrain.createExampleTerrain(), false);
        services.addService(sim);
        NonEntity entity = new NonEntity("test", EntityPrototypes.NULL);
        sim.addEntity(entity);
        
        SelectionManager sm = new SelectionManager();
        services.addService(sm);
        assertNull(sm.getSelectedObject());
        
        final ScriptRunner runner = new ScriptRunner(services);
        String script = "requireScript('ui');\nselectEntity(\"test\")";
        NativeJavaObject result = (NativeJavaObject) 
            ScriptRunSettings.builder().reader(new StringReader(script)).path("testSelectEntity").run(runner);
        assertNotNull(result);
        assertSame(entity, sm.getSelectedObject());
        assertSame(entity, result.unwrap());
    }
}
