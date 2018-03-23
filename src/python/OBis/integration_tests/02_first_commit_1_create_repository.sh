#!/bin/bash

rm -rf $1/obis_data
mkdir $1/obis_data && cd $1/obis_data
obis init data1 && cd data1
echo content >> file
obis status
