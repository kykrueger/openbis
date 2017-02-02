#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
data_mgmt_test.py


Created by Chandrasekhar Ramakrishnan on 2017-02-02.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""

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


def test_normal_use_case(shared_dm, tmpdir):
    # The folder should not be a git repo at first.
    result = data_mgmt.run_shell(['git', '-C', str(tmpdir), 'status', ])
    assert result.returncode == 128

    result = shared_dm.init_data(str(tmpdir), "test")
    assert result.returncode == 0

    # The folder should be a git repo now
    result = data_mgmt.run_shell(['git', '-C', str(tmpdir), 'status', str(tmpdir)])
    assert result.returncode == 0

    # ...and a git-annex repo as well.
    result = data_mgmt.run_shell(['git', '-C', str(tmpdir), 'annex', 'status', str(tmpdir)])
    assert result.returncode == 0
