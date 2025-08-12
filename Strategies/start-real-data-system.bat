@echo off
echo.
echo ===============================================
echo    BookmapAI Real Data System Launcher
echo    Version 5.4.0 - 100%% REAL DATA ONLY
echo ===============================================
echo.
echo Starting Real Data System...
echo - Dashboard: http://localhost:8080
echo - Data Source: REAL Bookmap feeds ONLY
echo - Simulation: COMPLETELY DISABLED
echo.

REM Use the simple server without external dependencies
java -cp "src/main/java" com.bookmaai.web.SimpleRealDataServer 8080

echo.
echo Real Data System stopped.
pause





