#!/bin/env bash

sudo pip3 install -e /vagrant_python/PyBis/
sudo pip3 install -e /vagrant_python/OBis/

sudo pip3 install pyOpenSSL

if [ ! -L /usr/bin/git ]; then

    pushd ~obis $@ > /dev/null

    wget https://downloads.kitenet.net/git-annex/linux/current/git-annex-standalone-amd64.tar.gz
    tar -xvvf git-annex-standalone-amd64.tar.gz

    sudo ln -s /home/obis/git-annex.linux/git /usr/bin/git
    sudo ln -s /home/obis/git-annex.linux/git-annex /usr/bin/git-annex

    popd $@ > /dev/null

fi
