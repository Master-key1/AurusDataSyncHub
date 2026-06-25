@echo off
setlocal

echo ==========================================
echo Maven Build Script
echo ==========================================
echo Current Directory: %CD%
echo.

if exist target (
    echo Attempting to delete target folder...
    attrib -R target /S /D >nul 2>&1
    rmdir /S /Q target >nul 2>&1
)

if exist target (
    echo.
    echo WARNING: Target folder is locked.
    echo Running Maven INSTALL only...
    echo.
    call mvn install -DskipTests
) else (
    echo.
    echo Target folder deleted successfully.
    echo Running Maven CLEAN INSTALL...
    echo.
    call mvn clean install -DskipTests
)

endlocal
