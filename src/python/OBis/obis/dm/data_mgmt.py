#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
data_mgmt.py

Module implementing data management operations.

Created by Chandrasekhar Ramakrishnan on 2017-02-01.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""
import abc
import os
import shutil
import traceback
import pybis
import requests
from . import config as dm_config
from .commands.addref import Addref
from .commands.removeref import Removeref
from .commands.clone import Clone
from .commands.openbis_sync import OpenbisSync
from .command_result import CommandResult
from .command_result import CommandException
from .git import GitWrapper
from .utils import default_echo
from .utils import complete_git_config
from .utils import complete_openbis_config
from .utils import cd
from ..scripts import cli


# noinspection PyPep8Naming
def DataMgmt(echo_func=None, config_resolver=None, openbis_config={}, git_config={}, openbis=None):
    """Factory method for DataMgmt instances"""

    echo_func = echo_func if echo_func is not None else default_echo

    complete_git_config(git_config)
    git_wrapper = GitWrapper(**git_config)
    if not git_wrapper.can_run():
        return NoGitDataMgmt(config_resolver, None, git_wrapper, openbis)

    if config_resolver is None:
        config_resolver = dm_config.ConfigResolver()
        result = git_wrapper.git_top_level_path()
        if result.success():
            config_resolver.set_resolver_location_roots('data_set', result.output)
    complete_openbis_config(openbis_config, config_resolver)

    return GitDataMgmt(config_resolver, openbis_config, git_wrapper, openbis)


class AbstractDataMgmt(metaclass=abc.ABCMeta):
    """Abstract object that implements operations.

    All operations throw an exepction if they fail.
    """

    def __init__(self, config_resolver, openbis_config, git_wrapper, openbis):
        self.config_resolver = config_resolver
        self.openbis_config = openbis_config
        self.git_wrapper = git_wrapper
        self.openbis = openbis

    def error_raise(self, command, reason):
        """Raise an exception."""
        message = "'{}' failed. {}".format(command, reason)
        raise ValueError(reason)

    @abc.abstractmethod
    def init_data(self, path, desc=None, create=True):
        """Initialize a data repository at the path with the description.
        :param path: Path for the repository.
        :param desc: An optional short description of the repository (used by git-annex)
        :param create: If True and the folder does not exist, create it. Defaults to true.
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def init_analysis(self, path, parent, desc=None, create=True, apply_config=False):
        """Initialize an analysis repository at the path.
        :param path: Path for the repository.
        :param parent: (required when outside of existing repository) Path for the parent repositort
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def commit(self, msg, auto_add=True, sync=True):
        """Commit the current repo.

        This issues a git commit and connects to openBIS and creates a data set in openBIS.
        :param msg: Commit message.
        :param auto_add: Automatically add all files in the folder to the repo. Defaults to True.
        :param sync: If true, sync with openBIS server.
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def sync(self):
        """Sync the current repo.

        This connects to openBIS and creates a data set in openBIS.
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def status(self):
        """Return the status of the current repository.
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def clone(self, data_set_id, ssh_user, content_copy_index):
        """Clone / copy a repository related to the given data set id.
        :param data_set_id: 
        :param ssh_user: ssh user for remote clone (optional)
        :param content_copy_index: index of content copy in case there are multiple copies (optional)
        :return: A CommandResult.
        """
        return

    def addref(self):
        """Add the current folder as an obis repository to openBIS.
        :return: A CommandResult.
        """
        return

    def removeref(self):
        """Remove the current folder / repository from openBIS.
        :return: A CommandResult.
        """
        return


class NoGitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations when git is not available -- show error messages."""

    def init_data(self, path, desc=None, create=True):
        self.error_raise("init data", "No git command found.")

    def init_analysis(self, path, parent, desc=None, create=True, apply_config=False):
        self.error_raise("init analysis", "No git command found.")

    def commit(self, msg, auto_add=True, sync=True):
        self.error_raise("commit", "No git command found.")

    def sync(self):
        self.error_raise("sync", "No git command found.")

    def status(self):
        self.error_raise("status", "No git command found.")

    def clone(self, data_set_id, ssh_user, content_copy_index):
        self.error_raise("clone", "No git command found.")

    def addref(self):
        self.error_raise("addref", "No git command found.")

    def removeref(self):
        self.error_raise("removeref", "No git command found.")


class GitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations in normal state."""

    def setup_local_config(self, config, path):
        with cd(path):
            self.config_resolver.set_resolver_location_roots('data_set', '.')
            for key, value in config.items():
                self.config_resolver.set_value_for_parameter(key, value, 'local')


    def check_repository_state(self, path):
        """Checks if the repo already exists and has uncommitted files."""
        with cd(path):
            git_status = self.git_wrapper.git_status()
            if git_status.failure():
                return ('NOT_INITIALIZED', None)
            if git_status.output is not None and len(git_status.output) > 0:
                return ('PENDING_CHANGES', git_status.output)
            return ('SYNCHRONIZED', None)


    def get_data_set_id(self, path):
        with cd(path):
            return self.config_resolver.config_dict().get('data_set_id')

    def get_config(self, path, key):
        with cd(path):
            return self.config_resolver.config_dict().get(key)

    def init_data(self, path, desc=None, create=True, apply_config=False):
        if not os.path.exists(path) and create:
            os.mkdir(path)
        result = self.git_wrapper.git_init(path)
        if result.failure():
            return result
        result = self.git_wrapper.git_annex_init(path, desc)
        if result.failure():
            return result
        with cd(path):
            # Update the resolvers location
            self.config_resolver.set_resolver_location_roots('data_set', '.')
            self.config_resolver.copy_global_to_local()
            self.commit_metadata_updates('local with global')
        return result


    def init_analysis(self, path, parent, desc=None, create=True, apply_config=False):

        # get data_set_id of parent from current folder or explicit parent argument
        parent_folder = parent if parent is not None and len(parent) > 0 else "."
        parent_data_set_id = self.get_data_set_id(parent_folder)
        # check that parent repository has been added to openBIS
        if self.get_config(parent_folder, 'repository_id') is None:
            return CommandResult(returncode=-1, output="Parent data set must be committed to openBIS before creating an analysis data set.")
        # check that analysis repository does not already exist
        if os.path.exists(path):
            return CommandResult(returncode=-1, output="Data set already exists: " + path)
        # init analysis repository
        result = self.init_data(path, desc, create, apply_config)
        if result.failure():
            return result
        # add analysis repository folder to .gitignore of parent
        if os.path.exists('.obis'):
            self.git_wrapper.git_ignore(path)
        elif parent is None:
            return CommandResult(returncode=-1, output="Not within a repository and no parent set.")
        # set data_set_id to analysis repository so it will be used as parent when committing
        with cd(path):
            cli.set_property(self, "data_set_id", parent_data_set_id, False, False)
        return result


    def sync(self):
        self.set_restorepoint()
        result = self._sync()
        if result.failure():
            self.restore()
        return result


    def _sync(self):
        cmd = OpenbisSync(self)
        return cmd.run()


    def commit(self, msg, auto_add=True, sync=True, path=None):
        if path is not None:
            with cd(path):
                return self._commit(msg, auto_add, sync);
        else:
            return self._commit(msg, auto_add, sync);


    def _commit(self, msg, auto_add=True, sync=True):
        self.set_restorepoint()
        if auto_add:
            result = self.git_wrapper.git_top_level_path()
            if result.failure():
                return result
            result = self.git_wrapper.git_add(result.output)
            if result.failure():
                return result
        result = self.git_wrapper.git_commit(msg)
        if result.failure():
            # TODO If no changes were made check if the data set is in openbis. If not, just sync.
            return result
        if sync:
            result = self._sync()
            if result.failure():
                self.restore()
        return result

    def status(self):
        git_status = self.git_wrapper.git_status()
        try:
            sync_status = OpenbisSync(self).run(info_only=True)
        except requests.exceptions.ConnectionError:
            sync_status = CommandResult(returncode=-1, output="Could not connect to openBIS.")
        output = git_status.output
        if sync_status.failure():
            if len(output) > 0:
                output += '\n'
            output += sync_status.output
        return CommandResult(returncode=0, output=output)

    def commit_metadata_updates(self, msg_fragment=None):
        properties_path = self.config_resolver.local_public_properties_path()
        status = self.git_wrapper.git_status(properties_path)
        if len(status.output.strip()) < 1:
            # Nothing to commit
            return CommandResult(returncode=0, output="")
        self.git_wrapper.git_add(properties_path)
        if msg_fragment is None:
            msg = "OBIS: Update openBIS metadata cache."
        else:
            msg = "OBIS: Update {}.".format(msg_fragment)
        return self.git_wrapper.git_commit(msg)

    def set_restorepoint(self):
        self.previous_git_commit_hash = self.git_wrapper.git_commit_hash().output

    def restore(self):
        self.git_wrapper.git_reset_to(self.previous_git_commit_hash)
        properties_path = self.config_resolver.local_public_properties_path()
        self.git_wrapper.git_checkout(properties_path)

    def clone(self, data_set_id, ssh_user, content_copy_index):
        try:
            cmd = Clone(self, data_set_id, ssh_user, content_copy_index)
            return cmd.run()
        except Exception as e:
            return CommandResult(returncode=-1, output="Error: " + str(e))

    def addref(self):
        try:
            cmd = Addref(self)
            return cmd.run()
        except Exception as e:
            return CommandResult(returncode=-1, output="Error: " + str(e))

    def removeref(self):
        try:
            cmd = Removeref(self)
            return cmd.run()
        except Exception as e:
            return CommandResult(returncode=-1, output="Error: " + str(e))
