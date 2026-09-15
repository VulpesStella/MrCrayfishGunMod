@echo off
rem Core set: framework, forgeconfigapiport, jei, cmdcam + Framework's nested jars.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\fetch-libs.ps1" -OutputDirectory "%~dp0libs" -Set core
exit /b %errorlevel%
