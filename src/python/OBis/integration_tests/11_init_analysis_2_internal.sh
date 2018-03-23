#!/bin/bash

cd $1/obis_data/data1
obis init_analysis analysis2
cd analysis2
obis config object_id /DEFAULT/DEFAULT
echo content >> file
obis commit -m 'commit message'

