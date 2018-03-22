#!/bin/bash

cd $1/obis_data/data4
obis config object_id /DEFAULT/DEFAULT
obis commit -m 'commit message'
