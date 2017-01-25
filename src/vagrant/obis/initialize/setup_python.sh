#!/bin/env bash

# Install git
sudo yum -y install git


# Get and install miniconda
wget https://repo.continuum.io/miniconda/Miniconda3-latest-Linux-x86_64.sh
/bin/bash Miniconda3-latest-Linux-x86_64.sh -b -p /home/vagrant/miniconda3
echo export PATH=\"/home/vagrant/miniconda3/bin:\$PATH\" >> .bashrc
export PATH=/home/vagrant/miniconda3/bin:$PATH
sudo chown -R vagrant:jupyterhub miniconda3/


# Install oft-used packages via conda
conda install -y numpy
conda install -y scipy
conda install -y pandas
conda install -y matplotlib
conda install -y jupyter

# Install other python packages via pip
pip install requests

# Install our python packages
pip install -e /vagrant_python/PyBis/
pip install -e /vagrant_python/JupyterBis/

sudo ln -s /home/vagrant/miniconda3/bin/* /usr/bin
