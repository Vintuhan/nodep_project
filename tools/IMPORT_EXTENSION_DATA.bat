@echo off
setlocal
if "%~1"=="" (
  echo Usage: IMPORT_EXTENSION_DATA.bat "C:\path\to\unpacked\extension"
  exit /b 1
)
set SRC=%~1
set DST=%~dp0..\app\src\main\assets\nodep\data
if not exist "%SRC%\data\keywords.js" goto missing
if not exist "%SRC%\data\whitelist.js" goto missing
if not exist "%SRC%\data\seed.json" goto missing
copy /Y "%SRC%\data\keywords.js" "%DST%\keywords.js" >nul
copy /Y "%SRC%\data\whitelist.js" "%DST%\whitelist.js" >nul
copy /Y "%SRC%\data\seed.json" "%DST%\blocklist.json" >nul
if exist "%SRC%\fonts" xcopy /E /Y "%SRC%\fonts\*" "%~dp0..\app\src\main\assets\nodep\fonts\" >nul
 echo Imported local extension data into Android assets.
 exit /b 0
:missing
echo Required files not found under %SRC%\data
exit /b 1
