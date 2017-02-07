#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
data_mgmt_test.py


Created by Chandrasekhar Ramakrishnan on 2017-02-02.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""
import os
import shutil

from . import data_mgmt


def test_no_git(tmpdir):
    dm = data_mgmt.DataMgmt()
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


def test_data_use_case(shared_dm, tmpdir):
    tmp_dir_path = str(tmpdir)
    assert git_status(tmp_dir_path).returncode == 128  # The folder should not be a git repo at first.

    result = shared_dm.init_data(tmp_dir_path, "test")
    assert result.returncode == 0

    assert git_status(tmp_dir_path).returncode == 0  # The folder should be a git repo now
    assert git_status(tmp_dir_path, annex=True).returncode == 0  # ...and a git-annex repo as well.

    copy_test_data(tmpdir)

    with data_mgmt.cd(tmp_dir_path):
        result = shared_dm.commit("Added data.")
        assert result.returncode == 0

        # The zip should be in the annex
        result = data_mgmt.run_shell(['git', 'annex', 'info', 'snb-data.zip'])
        present_p = result.output.split('\n')[-1]
        assert present_p == 'present: true'

        # The txt files should be in git normally
        result = data_mgmt.run_shell(['git', 'annex', 'info', 'test.txt'])
        present_p = result.output.split(' ')[-1]
        assert present_p == 'failed'
        result = data_mgmt.run_shell(['git', 'log', '--oneline', 'test.txt'])
        present_p = " ".join(result.output.split(' ')[1:])
        assert present_p == 'Added data.'


def copy_test_data(tmpdir):
    # Put some (binary) content into our new repository
    test_data_folder = os.path.join(os.path.dirname(__file__), '..', 'test-data')
    test_data_bin_src = os.path.join(test_data_folder, "snb-data.zip")
    test_data_bin_path = str(tmpdir.join(os.path.basename(test_data_bin_src)))
    shutil.copyfile(test_data_bin_src, test_data_bin_path)

    # Put some text content into our new repository
    test_data_txt_src = os.path.join(test_data_folder, "test.txt")
    test_data_txt_path = str(tmpdir.join(os.path.basename(test_data_txt_src)))
    shutil.copyfile(test_data_txt_src, test_data_txt_path)

    return test_data_bin_path, test_data_bin_path
