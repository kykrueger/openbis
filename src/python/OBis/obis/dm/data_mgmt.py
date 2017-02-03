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

import pybis


# noinspection PyPep8Naming
def DataMgmt(echo_func=None, openbis_config={}, git_config={}):
    """Factory method for DataMgmt instances"""

    echo_func = echo_func if echo_func is not None else default_echo

    complete_openbis_config(openbis_config)
    complete_git_config(git_config)

    openbis = pybis.Openbis(**openbis_config)
    git_wrapper = GitWrapper(**git_config)
    if git_wrapper.can_run():
        return GitDataMgmt(echo_func, openbis, git_wrapper)
    return NoGitDataMgmt(echo_func, openbis, git_wrapper)


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
        git_cmd = locate_command('git')
        if git_cmd.returncode == 0:
            config['git_path'] = git_cmd.output

        git_annex_cmd = locate_command('git-annex')
        if git_annex_cmd.returncode == 0:
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


def run_shell(args):
    return CommandResult(subprocess.run(args, stdout=subprocess.PIPE))


def locate_command(command):
    """Return a tuple of (returncode, stdout)."""
    return run_shell(['type', '-p', command])


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

    def __init__(self, echo_func, openbis, git_wrapper):
        self.echo_func = echo_func
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
        if result.returncode != 0:
            self.error(result.output)
            return False
        return True

    @abc.abstractmethod
    def init_data(self, path, desc=None):
        """Initialize a data repository at the path with the description."""
        return

    @abc.abstractmethod
    def init_analysis(self, path):
        """Initialize an analysis repository at the path."""
        return

    @abc.abstractmethod
    def commit(self, msg, auto_add=True):
        """
        Commit the current repo.

        This issues a git commit and connects to openBIS and creates a data set in openBIS.
        :param msg: Commit message.
        :param auto_add: Automatically add all files in the folder to the repo. Defaults to True.
        :return:
        """
        return


class NoGitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations when git is not available -- show error messages."""

    def init_data(self, path, desc=None):
        self.error_raise("init data", "No git command found.")

    def init_analysis(self, path):
        self.error_raise("init analysis", "No git command found.")

    def commit(self, msg, auto_add=True):
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
        # TODO create a data set in openBIS
        # - write a file to the .git/obis folder containing the commit id. Filename includes a timestamp so they can be sorted.
        # - call openbis to create a data set, using the existing data set as a parent, if there is one
        # - save the data set id to .git/obis/datasetid.
        return CommandResult(returncode=0, output="")

    def commit(self, msg, auto_add=True):
        if auto_add:
            result = self.git_wrapper.get_top_level_path()
            if not self.check_result_ok(result):
                return result
            result = self.add_content(result.output)
            if not self.check_result_ok(result):
                return result
        result = self.git_wrapper.git_commit(msg)
        if not self.check_result_ok(result):
            return result
        result = self.sync()
        if not self.check_result_ok(result):
            return result
        return result


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
        if run_shell([self.git_path, 'help']).returncode != 0:
            # git help should have a returncode of 0
            return False
        if run_shell([self.git_annex_path, 'help']).returncode != 0:
            # git help should have a returncode of 0
            return False
        return True

    def git_init(self, path):
        return run_shell([self.git_path, "init", path])

    def git_annex_init(self, path, desc):
        cmd = [self.git_path, "-C", path, "annex", "init", "--version=6"]
        if desc is not None:
            cmd.append(desc)
        result = run_shell(cmd)
        if result.returncode != 0:
            return result

        attributes_src = os.path.join(os.path.dirname(__file__), "git-annex-attributes")
        attributes_dst = os.path.join(path, ".gitattributes")
        shutil.copyfile(attributes_src, attributes_dst)
        cmd = [self.git_path, "-C", path, "add", ".gitattributes"]
        result = run_shell(cmd)
        if result.returncode != 0:
            return result

        # TODO Create a .obis directory and add it to gitignore

        cmd = [self.git_path, "-C", path, "commit", "-m", "Initial commit."]
        result = run_shell(cmd)
        return result

    def git_add(self, path):
        return run_shell([self.git_path, "add", path])

    def git_commit(self, msg):
        return run_shell([self.git_path, "commit", '-m', msg])

    def get_top_level_path(self):
        return run_shell([self.git_path, 'rev-parse', '--show-toplevel'])
