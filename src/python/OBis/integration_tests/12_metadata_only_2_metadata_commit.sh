#!/bin/bash

cd $1/obis_data/data7
obis config collection_id /DEFAULT/DEFAULT/DEFAULT
obis commit -m 'commit message'

