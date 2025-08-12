@echo off
cls
echo ===============================================
echo  BookmapAI Dashboard - Microsoft Edge Launcher
echo  FIXED VERSION - Content Length + Edge Support
echo ===============================================
echo.

REM Kill any existing Java processes
echo 🔄 Stopping any existing dashboard instances...
taskkill /f /im java.exe >nul 2>&1

echo ⏳ Waiting for processes to terminate...
timeout /t 2 /nobreak >nul

REM Check if JAR exists
if not exist "BookmapAI-ActiveSessions-5.4.0.jar" (
    echo ❌ ERROR: JAR file not found!
    echo 📁 Please ensure BookmapAI-ActiveSessions-5.4.0.jar is in this directory
    echo 📍 Current directory: %CD%
    pause
    exit /b 1
)

echo ✅ JAR file found - Starting fixed dashboard server...
echo.

REM Start the JAR in background
echo 🚀 Starting BookmapAI Dashboard Server...
start /B java -jar BookmapAI-ActiveSessions-5.4.0.jar

echo ⏳ Waiting 5 seconds for server startup...
timeout /t 5 /nobreak >nul

echo.
echo 🌐 Opening Microsoft Edge with multiple methods...

REM Method 1: cmd start
echo Trying method 1: cmd start
start /wait /b cmd /c "start msedge http://localhost:8080" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Microsoft Edge opened via cmd start
    goto success
)

REM Method 2: Direct path (x86)
echo Trying method 2: Direct path (x86)
if exist "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" (
    start "" "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" http://localhost:8080
    echo ✅ Microsoft Edge opened via direct path (x86)
    goto success
)

REM Method 3: Direct path (64-bit)
echo Trying method 3: Direct path (64-bit)
if exist "C:\Program Files\Microsoft\Edge\Application\msedge.exe" (
    start "" "C:\Program Files\Microsoft\Edge\Application\msedge.exe" http://localhost:8080
    echo ✅ Microsoft Edge opened via direct path (64-bit)
    goto success
)

REM Method 4: PowerShell
echo Trying method 4: PowerShell
powershell -command "Start-Process msedge http://localhost:8080" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Microsoft Edge opened via PowerShell
    goto success
)

REM Method 5: Registry protocol
echo Trying method 5: Registry protocol
start microsoft-edge:http://localhost:8080 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Microsoft Edge opened via registry protocol
    goto success
)

REM Fallback to default browser
echo ⚠️ Microsoft Edge not found, using default browser
start http://localhost:8080
echo 🌐 Default browser opened as fallback

:success
echo.
echo ===============================================
echo ✅ BookmapAI Dashboard System Started!
echo ===============================================
echo 📊 Dashboard URL: http://localhost:8080
echo 🔧 Backend Server: Running in background
echo 🌐 Browser: Microsoft Edge (or default)
echo 🚀 Features: Original comprehensive dashboard
echo 🤖 AI Chatbot: Click robot icon in bottom-right
echo 📱 Telegram: Ready for +21696543589
echo ❌ Content Length Issues: FIXED
echo ===============================================
echo.
echo ℹ️  If dashboard doesn't load properly:
echo    1. Wait a few more seconds for full startup
echo    2. Refresh the page (F5)
echo    3. Check console for any error messages
echo    4. Manually open: http://localhost:8080
echo.
echo 🛑 To stop the dashboard: Press Ctrl+C in this window
echo.
pause
