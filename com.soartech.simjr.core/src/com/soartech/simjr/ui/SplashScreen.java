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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.soartech.simjr.ProgressMonitor;

/**
 * Present a simple graphic to the user upon launch of the application, to
 * provide a faster initial response than is possible with the main window.
 * 
 * <P>
 * Adapted from an <a
 * href=http://developer.java.sun.com/developer/qow/archive/24/index.html>item</a>
 * on Sun's Java Developer Connection.
 * 
 * <P>
 * This splash screen appears within about 2.5 seconds on a development machine.
 * The main screen takes about 6.0 seconds to load, so use of a splash screen
 * cuts down the initial display delay by about 55 percent.
 * 
 * @used.By {@link stocksmonitor.Launcher}
 * @to.do Can the performance be improved to 1.0 second?
 * @author <a href="http://www.javapractices.com/">javapractices.com</a>
 */
public final class SplashScreen extends Frame implements ProgressMonitor
{

    private static final long serialVersionUID = 2062750256077170559L;

    /**
     * @param aImageId
     *            must have content, and is used by
     *            <code>Class.getResource</code> to retrieve the splash screen
     *            image.
     * @throws MalformedURLException 
     */
    public SplashScreen(String aImageId) throws MalformedURLException
    {
        fImageUrl = SplashScreen.class.getResource(aImageId);
        if(fImageUrl == null)
        {
            fImageUrl = (new File(aImageId)).toURI().toURL();
        }
    }

    /**
     * Show the splash screen to the end user.
     * 
     * <P>
     * Once this method returns, the splash screen is realized, which means that
     * almost all work on the splash screen should proceed through the event
     * dispatch thread. In particular, any call to <code>dispose</code> for
     * the splash screen must be performed in the event dispatch thread.
     */
    public void splash()
    {
        initImageAndTracker();
        setSize(fImage.getWidth(null), fImage.getHeight(null));
        center();

        fMediaTracker.addImage(fImage, 0);
        try
        {
            fMediaTracker.waitForID(0);
        }
        catch (InterruptedException ie)
        {
            System.out.println("Cannot track image load.");
        }

        window = new SplashWindow(this, fImage);
    }

    // PRIVATE//
    private URL fImageUrl;

    private MediaTracker fMediaTracker;

    private Image fImage;
    
    private String message = "";
    
    private SplashWindow window;
    
    public void setMessage(final String message)
    {
        EventQueue.invokeLater(new Runnable() {

            public void run()
            {
                SplashScreen.this.message = message;
                if(window != null)
                {
                    window.repaint();
                }
            }});
    }
    
    
    /* (non-Javadoc)
     * @see com.soartech.simjr.ProgressMonitor#subTask(java.lang.String)
     */
    public void subTask(String name)
    {
        setMessage(name);
    }

    public void hide()
    {
        EventQueue.invokeLater(new Runnable() {

            public void run()
            {
                window.setVisible(false);
            }});
    }

    private void initImageAndTracker()
    {
        fMediaTracker = new MediaTracker(this);
        fImage = Toolkit.getDefaultToolkit().getImage(fImageUrl);
    }

    /**
     * Centers the frame on the screen.
     * 
     * This centering service is more or less in {@link UiUtil}; this
     * duplication is justified only because the use of {@link UiUtil} would
     * entail more class loading, which is not desirable for a splash screen.
     */
    private void center()
    {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle frame = getBounds();
        setLocation((screen.width - frame.width) / 2,
                (screen.height - frame.height) / 2);
    }

    private class SplashWindow extends Window
    {
        private static final long serialVersionUID = 5192121006539889119L;

        SplashWindow(Frame aParent, Image aImage)
        {
            super(aParent);
            fImage = aImage;
            setSize(fImage.getWidth(null), fImage.getHeight(null));
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle window = getBounds();
            setLocation((screen.width - window.width) / 2,
                    (screen.height - window.height) / 2);
            setAlwaysOnTop(SplashScreen.this.isAlwaysOnTop());
            setVisible(true);
        }

        public void paint(Graphics graphics)
        {
            if (fImage != null)
            {
                graphics.drawImage(fImage, 0, 0, this);
            }
            FontMetrics fm = graphics.getFontMetrics();
            graphics.setColor(Color.BLACK);
            graphics.drawString(message, 13, getHeight() - (fm.getDescent() + 2));
        }

        private Image fImage;
    }

    /**
     * Developer test harness shows the splash screen for a fixed length of
     * time, without launching the full application.
     * @throws MalformedURLException 
     */
    public static void main(String[] args) throws MalformedURLException
    {
        SplashScreen splashScreen = new SplashScreen("images/simjr-splash.png");
        splashScreen.splash();
        
        try
        {
            splashScreen.setMessage("Message 1");
            Thread.sleep(750);
            splashScreen.setMessage("Message 2");
            Thread.sleep(750);
            splashScreen.setMessage("Message 3");
            Thread.sleep(750);
            splashScreen.setMessage("Message 4");
            Thread.sleep(750);
            splashScreen.setMessage("Message 5");
            Thread.sleep(750);
        }
        catch (InterruptedException ex)
        {
            System.out.println(ex);
        }
        System.exit(0);
    }
}
