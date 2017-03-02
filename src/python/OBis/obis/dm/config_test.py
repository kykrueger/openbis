#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
config_test.py


Created by Chandrasekhar Ramakrishnan on 2017-02-10.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""
import json
import os
import shutil

from . import config


def test_config_location_resolver():
    loc = config.ConfigLocation(['global'], 'user_home', '.obis')
    assert config.default_location_resolver(loc) == os.path.join(os.path.expanduser("~"), '.obis')


def user_config_test_data_path():
    return os.path.join(os.path.dirname(__file__), '..', 'test-data', 'user_config')


def copy_user_config_test_data(tmpdir):
    config_test_data_src = user_config_test_data_path()
    config_test_data_dst = str(tmpdir.join(os.path.basename(config_test_data_src)))
    shutil.copytree(config_test_data_src, config_test_data_dst)
    return config_test_data_src, config_test_data_dst


def location_resolver_for_test(tmpdir):
    def resolver(location):
        if location.root == 'user_home':
            return os.path.join(str(tmpdir), 'user_config', location.basename)
        else:
            return config.default_location_resolver(location)

    return resolver


def test_read_config(tmpdir):
    copy_user_config_test_data(tmpdir)
    resolver = config.ConfigResolver(location_resolver=location_resolver_for_test(tmpdir))
    config_dict = resolver.config_dict()
    assert config_dict is not None
    with open(os.path.join(user_config_test_data_path(), ".obis", "config.json")) as f:
        expected_dict = json.load(f)
    assert config_dict['user'] == expected_dict['user']


def test_write_config(tmpdir):
    copy_user_config_test_data(tmpdir)
    resolver = config.ConfigResolver(location_resolver=location_resolver_for_test(tmpdir))
    config_dict = resolver.config_dict()
    assert config_dict is not None
    with open(os.path.join(user_config_test_data_path(), ".obis", "config.json")) as f:
        expected_dict = json.load(f)
    assert config_dict['openbis_url'] == expected_dict['openbis_url']
    assert config_dict['user'] == expected_dict['user']

    resolver.set_value_for_parameter('user', 'new_user', 'global')
    config_dict = resolver.config_dict()
    assert config_dict['openbis_url'] == expected_dict['openbis_url']
    assert config_dict['user'] == 'new_user'
