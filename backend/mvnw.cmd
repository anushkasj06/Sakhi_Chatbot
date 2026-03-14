@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script, version 3.3.2
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar
set WRAPPER_PROPERTIES=%~dp0.mvn\wrapper\maven-wrapper.properties

if not exist "%WRAPPER_JAR%" (
  echo Maven Wrapper jar not found: %WRAPPER_JAR%
  echo Run: powershell -NoProfile -Command "Invoke-WebRequest -Uri (Select-String -Path '%WRAPPER_PROPERTIES%' -Pattern '^wrapperUrl=').Line.Substring(11) -OutFile '%WRAPPER_JAR%'"
  exit /b 1
)

set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

@REM Execute the wrapper
java -classpath "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR% org.apache.maven.wrapper.MavenWrapperMain %*
endlocal
