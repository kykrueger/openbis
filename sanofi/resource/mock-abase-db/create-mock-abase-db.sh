#!/bin/bash
USER=`whoami`
ABASE_DB="abase_mock_db"

psql -U postgres -c "drop database if exists $ABASE_DB;"
psql -U postgres -c "create database $ABASE_DB with owner $USER template = template0 encoding = 'UNICODE'"
psql -U $USER -d $ABASE_DB -f mock-abase-db.sql
