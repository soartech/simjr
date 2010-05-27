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
 * Created on Oct 28, 2007
 */
package com.soartech.simjr.ui.cheatsheets;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;

import org.apache.log4j.Logger;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.adaptables.Adaptables;
import com.soartech.simjr.services.ConstructOnDemand;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.services.SimulationService;
import com.soartech.simjr.ui.SimulationImages;

/**
 * @author ray
 */
@ConstructOnDemand
public class CheatSheetView extends DefaultSingleCDockable implements HyperlinkListener, SimulationService
{
    private static final Logger logger = Logger.getLogger(CheatSheetView.class);
    private static final long serialVersionUID = 6391575660063797762L;
    
    private ServiceManager services;
    private CheatSheetHtmlPane htmlPane;
    private List<CheatSheet> cheatSheets = new ArrayList<CheatSheet>(); 
    private int currentSheet = 0;
    
    public static final CheatSheetView findService(ServiceManager services)
    {
        return services.findService(CheatSheetView.class);
    }
    
    /**
     * Constructed on demand by SimulationManager.findService() 
     */
    public CheatSheetView(ServiceManager services)
    {
        super("CheatSheetView");

        //DF settings
        setLayout(new BorderLayout());
        setCloseable(true);
        setMinimizable(false);
        setExternalizable(true);
        setMaximizable(true);
        setTitleText("Cheat Sheet");
        setResizeLocked(true);
        setTitleIcon(SimulationImages.CHEATSHEET);
        
        this.services = services;
        
        htmlPane = new CheatSheetHtmlPane();
            
        htmlPane.addHyperlinkListener(this);
        
        // Add a listener so we know when the current page is finished loading and
        // the HTML is parsed
        htmlPane.addPropertyChangeListener("page", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt)
            {
                getCurrentSheet().pageLoaded((HTMLDocument) htmlPane.getDocument());
            }});
        
        htmlPane.setText("No cheat sheets loaded.");

        add(new JScrollPane(htmlPane), BorderLayout.CENTER);
    }

    /**
     * @return The owning service manager
     */
    public ServiceManager getServices()
    {
        return services;
    }

    public CheatSheet createCheatSheet(String name, String path)
    {
        CheatSheet sheet = new CheatSheet(name, new File(path).toURI().toString());
        
        cheatSheets.add(sheet);
        
        currentSheet = cheatSheets.size() - 1;
        
        sheet.activate(this);
        
        return sheet;
    }

    public CheatSheet getCurrentSheet()
    {
        if(currentSheet >= 0 && currentSheet < cheatSheets.size())
        {
            return cheatSheets.get(currentSheet);
        }
        return null;
    }
    
    void showPage(String url)
    {
        try
        {
            // This call is asynchronous. The HTML document will not be ready
            // until we get the "page" property change event above.
            htmlPane.setPage(url);
        }
        catch (IOException e)
        {
            logger.error(e);
            setError(e.getMessage());
        }
    }
    
    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
        {
            CheatSheet sheet = getCurrentSheet();
            if(sheet != null)
            {
                try
                {
                    URL url = e.getURL();
                    
                    final URI uri = url != null ? url.toURI() : new URI(e.getDescription());
                    sheet.handleAction(this, uri);;
                }
                catch (URISyntaxException e1)
                {
                    logger.error(e1);
                    setError(e1.getMessage());
                }
            }
        }
    }
    
    public void setError(String message)
    {
        htmlPane.setText("<html><h1>Error</h1><pre>" + message + "</pre></html>");
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#shutdown()
     */
    public void shutdown() throws SimulationException
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.SimulationService#start()
     */
    public void start(ProgressMonitor progress) throws SimulationException
    {
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.adaptables.Adaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class klass)
    {
        return Adaptables.adaptUnchecked(this, klass, false);
    }
}
