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


def shared_data_mgmt():
    git_config = {'find_git': True}
    return dm.DataMgmt(git_config=git_config)


@click.group()
@click.option('-q', '--quiet', default=False, is_flag=True, help='Suppress status reporting.')
@click.pass_context
def cli(ctx, quiet):
    ctx.obj['quiet'] = quiet


@cli.command()
@click.pass_context
@click.option('-g', '--global', default=False, is_flag=True, help='Configure global or local.')
def config(ctx):
    """Configure the openBIS setup.

    Configure the openBIS server url, the data set type, and the data set properties.
    """
    click_echo("config")


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
@click.option('-m', '--msg', prompt=True, help='A message explaining what was done.')
@click.option('-a', '--auto_add', default=True, is_flag=True, help='Automatically add all untracked files.')
def commit(ctx, msg, auto_add):
    """Commit the repository to git and inform openBIS.
    """
    data_mgmt = shared_data_mgmt()
    return data_mgmt.commit(msg, auto_add)


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
@click.argument('file')
def get(ctx, f):
    """Get one or more files from a clone of this repository.
    """
    click_echo("get {}".format(f))


def main():
    cli(obj={})


if __name__ == '__main__':
    main()
