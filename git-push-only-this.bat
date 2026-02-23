@echo off
chcp 65001 >nul
cd /d "%~dp0"

if not exist ".git" (
  echo [0/5] Git init...
  git init
)

echo [1/5] Remote (overwrite if exists)...
git remote remove origin 2>nul
git remote add origin https://github.com/longlong64bit/vulnable-board-web.git

echo [2/5] Git add...
git add .

echo [3/5] Git commit...
git commit -m "Initial commit: vulnerable board web" 2>nul
if errorlevel 1 (
  echo No changes or already committed. Trying push anyway.
)

echo [4/5] Branch to main...
git branch -M main

echo [5/5] Push (overwrite remote with only this folder)...
git push -u origin main --force

echo.
echo Done. Only 00_vulnerable-board_web contents are in the repo.
pause
