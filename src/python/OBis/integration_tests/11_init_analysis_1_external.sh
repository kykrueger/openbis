#!/bin/bash

cd $1/obis_data
obis init_analysis -p data1 analysis1
cd analysis1
obis config object_id /DEFAULT/DEFAULT
echo content >> file
obis commit -m 'commit message'

