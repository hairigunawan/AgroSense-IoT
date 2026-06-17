@echo off
title Android Auto Run

echo =========================
echo BUILDING APK...
echo =========================

call gradlew installDebug

echo.
echo =========================
echo OPENING APP...
echo =========================

"C:\Users\MyBook Hype\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell monkey -p com.example.it_project_2 -c android.intent.category.LAUNCHER 1

echo.
echo DONE
pause