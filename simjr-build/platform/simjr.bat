@echo off
rem Copyright (c) 2010, Soar Technology, Inc.
rem All rights reserved.
rem 
rem Redistribution and use in source and binary forms, with or without
rem modification, are permitted provided that the following conditions are met:
rem 
rem * Redistributions of source code must retain the above copyright notice, this
rem   list of conditions and the following disclaimer.
rem 
rem * Redistributions in binary form must reproduce the above copyright notice,
rem   this list of conditions and the following disclaimer in the
rem   documentation and/or other materials provided with the distribution.
rem 
rem * Neither the name of Soar Technology, Inc. nor the names of its contributors
rem   may be used to endorse or promote products derived from this software
rem   without the specific prior written permission of Soar Technology, Inc.
rem 
rem THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
rem ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
rem WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
rem DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
rem FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
rem DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
rem SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
rem CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
rem OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
rem USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

rem Usage:
rem
rem simjr.bat scenario-file
rem
rem This script can be called from any directory. When called from other
rem bat scripts, be sure to use "call simjr.bat". 

rem These files must be moved from plugins up to the root...
rem org.eclipse.osgi_3.4.2.R34x_v20080826-1230.jar

setlocal
set JAVA_OPTS=-Djava.net.preferIPv4Stack=true -Declipse.ignoreApp=true -Dosgi.noShutdown=true
set SIMJR_HOME=%~dsp0
java %JAVA_OPTS% %SIMJR_OPTS% -Dsimjr.home=%SIMJR_HOME% -Dsimjr.app=sim "-Dsimjr.args=%*" -jar org.eclipse.osgi_3.4.2.R34x_v20080826-1230.jar -console -consoleLog

