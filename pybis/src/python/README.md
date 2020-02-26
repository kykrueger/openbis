# Welcome to pyBIS!
pyBIS is a Python module for interacting with openBIS, designed to be used in Jupyter. It offers some sort of IDE for openBIS, supporting TAB completition and input checks, making the life of a researcher hopefully easier.

## Dependencies and Requirements
- pyBIS relies the openBIS API v3
- openBIS version 16.05.2 or newer is required
- 18.06.2 or later is recommended
- pyBIS uses Python 3.3 and pandas

## Installation

```
pip install pybis
```
That command will download install pybis and all its dependencies.

If you haven't done yet, install Jupyter Notebook:

```
pip install jupyter
```

# General Usage

## Tab completition and other hints
Used in a Jupyter Notebook environment, pybis helps you to enter the commands. After every dot `.` you might hit the `TAB` key in order to look at the available commands.

If you are unsure what parameters to add to a , add a question mark right after the method and hit `SHIFT+ENTER`. Jupyter will then look up the signature of the method and show some helpful docstring.

When working with properties of entities, they might use a **controlled vocabulary** or are of a specific **property type**. Add an underscore `_` character right after the property and hit `SHIFT+ENTER` to show the valid values. When a property only acceps a controlled vocabulary, you will be shown the valid terms in a nicely formatted table.

# connect to OpenBIS

## login

In an **interactive session** e.g. inside a Jupyter notebook, you can use `getpass` to enter your password safely:

```
from pybis import Openbis
o = Openbis('https://example.com', verify_certificates=False)

import getpass
password = getpass.getpass()

o.login('username', password, save_token=True)   # save the session token in ~/.pybis/example.com.token
```

In a **script** you would rather use two **environment variables** to provide username and password:

```
from pybis import Openbis
o = Openbis(os.environ['OPENBIS_HOST'], verify_certificates=False)

o.login(os.environ['OPENBIS_USERNAME'], os.environ['OPENBIS_PASSWORD'])
```


Check whether the session token is still valid and log out:

```
o.token
o.is_session_active()
o.logout()
```

## Mount openBIS dataStore server

### Prerequisites: FUSE / SSHFS

Mounting an openBIS dataStore server requires FUSE / SSHFS to be installed (requires root privileges):

**Mac OS X**

Follow the installation instructions on
https://osxfuse.github.io

**Unix Cent OS 7**

```
$ sudo yum install epel-release
$ sudo yum --enablerepo=epel -y install fuse-sshfs
$ user="$(whoami)"
$ usermod -a -G fuse "$user"
```
After the installation, an `sshfs` command should be available.

### Mount dataStore server with pyBIS

Because the mount/unmount procedure differs from platform to platform, pyBIS offers two simple methods:

```
o.mount()
o.mount(username, password, hostname, mountpoint, volname)
o.is_mounted()
o.unmount()
o.get_mountpoint()
```
Currently, mounting is supported for Linux and Mac OS X only.

All attributes, if not provided, are re-used by a previous login() command. If no mountpoint is provided, the default mounpoint will be `~/hostname`. If this directory does not exist, it will be created. The directory must be empty before mounting.


# Masterdata
OpenBIS stores quite a lot of meta-data along with your dataSets. The collection of data that describes this meta-data (i.e. meta-meta-data) is called masterdata. It consists of:

* sample types
* dataSet types
* material types
* experiment types
* property types
* vocabularies
* vocabulary terms
* plugins (jython scripts that allow complex data checks)
* tags
* semantic annotations

## browse masterdata

```
sample_types = o.get_sample_types()  # get a list of sample types 
sample_types.df                      # DataFrame object
st = o.get_sample_types()[3]         # get 4th element of that list
st = o.get_sample_type('YEAST')
st.code
st.generatedCodePrefix
st.attrs.all()                       # get all attributes as a dict
st.get_validationPlugin()            # returns a plugin object

st.get_property_assignments()        # show the list of properties
                                     # for that sample type

o.get_material_types()
o.get_dataset_types()
o.get_experiment_types()

o.get_property_types()
pt = o.get_property_type('BARCODE_COMPLEXITY_CHECKER')
pt.attrs.all()

o.get_plugins()
pl = o.get_plugin('Diff_time')
pl.script  # the Jython script that processes this property

o.get_vocabularies()
o.get_vocabulary('BACTERIAL_ANTIBIOTIC_RESISTANCE')
o.get_terms(vocabulary='STORAGE')
o.get_tags()
```

## create property types

Samples (objects), experiments (collections) and dataSets contain general **attributes** as well as type-specific **properties**. Before they can be assigned to their respective type, they need to be created first.

```
pt = o.new_property_type(
    code        = 'MY_NEW_PROPERTY_TYPE', 
    label       = 'yet another property type', 
    description = 'my first property',
    dataType    = 'VARCHAR',
)

pt_int = o.new_property_type(
    code        = '$DEFAULT_OBJECT_TYPE', 
    label       = 'default object type for ELN-LIMS', 
    dataType    = 'VARCHAR',
    internalNameSpace = True,
)

pt_voc = o.new_property_type(
    code        = 'MY_CONTROLLED_VOCABULARY', 
    label       = 'label me', 
    description = 'give me a description',
    dataType    = 'CONTROLLEDVOCABULARY',
    vocabulary  = 'STORAGE',
)
```

The `dataType` attribute can contain any of these values:

* `INTEGER`
* `VARCHAR`
* `MULTILINE_VARCHAR`
* `REAL`
* `TIMESTAMP`
* `BOOLEAN`
* `HYPERLINK`
* `XML`
* `CONTROLLEDVOCABULARY`
* `MATERIAL`

When choosing `CONTROLLEDVOCABULARY`, you must specify a `vocabulary` attribute (see example). Likewise, when choosing `MATERIAL`, a `materialType` attribute must be provided. PropertyTypes that start with a $ belong by definition to the `internalNameSpace` and therefore this attribute must be set to True.


## create sample types

```
sample_type = o.new_sample_type(
    code = 'my_own_sample_type',      # mandatory
    generatedCodePrefix	 = 'S',       # mandatory
    description = '',
    autoGeneratedCode = True,
    subcodeUnique = False,
    listable	= True,
    showContainer = False,
    showParents = True,
    showParentMetadata = False,
    validationPlugin = 'Has_Parents'  # see plugins below
)
sample_type.save()
```

## assign properties to sample type

A sample type needs to be saved before properties can be assigned to. This assignment procedure applies to all entity types (dataset type, experiment type, material type).

```
sample_type.assign_property(
	prop = 'diff_time',             # mandatory
	section = '',
	ordinal = 5,
	mandatory = True,
	initialValueForExistingEntities = 'initial value'
	showInEditView = True,
	showRawValueInForms = True
)
sample_type.revoke_property('diff_time')
sample_type.get_property_assignments()
```

## create dataset types

```
dataset_type = o.new_dataset_type(
    code = 'my_dataset_type',       # mandatory
    description=None,
    mainDataSetPattern=None,
    mainDataSetPath=None,
    disallowDeletion=False,
    validationPlugin=None,
)
dataset_type.save()
dataset_type.assign_property('property_name')
dataset_type.revoke_property('property_name')
dataset_type.get_property_assignments()
```

## create experiment types

```
experiment_type = o.new_experiment_type(
    code, 
    description=None,
    validationPlugin=None,
)
experiment_type.save()
experiment_type.assign_property('property_name')
experiment_type.revoke_property('property_name')
experiment_type.get_property_assignments()
```

## create material types

```
material_type = o.new_material_type(
    code, 
    description=None,
    validationPlugin=None,
)
material_type.save()
material_type.assign_property('property_name')
material_type.revoke_property('property_name')
material_type.get_property_assignments()

```

## create plugins

Plugins are Jython scripts that can accomplish more complex data-checks than ordinary types and vocabularies can achieve. They are assigned to entity types (dataset type, sample type etc). [Documentation and examples can be found here](https://wiki-bsse.ethz.ch/display/openBISDoc/Properties+Handled+By+Scripts)

```
pl = o.new_plugin(
    name       ='my_new_entry_validation_plugin',
    pluginType ='ENTITY_VALIDATION',               # or 'DYNAMIC_PROPERTY' or 'MANAGED_PROPERTY',
    entityKind = None,                             # or 'SAMPLE', 'MATERIAL', 'EXPERIMENT', 'DATA_SET'
    script     = 'def calculate(): pass'           # a JYTHON script
)
pl.save()
```

## Users, Groups and RoleAssignments

```
o.get_groups()
group = o.new_group(code='group_name', description='...')
group = o.get_group('group_name')
group.save()
group.assign_role(role='ADMIN', space='DEFAULT')
group.get_roles() 
group.revoke_role(role='ADMIN', space='DEFAULT')

group.add_members(['admin'])
group.get_members()
group.del_members(['admin'])
group.delete()

o.get_persons()
person = o.new_person(userId='username')
person.space = 'USER_SPACE'
person.save()

person.assign_role(role='ADMIN', space='MY_SPACE')
person.assign_role(role='OBSERVER')
person.get_roles()
person.revoke_role(role='ADMIN', space='MY_SPACE')
person.revoke_role(role='OBSERVER')

o.get_role_assignments()
o.get_role_assignments(space='MY_SPACE')
o.get_role_assignments(group='MY_GROUP')
ra = o.get_role_assignment(techId)
ra.delete()
```

## Spaces

```
space = o.new_space(code='space_name', description='')
space.save()
space.delete('reason for deletion')
o.get_spaces(
    start_with = 1,                   # start_with and count
    count = 7,                        # enable paging
)
space = o.get_space('MY_SPACE')
space.code
space.description
space.registrator
space.registrationDate
space.modifier
space.modificationDate
space.attrs.all()                     # returns a dict containing all attributes
```

## Projects
```
project = o.new_project(
    space=space, 
    code='project_name',
    description='some project description'
)
project = space.new_project(
	code='project_code',
	description='project description'
)
project.save()

o.get_projects(
    space = 'MY_SPACE',               # show only projects in MY_SPACE
    start_with = 1,                   # start_with and count
    count = 7,                        # enable paging
)
o.get_projects(space='MY_SPACE')
space.get_projects()

project.get_experiments()
project.get_attachments()
p.add_attachment(fileName='testfile', description= 'another file', title= 'one more attachment')
project.download_attachments()

project.code
project.description
# ... and many more
project.attrs.all()                   # returns a dict containing all attributes

project.freeze = True
project.freezeForExperiments = True
project.freezeForSamples = True

```

## Samples
Samples are nowadays called **Objects** in openBIS. pyBIS is not yet thoroughly supporting this term in all methods where «sample» occurs.

NOTE: In openBIS, `samples` entities have recently been renamed to `objects`. All methods have synonyms using the term `object`, e.g. `get_object`, `new_object`, `get_object_types`.

```
sample = o.new_sample(
    type     = 'YEAST', 
    space    = 'MY_SPACE',
    experiment = '/MY_SPACE/MY_PROJECT/EXPERIMENT_1',
    parents  = [parent_sample, '/MY_SPACE/YEA66'], 
    children = [child_sample],
    props    = {"name": "some name", "description": "something interesting"}
)
sample = space.new_sample( type='YEAST' )
sample.save()

sample = o.get_sample('/MY_SPACE/MY_SAMPLE_CODE')
sample = o.get_sample('20170518112808649-52')

sample.space
sample.code
sample.permId
sample.identifier
sample.type  # once the sample type is defined, you cannot modify it

sample.space
sample.space = 'MY_OTHER_SPACE'

sample.experiment    # a sample can belong to one experiment only
sample.experiment = '/MY_SPACE/MY_PROJECT/MY_EXPERIMENT'

sample.project
sample.project = '/MY_SPACE/MY_PROJECT'  # only works if project samples are
enabled

sample.tags
sample.tags = ['guten_tag', 'zahl_tag' ]

sample.attrs.all()                    # returns all attributes as a dict
sample.props.all()                    # returns all properties as a dict

sample.get_attachments()
sample.download_attachments()
sample.add_attachment('testfile.xls')
```

### parents, children, components and container

```
sample.get_parents()
sample.set_parents(['/MY_SPACE/PARENT_SAMPLE_NAME')
sample.add_parents('/MY_SPACE/PARENT_SAMPLE_NAME')
sample.del_parents('/MY_SPACE/PARENT_SAMPLE_NAME')

sample.get_children()
sample.set_children('/MY_SPACE/CHILD_SAMPLE_NAME')
sample.add_children('/MY_SPACE/CHILD_SAMPLE_NAME')
sample.del_children('/MY_SPACE/CHILD_SAMPLE_NAME')

# A Sample may belong to another Sample, which acts as a container.
# As opposed to DataSets, a Sample may only belong to one container.
sample.container    # returns a sample object
sample.container = '/MY_SPACE/CONTAINER_SAMPLE_NAME'   # watch out, this will change the identifier of the sample to:
                                                       # /MY_SPACE/CONTAINER_SAMPLE_NAME:SAMPLE_NAME
sample.container = ''                                  # this will remove the container. 

# A Sample may contain other Samples, in order to act like a container (see above)
# The Sample-objects inside that Sample are called «components» or «contained Samples»
# You may also use the xxx_contained() functions, which are just aliases.
sample.get_components()
sample.set_components('/MY_SPACE/COMPONENT_NAME')
sample.add_components('/MY_SPACE/COMPONENT_NAME')
sample.del_components('/MY_SPACE/COMPONENT_NAME')
```

### sample tags

```
sample.get_tags()
sample.set_tags('tag1')
sample.add_tags(['tag2','tag3'])
sample.del_tags('tag1')
```


### useful tricks when dealing with properties, using Jupyter or IPython

```
sample.p + TAB                        # in IPython or Jupyter: show list of available properties
sample.p.my_property_ + TAB           # in IPython or Jupyter: show datatype or controlled vocabulary
sample.p['my-weird.property-name']    # accessing properties containing a dash or a dot

sample.set_props({ ... })             # set properties by providing a dict
sample.p                              # same thing as .props
sample.p.my_property = "some value"   # set the value of a property
                                      # value is checked (type/vocabulary)
sample.save()                         # update the sample in openBIS
```

### querying samples

```
samples = o.get_samples(
    space ='MY_SPACE',
    type  ='YEAST',
    tags  =['*'],                     # only sample with existing tags
    start_with = 1,                   # start_with and count
    count = 7,                        # enable paging
    NAME  = 'some name',              # properties are always uppercase 
                                      # to distinguish them from attributes
    **{ "SOME.WEIRD:PROP": "value"}   # property name contains a dot or a
                                      # colon: cannot be passed as an argument 
    props=['NAME', 'MATING_TYPE']     # show these properties in the result
)
samples.df                            # returns a pandas DataFrame object
samples.get_datasets(type='ANALYZED_DATA')
```

### freezing samples

```
sample.freeze = True
sample.freezeForComponents = True
sample.freezeForChildren = True
sample.freezeForParents = True
sample.freezeForDataSets = True
```

## Experiments

NOTE: In openBIS, `experiment` entities have recently been renamed to `collection`. All methods have synonyms using the term `collection`, e.g. `get_collections`, `new_collection`, `get_collection_types`.

```
exp = o.new_experiment
    type='DEFAULT_EXPERIMENT',
    space='MY_SPACE',
    project='YEASTS'
)
exp.save()

o.get_experiments(
    project='YEASTS',
    space='MY_SPACE', 
    type='DEFAULT_EXPERIMENT',
    tags='*', 
    finished_flag=False,
    props=['name', 'finished_flag']
)
project.get_experiments()
exp = o.get_experiment('/MY_SPACE/MY_PROJECT/MY_EXPERIMENT')

exp.set_props({ key: value})
exp.props
exp.p                              # same thing as .props
exp.p.finished_flag=True
exp.p.my_property = "some value"   # set the value of a property (value is checked)
exp.p + TAB                        # in IPython or Jupyter: show list of available properties
exp.p.my_property_ + TAB           # in IPython or Jupyter: show datatype or controlled vocabulary
exp.p['my-weird.property-name']    # accessing properties containing a dash or a dot

exp.attrs.all()                    # returns all attributes as a dict
exp.props.all()                    # returns all properties as a dict

exp.attrs.tags = ['some', 'tags']
exp.tags = ['some', 'tags']        # same thing
exp.save()

exp.code
exp.description
exp.registrator
exp.registrationDate
exp.modifier
exp.modificationDate

exp.freeze = True
exp.freezeForDataSets = True
exp.freezeForSamples = True
```

## Datasets

### working with existing dataSets
```
sample.get_datasets()
ds = o.get_dataset('20160719143426517-259')
ds.get_parents()
ds.get_children()
ds.sample
ds.experiment
ds.physicalData
ds.status                         # AVAILABLE LOCKED ARCHIVED 
                                  # ARCHIVE_PENDING UNARCHIVE_PENDING
                                  # BACKUP_PENDING
ds.archive()
ds.unarchive()

ds.attrs.all()                    # returns all attributes as a dict
ds.props.all()                    # returns all properties as a dict

ds.add_attachment()               # attachments usually contain meta-data
ds.get_attachments()              # about the dataSet, not the data itself.
ds.download_attachments()
```

### download dataSets

```
ds.get_files(start_folder="/")    # get file list as pandas table
ds.file_list                      # get file list as array

ds.download()                     # simply download all files to hostname/permId/
ds.download(
	destination = 'my_data',       # download files to folder my_data/
	create_default_folders = False,# ignore the /original/DEFAULT folders made by openBIS
	wait_until_finished = False,   # download in background, continue immediately
	workers = 10                   # 10 downloads parallel (default)
)
```

### dataSet attributes and properties

```
ds.set_props({ key: value})
ds.props
ds.p                              # same thing as .props
ds.p.my_property = "some value"   # set the value of a property
ds.p + TAB                        # show list of available properties
ds.p.my_property_ + TAB           # show datatype or controlled vocabulary
ds.p['my-weird.property-name']    # accessing properties containing a dash or a dot

ds.attrs.all()                    # returns all attributes as a dict
ds.props.all()                    # returns all properties as a dict
```

### querying dataSets

* examples of a complex queries with methods chaining.
* NOTE: properties must be in UPPERCASE to distinguish them from attributes

```
datasets = o.get_experiments(project='YEASTS')\
			 .get_samples(type='FLY')\
			 .get_datasets(
					type='ANALYZED_DATA',
					props=['MY_PROPERTY'],
					MY_PROPERTY='some analyzed data'
		 	 )
```

```
datasets = o.get_experiment('/MY_NEW_SPACE/MY_PROJECT/MY_EXPERIMENT4')\
           .get_samples(type='UNKNOWN')\
           .get_parents()\
           .get_datasets(type='RAW_DATA')
```

### deal with dataSets query results

```
datasets.df                       # get a pandas dataFrame object

# use it in a for-loop:
for dataset in datasets:
    print(dataset.permID)
    dataset.delete('give me a reason')
```
### freeze dataSets
* once a dataSet has been frozen, it cannot be changed by anyone anymore
* so be careful!

```
ds.freeze = True
ds.freezeForChildren = True
ds.freezeForParents = True
ds.freezeForComponents = True
ds.freezeForContainers = True
ds.save()
```

### create a new dataSet

```
ds_new = o.new_dataset(
    type       = 'ANALYZED_DATA', 
    experiment = '/SPACE/PROJECT/EXP1', 
    sample     = '/SPACE/SAMP1',
    files      = ['my_analyzed_data.dat'], 
    props      = {'name': 'some good name', 'description': '...' }
)
ds_new.save()
```

### create dataSet with zipfile

```
# DataSet containing one zipfile which will be unzipped in openBIS
ds_new = o.new_dataset(
    type       = 'RAW_DATA', 
    sample     = '/SPACE/SAMP1',
    zipfile    = 'my_zipped_folder.zip', 
)
ds_new.save()
```

### create dataSet with mixed content

* mixed content means: folders and files are provided
* a relative specified folder (and all its content) will end up in the root, while keeping its structure
   * `../measurements/` --> `/measurements/`
   * `some/folder/somewhere/` --> `/somewhere/` 
* relative files will also end up in the root
   * `my_file.txt` --> `/my_file.txt`
   * `../somwhere/else/my_other_file.txt` --> `/my_other_file.txt`
   * `some/folder/file.txt` --> `/file.txt`

```
# Dataset containing files and folders
# the content of the folder will be zipped (on-the-fly) and uploaded to openBIS.
# openBIS will keep the folder structure intact.
# relative path will be shortened to its basename. For example:
# local                         openBIS
# ../../myData/                 myData/
# some/experiment/results/      results/
ds_new = o.new_dataset(
    type       = 'RAW_DATA', 
    sample     = '/SPACE/SAMP1',
    files     = ['../measurements/', 'my_analyis.ipynb', 'results/'] 
)
ds_new.save()
```

### create dataSet container

```
# DataSet CONTAINER (contains other DataSets, but no files)
ds_new = o.new_dataset(
    type       = 'ANALYZED_DATA', 
    experiment = '/SPACE/PROJECT/EXP1', 
    sample     = '/SPACE/SAMP1',
    kind       = 'CONTAINER',
    props      = {'name': 'some good name', 'description': '...' }
)
ds_new.save()
```

### get, set, add and remove parent datasets

```
dataset.get_parents()
dataset.set_parents(['20170115220259155-412'])
dataset.add_parents(['20170115220259155-412'])
dataset.del_parents(['20170115220259155-412'])
```

#### get, set, add and remove child datasets

```
dataset.get_children()
dataset.set_children(['20170115220259155-412'])
dataset.add_children(['20170115220259155-412'])
dataset.del_children(['20170115220259155-412'])
```

### dataSet containers

* A DataSet may belong to other DataSets, which must be of kind=CONTAINER
* As opposed to Samples, DataSets may belong (contained) to more than one DataSet-container

```
dataset.get_containers()
dataset.set_containers(['20170115220259155-412'])
dataset.add_containers(['20170115220259155-412'])
dataset.del_containers(['20170115220259155-412'])
```

* a DataSet of kind=CONTAINER may contain other DataSets, to act like a folder (see above)
* the DataSet-objects inside that DataSet are called components or contained DataSets
* you may also use the xxx_contained() functions, which are just aliases.

```
dataset.get_components()
dataset.set_components(['20170115220259155-412'])
dataset.add_components(['20170115220259155-412'])
dataset.del_components(['20170115220259155-412'])
```

## Semantic Annotations
```
# create semantic annotation for sample type 'UNKNOWN'
sa = o.new_semantic_annotation(
	entityType = 'UNKNOWN',
	predicateOntologyId = 'po_id',
	predicateOntologyVersion = 'po_version',
	predicateAccessionId = 'pa_id',
	descriptorOntologyId = 'do_id',
	descriptorOntologyVersion = 'do_version',
	descriptorAccessionId = 'da_id'
)
sa.save()

# create semantic annotation for property type 
# (predicate and descriptor values omitted for brevity)
sa = o.new_semantic_annotation(propertyType = 'DESCRIPTION', ...)
sa.save()

# create semantic annotation for sample property assignment (predicate and descriptor values omitted for brevity)
sa = o.new_semantic_annotation(entityType = 'UNKNOWN', propertyType = 'DESCRIPTION', ...)
sa.save()

# create a semantic annotation directly from a sample type
# will also create sample property assignment annotations when propertyType is given
st = o.get_sample_type("ORDER")
st.new_semantic_annotation(...)

# get all semantic annotations
o.get_semantic_annotations()

# get semantic annotation by perm id
sa = o.get_semantic_annotation("20171015135637955-30")

# update semantic annotation
sa.predicateOntologyId = 'new_po_id'
sa.descriptorOntologyId = 'new_do_id'
sa.save()

# delete semantic annotation
sa.delete('reason')
```

## Tags
```
new_tag = o.new_tag(
	code        = 'my_tag', 
	description = 'some descriptive text'
)
new_tag.description = 'some new description'
new_tag.save()
o.get_tags()
o.get_tag('/username/TAG_Name')
o.get_tag('TAG_Name')

tag.get_experiments()
tag.get_samples()
tag.get_owner()   # returns a person object
tag.delete('why?')
```

## Vocabulary and VocabularyTerms

An entity such as Sample (Object), Experiment (Collection), Material or DataSet can be of a specific *entity type*:

* Sample Type
* Experiment Type
* DataSet Type
* Material Type

Every type defines which **Properties** may be defined. Properties act like **Attributes**, but they are type-specific. Properties can contain all sorts of information, such as free text, XML, Hyperlink, Boolean and also **Controlled Vocabulary**. Such a Controlled Vocabulary consists of many **VocabularyTerms**. These terms are used to only allow certain values entered in a Property field.

So for example, you want to add a property called **Animal** to a Sample and you want to control which terms are entered in this Property field. For this you need to do a couple of steps:

1. create a new vocabulary *AnimalVocabulary*
2. add terms to that vocabulary: *Cat, Dog, Mouse*
3. create a new PropertyType (e.g. *Animal*) of DataType *CONTROLLEDVOCABULARY* and assign the *AnimalVocabulary* to it
4. create a new SampleType (e.g. *Pet*) and *assign* the created PropertyType to that Sample type.
5. If you now create a new Sample of type *Pet* you will be able to add a property *Animal* to it which only accepts the terms *Cat, Dog* or *Mouse*.


**create new Vocabulary with three VocabularyTerms**

```
voc = o.new_vocabulary(
    code = 'BBB',
    description = 'description of vocabulary aaa',
    urlTemplate = 'https://ethz.ch',
    terms = [
        { "code": 'term_code1', "label": "term_label1", "description": "term_description1"},
        { "code": 'term_code2', "label": "term_label2", "description": "term_description2"},
        { "code": 'term_code3', "label": "term_label3", "description": "term_description3"}
    ]   
)
voc.save()
```

**create additional VocabularyTerms**

```
term = o.new_term(
	code='TERM_CODE_XXX', 
	vocabularyCode='BBB', 
	label='here comes a label',
	description='here might appear a meaningful description'
)
term.save()
```

**update VocabularyTerms**

To change the ordinal of a term, it has to be moved either to the top with the `.move_to_top()` method or after another term using the `.move_after_term('TERM_BEFORE')` method.

```
voc = o.get_vocabulary('STORAGE')
term = voc.get_terms()['RT']
term.label = "Room Temperature"
term.official = True
term.move_to_top()
term.move_after_term('-40')
term.save()
term.delete()
```
