#!/bin/bash
#
# Backups all databases controlled by openBIS to a specified folder.
#
# Usage: backup-databases.sh $BACKUP_DIR
# 

#
# Sets the variable DB_LIST to contain a list of databases 
# to be backed up.
# Each line of the function's result has the form :
#
# database=XXXX;username=XXXX;password=XXXX
#
function listDatabases() {
  
  local LIB=$DSS_SERVER/lib

  DB_LIST=$(java -cp $LIB/\* ch.systemsx.cisd.openbis.dss.generic.server.dbbackup.BackupDatabaseDescriptionGenerator $@)
}

#
# Parses a property value from a semi-colon delimited string of key=value items.
# 
# $1 - a semi-color delimited string of properties (e.g. "key1=value1;key2=value2")
# $2 - the name of the property (e.g. "key1")
#
# The result is returned via the variable "propValue" 
#
function getProperty() {

  local properties=$1
  local propName=$2
  propValue=$(echo $properties | tr ";" "\n" | grep "$propName=" | sed "s/$propName=//")
  
}

#
# Backs up database specified by a string as returned 
# in the function listDatabases.
#
function backupDatabase() {

  DB_PROPS=$1
  
  getProperty $DB_PROPS "database"
  database=$propValue
  
  getProperty $DB_PROPS "username"
  username=$propValue
  
  local dumpFile=$BACKUP_DIR/$database.dmp
  
  echo "Backing up database $database to $dumpFile..."
  pg_dump -U $username -Fc $database > $dumpFile
  
  if [ "$?" -ne 0 ]; then
      echo "Failed to backup database '$database' !"
      exit 3
  fi
}

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

BACKUP_DIR=$1
if [ "$BACKUP_DIR" == "" ]; then
	echo ERROR: directory in which configuration should be stored has not been specified! 
	exit 1
fi

SERVERS=$BASE/../servers
AS_SERVER=$SERVERS/openBIS-server/
DSS_SERVER=$SERVERS/datastore_server

listDatabases $AS_SERVER/jetty/etc/service.properties $DSS_SERVER/etc/service.properties
if [ -z "$DB_LIST" ]; then
  echo "No database found for backup. Aborting..."
  exit 2
fi


for DB in $DB_LIST; do
  backupDatabase $DB
done


