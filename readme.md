# Sim Jr.

Sim Jr is a lightweight simulation framework built by Soar Technology. It was created to allow address a few needs:

* SoarTech needed a simulation environment in which to demonstrate its agent technology that was small enough to run on a typical laptop with minimal configuration. This is in contrast to large military simulations which often have particular CPU power and operating system requirements.
* SoarTech needed a simple environment for rapid prototyping of agent technologies.

Out of these needs came Sim Jr, which SoarTech decided to open source in May 2010. Sim Jr sacrifices high-fidelity platform and sensor models in exchange for simplicity and ease of development. Since 2007, SoarTech has used Sim Jr as a demonstration environment for a wide variety of intelligent agent technologies.


Now Using Maven
--------------------------------

SimJr now uses maven as both a dependency manager and build system.

Each SimJr module has its own pom, and can be distributed as a jar. These jars are then referenced in the other SimJr modules as dependencies.


Setting up Development Environment
--------------------------------

1. Install Eclipse GIT plugin (if it isn't already installed by default)

2. Install Eclipse Maven plugin (if it isn't already installed by default)

3. Setup your code templates to add the proper code rights assertion at the top of the file. Just go
   to “Java->Code Style->Code Templates” and:
       * check the “Automatically add comments for new methods and types” box
       * select the “Comments->Files” item and press “Edit...” and for open source modifications fill in the resulting dialog with (for non open source modifications the text depends on sponsoring project):

            /*
             * Copyright (c) ${year}, Soar Technology, Inc.
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
 
4. Import the the eclipse formatter settings for the project into your workspace by going to the
   "Java->Code->Formatter" preferences and importing the eclipse-formatter.xml file.

5. Check the “Insert spaces for tabs” box in the "General->Editors->Text Editors" preferences pane
   to avoid ending up with a weird combination of tabs and spaces.

6. Clone the SimJr GIT repository with your favorite GIT tool to a suitable location on your hard drive (maybe C:\src or ~/src)

         git clone https://github.com/soartech/simjr.git

7. Select the "File->Import..." menu item in Eclipse and select the "Existing Maven Projects" project type.

8. Set the "Root folder" to the root of the SID-CODE GIT repository you checked out in step 3 and press the
   "Select All" button and press the "Finish" button.

9. After about a minute of downloading some internal dependencies the various sub projects
    should appear in the project view. At this point you should be ready to begin development.

At times you may have to "Refresh (F5)" all the projects, run "Maven->Update Project..." or do a "Project->Clean" to resolve any remaining build issues.


System Dependencies
----------------------

This software was built and tested using the following:

* Java 1.7 
* Relatively recent Eclipse release 4.3 Kepler or later
* Windows XP or later (should also run on MacOS and Linux with a proper JVM)

Running the unit tests
--------------------------------

To run all the unit tests simply run the following from the command line in the root directory of
the source checkout:

    mvn test

Running Simjr in Eclipse
--------------------------------

1. Select Run -> Run Configurations. The Run Configurations dialog should open.

2. Right-click on Java Application and select New. A new run configuration should be created.

3. Rename the new run configuration something meaningful.

4. Click the Browse button next to the Project input and select your plugin (or simjr-example).

5. Click the Search button next to the Main Class input and type SimulationApplication. Select the matching item. Alternatively you can just type "com.soartech.simjr.ui.SimulationApplication" into the Main Class input.

6. Click Apply to save your changes, then Click Run. Simjr should start.

Using SimJr
--------------------------------

SimJr needs to load a scenario to do anything interesting.

1. Select File -> Load Scenario

2. Search for your scenario and click Open. The simjr-example plugin has some scenarios in /src/main/resources/scenarios

3. Once your scenario is loaded, you can start it by selecting the green Play icon in the upper left.



Building a Distribution
--------------------------------

TODO...

Project Descriptions
------------------

com.soartech.core
* This is the core Simjr module. It depends on the shapesystem.

com.soartech.shapesystem
* The shapesystem depends on the math module.

com.soartech.math
* Does math.

com.soartech.simjr.example
* An example plugin to run and show you how to make your own plugin.

com.soartech.simjr.web
* Has some networking functionality.



Maven Command Line Installation
------------------------------

To install the maven for use from the command line (DOS or Unix) follow the instructions at:

    http://maven.apache.org/download.cgi

