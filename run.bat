@echo off
title HireFlow

echo.
echo  =========================================
echo   HireFlow - Build and Run
echo  =========================================
echo.

echo  [1/2] Building project...
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo  BUILD FAILED. Check errors above.
    pause
    exit /b 1
)
echo  Build successful!
echo.

echo  [2/2] Starting HireFlow on http://localhost:9090
echo  Press Ctrl+C to stop the server.
echo.

java -jar target\HireFlow-0.0.1-SNAPSHOT.jar ^
  --spring.datasource.url=jdbc:mysql://localhost:3306/hireflow ^
  --spring.datasource.username=root ^
  --spring.datasource.password=Lavanya@123 ^
  --spring.mail.username=dineshkalapati2745@gmail.com ^
  --spring.mail.password=cbqwzaqfxeoowpbp ^
  --jwt.secret=HireFlowSuperSecretJwtKey2024Secure!!

pause
