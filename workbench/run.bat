@echo off

:begin
set libsvm=%cd%\lib\libsvm-3.22\java\*
set neuroph=%cd%\lib\neuroph-2.7\*
set apachecommons=%cd%\lib\commons-math3-3.6.1\*
set jetty=%cd%\lib\jetty-distribution-9.2.24\lib\*
set jwave=%cd%\lib\JWave-master\dist\*
set jsonsimple=%cd%\lib\json-simple\*

java.exe -cp .;%libsvm%;%neuroph%;%apachecommons%;%jetty%;%jwave%;%jsonsimple% Workbench


