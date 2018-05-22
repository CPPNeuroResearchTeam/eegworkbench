#!/bin/bash

DELIM=":"

if [ "${OS}" == "Windows_NT" ]
	then 
	DELIM=";"
fi

java -cp ".${DELIM}lib/*" Workbench
