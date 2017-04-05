#!/bin/env bash

pushd . $@ > /dev/null
ob_dir=$(find /vagrant/initialize/ -maxdepth 1 -type d -name openBIS-installation-standard-technologies-* | head -n1)
echo "Installing openBIS from $ob_dir"
cp /vagrant/config/openbis/console.properties $ob_dir
chmod a+rw "$ob_dir/console.properties"

sudo -u openbis cd ~openbis/
sudo -u openbis "$ob_dir/run-console.sh"

popd $@ > /dev/null
