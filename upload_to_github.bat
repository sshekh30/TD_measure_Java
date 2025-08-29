@echo off
setlocal EnableExtensions

REM ===== CONFIG =====
set "REPO_DIR=C:\GIFT-TD Measure\TD_measure_Java"
set "BRANCH=Data"
set "DEST_SUBDIR=MIX_IntegrationTest"
REM ==================

REM --- args ---
if "%~1"=="" goto :usage
set "SRC=%~1"
set "COMMIT_MSG=%~2"

REM --- sanity checks ---
where git >nul 2>&1 || goto :nogit
if not exist "%REPO_DIR%" goto :norepo
if not exist "%REPO_DIR%\.git" goto :notgit
if not exist "%SRC%" goto :nofile

REM --- destination dir (optional subdir) ---
set "DEST_DIR=%REPO_DIR%"
if not "%DEST_SUBDIR%"=="" set "DEST_DIR=%REPO_DIR%\%DEST_SUBDIR%"
if not exist "%DEST_DIR%" mkdir "%DEST_DIR%" >nul 2>&1

REM --- copy file (overwrite) ---
copy /Y "%SRC%" "%DEST_DIR%" >nul
if errorlevel 1 goto :copyfail

REM --- commit message default (no PowerShell) ---
if "%COMMIT_MSG%"=="" set "COMMIT_MSG=Auto-upload %~nx1"

REM --- git ops ---
pushd "%REPO_DIR%" >nul

set "REL_PATH=%~nx1"
if not "%DEST_SUBDIR%"=="" set "REL_PATH=%DEST_SUBDIR%\%~nx1"

git add "%REL_PATH%"
if errorlevel 1 goto :gitaddfail

git commit -m "%COMMIT_MSG%" >nul
REM If nothing to commit, proceed anyway
git pull --rebase origin "%BRANCH%"
if errorlevel 1 goto :gitpullfail

git push origin "%BRANCH%"
if errorlevel 1 goto :gitpushfail

popd >nul
echo [OK] Uploaded "%~nx1" to branch "%BRANCH%".
exit /b 0

:usage
echo Usage: %~nx0 ^<full_path_to_file^> [optional commit message]
exit /b 1

:nogit
echo [ERROR] Git not found in PATH.
exit /b 1

:norepo
echo [ERROR] REPO_DIR not found: %REPO_DIR%
exit /b 1

:notgit
echo [ERROR] %REPO_DIR% is not a Git repo (.git missing).
exit /b 1

:nofile
echo [ERROR] Source file not found: %SRC%
exit /b 1

:copyfail
echo [ERROR] Failed to copy file into repo.
exit /b 1

:gitaddfail
echo [ERROR] git add failed for %REL_PATH%.
popd >nul
exit /b 1

:gitpullfail
echo [ERROR] git pull --rebase failed. Resolve conflicts and retry.
popd >nul
exit /b 1

:gitpushfail
echo [ERROR] git push failed.
popd >nul
exit /b 1
