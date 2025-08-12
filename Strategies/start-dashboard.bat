@echo off
echo ===============================================
echo  BookmapAI Dashboard Launcher
echo ===============================================
echo.
echo Starting BookmapAI Dashboard Server...
echo Dashboard will be available at: http://localhost:8080
echo.

cd /d "%~dp0"

REM Try to start the standalone dashboard
java -cp "build/classes/java/main" com.bookmaai.web.StandaloneDashboard

REM If that fails, try the complete system launcher
if errorlevel 1 (
    echo.
    echo Trying alternative launcher...
    java -cp "build/classes/java/main" com.bookmaai.core.CompleteSystemLauncher
)

REM If both fail, show error message
if errorlevel 1 (
    echo.
    echo ERROR: Could not start dashboard
    echo Please ensure Java is installed and the project is built
    echo.
    echo To build the project, run: gradle build
    echo.
)

pause
