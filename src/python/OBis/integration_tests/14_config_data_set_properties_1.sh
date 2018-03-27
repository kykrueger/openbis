#!/bin/bash

cd $1/obis_data
obis init data8 && cd data8
obis config -p a 0

