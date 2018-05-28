import pybis
from .openbis_command import OpenbisCommand, ContentCopySelector
from ..command_result import CommandResult


class Download(OpenbisCommand):
    """
    Command to download files of a data set. Uses the microservice server to access the files.
    As opposed to the clone, the user does not need to be able to access the files via ssh 
    and no new content copy is created in openBIS.
    """


    def __init__(self, dm, data_set_id, content_copy_index, file):
        self.data_set_id = data_set_id
        self.content_copy_index = content_copy_index
        self.file = file
        self.load_global_config(dm)
        super(Download, self).__init__(dm)


    def run(self):

        if self.fileservice_url() is None:
            return CommandResult(returncode=-1, output="Configuration fileservice_url needs to be set for download.")

        data_set = self.openbis.get_dataset(self.data_set_id)
        content_copy_index =  ContentCopySelector(data_set, self.content_copy_index, get_index=True).select()
        files = [self.file] if self.file is not None else None
        output = data_set.download(files, linked_dataset_fileservice_url=self.fileservice_url(), content_copy_index=content_copy_index)
        return CommandResult(returncode=0, output=output)
