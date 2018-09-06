import click
import json
import sys

from datetime import datetime

from .. import dm
from ..dm.utils import cd
from ..dm.command_result import CommandResult
from ..dm.command_result import CommandException
from ..dm.command_log import CommandLog
from .click_util import click_echo
from .config_util import set_property


class DataMgmtRunner(object):

    def run(self, ctx, function):
        try:
            return function()
        except CommandException as e:
            return e.command_result
        except Exception as e:
            if ctx.obj['debug'] == True:
                raise e
            return CommandResult(returncode=-1, output="Error: " + str(e))


    def shared_data_mgmt(self, context={}, halt_on_error_log=True):
        git_config = {'find_git': True}
        openbis_config = {}
        if context.get('verify_certificates') is not None:
            openbis_config['verify_certificates'] = context['verify_certificates']
        log = CommandLog()
        if halt_on_error_log and log.any_log_exists():
            click_echo("Error: A previous command did not finish. Please check the log ({}) and remove it when you want to continue using obis".format(log.folder_path))
            sys.exit(-1)
        return dm.DataMgmt(openbis_config=openbis_config, git_config=git_config, log=log, debug=context['debug'])


    def check_result(self, command, result):
        if result.failure():
            click_echo("Could not {}:\n{}".format(command, result.output))
        elif len(result.output) > 0:
            click_echo(result.output)
        return result.returncode


    def init_handle_cleanup(self, result, object_id, collection_id, repository, data_mgmt):
        if (not object_id and not collection_id) or result.failure():
            return self.check_result("init_data", result)
        with dm.cd(repository):
            if object_id:
                resolver = data_mgmt.object
                return self.check_result("init_data", set_property(data_mgmt, resolver, 'id', object_id, False, False))
            if collection_id:
                resolver = data_mgmt.collection
                return self.check_result("init_data", set_property(data_mgmt, resolver, 'id', collection_id, False, False))
