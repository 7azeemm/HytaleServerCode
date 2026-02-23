@echo off
cd /d "%~dp0decompiled"
tar -a -c -f "..\HytaleServer-sources.zip" *
ren "..\HytaleServer-sources.zip" "HytaleServer-sources.jar"
echo Done.
pause
