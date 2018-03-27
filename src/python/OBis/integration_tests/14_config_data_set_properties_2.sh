#!/bin/bash

cd $1/obis_data/data8
obis config data_set_properties '{ "a": "0", "b": "1", "c": "2"}'
obis config -p c 3

