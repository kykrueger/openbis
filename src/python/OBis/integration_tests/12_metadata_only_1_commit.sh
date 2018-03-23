#!/bin/bash

cd $1/obis_data
obis init data7 && cd data7
obis config object_id /DEFAULT/DEFAULT
echo content >> file
obis commit -m 'commit message'

