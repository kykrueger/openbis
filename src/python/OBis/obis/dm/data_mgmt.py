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
import subprocess
from contextlib import contextmanager
from . import config as dm_config
import traceback
import getpass
import socket

import pybis


# noinspection PyPep8Naming
def DataMgmt(echo_func=None, config_resolver=None, openbis_config={}, git_config={}):
    """Factory method for DataMgmt instances"""

    echo_func = echo_func if echo_func is not None else default_echo

    complete_git_config(git_config)
    git_wrapper = GitWrapper(**git_config)
    if not git_wrapper.can_run():
        return NoGitDataMgmt(echo_func, config_resolver, None, git_wrapper)

    if config_resolver is None:
        config_resolver = dm_config.ConfigResolver()
        result = git_wrapper.git_top_level_path()
        if result.success():
            config_resolver.location_resolver.location_roots['data_set'] = result.output
    complete_openbis_config(openbis_config, config_resolver)

    openbis = None
    if openbis_config.get('url') is None:
        pass
    else:
        try:
            openbis = pybis.Openbis(**openbis_config)
        except ValueError:
            echo_func({'level': 'error', 'message': 'Could not connect to openBIS.'})
            traceback.print_exc()

    return GitDataMgmt(echo_func, config_resolver, openbis, git_wrapper)


def complete_openbis_config(config, resolver):
    """Add default values for empty entries in the config."""
    config_dict = resolver.config_dict(local_only=True)
    if config.get('url') is None:
        config['url'] = config_dict['openbis_url']
    if config.get('verify_certificates') is None:
        config['verify_certificates'] = True
    if config.get('token') is None:
        config['token'] = None


def complete_git_config(config):
    """Add default values for empty entries in the config."""

    find_git = config['find_git'] if config.get('find_git') is not None else True
    if find_git:
        git_cmd = locate_command('git')
        if git_cmd.success():
            config['git_path'] = git_cmd.output

        git_annex_cmd = locate_command('git-annex')
        if git_annex_cmd.success():
            config['git_annex_path'] = git_annex_cmd.output


def default_echo(details):
    if details.get('level') != "DEBUG":
        print(details['message'])


class CommandResult(object):
    """Encapsulate result from a subprocess call."""

    def __init__(self, completed_process=None, returncode=None, output=None):
        """Convert a completed_process object into a ShellResult."""
        if completed_process:
            self.returncode = completed_process.returncode
            self.output = completed_process.stdout.decode('utf-8').strip()
        else:
            self.returncode = returncode
            self.output = output

    def __str__(self):
        return "CommandResult({},{})".format(self.returncode, self.output)

    def __repr__(self):
        return "CommandResult({},{})".format(self.returncode, self.output)

    def success(self):
        return self.returncode == 0

    def failure(self):
        return not self.success()


def run_shell(args, shell=False):
    return CommandResult(subprocess.run(args, stdout=subprocess.PIPE, shell=shell))


def locate_command(command):
    """Return a tuple of (returncode, stdout)."""
    # Need to call this command in shell mode... not entirely sure why.
    return run_shell(['type -p {}'.format(command)], shell=True)


@contextmanager
def cd(newdir):
    """Safe cd -- return to original dir after execution, even if an exception is raised."""
    prevdir = os.getcwd()
    os.chdir(os.path.expanduser(newdir))
    try:
        yield
    finally:
        os.chdir(prevdir)


class AbstractDataMgmt(metaclass=abc.ABCMeta):
    """Abstract object that implements operations.

    All operations throw an exepction if they fail.
    """

    def __init__(self, echo_func, config_resolver, openbis, git_wrapper):
        self.echo_func = echo_func
        self.config_resolver = config_resolver
        self.openbis = openbis
        self.git_wrapper = git_wrapper

    def echo(self, level, message):
        details = {'level': level, 'message': message}
        self.echo_func(details)

    def info(self, message):
        """Print info to the user."""
        self.echo('info', message)

    def error(self, message):
        """Print an error to the user"""
        self.echo('error', message)

    def error_raise(self, command, reason):
        """Print an error to the user and raise an exception."""
        message = "'{}' failed. {}".format(command, reason)
        self.echo('error', message)
        raise ValueError(reason)

    def check_result_ok(self, result):
        if result.failure():
            self.error(result.output)
            return False
        return True

    @abc.abstractmethod
    def init_data(self, path, desc=None):
        """Initialize a data repository at the path with the description.
        :param path: Path for the repository.
        :param desc: An optional short description of the repository (used by git-annex)
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def init_analysis(self, path):
        """Initialize an analysis repository at the path.
        :param path: Path for the repository.
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


class NoGitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations when git is not available -- show error messages."""

    def init_data(self, path, desc=None):
        self.error_raise("init data", "No git command found.")

    def init_analysis(self, path):
        self.error_raise("init analysis", "No git command found.")

    def commit(self, msg, auto_add=True, sync=True):
        self.error_raise("commit", "No git command found.")

    def sync(self):
        self.error_raise("commit", "No git command found.")

    def status(self):
        self.error_raise("commit", "No git command found.")


class GitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations in normal state."""

    def init_data(self, path, desc=None):
        result = self.git_wrapper.git_init(path)
        if not self.check_result_ok(result):
            return result
        result = self.git_wrapper.git_annex_init(path, desc)
        if not self.check_result_ok(result):
            return result
        return result

    def init_analysis(self, path):
        result = self.git_wrapper.git_init(path)
        if not self.check_result_ok(result):
            return result
        return result

    def add_content(self, path):
        result = self.git_wrapper.git_add(path)
        if not self.check_result_ok(result):
            return result
        return result

    def sync(self):
        cmd = OpenbisSync(self)
        return cmd.run()

    def commit(self, msg, auto_add=True, sync=True):
        if auto_add:
            result = self.git_wrapper.git_top_level_path()
            if not self.check_result_ok(result):
                return result
            result = self.add_content(result.output)
            if not self.check_result_ok(result):
                return result
        result = self.git_wrapper.git_commit(msg)
        if not self.check_result_ok(result):
            # TODO If no changes were made check if the data set is in openbis. If not, just sync.
            return result
        if sync:
            result = self.sync()
            if not self.check_result_ok(result):
                return result
        return result

    def status(self):
        return self.git_wrapper.git_status()

    def commit_metadata_updates(self, msg_fragment=None):
        folder = self.config_resolver.local_public_config_folder_path()
        status = self.git_wrapper.git_status(folder)
        if len(status.output.strip()) < 1:
            # Nothing to commit
            return
        self.git_wrapper.git_add(folder)
        if msg_fragment is None:
            msg = "OBIS: Update openBIS metadata cache."
        else:
            msg = "OBIS: Update {}.".format(msg_fragment)
        self.git_wrapper.git_commit(msg)


class GitWrapper(object):
    """A wrapper on commands to git."""

    def __init__(self, git_path=None, git_annex_path=None, find_git=None):
        self.git_path = git_path
        self.git_annex_path = git_annex_path

    def can_run(self):
        """Return true if the perquisites are satisfied to run"""
        if self.git_path is None:
            return False
        if self.git_annex_path is None:
            return False
        if run_shell([self.git_path, 'help']).failure():
            # git help should have a returncode of 0
            return False
        if run_shell([self.git_annex_path, 'help']).failure():
            # git help should have a returncode of 0
            return False
        return True

    def git_init(self, path):
        return run_shell([self.git_path, "init", path])

    def git_status(self, path=None):
        if path is None:
            return run_shell([self.git_path, "status", "--porcelain"])
        else:
            return run_shell([self.git_path, "status", "--porcelain", path])

    def git_annex_init(self, path, desc):
        cmd = [self.git_path, "-C", path, "annex", "init", "--version=6"]
        if desc is not None:
            cmd.append(desc)
        result = run_shell(cmd)
        if result.failure():
            return result

        cmd = [self.git_path, "-C", path, "config", "annex.thin", "true"]
        result = run_shell(cmd)
        if result.failure():
            return result

        attributes_src = os.path.join(os.path.dirname(__file__), "git-annex-attributes")
        attributes_dst = os.path.join(path, ".gitattributes")
        shutil.copyfile(attributes_src, attributes_dst)
        cmd = [self.git_path, "-C", path, "add", ".gitattributes"]
        result = run_shell(cmd)
        if result.failure():
            return result

        cmd = [self.git_path, "-C", path, "commit", "-m", "Initial commit."]
        result = run_shell(cmd)
        return result

    def git_add(self, path):
        return run_shell([self.git_path, "add", path])

    def git_commit(self, msg):
        return run_shell([self.git_path, "commit", '-m', msg])

    def git_top_level_path(self):
        return run_shell([self.git_path, 'rev-parse', '--show-toplevel'])

    def git_commit_id(self):
        return run_shell([self.git_path, 'rev-parse', '--short', 'HEAD'])

    def git_ls_tree(self):
        return run_shell([self.git_path, 'ls-tree', '--full-tree', '-r', 'HEAD'])


class OpenbisSync(object):
    """A command object for synchronizing with openBIS."""

    def __init__(self, dm):
        self.data_mgmt = dm
        self.openbis = dm.openbis
        self.git_wrapper = dm.git_wrapper
        self.config_resolver = dm.config_resolver
        self.config_dict = dm.config_resolver.config_dict()

    def user(self):
        return self.config_dict.get('user')

    def external_dms_id(self):
        return self.config_dict.get('external_dms_id')

    def data_set_type(self):
        return self.config_dict.get('data_set_type')

    def data_set_id(self):
        return self.config_dict.get('data_set_id')

    def data_set_properties(self):
        return self.config_dict.get('data_set_properties')

    def object_id(self):
        return self.config_dict.get('object_id')

    def check_configuration(self):
        missing_config_settings = []
        if self.openbis is None:
            missing_config_settings.append('openbis_url')
        if self.user() is None:
            missing_config_settings.append('user')
        if self.data_set_type() is None:
            missing_config_settings.append('data_set_type')
        if self.data_set_type() is None:
            missing_config_settings.append('object_id')
        if len(missing_config_settings) > 0:
            return CommandResult(returncode=-1,
                                 output="Missing configuration settings for {}.".format(missing_config_settings))
        return CommandResult(returncode=0, output="")

    def login(self):
        if self.openbis.is_session_active():
            return CommandResult(returncode=0, output="")
        user = self.user()
        passwd = getpass.getpass("Password for {}:".format(user))
        try:
            self.openbis.login(user, passwd)
        except ValueError:
            msg = "Could not log into openbis {}".format(self.config_dict['openbis_url'])
            return CommandResult(returncode=-1, output=msg)
        return CommandResult(returncode=0, output='')

    def get_external_data_management_system(self):
        external_dms_id = self.external_dms_id()
        if external_dms_id is None:
            return None
        external_dms = self.openbis.get_external_data_management_system(external_dms_id.upper())
        return external_dms

    def create_external_data_management_system(self):
        external_dms_id = self.external_dms_id()
        user = self.user()
        result = self.git_wrapper.git_top_level_path()
        if result.failure():
            return result
        top_level_path = result.output
        path_name = os.path.basename(top_level_path)
        if external_dms_id is None:
            external_dms_id = "{}-{}".format(user, path_name).upper()
        try:
            hostname = socket.gethostname()
            edms = self.openbis.create_external_data_management_system(external_dms_id, external_dms_id,
                                                                       "{}:/{}".format(hostname, top_level_path))
            return CommandResult(returncode=0, output=""), edms
        except ValueError as e:
            # The EDMS might already be in the system. Try to get it.
            try:
                edms = self.openbis.get_external_data_management_system(external_dms_id)
                return CommandResult(returncode=0, output=""), edms
            except ValueError:
                return CommandResult(returncode=-1, output=str(e)), None

    def create_data_set_code(self):
        try:
            data_set_code = self.openbis.create_perm_id()
            return CommandResult(returncode=0, output=""), data_set_code
        except ValueError as e:
            return CommandResult(returncode=-1, output=str(e)), None

    def create_data_set(self, data_set_code, external_dms):
        data_set_type = self.data_set_type()
        parent_data_set_id = self.data_set_id()
        properties = self.data_set_properties()
        result = self.git_wrapper.git_top_level_path()
        if result.failure():
            return result
        top_level_path = result.output
        result = self.git_wrapper.git_commit_id()
        if result.failure():
            return result
        commit_id = result.output
        object_id = self.object_id()
        contents = GitRepoFileInfo(self.git_wrapper).contents()
        try:
            data_set = self.openbis.new_git_data_set(data_set_type, top_level_path, commit_id, external_dms.code,
                                                     object_id, data_set_code=data_set_code, parents=parent_data_set_id,
                                                     properties=properties, contents=contents)
            return CommandResult(returncode=0, output=""), data_set
        except ValueError as e:
            return CommandResult(returncode=-1, output=str(e)), None

    def commit_metadata_updates(self, msg_fragment=None):
        self.data_mgmt.commit_metadata_updates(msg_fragment)

    def prepare_run(self):
        result = self.check_configuration()
        if result.failure():
            return result
        result = self.login()
        if result.failure():
            return result
        return CommandResult(returncode=0, output="")

    def prepare_external_dms(self):
        # If there is no external data management system, create one.
        external_dms = self.get_external_data_management_system()
        if external_dms is None:
            result, external_dms = self.create_external_data_management_system()
            if result.failure():
                return result
            self.config_resolver.set_value_for_parameter('external_dms_id', external_dms.code, 'local')
        return CommandResult(returncode=0, output=external_dms)

    def run_workaround(self):
        # Do not create the data set code, since it is ignored due to a bug.

        result = self.prepare_run()
        if result.failure():
            return result

        result = self.prepare_external_dms()
        if result.failure():
            return result
        external_dms = result.output

        self.commit_metadata_updates()

        # create a data set, using the existing data set as a parent, if there is one
        result, data_set = self.create_data_set("DUMMY", external_dms)
        if result.failure():
            return result

        self.config_resolver.set_value_for_parameter('data_set_id', data_set.code, 'local')

        return CommandResult(returncode=0, output="")

    def run_correct(self):
        # These are the correct semantics -- get a data set code, register the data set, but
        #   a bug prevents this from working at the moment

        # TODO Write mementos in case openBIS is unreachable
        # - write a file to the .git/obis folder containing the commit id. Filename includes a timestamp so they can be sorted.

        result = self.prepare_run()
        if result.failure():
            return result

        result = self.prepare_external_dms()
        if result.failure():
            return result
        external_dms = result.output

        result, data_set_code = self.create_data_set_code()
        if result.failure():
            return result

        self.commit_metadata_updates()

        self.config_resolver.set_value_for_parameter('data_set_id', data_set_code, 'local')
        self.commit_metadata_updates("data set code")

        # create a data set, using the existing data set as a parent, if there is one
        result, data_set = self.create_data_set(data_set_code, external_dms)
        if result.failure():
            # TODO Revert the last commit to revert the data set id.
            return result

        return CommandResult(returncode=0, output="")

    def run(self):
        return self.run_workaround()


class GitRepoFileInfo(object):
    """Class that gathers checksums and file lengths for all files in the repo."""

    def __init__(self, git_wrapper):
        self.git_wrapper = git_wrapper

    def contents(self):
        """Return a list of dicts describing the contents of the repo.
        :return: A list of dictionaries
          {'crc32': checksum,
           'fileLength': size of the file,
           'path': path relative to repo root.
           'directory': False
          }"""
        files = self.file_list()
        cksum = self.cksum(files)
        return cksum

    def file_list(self):
        tree = self.git_wrapper.git_ls_tree()
        if tree.failure():
            return []
        lines = tree.output.split("\n")
        files = [line.split("\t")[-1].strip() for line in lines]
        return files

    def cksum(self, files):
        cmd = ['cksum']
        cmd.extend(files)
        result = run_shell(cmd)
        if result.failure():
            return []
        lines = result.output.split("\n")
        return [self.checksum_line_to_dict(line) for line in lines]

    @staticmethod
    def checksum_line_to_dict(line):
        fields = line.split(" ")
        return {
            'crc32': int(fields[0]),
            'fileLength': int(fields[1]),
            'path': fields[2]
        }
