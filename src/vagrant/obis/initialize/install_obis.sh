#!/bin/env bash

# Install prerequisites from EasyBD.tar.xz (not from the yum repo)

pushd .  $@ > /dev/null
cd ~
tar -xf /vagrant/initialize/EasyBD.tar.xz -C ./
echo export PATH=\"/home/vagrant/EasyBD/bin:/home/vagrant/EasyBD/bin\$PATH\" >> .bashrc
export PATH=/home/vagrant/EasyBD/bin:$PATH
pip install -e /vagrant_python/PyBis/
pip install -e /vagrant_python/OBis/

cd EasyBD/bin
ln -s ../Python-3.6/bin/obis ./

popd  $@ > /dev/null
