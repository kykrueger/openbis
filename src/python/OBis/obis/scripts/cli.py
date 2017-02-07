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


@click.group()
@click.option('-q', '--quiet', default=False, is_flag=True, help='Suppress status reporting.')
@click.pass_context
def cli(ctx, quiet):
    ctx.obj['quiet'] = quiet


@cli.group()
@click.pass_context
def init(ctx, folder):
    """Group for the various init subcommands"""
    pass


@init.command()
@click.pass_context
@click.argument('folder', type=click.Path(exists=True))
def data(ctx, folder):
    """Initialize the folder as a data folder."""
    click_echo("init data {}".format(folder))


@init.command()
@click.pass_context
@click.argument('folder', type=click.Path(exists=True))
def analysis(ctx, folder):
    """Initialize the folder as an analysis folder."""
    click_echo("init analysis {}".format(folder))


@cli.command()
@click.pass_context
def commit(ctx):
    """Commit the repository to openBIS.
    """
    click_echo("commit")


@cli.command()
@click.pass_context
@click.argument('path')
def add(ctx, path):
    """Add add content to the repository.
    """
    click_echo("add {}".format(path))


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
