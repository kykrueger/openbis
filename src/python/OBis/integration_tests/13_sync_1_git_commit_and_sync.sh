#!/bin/bash

cd $1/obis_data/data7
echo content >> file2
git add file2
git commit -m 'msg'
obis sync

