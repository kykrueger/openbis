#!/bin/bash

mkdir $1/obis_data_b && cd $1/obis_data_b
obis init data3 && cd data3
obis config object_id /DEFAULT/DEFAULT
echo content >> file
obis commit -m 'commit message'
