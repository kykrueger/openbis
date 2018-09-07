import click
import json
import sys

from datetime import datetime

from .. import dm
from ..dm.utils import cd
from ..dm.command_result import CommandResult, CommandException
from ..dm.command_log import CommandLog
from .click_util import click_echo, check_result


class DataMgmtRunner(object):


    def __init__(self, ctx, halt_on_error_log=True):
        self.ctx = ctx
        self.halt_on_error_log = halt_on_error_log
        self._dm = None


    def run(self, command, function, repository=None):
        if repository is not None:
            with cd(repository):
                result = self._run(function)
        else:
            result = self._run(function)
        return check_result(command, result)


    def _run(self, function):
        try:
            return function(self._get_dm())
        except CommandException as e:
            return e.command_result
        except Exception as e:
            if self.ctx.obj['debug'] == True:
                raise e
            return CommandResult(returncode=-1, output="Error: " + str(e))


    def get_settings(self):
        return self._get_dm().settings_resolver.config_dict()


    def get_settings_resolver(self):
        return self._get_dm().settings_resolver


    def config(self, resolver, is_global, is_data_set_property, prop, value, set, get, clear):
        self._get_dm().config(resolver, is_global, is_data_set_property, prop, value, set, get, clear)


    def _get_dm(self):
        if self._dm is None:
            self._dm = self._create_dm(self.ctx.obj)
        return self._dm


    def _create_dm(self, context={}):
        git_config = {'find_git': True}
        openbis_config = {}
        if context.get('verify_certificates') is not None:
            openbis_config['verify_certificates'] = context['verify_certificates']
        log = CommandLog()
        if self.halt_on_error_log and log.any_log_exists():
            click_echo("Error: A previous command did not finish. Please check the log ({}) and remove it when you want to continue using obis".format(log.folder_path))
            sys.exit(-1)
        return dm.DataMgmt(openbis_config=openbis_config, git_config=git_config, log=log, debug=context['debug'])
