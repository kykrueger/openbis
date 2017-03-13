#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
repo.py

Python interface to data management functionality. Recommended for use from Jupyter.

Created by Chandrasekhar Ramakrishnan on 2017-03-03.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""

from . import data_mgmt


class DataRepo(object):
    def __init__(self, root):
        """ Initialize with a folder as the root.
        :param root: The root path for the repo
        """
        self.root = root
        self.dm_api = data_mgmt.DataMgmt(git_config={'find_git': True})
        self.dm_api.config_resolver.location_resolver.location_roots['data_set'] = self.root

    def init(self, desc=None):
        return self.dm_api.init_data(self.root, desc)

    def commit(self, msg, auto_add=True, sync=True):
        with data_mgmt.cd(self.root):
            result = self.dm_api.commit(msg, auto_add, sync)
        return result
