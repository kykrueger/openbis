## Changes with pybis-1.7.2

* pyBIS now allows to create dataset-containers that contain no data themselves
* datasets now show a «kind» attribute, which can be either PHYSICAL, LINK or CONTAINER
* PropertyAssignments and other internal data are now finally nicely presented in Jupyter
* various bugfixes

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

