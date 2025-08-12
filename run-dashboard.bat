@echo off
echo ===============================================
echo  BookmapAI Dashboard - NEW JAR LAUNCHER
echo ===============================================
echo.
echo 🚀 Starting BookmapAI Dashboard Server...
echo 📦 JAR File: BookmapAI-ActiveSessions-5.4.0.jar
echo 🌐 Dashboard URL: http://localhost:8080
echo.

REM Check if JAR exists
if not exist "BookmapAI-ActiveSessions-5.4.0.jar" (
    echo ❌ ERROR: JAR file not found!
    echo 📁 Please ensure BookmapAI-ActiveSessions-5.4.0.jar is in this directory
    pause
    exit /b 1
)

echo ✅ JAR file found - Starting server...
echo.

REM Start the JAR in background
start /B java -jar BookmapAI-ActiveSessions-5.4.0.jar

echo ⏳ Waiting 10 seconds for server startup...
timeout /t 10 /nobreak >nul

echo.
echo 🌐 Opening Microsoft Edge...

REM Try different methods to open Microsoft Edge
start msedge http://localhost:8080
if errorlevel 1 (
    echo Method 1 failed, trying method 2...
    start "" "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" http://localhost:8080
    if errorlevel 1 (
        echo Method 2 failed, trying method 3...
        start microsoft-edge:http://localhost:8080
        if errorlevel 1 (
            echo Method 3 failed, using default browser...
            start http://localhost:8080
        )
    )
)

echo.
echo ✅ BookmapAI Dashboard System Started!
echo 📊 Dashboard URL: http://localhost:8080
echo 🔧 Backend Server: Running in background
echo 🌐 Browser: Microsoft Edge (or default)
echo.
echo ℹ️  If dashboard doesn't load:
echo    1. Wait a few more seconds for full startup
echo    2. Manually open: http://localhost:8080
echo    3. Check that port 8080 is not blocked
echo.
echo Press any key to continue...
pause >nul
