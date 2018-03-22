#!/bin/bash

data_set_id=$2

cd $1/obis_data_b
obis clone $data_set_id

