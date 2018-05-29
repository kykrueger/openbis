import os
import pybis
from .openbis_command import OpenbisCommand, ContentCopySelector
from ..command_result import CommandResult
from ..checksum import get_checksum_generator, ChecksumGeneratorCrc32

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
        files = [self.file] if self.file is not None else data_set.file_list
        destination = data_set.download(files, linked_dataset_fileservice_url=self.fileservice_url(), content_copy_index=content_copy_index)
        result = self._validate_checksum(files, data_set, destination)
        if result.failure():
            return result
        return CommandResult(returncode=0, output="Files downloaded to: %s" % os.path.join(destination, data_set.permId))


    def _validate_checksum(self, files, data_set, destination):
        dataset_files = self.openbis.search_files(data_set.permId)['objects']
        dataset_files_by_path = {}
        for dataset_file in dataset_files:
            dataset_files_by_path[dataset_file['path']] = dataset_file
        for filename in files:
            dataset_file = dataset_files_by_path[filename]
            filename_dest = os.path.join(destination, data_set.permId, filename)
            checksum_generator = None
            if dataset_file['checksumCRC32'] is not None and dataset_file['checksumCRC32'] > 0:
                checksum_generator = ChecksumGeneratorCrc32()
                expected_checksum = dataset_file['checksumCRC32']
            elif dataset_file['checksumType'] is not None:
                checksum_generator = get_checksum_generator(dataset_file['checksumType'])
                expected_checksum = dataset_file['checksum']
            if checksum_generator is not None:
                checksum = checksum_generator.get_checksum(filename_dest)['checksum']
                if checksum != expected_checksum:
                    return CommandResult(returncode=-1, output="Checksum wrong for file {}. Expected {} but was {}.".format(filename_dest, expected_checksum, checksum))
        return CommandResult(returncode=0, output="")
