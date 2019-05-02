## Changes with pybis-1.8.4

* totalCount attribute added in every Things object
* totalCount will return the total number of elements matching a search
* bugfix in get_semantic_annotation method

## Changes with pybis-1.8.3

* new method for attributes: .attrs.all() will return a dict, much like .props.all()
* attributes like registrator and modifier are now returned by default


## Changes with pybis-1.8.2

* added key-lookup and setting for properties that contain either dots or dashes
* sample.props['some-weird.property-name'] = "some value"
* check for mandatory properties in samples (objects), datasets and experiments (collections)

## Changes with pybis-1.8.1

* revised documentation
* improved DataSet creation
* added missing delete function for DataSets
* wrong entity attributes will now immediately throw an error
* more DataSet creation tests
* paging tests added
* `collection` is now alias for `experiment`
* `object` is alias for `sample`

## Changes with pybis-1.8.0

* better support for fetching entity-types (dataSetTypes, sampleTypes)
* separation of propertyAssignments from entity-types
* added .get_propertyAssignments() method to all entity-types

## Changes with pybis-1.7.6

* bugfix dataset upload for relative files (e.g. ../../file or /User/username/file)
* always only the filename is added to the dataset, not the folder containing it
* corrected License file

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

