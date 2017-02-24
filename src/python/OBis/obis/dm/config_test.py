#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
config_test.py


Created by Chandrasekhar Ramakrishnan on 2017-02-10.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""
import json
import os

from . import config


def test_config_location_resolver():
    loc = config.ConfigLocation(['global'], 'user_home', '.obis')
    assert config.default_location_resolver(loc) == os.path.join(os.path.expanduser("~"), '.obis')


def dummy_user_config_path():
    return os.path.join(os.path.dirname(__file__), '..', 'test-data', 'user_config')


def dummy_location_resolver(location):
    if location.root == 'user_home':
        return os.path.join(dummy_user_config_path(), location.basename)
    else:
        return config.default_location_resolver(location)


def test_read_config():
    resolver = config.ConfigResolver(location_resolver=dummy_location_resolver)
    config_dict = resolver.config_dict()
    assert config_dict is not None
    print(config_dict)
    with open(os.path.join(dummy_user_config_path(), ".obis", "config.json")) as f:
        expected_dict = json.load(f)
    assert config_dict['user'] == expected_dict['user']

# TODO Override the location resolver and test that we can write values
