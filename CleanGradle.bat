@echo off
echo =====================================
echo   Android Project Safe Cleaner
echo =====================================
echo.

echo [1/5] Stopping Gradle daemons...
call gradlew --stop >nul 2>&1

echo [2/5] Running Gradle clean...
call gradlew clean

echo [3/5] Removing build folders...
for /d /r . %%d in (build) do @if exist "%%d" (
    echo Deleting %%d
    rmdir /s /q "%%d"
)

echo [4/5] Removing Gradle cache (.gradle)...
if exist ".gradle" rmdir /s /q ".gradle"

echo [5/5] Removing local IDE temp files...
if exist ".idea\caches" rmdir /s /q ".idea\caches"
if exist ".idea\workspace.xml" del /f /q ".idea\workspace.xml"
if exist "local.properties" del /f /q "local.properties"

echo.
echo =====================================
echo   CLEAN COMPLETE SUCCESSFULLY
echo =====================================
pause