@echo off
@echo startup spider
set SPIDER_HOME=%~f0
set SPIDER_HOME=%SPIDER_HOME:\bin\run.bat=%

@echo %SPIDER_HOME%
:::::::::: set conf
set CLASSPATH=%CLASSPATH%;%SPIDER_HOME%\conf
set CLASSPATH=%CLASSPATH%;%SPIDER_HOME%\bin\spider.jar

FOR %%F IN (%SPIDER_HOME%\lib\*.jar) DO call :addcp %%F
goto extlibe
:addcp
SET classPath=%CLASSPATH%;%1
goto :eof
:extlibe
@echo starting spider
java six.com.crawler.StartMain --spider.home=%SPIDER_HOME%
pause