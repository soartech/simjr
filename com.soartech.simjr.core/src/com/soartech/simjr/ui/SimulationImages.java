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
 */
package com.soartech.simjr.ui;

import java.net.URL;

import javax.swing.ImageIcon;

/**
 * @author ray
 */
public final class SimulationImages
{
    public final static ImageIcon SIMJR_ICON = loadImageFromJar("simjr/images/simjr-icon.png");
    public final static ImageIcon NEW = loadImageFromJar("simjr/images/new.gif");
    public final static ImageIcon SAVE = loadImageFromJar("simjr/images/gif");
    public final static ImageIcon OPEN = loadImageFromJar("simjr/images/open.gif");
    public final static ImageIcon CLEAR = loadImageFromJar("simjr/images/clear.gif");
    public final static ImageIcon PRINT = loadImageFromJar("simjr/images/print.gif");
    public final static ImageIcon ZOOMIN = loadImageFromJar("simjr/images/zoomin.gif");
    public final static ImageIcon ZOOMOUT = loadImageFromJar("simjr/images/zoomout.gif");
    public final static ImageIcon AGENT = loadImageFromJar("simjr/images/newagent.gif");
    public final static ImageIcon COMMENT = loadImageFromJar("simjr/images/comment.gif");
    public final static ImageIcon SELECT = loadImageFromJar("simjr/images/select.gif");
    public final static ImageIcon DELETE = loadImageFromJar("simjr/images/delete.gif");
    public final static ImageIcon ERROR = loadImageFromJar("simjr/images/error.gif");
    public final static ImageIcon WARNING = loadImageFromJar("simjr/images/warning.gif");
    public final static ImageIcon INFO = loadImageFromJar("simjr/images/info.gif");
    public final static ImageIcon HIDE = loadImageFromJar("simjr/images/hide.gif");
    public final static ImageIcon SHOW = loadImageFromJar("simjr/images/show.gif");
    public static final ImageIcon REFRESH = loadImageFromJar("simjr/images/refresh.gif");
    public static final ImageIcon AUTO_REFRESH = loadImageFromJar("simjr/images/autorefresh.gif");
    public static final ImageIcon START = loadImageFromJar("simjr/images/start.gif");
    public static final ImageIcon STOP = loadImageFromJar("simjr/images/stop.gif");
    public static final ImageIcon PAUSE = loadImageFromJar("simjr/images/pause.gif");
    public static final ImageIcon CENTER = loadImageFromJar("simjr/images/center.gif");
    public static final ImageIcon SHOWALL = loadImageFromJar("simjr/images/showall.gif");
    public static final ImageIcon COPY = loadImageFromJar("simjr/images/copy.gif");
    public static final ImageIcon SPEECH_BUBBLE = loadImageFromJar("simjr/images/speechbubble.gif");
    public static final ImageIcon LOCK = loadImageFromJar("simjr/images/lock.gif");
    public static final ImageIcon DEBUG = loadImageFromJar("simjr/images/debug.gif");
    public static final ImageIcon PVD = loadImageFromJar("simjr/images/pvd.gif");
    public static final ImageIcon PROPERTIES = loadImageFromJar("simjr/images/properties.gif");
    public static final ImageIcon SOAR = loadImageFromJar("simjr/images/soar.gif");
    public static final ImageIcon CHEATSHEET = loadImageFromJar("simjr/images/cheatsheet.gif");
    public static final ImageIcon NEXT = loadImageFromJar("simjr/images/next.gif");
    public static final ImageIcon PREVIOUS = loadImageFromJar("simjr/images/previous.gif");
    public static final ImageIcon CONSOLE = loadImageFromJar("simjr/images/console.gif");
    public static final ImageIcon LOADING = loadImageFromJar("simjr/images/loading.gif");

    public static ImageIcon loadImageFromJar(String path)
    {
        URL url = SimulationImages.class.getResource('/' + path);
        if(url == null)
        {
            return new ImageIcon(path);
        }
        return new ImageIcon(url);
    }
}
