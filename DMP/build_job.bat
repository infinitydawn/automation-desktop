@echo off
rem Build a Remote Link job export from a job .ini - drag the .ini onto this file.
rem   build_job.bat "jobs\my job.ini"
rem See jobs\example.ini for the format. Needs DMP\settings.ini with the export key (see dmp_job.py).
setlocal
cd /d "%~dp0"
if "%~1"=="" (
  echo Drag a job .ini file onto build_job.bat
  pause
  exit /b 2
)
python dmp_job.py build "%~1"
set rc=%ERRORLEVEL%
pause
exit /b %rc%
