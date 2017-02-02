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
    dm.init_data(str(tmpdir))
