@echo off
setlocal

set SCRIPT_DIR=%~dp0
set FX_SDK=%SCRIPT_DIR%javafx-sdk
set FX_LIB=%FX_SDK%\lib
set APP_JAR=%SCRIPT_DIR%ExpenseManager.jar

if not exist "%FX_LIB%" (
    echo Could not find embedded JavaFX SDK under "%FX_LIB%".
    echo Make sure the javafx-sdk folder ships with this distribution.
    exit /b 1
)

if not exist "%APP_JAR%" (
    echo Could not find ExpenseManager.jar beside this script.
    exit /b 1
)

echo Launching Expense Manager...
java --module-path "%FX_LIB%" --add-modules javafx.controls,javafx.fxml -jar "%APP_JAR%"

endlocal


