import socket
import os
import pybis
from .openbis_command import OpenbisCommand
from ..command_result import CommandResult
from ..utils import cd
from ..utils import run_shell
from ..utils import complete_openbis_config
from .. import config as dm_config
from ...scripts.cli import shared_data_mgmt
from ... import dm


class Clone(OpenbisCommand):
    """
    Implements the clone command. Copies a repository from a content copy of a data set
    and adds the local copy as a new content copy using the addref command.
    """

    def __init__(self, dm, data_set_id, ssh_user, content_copy_index):
        self.data_set_id = data_set_id
        self.ssh_user = ssh_user
        self.content_copy_index = content_copy_index
        self.load_global_config(dm)
        super(Clone, self).__init__(dm)


    def load_global_config(self, dm):
        """
        Use global config only.
        """
        resolver = dm_config.ConfigResolver()
        config = {}
        complete_openbis_config(config, resolver, False)
        dm.openbis_config = config


    def check_configuration(self):
        missing_config_settings = []
        if self.openbis is None:
            missing_config_settings.append('openbis_url')
        if self.user() is None:
            missing_config_settings.append('user')
        if len(missing_config_settings) > 0:
            return CommandResult(returncode=-1,
                                 output="Missing configuration settings for {}.".format(missing_config_settings))
        return CommandResult(returncode=0, output="")


    def run(self):

        result = self.prepare_run()
        if result.failure():
            return result

        data_set = self.openbis.get_dataset(self.data_set_id)

        content_copy = self.get_content_copy(data_set)
        host = content_copy['externalDms']['address'].split(':')[0]
        path = content_copy['path']
        repository_folder = path.split('/')[-1]

        result = self.copy_repository(self.ssh_user, host, path)
        if result.failure():
            return result
        result = self.checkout_commit(content_copy, path)
        if result.failure():
            return result
        return self.add_content_copy_to_openbis(repository_folder)


    def get_content_copy(self, data_set):
        if data_set.data['kind'] != 'LINK':
            raise ValueError('Data set is of type ' + data_set.data['kind'] + ' but should be LINK.')
        content_copies = data_set.data['linkedData']['contentCopies']
        if len(content_copies) == 0:
            raise ValueError("Data set has no content copies.")
        elif len(content_copies) == 1:
            return content_copies[0]
        else:
            return self.select_content_copy(content_copies)


    def select_content_copy(self, content_copies):
        if self.content_copy_index is not None:
            # use provided content_copy_index
            if self.content_copy_index > 0 and self.content_copy_index <= len(content_copies):
                return content_copies[self.content_copy_index-1]
            else:
                raise ValueError("Invalid content copy index.")
        else:
            # ask user
            while True:
                print('From which content copy do you want to clone?')
                for i, content_copy in enumerate(content_copies):
                    host = content_copy['externalDms']['address'].split(":")[0]
                    path = content_copy['path']
                    print("  {}) {}:{}".format(i+1, host, path))

                copy_index_string = input('> ')
                if copy_index_string.isdigit():
                    copy_index_int = int(copy_index_string)
                    if copy_index_int > 0 and copy_index_int <= len(content_copies):
                        return content_copies[copy_index_int-1]


    def copy_repository(self, ssh_user, host, path):
        # abort if local folder already exists
        repository_folder = path.split('/')[-1]
        if os.path.exists(repository_folder):
            return CommandResult(returncode=-1, output="Folder for repository to clone already exists: " + repository_folder)
        # check if local or remote
        if host == socket.gethostname():
            location = path
        else:
            location = ssh_user + "@" if ssh_user is not None else ""
            location += host + ":" + path
        # copy repository
        return run_shell(["rsync", "--progress", "-av", location, "."])


    def checkout_commit(self, content_copy, path):
        """
        Checks out the commit of the content copy from which the clone was made 
        in case there are newer commits / data sets. So the new copy is based on the 
        data set given as an input.
        """
        commit_hash = content_copy['gitCommitHash']
        repository_folder = path.split('/')[-1]
        with cd(repository_folder):
            return self.git_wrapper.git_checkout(commit_hash)


    def add_content_copy_to_openbis(self, repository_folder):
        with cd(repository_folder):
            data_mgmt = dm.DataMgmt(openbis_config={}, git_config={'find_git': True})
            return data_mgmt.addref()
