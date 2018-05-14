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

    def __init__(self, name, private, is_json=False, ignore_global=False, default_value=None):
        """
        :param name: Name of the parameter.
        :param private: Should the parameter be private to the repo or visible in the data set?
        :param is_json: Is the parameter json? Default false
        """
        self.name = name
        self.private = private
        self.is_json = is_json
        self.ignore_global = ignore_global
        self.default_value = default_value

    def location_path(self, loc):
        if loc == 'global':
            return [loc]
        if self.private:
            return [loc, 'private']
        else:
            return [loc, 'public']

    def parse_value(self, value):
        if not self.is_json:
            return value
        if isinstance(value, str):
            return self.parse_json_value(value)
        return value

    def parse_json_value(self, value):
        value_dict = json.loads(value, object_hook=self.json_upper)
        return value_dict

    def json_upper(self, obj):
        for key in obj.keys():
            new_key = key.upper()
            if new_key != key:
                if new_key in obj:
                    raise ValueError("Duplicate key after capitalizing JSON config: " + new_key)
                obj[new_key] = obj[key]
                del obj[key]
        return obj


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
        self.add_param(ConfigParam(name='fileservice_url', private=False))
        self.add_param(ConfigParam(name='user', private=True))
        self.add_param(ConfigParam(name='verify_certificates', private=True, is_json=True, default_value=True))
        self.add_param(ConfigParam(name='object_id', private=False, ignore_global=True))
        self.add_param(ConfigParam(name='collection_id', private=False, ignore_global=True))
        self.add_param(ConfigParam(name='data_set_type', private=False))
        self.add_param(ConfigParam(name='data_set_properties', private=False, is_json=True))
        self.add_param(ConfigParam(name='hostname', private=False))
        self.add_param(ConfigParam(name='git_annex_hash_as_checksum', private=False, is_json=True, default_value=True))

    def add_param(self, param):
        self.params[param.name] = param

    def location_at_path(self, location_path):
        location = self.locations
        for path in location_path:
            location = location[path]
        return location

    def is_usersetting(self):
        return True


class DataSetEnv(ConfigEnv):

    # TODO remove data_set from property names
    def initialize_params(self):
        self.add_param(ConfigParam(name='data_set_type', private=False))
        self.add_param(ConfigParam(name='data_set_properties', private=False, is_json=True))        


class RepositoryEnv(ConfigEnv):
    """ These are properties which are not configured by the user but set by obis. """

    def initialize_params(self):
        self.add_param(ConfigParam(name='external_dms_id', private=True))
        self.add_param(ConfigParam(name='repository_id', private=True))
        self.add_param(ConfigParam(name='data_set_id', private=False))

    def is_usersetting(self):
        return False


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

    def __init__(self, env=None, location_resolver=None, config_file='config.json'):
        self.env = env if env is not None else ConfigEnv()
        self.location_resolver = location_resolver if location_resolver is not None else LocationResolver()
        self.location_search_order = ['global', 'local']
        self.location_cache = {}
        self.is_initialized = False
        self.config_file = config_file

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
            config_path = os.path.join(root_path, self.config_file)
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
                if l == 'global' and (local_only or param.ignore_global):
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
        value = param.parse_value(value)
        location_config_dict = self.set_cache_value_for_parameter(param, value, loc)
        location_path = param.location_path(loc)
        location = self.env.location_at_path(location_path)
        location_dir_path = self.location_resolver.resolve_location(location)
        if not os.path.exists(location_dir_path):
            os.makedirs(location_dir_path)
        config_path = os.path.join(location_dir_path, self.config_file)
        with open(config_path, "w") as f:
            json.dump(location_config_dict, f, sort_keys=True)

    def set_value_for_json_parameter(self, json_param_name, name, value, loc):
        """Set one field for the json parameter
        :param json_param_name: Name of the json parameter
        :param name: Name of the field
        :param loc: Either 'local' or 'global'
        :return:
        """
        if not self.is_initialized:
            self.initialize_location_cache()

        param = self.env.params[json_param_name]

        if not param.is_json:
            raise ValueError('Can not set json value for non-json parameter: ' + json_param_name)

        json_value = self.value_for_parameter(param, loc)
        if json_value is None:
            json_value = {}
        json_value[name.upper()] = value

        self.set_value_for_parameter(json_param_name, json.dumps(json_value), loc)


    def value_for_parameter(self, param, loc):
        config = self.location_cache[loc]
        if loc != 'global':
            if param.private:
                config = config['private']
            else:
                config = config['public']
        value = config.get(param.name)
        if loc == 'global' and value is None:
            value = param.default_value
        return value

    def set_cache_value_for_parameter(self, param, value, loc):
        config = self.location_cache[loc]
        if loc != 'global':
            if param.private:
                config = config['private']
            else:
                config = config['public']
        config[param.name] = value
        return config

    def local_public_properties_path(self):
        loc = self.env.location_at_path(['local', 'public'])
        return self.location_resolver.resolve_location(loc) + '/' + self.config_file

    def copy_global_to_local(self):
        config = self.config_dict(False)
        local_config = self.config_dict(True)
        for k, v in config.items():
            # Do not overwrite existing values
            if local_config.get(k) is not None:
                continue
            # Do not copy params which should not be taken from global
            param = self.env.params[k]
            if param.ignore_global:
                continue
            self.set_value_for_parameter(k, v, 'local')

    def is_usersetting(self):
        return self.env.is_usersetting()


class SettingsResolver(object):
    """ This class functions as a wrapper since we have multiple config resolvers. """

    def __init__(self, location_resolver=None):
        self.repository_resolver = ConfigResolver(location_resolver=location_resolver, env=RepositoryEnv(), config_file='repository.json')
        # TODO remove config_resolver in the end
        self.config_resolver = ConfigResolver(location_resolver=location_resolver, env=ConfigEnv())
        self.resolvers = []
        self.resolvers.append(self.config_resolver)
        self.resolvers.append(self.repository_resolver)

    def config_dict(self, local_only=False):
        combined_dict = {}
        for resolver in self.resolvers:
            combined_dict.update(resolver.config_dict(local_only=local_only))
        return combined_dict

    def set_value_for_parameter(self, name, value, loc):
        for resolver in self.resolvers:
            if name in resolver.env.params:
                return resolver.set_value_for_parameter(name, value, loc)
        raise ValueError('Config does not exist: ' + name)

    def set_value_for_json_parameter(self, json_param_name, name, value, loc):
        for resolver in self.resolvers:
            if json_param_name in resolver.env.params:
                return resolver.set_value_for_json_parameter(json_param_name, name, value, loc)

    # TODO return a list
    def local_public_properties_paths(self, get_usersettings=False):
        paths = []
        for resolver in self.resolvers:
            if get_usersettings == resolver.is_usersetting():
                paths.append(resolver.local_public_properties_path())
        return paths

    def copy_global_to_local(self):
        for resolver in self.resolvers:
            resolver.copy_global_to_local()

    def set_resolver_location_roots(self, key, value):
        for resolver in self.resolvers:
            resolver.location_resolver.location_roots[key] = value

    def set_location_search_order(self, order):
        for resolver in self.resolvers:
            resolver.location_search_order = order

    def is_usersetting(self, name):
        for resolver in self.resolvers:
            if name in resolver.env.params:
                return resolver.is_usersetting()
