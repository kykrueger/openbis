#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
cli.py

The module that implements the CLI for obis.


Created by Chandrasekhar Ramakrishnan on 2017-01-27.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""
import json
from datetime import datetime

import click

from .. import dm
from ..dm.command_result import CommandResult
from ..dm.command_result import CommandException
from ..dm.utils import cd


def click_echo(message):
    timestamp = datetime.now().strftime("%H:%M:%S")
    click.echo("{} {}".format(timestamp, message))


def click_progress(progress_data):
    if progress_data['type'] == 'progress':
        click_echo(progress_data['message'])


def click_progress_no_ts(progress_data):
    if progress_data['type'] == 'progress':
        click.echo("{}".format(progress_data['message']))


def shared_data_mgmt(context={}):
    git_config = {'find_git': True}
    openbis_config = {}
    if context.get('verify_certificates') is not None:
        openbis_config['verify_certificates'] = context['verify_certificates']
    return dm.DataMgmt(openbis_config=openbis_config, git_config=git_config)


def check_result(command, result):
    if result.failure():
        click_echo("Could not {}:\n{}".format(command, result.output))
    elif len(result.output) > 0:
        click_echo(result.output)
    return result.returncode


def run(function):
    try:
        return function()
    except CommandException as e:
        return e.command_result
    except Exception as e:
        return CommandResult(returncode=-1, output="Error: " + str(e))


@click.group()
@click.option('-q', '--quiet', default=False, is_flag=True, help='Suppress status reporting.')
@click.option('-s', '--skip_verification', default=False, is_flag=True, help='Do not verify cerficiates')
@click.pass_context
def cli(ctx, quiet, skip_verification):
    ctx.obj['quiet'] = quiet
    if skip_verification:
        ctx.obj['verify_certificates'] = False


@cli.command()
@click.pass_context
@click.argument('repository', type=click.Path(exists=True))
def addref(ctx, repository):
    """Add the given repository as a reference to openBIS.
    """
    with cd(repository):
        data_mgmt = shared_data_mgmt(ctx.obj)
        return check_result("addref", run(data_mgmt.addref))


@cli.command()
@click.pass_context
@click.argument('repository', type=click.Path(exists=True))
def removeref(ctx, repository):
    """Remove the reference to the given repository from openBIS.
    """
    with cd(repository):
        data_mgmt = shared_data_mgmt(ctx.obj)
        return check_result("addref", run(data_mgmt.removeref))


@cli.command()
@click.pass_context
@click.option('-u', '--ssh_user', default=None, help='User to connect to remote systems via ssh')
@click.option('-c', '--content_copy_index', type=int, default=None, help='Index of the content copy to clone from in case there are multiple copies')
@click.argument('data_set_id')
def clone(ctx, ssh_user, content_copy_index, data_set_id):
    """Clone the repository found in the given data set id.
    """
    data_mgmt = shared_data_mgmt(ctx.obj)
    return check_result("clone", run(lambda: data_mgmt.clone(data_set_id, ssh_user, content_copy_index)))


@cli.command()
@click.pass_context
@click.option('-m', '--msg', prompt=True, help='A message explaining what was done.')
@click.option('-a', '--auto_add', default=True, is_flag=True, help='Automatically add all untracked files.')
def commit(ctx, msg, auto_add):
    """Commit the repository to git and inform openBIS.
    """
    data_mgmt = shared_data_mgmt(ctx.obj)
    return check_result("commit", run(lambda: data_mgmt.commit(msg, auto_add)))


@cli.command()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Configure global or local.')
@click.option('-p', '--is_data_set_property', default=False, is_flag=True, help='Configure data set property.')
@click.argument('prop', default="")
@click.argument('value', default="")
@click.pass_context
def config(ctx, is_global, is_data_set_property, prop, value):
    """Configure the openBIS setup.

    Configure the openBIS server url, the data set type, and the data set properties.
    """
    data_mgmt = shared_data_mgmt(ctx.obj)
    config_internal(data_mgmt, is_global, is_data_set_property, prop, value)


@cli.command()
@click.option('-c', '--content_copy_index', type=int, default=None, help='Index of the content copy to download from.')
@click.option('-f', '--file', help='File in the data set to download - downloading all if not given.')
@click.argument('data_set_id')
@click.pass_context
def download(ctx, content_copy_index, file, data_set_id):
    """ Download files of a linked data set.
    """

    data_mgmt = shared_data_mgmt(ctx.obj)
    return check_result("download", run(lambda: data_mgmt.download(data_set_id, content_copy_index, file)))


def config_internal(data_mgmt, is_global, is_data_set_property, prop, value):
    resolver = data_mgmt.config_resolver
    if is_global:
        resolver.set_location_search_order(['global'])
    else:
        top_level_path = data_mgmt.git_wrapper.git_top_level_path()
        if top_level_path.success():
            resolver.set_resolver_location_roots('data_set', top_level_path.output)
            resolver.set_location_search_order(['local'])
        else:
            resolver.set_location_search_order(['global'])

    config_dict = resolver.config_dict()
    if not prop:
        config_str = json.dumps(config_dict, indent=4, sort_keys=True)
        click.echo("{}".format(config_str))
    elif not value:
        little_dict = {prop: config_dict[prop]}
        config_str = json.dumps(little_dict, indent=4, sort_keys=True)
        click.echo("{}".format(config_str))
    else:
        return check_result("config", set_property(data_mgmt, prop, value, is_global, is_data_set_property))


def set_property(data_mgmt, prop, value, is_global, is_data_set_property):
    """Helper function to implement the property setting semantics."""
    loc = 'global' if is_global else 'local'
    resolver = data_mgmt.config_resolver
    try:
        if is_data_set_property:
            resolver.set_value_for_json_parameter('data_set_properties', prop, value, loc)
        else:
            resolver.set_value_for_parameter(prop, value, loc)
    except ValueError as e:
        return CommandResult(returncode=-1, output="Error: " + str(e))
    if not is_global:
        return data_mgmt.commit_metadata_updates(prop)
    else:
        return CommandResult(returncode=0, output="")


def init_data_impl(ctx, object_id, collection_id, folder, desc):
    """Shared implementation for the init_data command."""
    click_echo("init_data {}".format(folder))
    data_mgmt = shared_data_mgmt(ctx.obj)
    desc = desc if desc != "" else None
    result = run(lambda: data_mgmt.init_data(folder, desc, create=True))
    init_handle_cleanup(result, object_id, collection_id, folder, data_mgmt)


def init_analysis_impl(ctx, parent, object_id, collection_id, folder, description):
    click_echo("init_analysis {}".format(folder))
    data_mgmt = shared_data_mgmt(ctx.obj)
    description = description if description != "" else None
    result = run(lambda: data_mgmt.init_analysis(folder, parent, description, create=True))
    init_handle_cleanup(result, object_id, collection_id, folder, data_mgmt)


def init_handle_cleanup(result, object_id, collection_id, folder, data_mgmt):
    if (not object_id and not collection_id) or result.failure():
        return check_result("init_data", result)
    with dm.cd(folder):
        if object_id:
            return check_result("init_data", set_property(data_mgmt, 'object_id', object_id, False, False))
        if collection_id:
            return check_result("init_data", set_property(data_mgmt, 'collection_id', collection_id, False, False))


@cli.command()
@click.pass_context
@click.option('-oi', '--object_id', help='Set the id of the owning sample.')
@click.option('-ci', '--collection_id', help='Set the id of the owning experiment.')
@click.argument('folder', type=click.Path(exists=False, file_okay=False))
@click.argument('description', default="")
def init(ctx, object_id, collection_id, folder, description):
    """Initialize the folder as a data folder (alias for init_data)."""
    return init_data_impl(ctx, object_id, collection_id, folder, description)


@cli.command()
@click.pass_context
@click.option('-oi', '--object_id', help='Set the id of the owning sample.')
@click.option('-ci', '--collection_id', help='Set the id of the owning experiment.')
@click.argument('folder', type=click.Path(exists=False, file_okay=False))
@click.argument('description', default="")
def init_data(ctx, object_id, collection_id, folder, description):
    """Initialize the folder as a data folder."""
    return init_data_impl(ctx, object_id, collection_id, folder, description)


@cli.command()
@click.pass_context
@click.option('-p', '--parent', type=click.Path(exists=False, file_okay=False))
@click.option('-oi', '--object_id', help='Set the id of the owning sample.')
@click.option('-ci', '--collection_id', help='Set the id of the owning experiment.')
@click.argument('folder', type=click.Path(exists=False, file_okay=False))
@click.argument('description', default="")
def init_analysis(ctx, parent, object_id, collection_id, folder, description):
    """Initialize the folder as an analysis folder."""
    return init_analysis_impl(ctx, parent, object_id, collection_id, folder, description)


@cli.command()
@click.pass_context
def status(ctx):
    """Show the state of the obis repository.
    """
    data_mgmt = shared_data_mgmt(ctx.obj)
    result = run(data_mgmt.status)
    click.echo(result.output)


@cli.command()
@click.pass_context
def sync(ctx):
    """Sync the repository with openBIS.
    """
    data_mgmt = shared_data_mgmt(ctx.obj)
    return check_result("sync", run(data_mgmt.sync))


def main():
    cli(obj={})


if __name__ == '__main__':
    main()
