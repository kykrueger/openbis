#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
config_test.py


Created by Chandrasekhar Ramakrishnan on 2017-02-10.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""
import os

from . import config


def test_config_location_resolver():
    loc_resolver = config.ConfigLocationResolver()
    loc = config.ConfigLocation(['global'], 'user_home', '.obis')
    assert loc_resolver.path_for_location(loc) == os.path.join(os.path.expanduser("~"), '.obis')

    # self.add_location(ConfigLocation(['local', 'public'], 'data_set', '.obis'))
    # self.add_location(ConfigLocation(['local', 'private'], 'data_set', '.git/obis'))


# TODO Test that the config resolves correctly in the default case
def test_read_config():
    resolver = config.ConfigResolver()
    assert resolver.env.params['user'] is not None

# TODO Override the location resolver and test that we can write values
