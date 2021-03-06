#!/bin/bash
#
# Runs on Unix / Linux systems.
# -------------------------------------------------------------------------

JAR_FILE=lib/openbis-properties-client.jar

#
# change to installation directory
#
bin=$0
if [ -L $bin ]; then
  bin=`dirname $bin`/`readlink $bin`
fi
WD=`dirname $bin`
cd $WD
SCRIPT=./`basename $0`

if [ "$JAVA_HOME" != "" ]; then
	JAVA_BIN="$JAVA_HOME/bin/java"
else
	JAVA_BIN="java"
fi

ALL_JAVA_OPTS="-Djavax.net.ssl.trustStore=etc/openBIS.keystore $JAVA_OPTS"
echo "Starting..."
${JAVA_BIN} ${ALL_JAVA_OPTS} -jar $JAR_FILE "$@"
