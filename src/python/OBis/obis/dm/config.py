#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
config.py

Configuration for obis.

Created by Chandrasekhar Ramakrishnan on 2017-02-10.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""
import json
import os


class ConfigLocation(object):
    """Path for configuration information."""

    def __init__(self, desc, root, basename):
        """
        :param desc: A description for the location in the form of a list of keys.
        :param root: The root for the path
        :param basename: The name of the folder for this location.
        """
        self.desc = desc
        self.root = root
        self.basename = basename


class ConfigParam(object):
    """Class for configuration parameters."""

    def __init__(self, name, private):
        """

        :param name: Name of the parameter.
        :param private: Should the parameter be private to the repo or visible in the data set?
        """
        self.name = name
        self.private = private


class ConfigEnv(object):
    """The environment in which configurations are constructed."""

    def __init__(self):
        self.locations = {}
        self.params = {}
        self.initialize_locations()
        self.initialize_params()

    def initialize_locations(self):
        self.add_location(ConfigLocation(['global'], 'user_home', '.obis'))
        self.add_location(ConfigLocation(['local', 'public'], 'data_set', '.obis'))
        self.add_location(ConfigLocation(['local', 'private'], 'data_set', '.git/obis'))

    def add_location(self, loc):
        desc = loc.desc
        depth = len(desc) - 1
        locations = self.locations
        for i, sub_desc in enumerate(desc):
            if i == depth:
                locations[sub_desc] = loc
            else:
                if locations.get(sub_desc) is None:
                    locations[sub_desc] = {}
                locations = locations[sub_desc]

    def initialize_params(self):
        self.add_param(ConfigParam(name='openbis_url', private=False))
        self.add_param(ConfigParam(name='user', private=True))
        self.add_param(ConfigParam(name='external_dms_id', private=True))
        self.add_param(ConfigParam(name='data_set_id', private=False))
        self.add_param(ConfigParam(name='data_set_type', private=False))
        self.add_param(ConfigParam(name='data_set_properties', private=False))

    def add_param(self, param):
        self.params[param.name] = param

    def location_at_path(self, location_path):
        location = self.locations
        for path in location_path:
            location = location[path]
        return location


class LocationResolver(object):
    def __init__(self):
        self.location_roots = {
            'user_home': os.path.expanduser('~'),
            'data_set': './'
        }

    def resolve_location(self, location):
        root = self.location_roots[location.root]
        return os.path.join(root, location.basename)


class ConfigResolver(object):
    """Construct a config dictionary."""

    def __init__(self, env=None, location_resolver=None):
        self.env = env if env is not None else ConfigEnv()
        self.location_resolver = location_resolver if location_resolver is not None else LocationResolver()
        self.location_search_order = ['global', 'local']
        self.location_cache = {}
        self.is_initialized = False

    def initialize_location_cache(self):
        env = self.env
        for k, v in env.locations.items():
            self.initialize_location(k, v, self.location_cache)

    def initialize_location(self, key, loc, cache):
        cache[key] = {}  # Default value is empty dict
        if isinstance(loc, dict):
            for k, v in loc.items():
                self.initialize_location(k, v, cache[key])
        else:
            root_path = self.location_resolver.resolve_location(loc)
            config_path = os.path.join(root_path, 'config.json')
            if os.path.exists(config_path):
                with open(config_path) as f:
                    config = json.load(f)
                    cache[key] = config

    def config_dict(self, local_only=False):
        """Return a configuration dictionary by applying the lookup/resolution rules.
        :param local_only: If true, do not check the global location.
        :return: A dictionary with the configuration.
        """
        if not self.is_initialized:
            self.initialize_location_cache()
        env = self.env
        result = {}
        # Iterate over the locations in the specified order searching for parameter values.
        # Later entries override earlier.
        for name, param in env.params.items():
            result[name] = None
            for l in self.location_search_order:
                if local_only and l == 'global':
                    continue
                val = self.value_for_parameter(param, l)
                if val is not None:
                    result[name] = val
        return result

    def set_value_for_parameter(self, name, value, loc):
        """Set the value for the parameter
        :param name: Name of the parameter
        :param loc: Either 'local' or 'global'
        :return:
        """
        if not self.is_initialized:
            self.initialize_location_cache()

        param = self.env.params[name]
        location_config_dict = self.set_cache_value_for_parameter(param, value, loc)
        if loc != 'global':
            if param.private:
                location_path = [loc, 'private']
            else:
                location_path = [loc, 'public']
        else:
            location_path = [loc]
        location = self.env.location_at_path(location_path)
        location_dir_path = self.location_resolver.resolve_location(location)
        if not os.path.exists(location_dir_path):
            print("Create {}".format(location_dir_path))
            os.makedirs(location_dir_path)
        config_path = os.path.join(location_dir_path, 'config.json')
        with open(config_path, "w") as f:
            json.dump(location_config_dict, f)

    def value_for_parameter(self, param, loc):
        config = self.location_cache[loc]
        if loc != 'global':
            if param.private:
                config = config['private']
            else:
                config = config['public']
        return config.get(param.name)

    def set_cache_value_for_parameter(self, param, value, loc):
        config = self.location_cache[loc]
        if loc != 'global':
            if param.private:
                config = config['private']
            else:
                config = config['public']
        config[param.name] = value
        return config
