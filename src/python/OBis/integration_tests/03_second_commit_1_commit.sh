#!/bin/bash

cd $1/obis_data/data1

dd if=/dev/zero of=big_file bs=1000000 count=1
obis commit -m 'commit message'
