import pybis
from ..command_result import CommandResult
import uuid
import os
from ..git import GitRepoFileInfo
from .openbis_command import OpenbisCommand


class OpenbisSync(OpenbisCommand):
    """A command object for synchronizing with openBIS."""

    def check_configuration(self):
        missing_config_settings = []
        if self.openbis is None:
            missing_config_settings.append('openbis_url')
        if self.user() is None:
            missing_config_settings.append('user')
        if self.data_set_type() is None:
            missing_config_settings.append('data_set_type')
        if self.sample_id() is None and self.experiment_id() is None:
            missing_config_settings.append('sample_id')
            missing_config_settings.append('experiment_id')
        if len(missing_config_settings) > 0:
            return CommandResult(returncode=-1,
                                 output="Missing configuration settings for {}.".format(missing_config_settings))
        return CommandResult(returncode=0, output="")

    def check_data_set_status(self):
        """If we are in sync with the data set on the server, there is nothing to do."""
        # TODO Get the DataSet from the server
        #  - Find the content copy that refers to this repo
        #  - Check if the commit id is the current commit id
        #  - If so, skip sync.
        return CommandResult(returncode=0, output="")

    def create_data_set_code(self):
        try:
            data_set_code = self.openbis.create_permId()
            return CommandResult(returncode=0, output=""), data_set_code
        except ValueError as e:
            return CommandResult(returncode=-1, output=str(e)), None

    def create_data_set(self, data_set_code, external_dms, repository_id):
        data_set_type = self.data_set_type()
        parent_data_set_id = self.data_set_id()
        properties = self.data_set_properties()
        result = self.git_wrapper.git_top_level_path()
        if result.failure():
            return result
        top_level_path = result.output
        result = self.git_wrapper.git_commit_hash()
        if result.failure():
            return result
        commit_id = result.output
        sample_id = self.sample_id()
        experiment_id = self.experiment_id()
        contents = GitRepoFileInfo(self.git_wrapper).contents()
        try:
            data_set = self.openbis.new_git_data_set(data_set_type, top_level_path, commit_id, repository_id, external_dms.code,
                                                     sample=sample_id, experiment=experiment_id, properties=properties, parents=parent_data_set_id,
                                                     data_set_code=data_set_code, contents=contents)
            return CommandResult(returncode=0, output=""), data_set
        except ValueError as e:
            return CommandResult(returncode=-1, output=str(e)), None


    def commit_metadata_updates(self, msg_fragment=None):
        return self.data_mgmt.commit_metadata_updates(msg_fragment)


    def prepare_repository_id(self):
        repository_id = self.repository_id()
        if self.repository_id() is None:
            repository_id = str(uuid.uuid4())
            self.config_resolver.set_value_for_parameter('repository_id', repository_id, 'local')
        return CommandResult(returncode=0, output=repository_id)


    def run(self):
        # TODO Write mementos in case openBIS is unreachable
        # - write a file to the .git/obis folder containing the commit id. Filename includes a timestamp so they can be sorted.

        result = self.prepare_run()
        if result.failure():
            return result

        result = self.prepare_repository_id()
        if result.failure():
            return result
        repository_id = result.output

        result = self.prepare_external_dms()
        if result.failure():
            return result
        external_dms = result.output

        result, data_set_code = self.create_data_set_code()
        if result.failure():
            return result

        self.commit_metadata_updates()

        # Update data set id as last commit so we can easily revert it on failure
        self.config_resolver.set_value_for_parameter('data_set_id', data_set_code, 'local')
        self.commit_metadata_updates("data set id")

        # create a data set, using the existing data set as a parent, if there is one
        result, data_set = self.create_data_set(data_set_code, external_dms, repository_id)
        return result
