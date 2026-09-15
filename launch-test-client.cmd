@echo off
setlocal
pushd "%~dp0"
if errorlevel 1 exit /b 1

rem ---------------------------------------------------------------------------
rem Production test client for CGM on Fabric 1.20.1.
rem
rem Usage:  launch-test-client.cmd [combo] [--check]
rem
rem   combo   base | backpacked | travelers | sophisticated | jei (default) |
rem           all-supported
rem
rem   base           CGM + required prerequisites only. NO red-line mods are
rem                  loaded. This is the combination that proves CGM still runs
rem                  with none of the compatibility jars present.
rem   backpacked     base + Backpacked
rem   travelers      base + Traveler's Backpack (+ Cardinal Components, Cloth Config)
rem   sophisticated  base + Sophisticated Backpacks (+ Sophisticated Core)
rem   jei            base + JEI
rem   all-supported  base + all of the above
rem
rem Each combination runs in its own directory under
rem run\fabric-1.20.1\compat\<combo>\client, so worlds and configs never mix.
rem
rem --check verifies the required jars (download + SHA-256) without launching.
rem
rem Gradle/Loom needs JDK 21 to build; the actual Minecraft client runs on Java 17.
rem ---------------------------------------------------------------------------

set "COMBO=%~1"
set "CHECK_ONLY=0"
if /i "%~1"=="--check" set "CHECK_ONLY=1"
if /i "%~2"=="--check" set "CHECK_ONLY=1"
if "%COMBO%"=="" set "COMBO=jei"
if /i "%COMBO%"=="--check" set "COMBO=jei"
if /i "%COMBO%"=="--help" goto usage
if /i "%COMBO%"=="-h" goto usage

rem Map the combination name to the Gradle production task suffix.
rem base maps to the bare prodClient, matching the pre-existing documented command.
set "SUFFIX="
set "VALID="
if /i "%COMBO%"=="base"          set "VALID=1"
if /i "%COMBO%"=="backpacked"    set "SUFFIX=Backpacked"    & set "VALID=1"
if /i "%COMBO%"=="travelers"     set "SUFFIX=Travelers"     & set "VALID=1"
if /i "%COMBO%"=="sophisticated" set "SUFFIX=Sophisticated" & set "VALID=1"
if /i "%COMBO%"=="jei"           set "SUFFIX=Jei"           & set "VALID=1"
if /i "%COMBO%"=="all-supported" set "SUFFIX=AllSupported"  & set "VALID=1"
if not defined VALID (
    echo [ERROR] Unknown combination: %COMBO%
    goto usage
)

set "JAVA_HOME=%~dp0..\AirdropMod\tooling\jdk-21.0.12.1+1"
set "CGM_TEST_JAVA17=%~dp0..\tooling\jdk-17.0.20.1+1"
set "PATH=%JAVA_HOME%\bin;%PATH%"
rem Avoid inheriting incompatible PowerShell 7 module paths in Windows PowerShell.
set "PSModulePath=%SystemRoot%\System32\WindowsPowerShell\v1.0\Modules"
set "CGM_EXIT=1"
set "CGM_LAUNCH_ARGS="

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] Build JDK 21 not found: %JAVA_HOME%
    goto finish
)
if not exist "%CGM_TEST_JAVA17%\bin\java.exe" (
    echo [ERROR] Game Java 17 not found: %CGM_TEST_JAVA17%
    goto finish
)

echo === CGM Fabric 1.20.1 production test client ===
echo Combination: %COMBO%
if /i "%COMBO%"=="base" (
    echo Red-line compatibility mods: NONE ^(base never loads them^)
    echo Run dir: %~dp0run\fabric-1.20.1\production-client
) else (
    echo Red-line compatibility mods: supplied by the %COMBO% production task
    echo Run dir: %~dp0run\fabric-1.20.1\compat\%COMBO%\client
)
echo First launch may download dependencies. Please keep this window open.
echo.

if "%CHECK_ONLY%"=="1" goto check

call "%~dp0fetch-compat-libs.cmd"
if errorlevel 1 goto finish

call "%~dp0gradlew.bat" --no-daemon "-Porg.gradle.java.installations.paths=%CGM_TEST_JAVA17%" "prodClient%SUFFIX%" --console=plain %CGM_LAUNCH_ARGS%
set "CGM_EXIT=%errorlevel%"

goto finish

:check
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\fetch-libs.ps1" -Set compat -VerifyOnly
if errorlevel 1 goto finish
call "%~dp0gradlew.bat" --no-daemon "-Porg.gradle.java.installations.paths=%CGM_TEST_JAVA17%" "prodClient%SUFFIX%" --console=plain --offline --dry-run
set "CGM_EXIT=%errorlevel%"
goto finish

:usage
echo Usage: launch-test-client.cmd [combo] [--check]
echo   combo: base ^(default^) ^| backpacked ^| travelers ^| sophisticated ^| jei ^| all-supported
echo   --check: verify jars and task wiring without launching the game
echo.
echo Dedicated server: use scripts\verify-combos.ps1 -Side server, or
echo gradlew prodServer^<Combo^> ^(see docs\compat-redline-1.20.1\DEPENDENCIES.md^).
exit /b 2

:finish
if not "%CGM_EXIT%"=="0" echo [ERROR] Client launch/check failed. See the output above.
popd
if "%CHECK_ONLY%"=="0" pause
exit /b %CGM_EXIT%
