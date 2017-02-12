#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
config.py

Configuration for obis.

Created by Chandrasekhar Ramakrishnan on 2017-02-10.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""


class ConfigLocation(object):
    """Path for configuration information."""

    def __init__(self, desc, root, path):
        """
        :param desc: A description for the location.
        :param root: The root for the path
        :param path: The path for this location.
        """
        self.desc = desc
        self.root = root
        self.path = path


class ConfigParam(object):
    """Class for configuration parameters."""

    def __init__(self, name, copy_down, public):
        """

        :param name: Name of the parameter.
        :param copy_down: Should the parameter be copied to the data set?
        :param public: Is the parameter public?
        """
        self.name = name
        self.copy_down = copy_down
        self.public = public


class ConfigEnv(object):
    """The environment in which configurations are constructed."""

    def __init__(self):
        self.locations = {}
        self.params = {}
        self.initialize_locations()
        self.initialize_params()

    def initialize_locations(self):
        self.add_location(ConfigLocation('global', 'user_home', '.obis'))
        self.add_location(ConfigLocation('local_public', 'data_set', '.obis'))
        self.add_location(ConfigLocation('local_private', 'data_set', '.git/obis'))

    def add_location(self, loc):
        self.locations[loc.desc] = loc

    def initialize_params(self):
        self.add_param(ConfigParam(name='openbis_url', copy_down=True, public=True))
        self.add_param(ConfigParam(name='user', copy_down=False, public=False))
        self.add_param(ConfigParam(name='data_set_type', copy_down=True, public=False))
        self.add_param(ConfigParam(name='data_set_properties', copy_down=False, public=True))

    def add_param(self, param):
        self.params[param.name] = param


def default_location_resolver(location):
    """Given a location, return a path"""
    return None


class ConfigResolver(object):
    """Construct a config dictionary."""

    def __init__(self, env=None, location_resolver=None):
        self.env = env if env is not None else ConfigEnv()
        self.location_resolver = location_resolver if location_resolver is not None else default_location_resolver

    def config_dict(self):
        """Return a configuration dictionary by applying the lookup/resolution rules."""
        return {}
