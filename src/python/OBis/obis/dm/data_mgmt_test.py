#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
data_mgmt_test.py


Created by Chandrasekhar Ramakrishnan on 2017-02-02.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""
import json
import os
import random
import shutil

from datetime import datetime

from . import data_mgmt
from unittest.mock import Mock, MagicMock, ANY
from pybis.pybis import ExternalDMS, DataSet


def generate_perm_id():
    sequence = random.randrange(9999)
    ts = datetime.now().strftime("%Y%m%d%H%M%S%f")
    return "{}-{:04d}".format(ts, sequence)


def shared_dm():
    dm = data_mgmt.DataMgmt()
    return dm


def test_no_git(tmpdir):
    git_config = {'find_git': False}
    dm = data_mgmt.DataMgmt(git_config=git_config)
    try:
        dm.init_data(str(tmpdir), "")
        assert False, "Command should have failed -- no git defined."
    except ValueError:
        pass


def test_locate_command():
    result = data_mgmt.locate_command("bash")
    assert result.returncode == 0
    assert result.output == "/bin/bash"

    result = data_mgmt.locate_command("this_is_not_a_real_command")
    assert result.returncode == 1


def git_status(path=None, annex=False):
    cmd = ['git']
    if path:
        cmd.extend(['-C', path])
    if annex:
        cmd.extend(['annex', 'status'])
    else:
        cmd.extend(['status', '--porcelain'])
    return data_mgmt.run_shell(cmd)


def check_correct_config_semantics():
    # This how things should work
    with open('.obis/config.json') as f:
        config_local = json.load(f)
    assert config_local.get('data_set_id') is not None


def check_workaround_config_semantics():
    # This how things should work
    with open('.obis/config.json') as f:
        config_local = json.load(f)
    assert config_local.get('data_set_id') is None


def test_data_use_case(tmpdir):
    dm = shared_dm()

    tmp_dir_path = str(tmpdir)
    assert git_status(tmp_dir_path).returncode == 128  # The folder should not be a git repo at first.

    result = dm.init_data(tmp_dir_path, "test")
    assert result.returncode == 0

    assert git_status(tmp_dir_path).returncode == 0  # The folder should be a git repo now
    assert git_status(tmp_dir_path, annex=True).returncode == 0  # ...and a git-annex repo as well.

    copy_test_data(tmpdir)

    with data_mgmt.cd(tmp_dir_path):
        dm = shared_dm()
        prepare_registration_expectations(dm)
        set_registration_configuration(dm)

        raw_status = git_status()
        status = dm.status()
        assert raw_status.returncode == status.returncode
        assert raw_status.output == status.output
        assert len(status.output) > 0

        result = dm.commit("Added data.")
        assert result.returncode == 0

        # The zip should be in the annex
        result = data_mgmt.run_shell(['git', 'annex', 'info', 'snb-data.zip'])
        present_p = result.output.split('\n')[-1]
        assert present_p == 'present: true'

        # This file should be in the annex and a hardlink
        stat = os.stat("snb-data.zip")
        assert stat.st_nlink == 2

        # The txt files should be in git normally
        result = data_mgmt.run_shell(['git', 'annex', 'info', 'text-data.txt'])
        present_p = result.output.split(' ')[-1]
        assert present_p == 'failed'
        result = data_mgmt.run_shell(['git', 'log', '--oneline', 'text-data.txt'])
        present_p = " ".join(result.output.split(' ')[1:])
        assert present_p == 'Added data.'

        # This file is not in the annex and should not be a hardlink
        stat = os.stat("text-data.txt")
        assert stat.st_nlink == 1

        status = dm.status()
        assert len(status.output) == 0

        check_correct_config_semantics()


def test_child_data_set(tmpdir):
    dm = shared_dm()

    tmp_dir_path = str(tmpdir)

    result = dm.init_data(tmp_dir_path, "test")
    assert result.returncode == 0

    copy_test_data(tmpdir)

    with data_mgmt.cd(tmp_dir_path):
        dm = shared_dm()
        prepare_registration_expectations(dm)
        set_registration_configuration(dm)

        result = dm.commit("Added data.")
        assert result.returncode == 0
        parent_ds_code = dm.config_resolver.config_dict()['data_set_id']

        update_test_data(tmpdir)
        properties = {'DESCRIPTION': 'Updated content.'}
        set_registration_configuration(dm, properties)
        prepare_new_data_set_expectations(dm, properties)
        result = dm.commit("Updated data.")
        assert result.returncode == 0
        child_ds_code = dm.config_resolver.config_dict()['data_set_id']
        assert parent_ds_code != child_ds_code
        commit_id = dm.git_wrapper.git_commit_id().output

        contents = data_mgmt.GitRepoFileInfo(dm.git_wrapper).contents()
        check_new_data_set_expectations(dm, tmp_dir_path, commit_id, ANY, child_ds_code, parent_ds_code, properties,
                                        contents)


# TODO Test that if the data set registration fails, the data_set_id is reverted

def set_registration_configuration(dm, properties=None):
    resolver = dm.config_resolver
    resolver.set_value_for_parameter('openbis_url', "https://localhost:8443", 'local')
    resolver.set_value_for_parameter('user', "auser", 'local')
    resolver.set_value_for_parameter('data_set_type', "DS_TYPE", 'local')
    resolver.set_value_for_parameter('object_id', "/SAMPLE/ID", 'local')
    if properties is not None:
        resolver.set_value_for_parameter('data_set_properties', properties, 'local')


def prepare_registration_expectations(dm):
    dm.openbis = Mock()
    dm.openbis.is_session_active = MagicMock(return_value=True)
    edms = ExternalDMS(dm.openbis, {'code': 'AUSER-PATH', 'label': 'AUSER-PATH'})
    dm.openbis.create_external_data_management_system = MagicMock(return_value=edms)

    prepare_new_data_set_expectations(dm)


def prepare_new_data_set_expectations(dm, properties={}):
    perm_id = generate_perm_id()
    dm.openbis.create_perm_id = MagicMock(return_value=perm_id)
    data_set = DataSet(dm.openbis, None,
                       {'code': perm_id, 'properties': properties,
                        "parents": [], "children": [], "samples": [], 'tags': [],
                        'physicalData': None})
    dm.openbis.new_git_data_set = MagicMock(return_value=data_set)


def check_new_data_set_expectations(dm, tmp_dir_path, commit_id, external_dms, data_set_id, parent_id, properties,
                                    contents):
    dm.openbis.new_git_data_set.assert_called_with('DS_TYPE', tmp_dir_path, commit_id, external_dms, "/SAMPLE/ID",
                                                   data_set_code=data_set_id, parents=parent_id, properties=properties,
                                                   contents=contents)


def copy_test_data(tmpdir):
    # Put some (binary) content into our new repository
    test_data_folder = os.path.join(os.path.dirname(__file__), '..', 'test-data')
    test_data_bin_src = os.path.join(test_data_folder, "snb-data.zip")
    test_data_bin_path = str(tmpdir.join(os.path.basename(test_data_bin_src)))
    shutil.copyfile(test_data_bin_src, test_data_bin_path)

    # Put some text content into our new repository
    test_data_txt_src = os.path.join(test_data_folder, "text-data.txt")
    test_data_txt_path = str(tmpdir.join(os.path.basename(test_data_txt_src)))
    shutil.copyfile(test_data_txt_src, test_data_txt_path)

    return test_data_bin_path, test_data_txt_path


def update_test_data(tmpdir):
    # Put some (binary) content into our new repository
    test_data_folder = os.path.join(os.path.dirname(__file__), '..', 'test-data')
    # Put some text content into our new repository
    test_data_txt_src = os.path.join(test_data_folder, "text-data-2.txt")
    test_data_txt_path = str(tmpdir.join(os.path.basename(test_data_txt_src)))
    shutil.copyfile(test_data_txt_src, test_data_txt_path)

    return test_data_txt_path
