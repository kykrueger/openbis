import subprocess
import os
from contextlib import contextmanager
from .command_result import CommandResult


def complete_openbis_config(config, resolver, local_only=True):
    """Add default values for empty entries in the config."""
    config_dict = resolver.config_dict(local_only)
    if config.get('url') is None:
        config['url'] = config_dict['openbis_url']
    if config.get('verify_certificates') is None:
        if config_dict.get('verify_certificates') is not None:
            config['verify_certificates'] = config_dict['verify_certificates']
        else:
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


def run_shell(args, shell=False):
    return CommandResult(subprocess.run(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=shell))


def locate_command(command):
    """Return a tuple of (returncode, stdout)."""
    # Need to call this command in shell mode so we have the system PATH
    result = run_shell(['type {}'.format(command)], shell=True)
    # 'type -p' not supported by all shells, so we do it manually
    if result.success():
        result.output = result.output.split(" ")[-1]
    return result


@contextmanager
def cd(newdir):
    """Safe cd -- return to original dir after execution, even if an exception is raised."""
    prevdir = os.getcwd()
    os.chdir(os.path.expanduser(newdir))
    try:
        yield
    finally:
        os.chdir(prevdir)
