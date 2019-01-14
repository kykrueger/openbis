#!/usr/bin/python
# encoding=utf8
#!/usr/bin/python
#
# Requirement:
#   The pybis module must be available.

import settings
import systemtest.testcase
import os
from random import randrange


class TestCase(systemtest.testcase.TestCase):


    def execute(self):

        self.SPACE = 'TEST_SPACE_' + str(randrange(100000))
        self.PROJECT = 'TEST_PROJECT_' + str(randrange(100000))
        self.USER_ID = 'SunWukong_' + str(randrange(100000))
        self.SAMPLE_CODE_1 = 'SAMPLE_' + str(randrange(100000))
        self.SAMPLE_CODE_2 = 'SAMPLE_' + str(randrange(100000))
        self.EXPERIMENT_CODE = "EXP_" + str(randrange(100000))
        self.FILE = 'TEST_FILE_' + str(randrange(100000))

        self.installOpenbis()
        self.installPybis()
        self.openbisController = self.createOpenbisController()
        self.openbisController.createTestDatabase("openbis")
        self.openbisController.allUp()

        openbis = self._get_openbis()
        self._test_login(openbis)
        self._test_server_information(openbis)
        self._test_datastores(openbis)
        self._test_spaces(openbis)
        self._test_id_generation(openbis)
        self._test_persons(openbis)
        self._test_groups(openbis)
        self._test_role_assignments(openbis)
        self._test_samples(openbis)
        self._test_experiments(openbis)
        self._test_datasets(openbis)
        self._test_entity_types(openbis)
        self._test_semantic_annotations(openbis)
        self._test_tags(openbis)
        self._test_vocabularies(openbis)
        self._test_logout(openbis)


    def _get_openbis(self):
        # pybis can only be imported after installPybis is called
        import pybis
        return pybis.Openbis(url="https://localhost:8443", verify_certificates=False)


    def _test_login(self, openbis):
        openbis.login('admin', 'admin', save_token=True)
        assert openbis.is_session_active() == True


    def _test_server_information(self, openbis):
        server_information = openbis.get_server_information()
        assert type(server_information.get_major_version()) is int
        assert type(server_information.get_minor_version()) is int


    def _test_datastores(self, openbis):
        assert 'DSS1' in openbis.get_datastores().code.values


    def _test_spaces(self, openbis):
        space = openbis.new_space(code=self.SPACE)
        assert space.permId is None
        space.save()
        assert space.permId == self.SPACE
        assert self.SPACE in openbis.spaces.df.code.values
        assert self.SPACE in openbis.get_spaces().df.code.values
        assert len(openbis.get_spaces(code=self.SPACE)) == 1


    def _test_projects(self, openbis):
        project = openbis.new_project(self.SPACE, self.PROJECT)
        assert project.permId is None
        project.save()
        assert project.permId is not None
        assert project.permId == openbis.get_project(self.SPACE + '/' + self.PROJECT).permId
        assert project.permId in openbis.get_projects(space=self.SPACE).df.permId.values
        assert project.permId in openbis.projects.df.permId.values


    def _test_id_generation(self, openbis):
        # should create incremental ids
        permId1 = openbis.create_permId()
        permId2 = openbis.create_permId()
        permId1_number = int(permId1.split("-")[1])
        permId2_number = int(permId2.split("-")[1])
        assert permId1_number + 1 == permId2_number
        object_code_1 = openbis.gen_code("OBJECT")
        object_code_2 = openbis.gen_code("OBJECT")
        assert int(object_code_1) + 1 == int(object_code_2)


    def _test_persons(self, openbis):
        # should throw error when person exists
        try:
            error = False
            openbis.new_person("admin")
        except ValueError as e:
            error = True
            assert "There already exists a user" in str(e)
        assert error == True
        # should create new person
        person = openbis.new_person(self.USER_ID)
        assert person.userId == self.USER_ID
        assert person.permId is None
        self.openbisController.addUser(self.USER_ID, 'password')
        person.save()
        person = openbis.get_person(self.USER_ID)
        assert person.userId == self.USER_ID
        assert person.permId is not None
        assert person.userId in openbis.get_persons().df.userId.values

        persons = openbis.get_persons(roleLevel="INSTANCE")
        assert "admin" in persons.df.userId.values



    def _test_groups(self, openbis):
        group_id = "test_group"
        group = openbis.new_group(group_id)
        assert group.code == group_id
        assert group.permId is None
        group = group.save()
        assert group.permId is not None


    def _test_role_assignments(self, openbis):
        # should find instance admin
        for i, role_assignment in openbis.get_role_assignments().df.iterrows():
            if role_assignment.user == "admin":
                found = True
                assert role_assignment.role == "ADMIN"
                assert role_assignment.roleLevel == "INSTANCE"
        assert found == True
        assert openbis.get_role_assignment(1).id == "1"
        # should assign role to user
        openbis.assign_role("ADMIN", person=self.USER_ID)
        found = False
        for i, role_assignment in openbis.get_role_assignments().df.iterrows():
            if role_assignment.user == self.USER_ID:
                found = True
                assert role_assignment.role == "ADMIN"
        assert found == True


    def _test_samples(self, openbis):
        samples = openbis.get_samples(code=self.SAMPLE_CODE_1)
        assert len(samples) == 0
        sample = openbis.new_sample(type="UNKNOWN", code=self.SAMPLE_CODE_1, space=self.SPACE)
        assert sample.permId is None
        sample.save()
        assert sample.permId is not None
        openbis.delete_entity("Sample", sample.permId, "reason")
        assert len(openbis.get_samples(code=sample.code)) == 0
        assert sample.permId in openbis.get_deletions().permId.values


    def _test_experiments(self, openbis):
        # should check existing experiment
        assert len(openbis.get_experiments()) > 0
        assert len(openbis.get_experiments(code="DEFAULT")) == 1
        assert len(openbis.get_experiments(type="UNKNOWN")) > 0
        assert len(openbis.get_experiments(project="DEFAULT")) > 0
        experiment = openbis.get_experiment("/DEFAULT/DEFAULT/DEFAULT")
        assert experiment.project.identifier == "/DEFAULT/DEFAULT"
        # should create new experiment
        experiment = openbis.new_experiment(type="UNKNOWN", code=self.EXPERIMENT_CODE, project="DEFAULT")
        assert experiment.permId is None
        experiment.save()
        assert experiment.permId is not None


    def _test_datasets(self, openbis):
        sample = openbis.new_sample(type="UNKNOWN", code=self.SAMPLE_CODE_2, space=self.SPACE)
        sample.save()
        with open(self.FILE, "w") as file:
            file.write("content")
        dataset = openbis.new_dataset(files=[self.FILE], type="UNKNOWN")
        dataset.sample = openbis.get_sample('/' + self.SPACE + '/' + self.SAMPLE_CODE_2)
        assert dataset.permId is None
        dataset.save()
        assert dataset.permId is not None
        assert "original/" + self.FILE in dataset.file_list
        host = dataset.download()
        assert os.path.exists(os.path.join(host, dataset.permId, "original", self.FILE))


    def _test_entity_types(self, openbis):
        for entity in ['dataset', 'experiment', 'sample']:
            method_all = getattr(openbis, 'get_' + entity + '_types')
            method_one = getattr(openbis, 'get_' + entity + '_type')
            assert len(method_all()) > 0
            entity_type = method_one('UNKNOWN')
            assert entity_type.code == 'UNKNOWN'


    def _test_semantic_annotations(self, openbis):
        sa = openbis.new_semantic_annotation(
            entityType = 'UNKNOWN',
            predicateOntologyId = 'po_id', predicateOntologyVersion = 'po_version', predicateAccessionId = 'pa_id', 
            descriptorOntologyId = 'do_id',    descriptorOntologyVersion = 'do_version', descriptorAccessionId = 'da_id')
        assert sa.permId is None
        sa.save()
        assert sa.permId is not None
        sas = openbis.get_semantic_annotations()
        assert sa.permId in sas.df.permId.values


    def _test_tags(self, openbis):
        tag = openbis.new_tag('TAG1')
        assert tag.permId is None
        tag.save()
        assert tag.permId is not None
        assert tag.permId in openbis.get_tags().df.permId.values


    def _test_vocabularies(self, openbis):
        # should create vocabulary
        vocabulary = openbis.new_vocabulary(
            code = 'VOBABULARY_1',
            description = 'description',
            terms = [
                { "code": "TERM1", "label": "label1", "description": "description1" },
                { "code": "TERM2", "label": "label2", "description": "description2" },
            ]
        )
        assert vocabulary.registrationDate is None
        vocabulary.save()
        assert vocabulary.registrationDate is not None
        # should get terms
        terms = openbis.get_terms(vocabulary='VOBABULARY_1')
        assert 'TERM1' in terms.df.code.values
        assert 'TERM2' in terms.df.code.values


    def _test_logout(self, openbis):
        openbis.logout()
        assert openbis.is_session_active() == False

TestCase(settings, __file__).runTest()
