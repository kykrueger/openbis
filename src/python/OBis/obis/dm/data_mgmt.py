#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
data_mgmt.py

Module implementing data management operations.

Created by Chandrasekhar Ramakrishnan on 2017-02-01.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""

import pybis
import abc


# noinspection PyPep8Naming
def DataMgmt(openbis_config={}, git_config={}):
    """Factory method for DataMgmt instances"""
    complete_openbis_config(openbis_config)
    complete_git_config(git_config)

    openbis = pybis.Openbis(**openbis_config)
    git_wrapper = GitWrapper(**git_config)
    if git_wrapper.can_run():
        return GitDataMgmt(openbis, git_wrapper)
    return NoGitDataMgmt(openbis, git_wrapper)


def complete_openbis_config(config):
    """Add default values for empty entries in the config."""
    if not config.get('url'):
        config['url'] = 'https://localhost:8443'
    if not config.get('verify_certificates'):
        config['verify_certificates'] = True
    if not config.get('token'):
        config['token'] = None


def complete_git_config(config):
    """Add default values for empty entries in the config."""

    find_git = config['find_git'] if config.get('find_git') else False
    if find_git:
        config['git_path'] = locate_path('git')
        config['git_annex_path'] = locate_path('git-annex')


def locate_path(command):
    return None


class AbstractDataMgmt(object):
    __metaclass__ = abc.ABCMeta
    """Abstract object that implements operations."""

    def __init__(self, openbis, git_wrapper):
        self.openbis = openbis
        self.git_wrapper = git_wrapper

    def init_data(self, path):
        return None


class NoGitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations when git is not available -- show error messages."""
    pass


class GitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations in normal state."""
    pass


class GitWrapper(object):
    """A wrapper on commands to git."""

    def __init__(self, git_path=None, git_annex_path=None):
        self.git_path = git_path
        self.git_annex_path = git_annex_path

    def can_run(self):
        """Return true if the perquisites are satisfied to run"""
        if self.git_path is None:
            return False
        if self.git_annex_path is None:
            return False
        return False
