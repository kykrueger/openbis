# Welcome to pyBIS!
pyBIS is a Python module for interacting with openBIS, designed to be used in Jupyter. It offers a sort of IDE for openBIS, supporting TAB completition and input checks, making the life of a researcher hopefully easier.


# SYNOPSIS

```
from pybis import Openbis
o = Openbis('https://example.com:8443', verify_certificates=False)
o.login('username', 'password', save_token=True)   # saves the session token in ~/.pybis/example.com.token
o.token
o.is_session_active()
o.get_datastores()
o.logout()


# Masterdata
o.get_experiment_types()
o.get_sample_types()
o.get_sample_type('YEAST')
o.get_material_types()
o.get_dataset_types()
o.get_dataset_types()[0]
o.get_dataset_type('RAW_DATA')
o.get_terms()
o.get_terms('MATING_TYPE')
o.get_tags()


# Spaces and Projects
o.get_spaces()
o.get_space('MY_SPACE')
o.get_projects(space='MY_SPACE')
space.get_projects()
project.get_experiments()
project.get_attachments()
project.download_attachments()
p.add_attachment(fileName='testfile', description= 'another file', title= 'one more attachment')
p.save()


# Samples
sample = o.new_sample(
    type='YEAST', 
    space='MY_SPACE', 
    parents=[parent_sample, '/MY_SPACE/YEA66'], 
    children=[child_sample]
)
sample.space
sample.code
sample.permId
sample.identifier
sample.type  # once the sample type is defined, you cannot modify it
sample.space
sample.space = 'MY_OTHER_SPACE'
sample.experiment    # a sample can belong to one experiment only
sample.experiment = 'MY_SPACE/MY_PROJECT/MY_EXPERIMENT'
sample.tags
sample.tags = ['guten_tag', 'zahl_tag' ]
sample.get_parents()
sample.get_childeren()
sample.props, sample.p
sample.get_attachments()
sample.download_attachments()
sample.add_attachment('testfile.xls')
samples = o.get_samples(
    space='MY_SPACE',
    type='YEAST'
    tags=['*']  # tags must be present
    NAME = 'some name'   # properties are all uppercase
    props=['name', 'mating_type','show_in_project_overview']    # properties to be displayed in the dataFrame
)
samples.df  # returns a pandas dataframe object
samples.get_datasets(type='ANALYZED_DATA')  # for all found samples get all datasets
# Note: Project samples are not implemented yet.


# Experiments
o.new_experiment
    type='DEFAULT_EXPERIMENT',
    space='MY_SPACE',
    project='YEASTS'
)
o.get_experiments(
    project='YEASTS',
    space='MY_SPACE', 
    type='DEFAULT_EXPERIMENT',
    tags='*', 
    finished_flag=False,
    props=['name', 'finished_flag']
)
exp = o.get_experiment('/MY_SPACE/MY_PROJECT/MY_EXPERIMENT')
exp.props
exp.p     # same as exp.props
exp.p.finished_flag=True
exp.attrs
exp.a     # same as exp.attrs
exp.attrs.tags = ['some', 'extra', 'tags']
exp.tags = ['some', 'extra', 'tags']          # same thing
exp.save()


# Datasets
sample.get_datasets()
ds = o.get_dataset('20160719143426517-259')
ds.get_parents()
ds.get_children()
sample = ds.sample
experiment = ds.experiment
ds.physicalData
ds.status        # AVAILABLE LOCKED ARCHIVED UNARCHIVE_PENDING ARCHIVE_PENDING BACKUP_PENDING
ds.archive()
ds.unarchive()
ds.get_files(start_folder="/")
ds.file_list
ds.add_attachment()
ds.get_attachments()
ds.download_attachments()
ds.download(destination='/tmp', wait_until_finished=False)

ds_new = o.new_dataset(
    type='ANALYZED_DATA', 
    experiment=exp, 
    sample= samp,
    files = ['my_analyzed_data.dat'], 
    props={'name': 'we give this dataset a name', 'notes': 'and we might need some notes, too'})
)
ds_new.save()

```

# Requirements and organization

### Requirements
pyBIS uses the openBIS API v3. Because of some compatibility problems, openBIS version 16.05.1 is the minimal requirement. On the Python side, pyBIS uses Python 3.5 and pandas.

### Organization
pyBIS is devided in several parts:

- the **pyBIS module** which holds all the method to interact with openBIS
- the **JupyterHub authenticator** which uses pyBIS for authenticating against openBIS, validating and storing the session token
- the **Vagrantfile** to set up a complete virtual machine, based on Cent OS 7, including JupyterHub
- the **dataset-uploader-api.py**, an ingestion plug-in for openBIS, allowing people to upload new datasets
