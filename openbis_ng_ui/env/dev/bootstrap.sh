#!/usr/bin/env bash

sudo locale-gen en_US.UTF-8

echo "
LC_ALL=en_US.UTF-8
LANG=en_US.UTF-8
" >> /etc/environment

apt update
apt install -y unzip elinks openjdk-8-jdk inotify-tools git postgresql

cp /files/pg_hba.conf /etc/postgresql/10/main/pg_hba.conf 
service postgresql restart
sleep 10 # let the db engine start

mkdir -p /node/node_modules
chown -R vagrant:vagrant /node

sudo -E -u postgres -H -i /files/setup-postgres.sh
sudo -E -u vagrant -H -i /files/setup-vagrant.sh
