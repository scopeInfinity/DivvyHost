@echo off
set JAR_FILE=DivvyHost.jar
set LOG_FILE=%temp%\divvy.log
set PID_FILE=%temp%\divvy.pid
;set BASEDIR=%~dp0
set HOME=C:
set INSTALLDIR=%HOME%\Divvy

set JAR_CP=-cp %JAR_FILE%;lib\kryonet-2.21-all.jar divvyhost.DivvyHost

:: init Testing
:: Check, if Everything is Alright

	where java > NUL 2>&1
	if %ERRORLEVEL%==0 goto:init2
	:: if `java` is not found

	echo `java` not found!
	echo[ Make Sure `java` is Included in your %%PATH%%
	echo[ 
	echo[ If java not Installed, then try
	echo[   http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
	echo[   After Installation, add `java` to your %%PATH%%
	echo[ 
	echo Terminating!
	EXIT /B 1

::Check Python
:init2
	where python > NUL 2>&1
	if %ERRORLEVEL%==0 goto:initfine
	:: if `python` is not found
	echo `python` not found!
	echo[ Make Sure `python` is Included in your %%PATH%%
	echo[ 
	echo[ If python 2.7 is not Installed, then try
	echo[   https://www.python.org/downloads/ 
	echo[   After Installation, add `python` to your %%PATH%%
	echo[
	echo Ignoring Python...
	echo[ Web Hosting may not work
	echo[ 

:initfine

:: Startup File
goto createAutoStartCont
:createAutoStart
	set STARTFILE="%USERPROFILE%\Start Menu\Programs\Startup\divvy.cmd"
	if not exist %INSTALLDIR%\bin.cmd% (
			echo[ Not Installed Properly!
			EXIT /B 1
		)
	echo @echo on > %STARTFILE% || (
		echo[ Startup File Creation Failed!!
		EXIT /B 1
		)
	echo %INSTALLDIR%\bin.cmd start >> %STARTFILE%
	echo pause >> %STARTFILE%
	
	echo[
	echo[ Startup Created!
	echo[
EXIT /B 0
:createAutoStartCont

:: Print Usage
goto _helpContinue
:_help 
	echo Usage^: bin.cmd ^<option^>
	echo Usage^: bin.cmd install
	echo[
	echo Options^:
	echo[ -help 		Help
	echo[ -run 		Run With GUI
	echo[ -nogui 	Run Without GUI
	echo[ -test 		Check Any Running Instance
	echo[ -show 		Show GUI
	echo[ -hide 		Hide GUI
	echo[
	echo Service Options^:
	echo[ start 		Start DivvyHost
	echo[ stop		Stop DivvyHost
	echo[ restart	Restart DivvyHost
	echo[
EXIT /B 0
:_helpContinue

::Start DivvyHost
goto startContinue
:start
	pushd %BASEDIR%
	start javaw %JAR_CP% -nogui > %LOG_FILE% 2>&1 
	::echo $! > $PID_FILE;
	popd
	echo DivvyHost Started Called

EXIT /B 0
:startContinue

::Stop DivvyHost
goto stopContinue
:stop
::	if [ -f $PID_FILE ]
::	then
::		cat $PID_FILE | xargs kill;
::		echo "DivvyHost Stop Called"
::	else
::		echo "No DivvyHost Last Instance"
::	fi
EXIT /B 0
:stopContinue

::Install
goto installContinue
:install 

	echo Installing
	mkdir %INSTALLDIR% > NUL 2>&1
	if %ERRORLEVEL%==0 echo Directory Created %INSTALLDIR%

	mkdir %INSTALLDIR%\lib > NUL 2>&1
	copy /Y %BASEDIR% %INSTALLDIR%
	if not %ERRORLEVEL%==0 (
		echo Unable to Copy from %BASEDIR% to %INSTALLDIR%
		EXIT 1
	)
	copy /Y %BASEDIR%\lib %INSTALLDIR%\lib
	if not %ERRORLEVEL%==0 (
		echo Unable to Copy from %BASEDIR%\lib to %INSTALLDIR%\lib
		EXIT 1
	)
	
	call:createAutoStart

EXIT /B 0
:installContinue

goto launchContinue
:launch
	pushd %BASEDIR%
	::generate Arguments
	set arg=%1
	if not "%2"=="" set arg=%1=%2
	java %JAR_CP% %arg%
	popd
EXIT /B 0
:launchContinue

if "%1"==""  (
	:: If not Arguments
	call:_help
) else (

	IF "%1"=="-help" (call:_help
	) ELSE IF "%1"=="-run" (call:launch
	) ELSE IF "%1"=="-nogui" (call:launch -nogui
	) ELSE IF "%1"=="-test" (call:launch -service=test
	) ELSE IF "%1"=="-show" (call:launch -service=startgui
	) ELSE IF "%1"=="-hide" (call:launch -service=stopgui
	) ELSE IF "%1"=="install" (call:install
	) ELSE IF "%1"=="start" (call:start
	) ELSE IF "%1"=="stop" (call:stop
	) ELSE IF "%1"=="restart" (
		call:start
		call:stop
	) ELSE (
		echo Invalid Option!
		call:_help
	)
)
:END