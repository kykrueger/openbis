# Welcome to pyBIS!

pyBIS is a Python module for interacting with openBIS, designed to be used in Jupyter. It offers a sort of IDE for openBIS, supporting TAB completition and input checks, making the life of a researcher hopefully easier.

[PyBIS Documentation](src/python/PyBis/README.md)


# Requirements and organization

### Dependencies and Requirements
- pyBIS relies the openBIS API v3; openBIS version 16.05.2 or newer 
- pyBIS uses Python 3.3 and pandas


### Installation

- locate the `jupyter-api` folder found in `pybis/src/coreplugins`
- copy this folder to `openbis/servers/core-plugins` in your openBIS installation
- register the plugin by editing `openbis/servers/core-plugins/core-plugins.properties` :
- `enabled-modules = jupyter-api` (separate multiple plugins with comma)
- restart your DSS to activate the plugin


### Project Organization
This project is devided in several parts:

- src/python/**PyBis** Python module which holds all the method to interact with OpenBIS
- src/python/**OBis** a command-line tool to register large datasets in OpenBIS without actually copying the data. Uses git annex for version control and OpenBIS linkedDataSet objects to register the metadata.
- src/python/**JupyterBis** a JupyterHub authenticator module which uses pyBIS for authenticating against openBIS, validating and storing the session token
- src/core-plugins/**jupyter-api**, an ingestion plug-in for openBIS, allowing people to upload new datasets
- src/vagrant/**jupyter-bis/Vagrantfile** to set up JupyterHub on a virtual machine (CentOS 7), which uses the JupyterBis authenticator module
- src/vagrant/**obis/Vagrantfile** to set up a complete OpenBIS instance on a virtual machine (CentOS 7)
