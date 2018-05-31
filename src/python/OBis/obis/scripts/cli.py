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


def add_params(params):
    def _add_params(func):
        for param in reversed(params):
            func = param(func)
        return func
    return _add_params


def shared_data_mgmt(context={}):
    git_config = {'find_git': True}
    openbis_config = {}
    if context.get('verify_certificates') is not None:
        openbis_config['verify_certificates'] = context['verify_certificates']
    return dm.DataMgmt(openbis_config=openbis_config, git_config=git_config, debug=context['debug'])


def check_result(command, result):
    if result.failure():
        click_echo("Could not {}:\n{}".format(command, result.output))
    elif len(result.output) > 0:
        click_echo(result.output)
    return result.returncode


def run(ctx, function):
    try:
        return function()
    except CommandException as e:
        return e.command_result
    except Exception as e:
        if ctx.obj['debug'] == True:
            raise e
        return CommandResult(returncode=-1, output="Error: " + str(e))


@click.group()
@click.option('-q', '--quiet', default=False, is_flag=True, help='Suppress status reporting.')
@click.option('-s', '--skip_verification', default=False, is_flag=True, help='Do not verify cerficiates')
@click.option('-d', '--debug', default=False, is_flag=True, help="Show stack trace on error.")
@click.pass_context
def cli(ctx, quiet, skip_verification, debug):
    ctx.obj['quiet'] = quiet
    if skip_verification:
        ctx.obj['verify_certificates'] = False
    ctx.obj['debug'] = debug


def set_property(data_mgmt, resolver, prop, value, is_global, is_data_set_property=False):
    """Helper function to implement the property setting semantics."""
    loc = 'global' if is_global else 'local'
    try:
        if is_data_set_property:
            resolver.set_value_for_json_parameter('properties', prop, value, loc)
        else:
            resolver.set_value_for_parameter(prop, value, loc)
    except ValueError as e:
        if data_mgmt.debug ==  True:
            raise e
        return CommandResult(returncode=-1, output="Error: " + str(e))
    if not is_global:
        return data_mgmt.commit_metadata_updates(prop)
    else:
        return CommandResult(returncode=0, output="")


def init_data_impl(ctx, object_id, collection_id, repository, desc):
    """Shared implementation for the init_data command."""
    if repository is None:
        repository = "."
    click_echo("init_data {}".format(repository))
    data_mgmt = shared_data_mgmt(ctx.obj)
    desc = desc if desc != "" else None
    result = run(ctx, lambda: data_mgmt.init_data(repository, desc, create=True))
    init_handle_cleanup(result, object_id, collection_id, repository, data_mgmt)


def init_analysis_impl(ctx, parent, object_id, collection_id, repository, description):
    click_echo("init_analysis {}".format(repository))
    data_mgmt = shared_data_mgmt(ctx.obj)
    description = description if description != "" else None
    result = run(ctx, lambda: data_mgmt.init_analysis(repository, parent, description, create=True))
    init_handle_cleanup(result, object_id, collection_id, repository, data_mgmt)


def init_handle_cleanup(result, object_id, collection_id, repository, data_mgmt):
    if (not object_id and not collection_id) or result.failure():
        return check_result("init_data", result)
    with dm.cd(repository):
        if object_id:
            resolver = data_mgmt.object
            return check_result("init_data", set_property(data_mgmt, resolver, 'id', object_id, False, False))
        if collection_id:
            resolver = data_mgmt.collection
            return check_result("init_data", set_property(data_mgmt, resolver, 'id', collection_id, False, False))


# settings commands


class SettingsGet(click.ParamType):
    name = 'settings_get'

    def convert(self, value, param, ctx):
        try:
            split = list(filter(lambda term: len(term) > 0, value.split(',')))
            return split
        except:
            self._fail(param)

    def _fail(self, param):
            self.fail(param=param, message='Settings must be in the format: key1, key2, ...')


class SettingsClear(SettingsGet):
    pass


class SettingsSet(click.ParamType):
    name = 'settings_set'

    def convert(self, value, param, ctx):
        try:
            value = self._encode_json(value)
            settings = {}
            split = list(filter(lambda term: len(term) > 0, value.split(',')))
            for setting in split:
                setting_split = setting.split('=')
                if len(setting_split) != 2:
                    self._fail(param)
                key = setting_split[0]
                value = setting_split[1]
                settings[key] = self._decode_json(value)
            return settings
        except:
            self._fail(param)

    def _encode_json(self, value):
        encoded = ''
        SEEK = 0
        ENCODE = 1
        mode = SEEK
        for char in value:
            if char == '{':
                mode = ENCODE
            elif char == '}':
                mode = SEEK
            if mode == SEEK:
                encoded += char
            elif mode == ENCODE:
                encoded += char.replace(',', '|')
        return encoded

    def _decode_json(self, value):
        return value.replace('|', ',')

    def _fail(self, param):
            self.fail(param=param, message='Settings must be in the format: key1=value1, key2=value2, ...')


def _join_settings_set(setting_dicts):
    joined = {}
    for setting_dict in setting_dicts:
        for key, value in setting_dict.items():
            joined[key] = value
    return joined


def _join_settings_get(setting_lists):
    joined = []
    for setting_list in setting_lists:
        joined += setting_list
    return joined


def config_internal(data_mgmt, resolver, is_global, is_data_set_property, prop=None, value=None, set=False, get=False, clear=False):
    if set == True:
        assert get == False
        assert clear == False
        assert prop is not None
        assert value is not None
    elif get == True:
        assert set == False
        assert clear == False
        assert value is None
    elif clear == True:
        assert get == False
        assert set == False
        assert value is None

    assert set == True or get == True or clear == True
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
    if is_data_set_property:
        config_dict = config_dict['properties']
    if get == True:
        if prop is None:
            config_str = json.dumps(config_dict, indent=4, sort_keys=True)
            click.echo("{}".format(config_str))
        else:
            if not prop in config_dict:
                raise ValueError("Unknown setting {} for {}.".format(prop, resolver.categoty))
            little_dict = {prop: config_dict[prop]}
            config_str = json.dumps(little_dict, indent=4, sort_keys=True)
            click.echo("{}".format(config_str))            
    elif set == True:
        return check_result("config", set_property(data_mgmt, resolver, prop, value, is_global, is_data_set_property))
    elif clear == True:
        if prop is None:
            returncode = 0
            for prop in config_dict.keys():
                returncode += check_result("config", set_property(data_mgmt, resolver, prop, None, is_global, is_data_set_property))
            return returncode
        else:
            return check_result("config", set_property(data_mgmt, resolver, prop, None, is_global, is_data_set_property))


def _access_settings(ctx, prop=None, value=None, set=False, get=False, clear=False):
    is_global = ctx.obj['is_global']
    data_mgmt = ctx.obj['data_mgmt']
    resolver = ctx.obj['resolver']
    is_data_set_property = False
    if 'is_data_set_property' in ctx.obj:
        is_data_set_property = ctx.obj['is_data_set_property']
    config_internal(data_mgmt, resolver, is_global, is_data_set_property, prop=prop, value=value, set=set, get=get, clear=clear)


def _set(ctx, settings):
    settings_dict = _join_settings_set(settings)
    for prop, value in settings_dict.items():
        _access_settings(ctx, prop=prop, value=value, set=True)
    return CommandResult(returncode=0, output='')


def _get(ctx, settings):
    settings_list = _join_settings_get(settings)
    if len(settings_list) == 0:
        settings_list = [None]
    for prop in settings_list:
        _access_settings(ctx, prop=prop, get=True)
    return CommandResult(returncode=0, output='')


def _clear(ctx, settings):
    settings_list = _join_settings_get(settings)
    if len(settings_list) == 0:
        settings_list = [None]
    for prop in settings_list:
        _access_settings(ctx, prop=prop, clear=True)
    return CommandResult(returncode=0, output='')


## get all settings

@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Get global or local.')
@click.pass_context
def settings(ctx, is_global):
    """ Get all settings.
    """
    ctx.obj['is_global'] = is_global


@settings.command('get')
@click.pass_context
def settings_get(ctx):
    data_mgmt = shared_data_mgmt(ctx.obj)
    settings = data_mgmt.settings_resolver.config_dict()
    settings_str = json.dumps(settings, indent=4, sort_keys=True)
    click.echo("{}".format(settings_str))


## repository: repository_id, external_dms_id, data_set_id

@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Set/get global or local.')
@click.pass_context
def repository(ctx, is_global):
    """ Get/set settings related to the repository.
    """
    ctx.obj['is_global'] = is_global
    ctx.obj['data_mgmt'] = shared_data_mgmt(ctx.obj)
    ctx.obj['resolver'] = ctx.obj['data_mgmt'].settings_resolver.repository


@repository.command('set')
@click.argument('settings', type=SettingsSet(), nargs=-1)
@click.pass_context
def repository_set(ctx, settings):
    return check_result("repository_set", run(ctx, lambda: _set(ctx, settings)))


@repository.command('get')
@click.argument('settings', type=SettingsGet(), nargs=-1)
@click.pass_context
def repository_get(ctx, settings):
    return check_result("repository_get", run(ctx, lambda: _get(ctx, settings)))


@repository.command('clear')
@click.argument('settings', type=SettingsClear(), nargs=-1)
@click.pass_context
def repository_clear(ctx, settings):
    return check_result("repository_clear", run(ctx, lambda: _clear(ctx, settings)))


## data_set: type, properties


@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Set/get global or local.')
@click.option('-p', '--is_data_set_property', default=False, is_flag=True, help='Configure data set property.')
@click.pass_context
def data_set(ctx, is_global, is_data_set_property):
    """ Get/set settings related to the data set.
    """
    ctx.obj['is_global'] = is_global
    ctx.obj['is_data_set_property'] = is_data_set_property
    ctx.obj['data_mgmt'] = shared_data_mgmt(ctx.obj)
    ctx.obj['resolver'] = ctx.obj['data_mgmt'].settings_resolver.data_set


@data_set.command('set')
@click.argument('settings', type=SettingsSet(), nargs=-1)
@click.pass_context
def data_set_set(ctx, settings):
    return check_result("data_set_set", run(ctx, lambda: _set(ctx, settings)))


@data_set.command('get')
@click.argument('settings', type=SettingsGet(), nargs=-1)
@click.pass_context
def data_set_get(ctx, settings):
    return check_result("data_set_get", run(ctx, lambda: _get(ctx, settings)))


@data_set.command('clear')
@click.argument('settings', type=SettingsClear(), nargs=-1)
@click.pass_context
def data_set_clear(ctx, settings):
    return check_result("data_set_clear", run(ctx, lambda: _clear(ctx, settings)))


## object: object_id


@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Set/get global or local.')
@click.pass_context
def object(ctx, is_global):
    """ Get/set settings related to the object.
    """
    ctx.obj['is_global'] = is_global
    ctx.obj['data_mgmt'] = shared_data_mgmt(ctx.obj)
    ctx.obj['resolver'] = ctx.obj['data_mgmt'].settings_resolver.object


@object.command('set')
@click.argument('settings', type=SettingsSet(), nargs=-1)
@click.pass_context
def object_set(ctx, settings):
    return check_result("object_set", run(ctx, lambda: _set(ctx, settings)))


@object.command('get')
@click.argument('settings', type=SettingsGet(), nargs=-1)
@click.pass_context
def object_get(ctx, settings):
    return check_result("object_get", run(ctx, lambda: _get(ctx, settings)))


@object.command('clear')
@click.argument('settings', type=SettingsClear(), nargs=-1)
@click.pass_context
def object_clear(ctx, settings):
    return check_result("object_clear", run(ctx, lambda: _clear(ctx, settings)))


## collection: collection_id


@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Set/get global or local.')
@click.pass_context
def collection(ctx, is_global):
    """ Get/set settings related to the collection.
    """
    ctx.obj['is_global'] = is_global
    ctx.obj['data_mgmt'] = shared_data_mgmt(ctx.obj)
    ctx.obj['resolver'] = ctx.obj['data_mgmt'].settings_resolver.collection


@collection.command('set')
@click.argument('settings', type=SettingsSet(), nargs=-1)
@click.pass_context
def collection_set(ctx, settings):
    return check_result("collection_set", run(ctx, lambda: _set(ctx, settings)))


@collection.command('get')
@click.argument('settings', type=SettingsGet(), nargs=-1)
@click.pass_context
def collection_get(ctx, settings):
    return check_result("collection_get", run(ctx, lambda: _get(ctx, settings)))


@collection.command('clear')
@click.argument('settings', type=SettingsClear(), nargs=-1)
@click.pass_context
def collection_clear(ctx, settings):
    return check_result("collection_clear", run(ctx, lambda: _clear(ctx, settings)))


## config: fileservice_url, git_annex_hash_as_checksum, hostname, openbis_url, user, verify_certificates


@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Set/get global or local.')
@click.pass_context
def config(ctx, is_global):
    """ Get/set configurations.
    """
    ctx.obj['is_global'] = is_global
    ctx.obj['data_mgmt'] = shared_data_mgmt(ctx.obj)
    ctx.obj['resolver'] = ctx.obj['data_mgmt'].settings_resolver.config


@config.command('set')
@click.argument('settings', type=SettingsSet(), nargs=-1)
@click.pass_context
def config_set(ctx, settings):
    return check_result("config_set", run(ctx, lambda: _set(ctx, settings)))


@config.command('get')
@click.argument('settings', type=SettingsGet(), nargs=-1)
@click.pass_context
def config_get(ctx, settings):
    return check_result("config_get", run(ctx, lambda: _get(ctx, settings)))


@config.command('clear')
@click.argument('settings', type=SettingsClear(), nargs=-1)
@click.pass_context
def config_clear(ctx, settings):
    return check_result("config_clear", run(ctx, lambda: _clear(ctx, settings)))


# repository commands: status, sync, commit, init, addref, removeref, init_analysis

## commit

_commit_params = [
    click.option('-m', '--msg', prompt=True, help='A message explaining what was done.'),
    click.option('-a', '--auto_add', default=True, is_flag=True, help='Automatically add all untracked files.'),
    click.option('-i', '--ignore_missing_parent', default=True, is_flag=True, help='If parent data set is missing, ignore it.'),
    click.argument('repository', type=click.Path(exists=True, file_okay=False), required=False),
]

def _repository_commit(ctx, msg, auto_add, ignore_missing_parent):
    data_mgmt = shared_data_mgmt(ctx.obj)
    return check_result("commit", run(ctx, lambda: data_mgmt.commit(msg, auto_add, ignore_missing_parent)))

@repository.command("commit")
@click.pass_context
@add_params(_commit_params)
def repository_commit(ctx, msg, auto_add, ignore_missing_parent, repository):
    """Commit the repository to git and inform openBIS.
    """
    if repository is None:
        return _repository_commit(ctx, msg, auto_add, ignore_missing_parent)
    with cd(repository):
        return _repository_commit(ctx, msg, auto_add, ignore_missing_parent)

@cli.command()
@click.pass_context
@add_params(_commit_params)
def commit(ctx, msg, auto_add, ignore_missing_parent, repository):
    """Commit the repository to git and inform openBIS.
    """
    ctx.invoke(repository_commit, msg=msg, auto_add=auto_add, ignore_missing_parent=ignore_missing_parent, repository=repository)

## init

_init_params = [
    click.option('-oi', '--object_id', help='Set the id of the owning sample.'),
    click.option('-ci', '--collection_id', help='Set the id of the owning experiment.'),
    click.argument('repository', type=click.Path(exists=False, file_okay=False), required=False),
    click.argument('description', default=""),
]

@repository.command("init")
@click.pass_context
@add_params(_init_params)
def repository_init(ctx, object_id, collection_id, repository, description):
    """Initialize the folder as a data repository."""
    return init_data_impl(ctx, object_id, collection_id, repository, description)

@cli.command()
@click.pass_context
@add_params(_init_params)
def init(ctx, object_id, collection_id, repository, description):
    """Initialize the folder as a data repository."""
    ctx.invoke(repository_init, object_id=object_id, collection_id=collection_id, repository=repository, description=description)

## init analysis

_init_analysis_params = [
    click.option('-p', '--parent', type=click.Path(exists=False, file_okay=False)),
]
_init_analysis_params += _init_params

@repository.command("init_analysis")
@click.pass_context
@add_params(_init_analysis_params)
def repository_init_analysis(ctx, parent, object_id, collection_id, repository, description):
    """Initialize the folder as an analysis folder."""
    return init_analysis_impl(ctx, parent, object_id, collection_id, repository, description)

@cli.command()
@click.pass_context
@add_params(_init_analysis_params)
def init_analysis(ctx, parent, object_id, collection_id, repository, description):
    """Initialize the folder as an analysis folder."""
    ctx.invoke(repository_init_analysis, parent=parent, object_id=object_id, collection_id=collection_id, repository=repository, description=description)

## status

_status_params = [
    click.argument('repository', type=click.Path(exists=True, file_okay=False), required=False),
]

def _repository_status(ctx):
    data_mgmt = shared_data_mgmt(ctx.obj)
    result = run(ctx, data_mgmt.status)
    click.echo(result.output)    

@repository.command("status")
@click.pass_context
@add_params(_status_params)
def repository_status(ctx, repository):
    """Show the state of the obis repository.
    """
    if repository is None:
        return _repository_status(ctx)
    with cd(repository):
        return _repository_status(ctx)        

@cli.command()
@click.pass_context
@add_params(_status_params)
def status(ctx, repository):
    """Show the state of the obis repository.
    """
    ctx.invoke(repository_status, repository=repository)

## sync

_sync_params = [
    click.option('-i', '--ignore_missing_parent', default=True, is_flag=True, help='If parent data set is missing, ignore it.'),
    click.argument('repository', type=click.Path(exists=True, file_okay=False), required=False),
]

def _repository_sync(ctx, ignore_missing_parent):
    data_mgmt = shared_data_mgmt(ctx.obj)
    return check_result("sync", run(ctx, lambda: data_mgmt.sync(ignore_missing_parent)))

@repository.command("sync")
@click.pass_context
@add_params(_sync_params)
def repository_sync(ctx, ignore_missing_parent, repository):
    """Sync the repository with openBIS.
    """
    if repository is None:
        return _repository_sync(ctx, ignore_missing_parent)
    with cd(repository):
        return _repository_sync(ctx, ignore_missing_parent)

@cli.command()
@click.pass_context
@add_params(_sync_params)
def sync(ctx, ignore_missing_parent, repository):
    """Sync the repository with openBIS.
    """
    ctx.invoke(repository_sync, ignore_missing_parent=ignore_missing_parent, repository=repository)

## addref

_addref_params = [
    click.argument('repository', type=click.Path(exists=True, file_okay=False), required=False),
]

def _repository_addref(ctx):
    data_mgmt = shared_data_mgmt(ctx.obj)
    return check_result("addref", run(ctx, data_mgmt.addref))

@repository.command("addref")
@click.pass_context
@add_params(_addref_params)
def repository_addref(ctx, repository):
    """Add the given repository as a reference to openBIS.
    """
    if repository is None:
        return _repository_addref(ctx)
    with cd(repository):
        return _repository_addref(ctx)

@cli.command()
@click.pass_context
@add_params(_addref_params)
def addref(ctx, repository):
    """Add the given repository as a reference to openBIS.
    """
    ctx.invoke(repository_addref, repository=repository)

# removeref

_removeref_params = [
    click.argument('repository', type=click.Path(exists=True, file_okay=False), required=False),
]

def _repository_removeref(ctx):
    data_mgmt = shared_data_mgmt(ctx.obj)
    return check_result("addref", run(ctx, data_mgmt.removeref))

@repository.command("removeref")
@click.pass_context
@add_params(_removeref_params)
def repository_removeref(ctx, repository):
    """Remove the reference to the given repository from openBIS.
    """
    if repository is None:
        return _repository_removeref(ctx)
    with cd(repository):
        return _repository_removeref(ctx)

@cli.command()
@click.pass_context
@add_params(_removeref_params)
def removeref(ctx, repository):
    """Remove the reference to the given repository from openBIS.
    """
    ctx.invoke(repository_removeref, repository=repository)


# data set commands: download / clone

## download

_download_params = [
    click.option('-c', '--content_copy_index', type=int, default=None, help='Index of the content copy to download from.'),
    click.option('-f', '--file', help='File in the data set to download - downloading all if not given.'),
    click.option('-s', '--skip_integrity_check', default=False, is_flag=True, help='Skip file integrity check with checksums.'),
    click.argument('data_set_id'),
]

@data_set.command("download")
@add_params(_download_params)
@click.pass_context 
def data_set_download(ctx, content_copy_index, file, data_set_id, skip_integrity_check):
    """ Download files of a linked data set.
    """
    data_mgmt = shared_data_mgmt(ctx.obj)
    return check_result("download", run(ctx, lambda: data_mgmt.download(data_set_id, content_copy_index, file, skip_integrity_check)))

@cli.command()
@add_params(_download_params)
@click.pass_context
def download(ctx, content_copy_index, file, data_set_id, skip_integrity_check):
    """ Download files of a linked data set.
    """
    ctx.invoke(data_set_download, content_copy_index=content_copy_index, file=file, data_set_id=data_set_id, skip_integrity_check=skip_integrity_check)

## clone

_clone_params = [
    click.option('-u', '--ssh_user', default=None, help='User to connect to remote systems via ssh'),
    click.option('-c', '--content_copy_index', type=int, default=None, help='Index of the content copy to clone from in case there are multiple copies'),
    click.option('-s', '--skip_integrity_check', default=False, is_flag=True, help='Skip file integrity check with checksums.'),
    click.argument('data_set_id'),
]

@data_set.command("clone")
@click.pass_context
@add_params(_clone_params)
def data_set_clone(ctx, ssh_user, content_copy_index, data_set_id, skip_integrity_check):
    """Clone the repository found in the given data set id.
    """
    data_mgmt = shared_data_mgmt(ctx.obj)
    return check_result("clone", run(ctx, lambda: data_mgmt.clone(data_set_id, ssh_user, content_copy_index, skip_integrity_check)))

@cli.command()
@click.pass_context
@add_params(_clone_params)
def clone(ctx, ssh_user, content_copy_index, data_set_id, skip_integrity_check):
    """Clone the repository found in the given data set id.
    """
    ctx.invoke(data_set_clone, ssh_user=ssh_user, content_copy_index=content_copy_index, data_set_id=data_set_id, skip_integrity_check=skip_integrity_check)


def main():
    cli(obj={})


if __name__ == '__main__':
    main()
