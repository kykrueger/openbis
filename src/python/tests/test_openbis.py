import json
import random
import re

import pytest
import time
from pybis import DataSet
from pybis import Openbis


def test_token(openbis_instance):
    assert openbis_instance.token is not None
    assert openbis_instance.is_token_valid(openbis_instance.token) is True
    assert openbis_instance.is_session_active() is True


def test_wrong_login(openbis_instance):
    new_instance = Openbis(openbis_instance.url, verify_certificates=openbis_instance.verify_certificates)
    with pytest.raises(ValueError):
        new_instance.login('anyuser', 'any_test_password')

    assert new_instance.token is None
    assert new_instance.is_session_active() is False


def test_http_only(openbis_instance):
    with pytest.raises(Exception):
        new_instance = Openbis('http://localhost')
        assert new_instance is None

    new_instance = Openbis(url='http://localhost', allow_http_but_do_not_use_this_in_production_and_only_within_safe_networks=True)
    assert new_instance is not None


def test_cached_token(openbis_instance):
    openbis_instance.save_token()
    assert openbis_instance.token_path is not None
    assert openbis_instance._get_cached_token() is not None

    another_instance = Openbis(openbis_instance.url, verify_certificates=openbis_instance.verify_certificates)
    assert another_instance.is_token_valid() is True

    openbis_instance.delete_token()
    assert openbis_instance._get_cached_token() is None


def test_get_dataset_parents(openbis_instance):
    permid = '20130415093804724-403'
    parent_permid = '20130415100158230-407'
    dataset = openbis_instance.get_dataset(permid)
    assert dataset is not None
    parents = dataset.get_parents()
    assert isinstance(parents, list)
    assert parents[0] is not None
    assert isinstance(parents[0], DataSet)
    assert parents[0].permid == parent_permid

    children = parents[0].get_children()
    assert isinstance(children, list)
    assert children[0] is not None
    assert isinstance(children[0], DataSet)


def test_get_dataset_by_permid(openbis_instance):
    permid = '20130412142942295-198'
    permid = '20130412153118625-384'
    dataset = openbis_instance.get_dataset(permid)
    assert dataset is not None
    assert isinstance(dataset, DataSet)
    assert 'dataStore' in dataset.data
    assert 'downloadUrl' in dataset.data['dataStore']
    file_list = dataset.get_file_list(recursive=False)
    assert file_list is not None
    assert isinstance(file_list, list)
    assert len(file_list) == 1

    file_list = dataset.get_file_list(recursive=True)
    assert file_list is not None
    assert len(file_list) > 10


def test_dataset_upload(openbis_instance):
    datastores = openbis_instance.get_datastores()
    assert datastores is not None
    #    assert isinstance(datastores, list)
    # filename = 'testfile.txt'
    # with open(filename, 'w') as f:
    #    f.write('test-data')

    # ds = openbis_instance.new_dataset(
    #    name        = "My Dataset",
    #    description = "description",
    #    type        = "UNKNOWN",
    #    sample      = sample,
    #    files       = ["testfile.txt"],
    # )

    # analysis = openbis_instance.new_analysis(
    #    name = "My analysis",                       # * name of the container
    #    description = "a description",              #
    #    sample = sample,                            #   my_dataset.sample is the default

    #    # result files will be registered as JUPYTER_RESULT datatype
    #    result_files = ["my_wonderful_result.txt"], #   path of my results

    #    # jupyter notebooks file will be registered as JUPYTER_NOTEBOOk datatype
    #    notebook_files = ["notebook.ipynb"],        #   specify a specific notebook
    #    #notebook_files = "~/notebooks/",           #   path of notebooks
    #    parents = [parent_dataset],                 # other parents are optional, my_dataset is the default parent
    # )

    # analysis.save     # start registering process


def test_create_permId(openbis_instance):
    permId = openbis_instance.create_permId()
    assert permId is not None
    m = re.search('([0-9]){17}-([0-9]*)', permId)
    ts = m.group(0)
    assert ts is not None
    count = m.group(1)
    assert count is not None

def test_get_dataset_types(openbis_instance):
    dataset_types = openbis_instance.get_dataset_types();
    dataset_type_unknown = dataset_types['UNKNOWN']
    assert dataset_type_unknown is not None 
    assert dataset_type_unknown.code is not None 
    assert dataset_type_unknown.description is not None 
