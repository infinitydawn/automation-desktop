@echo off
rem Correlation checker: reads a configurator job (.mdb) and reports correlation mistakes. Never changes the job.
rem   checker.bat                       pick the job file in a file browser
rem   checker.bat "job.mdb"             check an exported job
rem   checker.bat "job.mdb" --csv out.csv --rules FSAE
rem Exit code: 0 = no errors, 1 = errors found, 2 = could not check
setlocal
cd /d "%~dp0"
javac -encoding UTF-8 -cp "./dependencies/*" -d . checker/*.java || exit /b 2
java -cp "./dependencies/*;." checker.CorrelationChecker %*
set rc=%ERRORLEVEL%
if "%~1"=="" PAUSE
exit /b %rc%
