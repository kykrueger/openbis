#!/bin/bash
# Performs the sprint components installation.
# This script assumes that you already are on the sprint server and must be run from that place
# in the home directory.

if [ $# -lt 1 ]; then
    echo "Usage: $0 <sprint number>"
    exit 1
fi

VER=$1
PREV_VER=$(( $VER - 1 ))
DB_SNAPSHOT=db_snapshots/sprint$PREV_VER-lims_productive.sql

# Unalias rm command
unalias rm

echo Stopping the components...
./sprint/openBIS-server/apache-tomcat/bin/shutdown.sh
./sprint/datastore_server/datastore_server.sh stop

echo Making a database dump...
pg_dump -U postgres -O lims_productive > $DB_SNAPSHOT
tar -cf - $DB_SNAPSHOT | bzip2 >db_snapshots/sprint$PREV_VER-lims_productive.tar.bz2
rm -f $DB_SNAPSHOT

echo Installing openBIS server...
mv sprint-$PREV_VER old
rm -f sprint
mkdir sprint-$VER
ln -s sprint-$VER sprint
cd sprint
unzip ../openBIS-server-S$VER*
cd openBIS-server
./install.sh $PWD ../../service.properties

echo Installing datastore server...
cd ..
unzip ../datastore_server-S$VER*
cd datastore_server
cp ~/datastore_server-service.properties etc/service.properties
chmod 700 datastore_server.sh
export JAVA_HOME=/usr
./datastore_server.sh start

echo Doing some cleaning...
cd
mv *.zip tmp
rm -rf openbis

# Reset the rm command alias
alias 'rm=rm -i'

echo Done!