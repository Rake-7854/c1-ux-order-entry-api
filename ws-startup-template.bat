@echo off
rem ---------------------------------------------------------------------------
rem Main Script for ConnectOne UX API startup
rem
rem Environment Variable Prequisites
rem
rem   APP_HOME        Home of the web app installation. If not set I will  try
rem                   to figure it out.
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem
rem   JAVA_OPTS       (Optional) Java runtime options used when the commands
rem                   is executed.
rem ---------------------------------------------------------------------------

rem ----- if JAVA_HOME is not set we're not happy ------------------------------
:checkJava

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
goto checkServer

:noJavaHome
echo "You must set the JAVA_HOME variable before running the app."
goto end

rem ----- Only set APP_HOME if not already set ----------------------------
:checkServer
setlocal enabledelayedexpansion
rem %~sdp0 is expanded pathname of the current script under NT with spaces in the path removed
if "%APP_HOME%"=="" set APP_HOME=%~sdp0
SET curDrive=%cd:~0,1%
SET wsasDrive=%APP_HOME:~0,1%
if not "%curDrive%" == "%wsasDrive%" %wsasDrive%:
echo APP_HOME environment variable is set to %APP_HOME%

rem find APP_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x
if not exist "%APP_HOME%\README.md" goto noServerHome

goto setupArgs

:noServerHome
echo APP_HOME is set incorrectly or the app could not be located. Please set APP_HOME.
goto end

:setupArgs
if ""%1""=="""" goto doneStart

if ""%1""==""-run""     goto commandLifecycle
if ""%1""==""--run""    goto commandLifecycle
if ""%1""==""run""      goto commandLifecycle

if ""%1""==""-restart""  goto commandLifecycle
if ""%1""==""--restart"" goto commandLifecycle
if ""%1""==""restart""   goto commandLifecycle

if ""%1""==""debug""    goto commandDebug
if ""%1""==""-debug""   goto commandDebug
if ""%1""==""--debug""  goto commandDebug

if ""%1""==""optimize""  	goto profileOptimizer
if ""%1""==""-optimize"" 	goto profileOptimizer
if ""%1""==""--optimize""	goto profileOptimizer

shift
goto setupArgs

rem ----- commandDebug ---------------------------------------------------------
:commandDebug
shift
set DEBUG_PORT=%1
if "%DEBUG_PORT%"=="" goto noDebugPort
if not "%JAVA_OPTS%"=="" echo Warning !!!. User specified JAVA_OPTS will be ignored, once you give the --debug option.
set JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=%DEBUG_PORT%
echo Please start the remote debugging client on port %DEBUG_PORT% to continue...
goto findJdk

:noDebugPort
echo Please specify the debug port after the --debug option
goto end

rem ----- commandLifecycle -----------------------------------------------------
:commandLifecycle
goto findJdk

rem ----- profile optimization then start the server---------------------------------
:profileOptimizer
setlocal enableDelayedExpansion
set found=false
for %%a in (!originalArgs!) do (
	if !found!==true (
		set profile=-Dprofile=%%a
		set found=false
	)
	if %%a==-Dprofile ( set found=true
	)
)

for %%a in (!originalArgs!) do (
	if %%a==--skipConfigOptimization (
	    set skipConfigOptimizationOption=%%a
	    goto runProfileSetup
	)
	if %%a==-skipConfigOptimization (
	    set skipConfigOptimizationOption=%%a
        goto runProfileSetup
    )
    if %%a==skipConfigOptimization (
	    set skipConfigOptimizationOption=%%a
        goto runProfileSetup
    )
)

:runProfileSetup
call bin\profileSetup.bat %profile% %skipConfigOptimizationOption%
endlocal
goto findJdk

:doneStart
if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

rem ---------- Handle the SSL Issue with proper JDK version --------------------
rem find the version of the jdk
:findJdk

rem set CMD=RUN %*

:checkJdk17
PATH %PATH%;%JAVA_HOME%\bin\
for /f tokens^=2-5^ delims^=.-_^" %%j in ('java -fullversion 2^>^&1') do set "JAVA_VERSION=%%j%%k"
if %JAVA_VERSION% LSS 17 goto unknownJdk
if %JAVA_VERSION% GTR 110 goto unknownJdk
goto jdk17

:unknownJdk
echo Starting the web app (in unsupported JDK)
echo [ERROR] the web app is supported only on JDK 1.7, 1.8, 9, 10 and 11
goto jdk17

:jdk17
goto runServer

rem ----------------- Execute The Requested Command ----------------------------

:runServer
cd %APP_HOME%

rem ---------- Add jars to classpath ----------------

set CP_REPO_ROOT=C:/git/custompoint
set APP_CLASSPATH="./target/api-1.0.0-SNAPSHOT.jar";"%CP_REPO_ROOT%/Properties"

rem if %JAVA_VERSION% GEQ 110 set APP_CLASSPATH=".\lib\endorsed\*";%APP_CLASSPATH%

rem if %JAVA_VERSION% LEQ 18 set JAVA_VER_BASED_OPTS=-Djava.endorsed.dirs=".\lib\endorsed";"%JAVA_HOME%\jre\lib\endorsed";"%JAVA_HOME%\lib\endorsed"
rem if %JAVA_VERSION% GEQ 110 set JAVA_VER_BASED_OPTS=--add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens java.rmi/sun.rmi.transport=ALL-UNNAMED

rem set CMD_LINE_ARGS=-Xms256m -Xmx2048m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath="%APP_HOME%\logs\heap-dump.hprof"
set CMD_LINE_ARGS=-Xms256m -Xmx2048m
rem set CMD_LINE_ARGS=%CMD_LINE_ARGS% -Dcom.sun.management.jmxremote -classpath %APP_CLASSPATH% %JAVA_OPTS% %JAVA_VER_BASED_OPTS%
set CMD_LINE_ARGS=%CMD_LINE_ARGS% -Dcom.sun.management.jmxremote -cp %APP_CLASSPATH% %JAVA_OPTS% -Dspring.config.location=./src/main/resources/application.yml
rem set CMD_LINE_ARGS=%CMD_LINE_ARGS% -Djava.opts="%JAVA_OPTS%" -Djava.io.tmpdir="%APP_HOME%\tmp" -Dcom.sun.tools.javac.main.largebranch=true -Dorg.apache.commons.collections.enableUnsafeSerialization=true
set CMD_LINE_ARGS=%CMD_LINE_ARGS% -Djava.opts="%JAVA_OPTS%" -Dcom.sun.tools.javac.main.largebranch=true -Dorg.apache.commons.collections.enableUnsafeSerialization=true
set CMD_LINE_ARGS=%CMD_LINE_ARGS% -Dtt.arch.env.var=X4 -DXS.SERVER.TOKEN=WSAD -DXS.SERVER.ID=1 -Djxl.nogc=true -DXS.APP.LOCALE=en_US -Dfile.encoding=UTF8 -DCP_ENV=RAD -DENV_CODE=WSAD
set CMD_LINE_ARGS=%CMD_LINE_ARGS% -Dtt.ParmCheckClass=com.wallace.atwinxs.framework.util.AtWinXSParmChecker -Dtt.PortalCookieClass=com.rrd.custompoint.framework.component.AuthServicesProcessorImpl -Dtt.SessionAuditClass=com.wallace.atwinxs.admin.dao.LoginAuditDAO -Dtt.SiteClass=com.wallace.atwinxs.framework.dao.LoginProcessDAO -Dtt.UserClass=com.wallace.atwinxs.framework.dao.LoginProcessDAO 
set CMD_LINE_ARGS=%CMD_LINE_ARGS% -DSERVER_LOG_ROOT="C:/xs2files/Logfiles" -Dtranslation="C:/xs2files/translation"
set CMD_LINE_ARGS=%CMD_LINE_ARGS% -Dmssqldriver="%CP_REPO_ROOT%/Servers/Runtimes/sqlserver/sqljdbc4.jar"
set CMD_LINE_ARGS=%CMD_LINE_ARGS% -Dthirdparty="%CP_REPO_ROOT%/ThirdParty"
set CMD_LINE_ARGS=%CMD_LINE_ARGS% -Dwurfl="%CP_REPO_ROOT%/Servers/Runtimes/wurfl"
set CMD_LINE_ARGS=%CMD_LINE_ARGS% -Djaxws="%CP_REPO_ROOT%/Servers/Runtimes/jaxws-ri-2.2.10"
set CMD_LINE_ARGS=%CMD_LINE_ARGS% -Dproperties="%CP_REPO_ROOT%/Properties"
set CMD_LINE_ARGS=%CMD_LINE_ARGS% -Dmqconnector="%CP_REPO_ROOT%/Servers/Runtimes/mq"
set CMD_LINE_ARGS=%CMD_LINE_ARGS% -Ddb2pwd_path="%CP_REPO_ROOT%/Servers/Runtimes/conf/db2_pwd.txt"
set CMD_LINE_ARGS=%CMD_LINE_ARGS% -Djava.endorsed.dirs=%JAVA_ENDORSED% -Dhttpclient.hostnameVerifier="DefaultAndLocalhost"

:runJava
echo JAVA_HOME environment variable is set to %JAVA_HOME%
echo CMD_LINE_ARGS is set to %CMD_LINE_ARGS%
"%JAVA_HOME%\bin\java" %CMD_LINE_ARGS% -Dloader.main=com.rrd.c1ux.api.ApiApplication org.springframework.boot.loader.PropertiesLauncher
if "%ERRORLEVEL%"=="121" goto runJava
:end
goto endlocal

:endlocal

:END
