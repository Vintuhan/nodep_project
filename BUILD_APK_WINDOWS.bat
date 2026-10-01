@echo off
setlocal EnableExtensions
cd /d "%~dp0"

echo ===============================================
echo nodep project - Android APK builder
echo ===============================================

echo.
if not exist "%LOCALAPPDATA%\Android\Sdk" (
  echo Android SDK not found at %%LOCALAPPDATA%%\Android\Sdk
  echo Install Android Studio first, then run this file again.
  pause
  exit /b 1
)
set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
set PATH=%ANDROID_HOME%\platform-tools;%PATH%

echo Checking Java...
java -version >nul 2>&1
if errorlevel 1 (
  echo Java was not found. Install a JDK supported by your Android Studio.
  pause
  exit /b 1
)

if not exist "%~dp0.gradle-dist\gradle-9.5\bin\gradle.bat" (
  echo Downloading Gradle 9.5...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$u='https://services.gradle.org/distributions/gradle-9.5-bin.zip'; $o='gradle.zip'; Invoke-WebRequest -UseBasicParsing $u -OutFile $o" || goto fail
  if not exist ".gradle-dist" mkdir ".gradle-dist"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force 'gradle.zip' '.gradle-dist'" || goto fail
  del /q gradle.zip
)

call ".gradle-dist\gradle-9.5\bin\gradle.bat" assembleDebug
if errorlevel 1 goto fail

echo.
echo APK created:
echo %CD%\app\build\outputs\apk\debug\app-debug.apk
pause
exit /b 0

:fail
echo.
echo Build failed. Open this project in Android Studio to see the exact Gradle/SDK error.
pause
exit /b 1
