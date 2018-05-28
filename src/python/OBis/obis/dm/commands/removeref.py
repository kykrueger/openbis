import json
import os
from .openbis_command import OpenbisCommand
from ..command_result import CommandResult
from ..utils import complete_openbis_config


class Removeref(OpenbisCommand):
    """
    Command to add the current folder, which is supposed to be an obis repository, as 
    a new content copy to openBIS.
    """

    def __init__(self, dm):
        super(Removeref, self).__init__(dm)


    def run(self):
        result = self.check_obis_repository()
        if result.failure():
            return result

        data_set = self.openbis.get_dataset(self.data_set_id()).data

        if data_set['linkedData'] is None:
            return CommandResult(returncode=-1, output="Data set has no linked data: " + self.data_set_id())
        if data_set['linkedData']['contentCopies'] is None:
            return CommandResult(returncode=-1, output="Data set has no content copies: " + self.data_set_id())

        content_copies = data_set['linkedData']['contentCopies']
        matching_content_copies = list(filter(lambda cc: 
                cc['externalDms']['code'] == self.external_dms_id() and cc['path'] == self.path()
            , content_copies))

        if len(matching_content_copies) == 0:
            return CommandResult(returncode=-1, output="Matching content copy not fount in data set: " + self.data_set_id())

        for content_copy in matching_content_copies:
            self.openbis.delete_content_copy(self.data_set_id(), content_copy)

        return CommandResult(returncode=0, output="")


    def check_obis_repository(self):
        if os.path.exists('.obis'):
            return CommandResult(returncode=0, output="")
        else:
            return CommandResult(returncode=-1, output="This is not an obis repository.")


    def path(self):
        result = self.git_wrapper.git_top_level_path()
        if result.failure():
            return result
        return result.output


    def commit_id(self):
        result = self.git_wrapper.git_commit_hash()
        if result.failure():
            return result
        return result.output
