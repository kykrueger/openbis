import os
import pybis
from .openbis_command import OpenbisCommand, ContentCopySelector
from ..command_result import CommandResult
from ..checksum import validate_checksum

class Download(OpenbisCommand):
    """
    Command to download files of a data set. Uses the microservice server to access the files.
    As opposed to the clone, the user does not need to be able to access the files via ssh 
    and no new content copy is created in openBIS.
    """


    def __init__(self, dm, data_set_id, content_copy_index, file, skip_integrity_check):
        self.data_set_id = data_set_id
        self.content_copy_index = content_copy_index
        self.file = file
        self.skip_integrity_check = skip_integrity_check
        self.load_global_config(dm)
        super(Download, self).__init__(dm)


    def run(self):

        if self.fileservice_url() is None:
            return CommandResult(returncode=-1, output="Configuration fileservice_url needs to be set for download.")

        data_set = self.openbis.get_dataset(self.data_set_id)
        content_copy_index =  ContentCopySelector(data_set, self.content_copy_index, get_index=True).select()
        files = [self.file] if self.file is not None else data_set.file_list
        destination = data_set.download(files, linked_dataset_fileservice_url=self.fileservice_url(), content_copy_index=content_copy_index)
        if self.skip_integrity_check != True:
            validate_checksum(self.openbis, files, data_set.permId, os.path.join(destination, data_set.permId))
        return CommandResult(returncode=0, output="Files downloaded to: %s" % os.path.join(destination, data_set.permId))
