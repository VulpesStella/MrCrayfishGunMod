@echo off
rem Core set plus the red-line compatibility jars (backpacked, travelers,
rem cardinals, cloth-config, sophisticated backpacks+core).
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\fetch-libs.ps1" -OutputDirectory "%~dp0libs" -Set compat
exit /b %errorlevel%
