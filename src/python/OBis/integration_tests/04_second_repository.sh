#!/bin/bash

cd $1/obis_data
obis init data2 && cd data2
obis config object_id /DEFAULT/DEFAULT
echo content >> file
obis commit -m 'commit message'
