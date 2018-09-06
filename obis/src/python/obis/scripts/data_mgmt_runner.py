import click
import json
import sys

from datetime import datetime

from .. import dm
from ..dm.utils import cd
from ..dm.command_result import CommandResult
from ..dm.command_result import CommandException
from ..dm.command_log import CommandLog
from .click_util import click_echo, check_result
from .config_util import set_property


class DataMgmtRunner(object):


    def __init__(self, ctx, halt_on_error_log=True):
        self.ctx = ctx
        self.halt_on_error_log = halt_on_error_log
        self.dm = self._shared_data_mgmt(ctx.obj)

    def run(self, function):
        try:
            return function()
        except CommandException as e:
            return e.command_result
        except Exception as e:
            if self.ctx.obj['debug'] == True:
                raise e
            return CommandResult(returncode=-1, output="Error: " + str(e))


    def _shared_data_mgmt(self, context={}):
        git_config = {'find_git': True}
        openbis_config = {}
        if context.get('verify_certificates') is not None:
            openbis_config['verify_certificates'] = context['verify_certificates']
        log = CommandLog()
        if self.halt_on_error_log and log.any_log_exists():
            click_echo("Error: A previous command did not finish. Please check the log ({}) and remove it when you want to continue using obis".format(log.folder_path))
            sys.exit(-1)
        return dm.DataMgmt(openbis_config=openbis_config, git_config=git_config, log=log, debug=context['debug'])
