@echo off
rem Starts the bot as administrator. Needed for configurators that run as administrator (e.g. FX-400):
rem Windows blocks a normal program from typing into, or seeing hotkeys from, an administrator program.
cd /d "%~dp0"

rem The admin login can't see Python packages installed for this user (pandas), so hand their folder over
set "BOT_DIR=%~dp0"
set "BOT_USER_SITE="
for /f "delims=" %%p in ('python -m site --user-site 2^>nul') do set "BOT_USER_SITE=%%p"

powershell -NoProfile -Command "Start-Process -FilePath ($env:BOT_DIR + 'start.bat') -ArgumentList ([char]34 + $env:BOT_USER_SITE + [char]34) -Verb RunAs"
if errorlevel 1 (
    echo Could not start the bot as administrator.
    pause
)
