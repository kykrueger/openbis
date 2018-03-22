#!/bin/bash

cd $1/obis_data
obis init data4 && cd data4
echo content >> file
obis commit -m 'commit message'
