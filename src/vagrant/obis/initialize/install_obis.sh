#!/bin/env bash

# Install prerequisites from obis.tar.xz (not from the yum repo)

pushd .  $@ > /dev/null
cd ~
tar -xf /vagrant/initialize/obis_tools.tar.xz -C ./
echo export PATH=\"/home/vagrant/obis_tools/bin:/home/vagrant/obis_tools/bin\$PATH\" >> .bashrc
export PATH=/home/vagrant/obis_tools/bin:$PATH
pip install -e /vagrant_python/PyBis/
pip install -e /vagrant_python/OBis/

cd obis_tools/bin
ln -s ../Python-3.6/bin/obis ./

popd  $@ > /dev/null
