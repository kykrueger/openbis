#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
config_test.py


Created by Chandrasekhar Ramakrishnan on 2017-02-10.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""

from . import config


# TODO Test that the config reolves correctly in the default case
def test_read_config():
    resolver = config.ConfigResolver()
    assert resolver.env.params['user'] is not None

# TODO Override the location resolver and test that we can write values
