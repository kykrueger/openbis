#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
conftest.py

Global fixtures for tests.

Created by Chandrasekhar Ramakrishnan on 2017-02-02.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""

import pytest
from . import dm

pytest.fixture(scope="session")


def shared_obis():
    git_config = {'find_git': True}
    return dm.DataMgmt(git_config=git_config)
