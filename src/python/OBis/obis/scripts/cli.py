#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
cli.py

The module that implements the CLI for obis.


Created by Chandrasekhar Ramakrishnan on 2017-01-27.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""

import os
from datetime import datetime
from .. import dm
import click


def click_echo(message):
    timestamp = datetime.now().strftime("%H:%M:%S")
    click.echo("{} {}".format(timestamp, message))


def click_progress(progress_data):
    if progress_data['type'] == 'progress':
        click_echo(progress_data['message'])


def click_progress_no_ts(progress_data):
    if progress_data['type'] == 'progress':
        click.echo("{}".format(progress_data['message']))


def shared_data_mgmt(verify_certificates=True):
    git_config = {'find_git': True}
    openbis_config = {'verify_certificates': verify_certificates}
    return dm.DataMgmt(openbis_config=openbis_config, git_config=git_config)


@click.group()
@click.option('-q', '--quiet', default=False, is_flag=True, help='Suppress status reporting.')
@click.pass_context
def cli(ctx, quiet):
    ctx.obj['quiet'] = quiet


@cli.command()
@click.pass_context
@click.argument('other', type=click.Path(exists=True))
def addref(ctx, other):
    """Add a reference to the other repository in this repository.
    """
    click_echo("addref {}".format(other))


@cli.command()
@click.pass_context
@click.argument('url')
def clone(ctx, url):
    """Clone the repository found at url.
    """
    click_echo("clone {}".format(url))


@cli.command()
@click.pass_context
@click.option('-m', '--msg', prompt=True, help='A message explaining what was done.')
@click.option('-a', '--auto_add', default=True, is_flag=True, help='Automatically add all untracked files.')
@click.option('-s', '--skip_verification', default=False, is_flag=True, help='Do not verify cerficiates')
def commit(ctx, msg, auto_add, skip_verification):
    """Commit the repository to git and inform openBIS.
    """
    verify_certificates = not skip_verification
    data_mgmt = shared_data_mgmt(verify_certificates=verify_certificates)
    return data_mgmt.commit(msg, auto_add)


@cli.command()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Configure global or local.')
@click.argument('property', default="")
@click.argument('value', default="")
@click.pass_context
def config(ctx, is_global, property, value):
    """Configure the openBIS setup.

    Configure the openBIS server url, the data set type, and the data set properties.
    """
    ctx.obj['global'] = is_global
    dm = shared_data_mgmt()
    resolver = dm.config_resolver
    top_level_path = dm.git_wrapper.git_top_level_path()
    if top_level_path.success():
        resolver.location_resolver.location_roots['data_set'] = top_level_path.output
    is_global = ctx.obj['global']
    if is_global:
        resolver.location_search_order = ['global']
    else:
        resolver.location_search_order = ['local']

    config_dict = resolver.config_dict()
    if not property:
        click.echo("{}".format(config_dict))
    elif not value:
        click.echo("{}".format(config_dict[property]))
    else:
        loc = 'global' if is_global else 'local'
        resolver.set_value_for_parameter(property, value, loc)


@cli.group()
@click.pass_context
def init(ctx):
    """Group for the various init subcommands"""
    pass


@init.command()
@click.pass_context
@click.argument('folder', type=click.Path(exists=True))
@click.argument('name', default="")
def data(ctx, folder, name):
    """Initialize the folder as a data folder."""
    click_echo("init data {}".format(folder))
    data_mgmt = shared_data_mgmt()
    name = name if name != "" else None
    return data_mgmt.init_data(folder, name)


@init.command()
@click.pass_context
@click.argument('folder', type=click.Path(exists=True))
def analysis(ctx, folder):
    """Initialize the folder as an analysis folder."""
    click_echo("init analysis {}".format(folder))


@cli.command()
@click.pass_context
@click.argument('file')
def get(ctx, f):
    """Get one or more files from a clone of this repository.
    """
    click_echo("get {}".format(f))


@cli.command()
@click.pass_context
def status(ctx):
    """Sync the repository with openBIS.
    """
    data_mgmt = shared_data_mgmt()
    result = data_mgmt.status()
    click.echo(result.output)


@cli.command()
@click.pass_context
@click.option('-s', '--skip_verification', default=False, is_flag=True, help='Do not verify cerficiates')
def sync(ctx, skip_verification):
    """Sync the repository with openBIS.
    """
    verify_certificates = not skip_verification
    data_mgmt = shared_data_mgmt(verify_certificates=verify_certificates)
    return data_mgmt.sync()


def main():
    cli(obj={})


if __name__ == '__main__':
    main()
