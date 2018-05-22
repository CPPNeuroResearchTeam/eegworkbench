#!/bin/bash

DELIM=":"

LIBSVM=$(pwd)"/lib/libsvm-3.22/java/*"
NEUROPH=$(pwd)"/lib/neuroph-2.7/*"
APACHECOMMONS=$(pwd)"/lib/commons-math3-3.6.1/*"
JETTY=$(pwd)"/lib/jetty-distribution-9.2.24/lib/*"
JWAVE=$(pwd)"/lib/JWave-master/dist/*"
JSONSIMPLE=$(pwd)"/lib/json-simple/*"


if [ "${OS}" == "Windows_NT" ]
	then 
	DELIM=";"
fi

java -cp ".${DELIM}${LIBSVM}${DELIM}${NEUROPH}${DELIM}${APACHECOMMONS}${DELIM}${JETTY}${DELIM}${JWAVE}$DELIM${JSONSIMPLE}" Workbench
