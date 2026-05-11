@echo off
title HireFlow - Starting...
color 0A

echo.
echo  =========================================
echo   HireFlow - Starting Application
echo  =========================================
echo.

:: Kill any existing Java process on port 9090
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":9090" 2^>nul') do (
    taskkill /F /PID %%a >nul 2>&1
)

:: Load .env file
if not exist ".env" (
    echo  [ERROR] .env file not found!
    echo  Please create a .env file with your credentials.
    pause
    exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%A in (`findstr /v "^#" .env`) do (
    if not "%%A"=="" if not "%%B"=="" set "%%A=%%B"
)

:: Validate required vars
if "%DB_PASSWORD%"=="" (
    echo  [ERROR] DB_PASSWORD is not set in .env
    pause
    exit /b 1
)
if "%JWT_SECRET%"=="" (
    echo  [ERROR] JWT_SECRET is not set in .env
    pause
    exit /b 1
)
if "%MAIL_USERNAME%"=="" (
    echo  [ERROR] MAIL_USERNAME is not set in .env
    pause
    exit /b 1
)
if "%MAIL_PASSWORD%"=="" (
    echo  [ERROR] MAIL_PASSWORD is not set in .env
    pause
    exit /b 1
)

echo  [OK] Environment loaded from .env
echo  [OK] Starting HireFlow on http://localhost:9090
echo.
echo  Press Ctrl+C to stop the application.
echo.

:: Start the app
mvn spring-boot:run

pause
