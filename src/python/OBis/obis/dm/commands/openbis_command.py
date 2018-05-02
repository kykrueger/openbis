import getpass
import hashlib
import os
import socket
import pybis
from ..command_result import CommandResult
from ..command_result import CommandException
from .. import config as dm_config
from ..utils import complete_openbis_config
from ...scripts import cli


class OpenbisCommand(object):

    def __init__(self, dm, openbis=None):
        self.data_mgmt = dm
        self.openbis = dm.openbis
        self.git_wrapper = dm.git_wrapper
        self.config_resolver = dm.config_resolver
        self.config_dict = dm.config_resolver.config_dict()

        if self.openbis is None and dm.openbis_config.get('url') is not None:
            self.openbis = pybis.Openbis(**dm.openbis_config)
            if self.user() is not None:
                result = self.login()
                if result.failure():
                    raise CommandException(result)


    def external_dms_id(self):
        return self.config_dict.get('external_dms_id')

    def repository_id(self):
        return self.config_dict.get('repository_id')

    def data_set_type(self):
        return self.config_dict.get('data_set_type')

    def data_set_id(self):
        return self.config_dict.get('data_set_id')

    def data_set_properties(self):
        return self.config_dict.get('data_set_properties')

    def object_id(self):
        return self.config_dict.get('object_id')

    def collection_id(self):
        return self.config_dict.get('collection_id')

    def user(self):
        return self.config_dict.get('user')

    def hostname(self):
        return self.config_dict.get('hostname')

    def fileservice_url(self):
        return self.config_dict.get('fileservice_url')

    def git_annex_hash_as_checksum(self):
        return self.config_dict.get('git_annex_hash_as_checksum')

    def prepare_run(self):
        result = self.check_configuration()
        if result.failure():
            return result
        result = self.login()
        if result.failure():
            return result
        return CommandResult(returncode=0, output="")


    def check_configuration(self):
        """ overwrite in subclass """
        return CommandResult(returncode=0, output="")


    def login(self):
        user = self.user()
        if self.openbis.is_session_active():
            if self.openbis.token.startswith(user):
                return CommandResult(returncode=0, output="")
            else:
                self.openbis.logout()
        passwd = getpass.getpass("Password for {}:".format(user))
        try:
            self.openbis.login(user, passwd, save_token=True)
        except ValueError:
            msg = "Could not log into openbis {}".format(self.config_dict['openbis_url'])
            return CommandResult(returncode=-1, output=msg)
        return CommandResult(returncode=0, output='')

    def prepare_external_dms(self):
        # If there is no external data management system, create one.
        result = self.get_or_create_external_data_management_system()
        if result.failure():
            return result
        external_dms = result.output
        self.config_resolver.set_value_for_parameter('external_dms_id', external_dms.code, 'local')
        self.config_dict['external_dms_id'] = external_dms.code
        return result

    def generate_external_data_management_system_code(self, user, hostname, edms_path):
        path_hash = hashlib.sha1(edms_path.encode("utf-8")).hexdigest()[0:8]
        return "{}-{}-{}".format(user, hostname, path_hash).upper()

    def get_or_create_external_data_management_system(self):
        external_dms_id = self.external_dms_id()
        user = self.user()
        hostname = self.determine_hostname()
        result = self.git_wrapper.git_top_level_path()
        if result.failure():
            return result
        top_level_path = result.output
        edms_path, path_name = os.path.split(result.output)
        if external_dms_id is None:
            external_dms_id = self.generate_external_data_management_system_code(user, hostname, edms_path)
        try:
            external_dms = self.openbis.get_external_data_management_system(external_dms_id.upper())
        except ValueError as e:
            # external dms does not exist - create it
            try:
                external_dms = self.openbis.create_external_data_management_system(external_dms_id, external_dms_id,
                                                                    "{}:/{}".format(hostname, edms_path))
            except ValueError as error:
                return CommandResult(returncode=-1, output=str(error))
        return CommandResult(returncode=0, output=external_dms)

    def determine_hostname(self):
        """ Returns globally defined hostname if available.
            Otherwies, lets the user choose one and stores that globally. """
        # from global config
        hostname = self.hostname()
        if hostname is not None:
            return hostname
        # ask user
        hostname = self.ask_for_hostname(socket.gethostname())
        # store
        cli.config_internal(self.data_mgmt, True, False, 'hostname', hostname)
        return hostname

    def ask_for_hostname(self, hostname):
        """ Asks the user to confirm the suggestes hostname or choose a custom one. """
        hostname_input = input('Enter hostname (empty to confirm \'' + str(hostname) + '\'): ')
        if hostname_input:
            return hostname_input
        else:
            return hostname

    def path(self):
        result = self.git_wrapper.git_top_level_path()
        if result.failure():
            return result
        return result.output

    def load_global_config(self, dm):
        """
        Use global config only.
        """
        resolver = dm_config.ConfigResolver()
        config = {}
        complete_openbis_config(config, resolver, False)
        dm.openbis_config = config


class ContentCopySelector(object):


    def __init__(self, data_set, content_copy_index, get_index=False):
        self.data_set = data_set
        self.content_copy_index = content_copy_index
        self.get_index = get_index


    def select(self):
        content_copy_index = self.select_index()
        if self.get_index == True:
            return content_copy_index
        else:
            return self.data_set.data['linkedData']['contentCopies'][content_copy_index]


    def select_index(self):
        if self.data_set.data['kind'] != 'LINK':
            raise ValueError('Data set is of type ' + self.data_set.data['kind'] + ' but should be LINK.')
        content_copies = self.data_set.data['linkedData']['contentCopies']
        if len(content_copies) == 0:
            raise ValueError("Data set has no content copies.")
        elif len(content_copies) == 1:
            return 0
        else:
            return self.select_content_copy_index(content_copies)


    def select_content_copy_index(self, content_copies):
        if self.content_copy_index is not None:
            # use provided content_copy_index
            if self.content_copy_index >= 0 and self.content_copy_index < len(content_copies):
                return self.content_copy_index
            else:
                raise ValueError("Invalid content copy index.")
        else:
            # ask user
            while True:
                print('From which location should the files be copied?')
                for i, content_copy in enumerate(content_copies):
                    host = content_copy['externalDms']['address'].split(":")[0]
                    path = content_copy['path']
                    print("  {}) {}:{}".format(i, host, path))

                copy_index_string = input('> ')
                if copy_index_string.isdigit():
                    copy_index_int = int(copy_index_string)
                    if copy_index_int >= 0 and copy_index_int < len(content_copies):
                        return copy_index_int
