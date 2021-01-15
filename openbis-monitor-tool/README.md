# MICROSERVICE SERVER #

## Introduction ##

This project is supposed to be used to monitor openBIS.

## Build ##

./gradlew distZip

The build will be found at ./build/distributions/openbis_monitor_tool.zip

## Usage ##

1. Unzip the zip file.
2. From the root folder of the decompressed zip call the script on the bin directory.
3. Example of expected output.

openbis-monitor-tool % ./bin/openbis-monitor-tool https://openbis-eln.ethz.ch/ 30000 user_id user_password 20150407224113301-26972
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
sessionToken: user_id-210115124729932xA447FC439B489276083A5B201F521A6D
isSessionActive: true
true

