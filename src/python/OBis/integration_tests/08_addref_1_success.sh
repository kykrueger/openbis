#!/bin/bash

cd $1/obis_data
cp -r data1 data6
obis addref data6

