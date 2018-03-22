#!/bin/bash

if [ -d "~/.obis" ]; then
  rm -r ~/.obis
fi

obis config -g openbis_url https://localhost:8443
obis config -g user admin
obis config -g data_set_type UNKNOWN
obis config -g verify_certificates false
obis config -g hostname `hostname`
