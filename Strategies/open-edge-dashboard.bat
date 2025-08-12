@echo off
echo ===============================================
echo  BookmapAI Dashboard - Microsoft Edge Launcher
echo ===============================================
echo.
echo Opening BookmapAI Dashboard in Microsoft Edge...
echo URL: http://localhost:8080
echo.

REM Try different ways to open Microsoft Edge
echo Attempting to launch Microsoft Edge...

REM Method 1: Direct msedge command
start msedge http://localhost:8080
if errorlevel 1 (
    echo Method 1 failed, trying method 2...
    
    REM Method 2: Using full path
    start "" "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" http://localhost:8080
    if errorlevel 1 (
        echo Method 2 failed, trying method 3...
        
        REM Method 3: Using registry
        start microsoft-edge:http://localhost:8080
        if errorlevel 1 (
            echo Method 3 failed, using default browser...
            
            REM Method 4: Default browser fallback
            start http://localhost:8080
        )
    )
)

echo.
echo ✅ Dashboard should now be open in Microsoft Edge
echo 📊 URL: http://localhost:8080
echo.
echo If the dashboard doesn't load:
echo 1. Ensure the backend server is running
echo 2. Check that port 8080 is not blocked
echo 3. Manually navigate to http://localhost:8080
echo.

timeout /t 5 /nobreak >nul
