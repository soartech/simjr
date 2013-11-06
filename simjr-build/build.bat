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


setlocal
set PATH=%PATH%;dist\plugins\vendor\soarspeak-1.0.2\bin
set ANT4ECLIPSE_VERSION=1.0.0.M3
set ANT4ECLIPSE_HOME=tools\org.ant4eclipse_%ANT4ECLIPSE_VERSION%
ant -lib %ANT4ECLIPSE_HOME%\org.ant4eclipse_%ANT4ECLIPSE_VERSION%.jar -lib %ANT4ECLIPSE_HOME%\libs %*
