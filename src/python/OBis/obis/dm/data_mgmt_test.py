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
        dm.init_data(str(tmpdir))
        assert False, "Command should have failed -- no git defined."
    except ValueError:
        pass


def test_locate_command():
    result = data_mgmt.locate_command("bash")
    assert result[0] == 0
    assert result[1] == "/bin/bash"
