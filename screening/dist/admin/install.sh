#!/bin/bash
# Installs openbis AS and DSS for the first time. 
# openBIS will be installed in the 'servers' directory on the same level as this script parent directory.

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/env

ROOT_DIR=$BASE/../servers
BACKUP_DIR=$BASE/../backup

mkdir -p $ROOT_DIR
mkdir -p $BACKUP_DIR

# screening-specific
echo Restoring empty screening database
USER=`whoami`
psql -U $DB_USER_NAME -c "create database $OPENBIS_DB with owner $USER template = template0 encoding = 'UNICODE'"
psql -U $USER -d $OPENBIS_DB -f $BASE/empty-screening-database.sql

echo Installing openBIS Datastore Server
unzip $ROOT_DIR/datastore*.zip -d $ROOT_DIR

echo Installing openBIS Application Server
TMP_EXTRACT=$ROOT_DIR/tmp-extract
mkdir -p "$TMP_EXTRACT"
mkdir $ROOT_DIR/openBIS-server
unzip $ROOT_DIR/openBIS-*.zip -d "$TMP_EXTRACT"
$TMP_EXTRACT/openBIS-server/install.sh $ROOT_DIR/openBIS-server

rm -rf "$TMP_EXTRACT"
mv $ROOT_DIR/*.zip $BACKUP_DIR/


