@ECHO OFF
SETLOCAL
SET GRADLE_VERSION=8.11.1
SET GRADLE_HOME=%USERPROFILE%\.gradle\kakaanime-gradle\%GRADLE_VERSION%
SET GRADLE_BIN=%GRADLE_HOME%\gradle-%GRADLE_VERSION%\bin\gradle.bat
IF EXIST "%GRADLE_BIN%" GOTO RUN
ECHO Please use the Unix gradlew bootstrap on CI or install Gradle 8.11.1 locally.
EXIT /B 1
:RUN
CALL "%GRADLE_BIN%" %*
ENDLOCAL
