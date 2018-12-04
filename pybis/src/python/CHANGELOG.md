## Changes with pybis-1.7.5

* added paging support for all search functions by providing start_with and count arguments
* make search more robust: allow get_sample('SPACE/CODE') instead of get_sample('/SPACE/CODE')
* make search more robust: allow get_sample('   20160706001644827-208   ') 
* make interface more robust (allow sample.permid instead of sample.permId)
* make properties more robust: allow get_samples(props='name') instead of get_samples(props=['name'])
* fixed bug when parent/children of more than one element was searched: o.get_experiments().get_samples().get_parents()

## Changes with pybis-1.7.4

* pyBIS now allows to create dataset-containers that contain no data themselves
* datasets now show a «kind» attribute, which can be either PHYSICAL, LINK or CONTAINER
* PropertyAssignments and other internal data are now finally nicely presented in Jupyter
* various bugfixes
* README.md is now correctly displayed
* setup.py is fixed, installation should no longer fail because of some utf-8 problems on certain machines

## Changes with pybis-1.7.3

* improved packaging information
* LICENSE included (Apache License v.2)

## Changes with pybis-1.7.2

* added server_information to openBIS connection
* bugfix: project samples are only fetched when instance supports them

## Changes with pybis-1.7.1

* fixed bug in controlled vocabulary when property name did not match the vocabulary name
* added `xxx_contained()` methods to Samples and DataSets
* updated documentation


## Changes with pybis-1.7.0

* added components and containers functionality to both datasets and samples
* `set_attributes()` no longer automatically saves the object
* tags now have to be created (and saved) before they can be assigned
* `get_tag()` now can search for more than one tag at once and supports both code and permId
* `get_tags()` now available for almost all objects, returns a dataframe
* improved and enhanced documentation


## Changes with pybis-1.6.8

* fixed bugs with parents and children of both samples and datasets
* new samples can be defined with parents / children
* `get_parents()` and `get_children()` methods now also work on new, not yet saved objects
* `get_sample()` and `get_dataset()` now also accept arrays of permIds / identifiers
* pybis now has a CHANGELOG!

