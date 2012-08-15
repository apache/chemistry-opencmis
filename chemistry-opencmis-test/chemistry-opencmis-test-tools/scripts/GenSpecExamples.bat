@echo OFF
set JAVA_HOME=D:\Java\jdk1.6.0_22-x64
set PATH=%JAVA_HOME%\bin;%PATH%
call cp.bat
java -cp %CP% org.apache.chemistry.opencmis.tools.specexamples.Main
