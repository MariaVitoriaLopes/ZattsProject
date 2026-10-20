@echo off
setlocal EnableExtensions
title Zatts - Configurar Android
cd /d "%~dp0"

echo ========================================
echo       ZATTS - CONFIGURACAO ADB
echo ========================================
echo.

set "ADB="

REM Procurar ADB nas variaveis do Android SDK
if defined ANDROID_HOME (
    if exist "%ANDROID_HOME%\platform-tools\adb.exe" (
        set "ADB=%ANDROID_HOME%\platform-tools\adb.exe"
    )
)

if not defined ADB (
    if defined ANDROID_SDK_ROOT (
        if exist "%ANDROID_SDK_ROOT%\platform-tools\adb.exe" (
            set "ADB=%ANDROID_SDK_ROOT%\platform-tools\adb.exe"
        )
    )
)

REM Procurar ADB no local.properties do projeto
if not defined ADB (
    if exist "local.properties" (
        for /f "tokens=1,* delims==" %%A in ('findstr /b "sdk.dir=" "local.properties"') do (
            set "SDK_DIR=%%B"
        )
        if defined SDK_DIR (
            set "SDK_DIR=%SDK_DIR:\=/%"
            set "SDK_DIR=%SDK_DIR:\\=\%"
            if exist "%SDK_DIR%\platform-tools\adb.exe" (
                set "ADB=%SDK_DIR%\platform-tools\adb.exe"
            )
        )
    )
)

REM Procurar ADB no PATH
if not defined ADB (
    for /f "delims=" %%A in ('where adb.exe 2^>nul') do (
        if not defined ADB set "ADB=%%A"
    )
)

if not defined ADB (
    echo ERRO: Android SDK Platform-Tools nao encontrado.
    echo Verifique a instalacao do Android Studio.
    pause
    exit /b 1
)

echo ADB encontrado:
echo %ADB%
echo.

"%ADB%" start-server

echo Dispositivos conectados:
"%ADB%" devices
echo.

set /a COUNT=0
for /f "skip=1 tokens=1,2" %%A in ('"%ADB%" devices') do (
    if "%%B"=="device" set /a COUNT+=1
)

if %COUNT% EQU 0 (
    echo ERRO: Nenhum celular autorizado encontrado.
    echo Conecte o celular e habilite a depuracao USB.
    pause
    exit /b 1
)

if %COUNT% GTR 1 (
    echo ERRO: Mais de um dispositivo conectado.
    echo Deixe apenas um dispositivo ativo.
    pause
    exit /b 1
)

echo Configurando porta 8081...
"%ADB%" reverse tcp:8081 tcp:8081

if errorlevel 1 (
    echo ERRO ao configurar ADB Reverse.
    pause
    exit /b 1
)

echo.
echo ========================================
echo    ADB REVERSE CONFIGURADO!
echo ========================================
echo.
"%ADB%" reverse --list
echo.
echo Agora execute o aplicativo no Android Studio.
pause
