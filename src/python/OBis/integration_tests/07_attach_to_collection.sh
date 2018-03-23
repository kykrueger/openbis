#!/bin/bash

cd $1/obis_data
obis init data5 && cd data5
echo content >> file
obis config collection_id /DEFAULT/DEFAULT/DEFAULT
obis commit -m 'msg'

