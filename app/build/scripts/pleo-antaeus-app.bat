@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  pleo-antaeus-app startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and PLEO_ANTAEUS_APP_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\pleo-antaeus-app-1.0.jar;%APP_HOME%\lib\pleo-antaeus-rest-1.0.jar;%APP_HOME%\lib\pleo-antaeus-core-1.0.jar;%APP_HOME%\lib\pleo-antaeus-data-1.0.jar;%APP_HOME%\lib\pleo-antaeus-models-1.0.jar;%APP_HOME%\lib\pleo-antaeus-messaging-1.0.jar;%APP_HOME%\lib\arrow-resilience-jvm-1.2.1.jar;%APP_HOME%\lib\exposed-dao-0.44.1.jar;%APP_HOME%\lib\exposed-jdbc-0.44.1.jar;%APP_HOME%\lib\exposed-core-0.44.1.jar;%APP_HOME%\lib\arrow-fx-coroutines-jvm-1.2.1.jar;%APP_HOME%\lib\suspendapp-jvm-0.4.0.jar;%APP_HOME%\lib\kotlinx-coroutines-reactor-1.7.3.jar;%APP_HOME%\lib\kotlinx-coroutines-reactive-1.7.3.jar;%APP_HOME%\lib\kotlinx-coroutines-core-jvm-1.7.3.jar;%APP_HOME%\lib\kotlinx-coroutines-slf4j-1.7.3.jar;%APP_HOME%\lib\javalin-5.6.3.jar;%APP_HOME%\lib\kotlin-logging-jvm-3.0.5.jar;%APP_HOME%\lib\kotlin-stdlib-jdk8-1.8.21.jar;%APP_HOME%\lib\arrow-core-jvm-1.2.1.jar;%APP_HOME%\lib\jackson-dataformat-yaml-2.15.3.jar;%APP_HOME%\lib\jackson-datatype-jsr310-2.15.3.jar;%APP_HOME%\lib\jackson-databind-2.15.3.jar;%APP_HOME%\lib\jackson-annotations-2.15.3.jar;%APP_HOME%\lib\jackson-core-2.15.3.jar;%APP_HOME%\lib\jackson-module-kotlin-2.15.3.jar;%APP_HOME%\lib\kotlin-reflect-1.9.10.jar;%APP_HOME%\lib\kotlin-stdlib-jdk7-1.8.21.jar;%APP_HOME%\lib\arrow-atomic-jvm-1.2.1.jar;%APP_HOME%\lib\arrow-continuations-jvm-1.2.1.jar;%APP_HOME%\lib\arrow-annotations-jvm-1.2.1.jar;%APP_HOME%\lib\kotlin-stdlib-1.9.10.jar;%APP_HOME%\lib\slf4j-simple-2.0.9.jar;%APP_HOME%\lib\sqlite-jdbc-3.30.1.jar;%APP_HOME%\lib\junit-platform-engine-1.10.0.jar;%APP_HOME%\lib\junit-jupiter-api-5.10.0.jar;%APP_HOME%\lib\junit-platform-commons-1.10.0.jar;%APP_HOME%\lib\junit-jupiter-engine-5.10.0.jar;%APP_HOME%\lib\kotlin-stdlib-common-1.9.10.jar;%APP_HOME%\lib\annotations-24.0.1.jar;%APP_HOME%\lib\websocket-jetty-server-11.0.17.jar;%APP_HOME%\lib\jetty-webapp-11.0.17.jar;%APP_HOME%\lib\websocket-servlet-11.0.17.jar;%APP_HOME%\lib\jetty-servlet-11.0.17.jar;%APP_HOME%\lib\jetty-security-11.0.17.jar;%APP_HOME%\lib\websocket-core-server-11.0.17.jar;%APP_HOME%\lib\jetty-server-11.0.17.jar;%APP_HOME%\lib\HikariCP-5.0.1.jar;%APP_HOME%\lib\quartz-2.3.2.jar;%APP_HOME%\lib\reactor-kafka-1.3.21.jar;%APP_HOME%\lib\kafka-clients-3.6.0.jar;%APP_HOME%\lib\websocket-jetty-common-11.0.17.jar;%APP_HOME%\lib\websocket-core-common-11.0.17.jar;%APP_HOME%\lib\jetty-http-11.0.17.jar;%APP_HOME%\lib\jetty-io-11.0.17.jar;%APP_HOME%\lib\jetty-xml-11.0.17.jar;%APP_HOME%\lib\HikariCP-java7-2.4.13.jar;%APP_HOME%\lib\jetty-util-11.0.17.jar;%APP_HOME%\lib\slf4j-api-2.0.9.jar;%APP_HOME%\lib\snakeyaml-2.1.jar;%APP_HOME%\lib\websocket-jetty-api-11.0.17.jar;%APP_HOME%\lib\postgresql-42.6.0.jar;%APP_HOME%\lib\animal-sniffer-annotations-1.23.jar;%APP_HOME%\lib\jetty-jakarta-servlet-api-5.0.2.jar;%APP_HOME%\lib\checker-qual-3.31.0.jar;%APP_HOME%\lib\c3p0-0.9.5.4.jar;%APP_HOME%\lib\mchange-commons-java-0.2.15.jar;%APP_HOME%\lib\reactor-core-3.4.32.jar;%APP_HOME%\lib\zstd-jni-1.5.5-1.jar;%APP_HOME%\lib\lz4-java-1.8.0.jar;%APP_HOME%\lib\snappy-java-1.1.10.4.jar;%APP_HOME%\lib\opentest4j-1.3.0.jar;%APP_HOME%\lib\reactive-streams-1.0.4.jar


@rem Execute pleo-antaeus-app
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %PLEO_ANTAEUS_APP_OPTS%  -classpath "%CLASSPATH%" io.pleo.antaeus.app.AntaeusApp %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable PLEO_ANTAEUS_APP_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%PLEO_ANTAEUS_APP_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
