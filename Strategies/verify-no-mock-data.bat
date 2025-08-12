@echo off
echo.
echo ===============================================
echo    BookmapAI Mock Data Verification
echo    Checking for Math.random() and simulation
echo ===============================================
echo.

echo Searching for Math.random() calls...
findstr /S /N /C:"Math.random" src\main\java\*.java 2>nul
if %errorlevel% equ 0 (
    echo.
    echo ❌ WARNING: Math.random() calls found above!
    echo These should be eliminated for 100%% real data.
) else (
    echo ✅ No Math.random() calls found - GOOD!
)

echo.
echo Searching for simulation keywords...
findstr /S /N /C:"simulate" /C:"mock" /C:"fake" /C:"dummy" src\main\java\*.java 2>nul
if %errorlevel% equ 0 (
    echo.
    echo ⚠️  Simulation-related keywords found above.
    echo Review to ensure they are comments or disabled code.
) else (
    echo ✅ No active simulation keywords found - GOOD!
)

echo.
echo Checking for BookmapAISimulator usage...
findstr /S /N /C:"BookmapAISimulator" src\main\java\*.java 2>nul
if %errorlevel% equ 0 (
    echo.
    echo ❌ WARNING: BookmapAISimulator references found!
    echo This simulator should be completely disabled.
) else (
    echo ✅ No BookmapAISimulator usage found - GOOD!
)

echo.
echo ===============================================
echo    Verification Complete
echo ===============================================
echo.
echo If all checks show ✅, the system is clean of mock data.
echo The dashboard will only show real data from Bookmap.
echo.
pause





