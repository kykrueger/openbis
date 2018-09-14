from copy import copy

from ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search import SearchOperator
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetTypeSearchCriteria, SearchDataSetTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentTypeFetchOptions, ExperimentFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id import ExperimentPermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search import ExperimentTypeSearchCriteria, \
    SearchExperimentTypesOperation, ExperimentSearchCriteria, SearchExperimentsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.operation import SynchronousOperationExecutionOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions import PluginFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search import PluginSearchCriteria, SearchPluginsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions import ProjectFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id import ProjectPermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search import ProjectSearchCriteria, SearchProjectsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions import PropertyTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search import PropertyTypeSearchCriteria, SearchPropertyTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleTypeFetchOptions, SampleFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SamplePermId, SampleIdentifier
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleTypeSearchCriteria, \
    SearchSampleTypesOperation, SampleSearchCriteria, SearchSamplesOperation, \
    SampleSearchRelation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions import SpaceFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id import SpacePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search import SpaceSearchCriteria, SearchSpacesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions import VocabularyFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search import VocabularySearchCriteria, SearchVocabulariesOperation
from parsers import VocabularyDefinitionToCreationParser, PropertyTypeDefinitionToCreationParser, SampleTypeDefinitionToCreationParser, \
                    ExperimentTypeDefinitionToCreationParser, DatasetTypeDefinitionToCreationParser, SpaceDefinitionToCreationParser, \
                    ProjectDefinitionToCreationParser, ExperimentDefinitionToCreationParser, ScriptDefinitionToCreationParser, SampleDefinitionToCreationParser


class OpenbisLogicHandler(object):

    def __init__(self, api, sesstion_token, creations):
        self.api = api
        self.session_token = sesstion_token
        self.creations = creations

    def process_entities(self):
        '''
            Function removes creations if such entity exist on server.
            Function rewrites CreationIds of entity pointers such as spaceId, projectId etc. 
        '''
        existing_elements = self.find_all_existing_elements()
        self.creations = self.remove_existing_elements_from_creations(existing_elements)
        self.creations = self.rewrite_parentchild_creationid_to_permid(existing_elements)
        self.creations = self.rewrite_vocabulary_labels(exiisting_elements)
        return self.creations

    def rewrite_vocabulary_labels(self):
        pass

    def rewrite_parentchild_creationid_to_permid(self, existing_elements):
        if ProjectDefinitionToCreationParser.type in self.creations:
            for creation in self.creations[ProjectDefinitionToCreationParser.type]:
                for existing_element in existing_elements[SpaceDefinitionToCreationParser.type]:
                    if existing_element.code == creation.spaceId.creationId:
                        creation.spaceId = SpacePermId(str(existing_element.permId))
                        break
        if ExperimentDefinitionToCreationParser.type in self.creations:
            for creation in self.creations[ExperimentDefinitionToCreationParser.type]:
                for existing_element in existing_elements[ProjectDefinitionToCreationParser.type]:
                    if existing_element.code == creation.projectId.creationId:
                        creation.projectId = ProjectPermId(str(existing_element.permId))
                        break
        if SampleDefinitionToCreationParser.type in self.creations:
            for creation in self.creations[SampleDefinitionToCreationParser.type]:
                if creation.spaceId is not None:
                    for existing_element in existing_elements[SpaceDefinitionToCreationParser.type]:
                        if existing_element.code == creation.spaceId.creationId:
                            creation.spaceId = SpacePermId(str(existing_element.permId))
                            break
                if creation.projectId is not None:
                    for existing_element in existing_elements[ProjectDefinitionToCreationParser.type]:
                        if existing_element.code == creation.projectId.creationId:
                            creation.projectId = ProjectPermId(str(existing_element.permId))
                            break
                if creation.experimentId is not None:
                    for existing_element in existing_elements[ExperimentDefinitionToCreationParser.type]:
                        if existing_element.code == creation.experimentId.creationId:
                            creation.experimentId = ExperimentPermId(str(existing_element.permId))
                            break

                rewritten_children = []
                if creation.childIds is not None:
                    for child in creation.childIds:
                        new_id = None
                        for existing_element in existing_elements[SampleDefinitionToCreationParser.type]:
                            if existing_element.permId.permId == child.creationId:
                                new_id = existing_element.permId
                                break
                            elif existing_element.identifier.identifier == child.creationId:
                                new_id = existing_element.identifier
                                break

                        if new_id is None:
                            rewritten_children.append(child)
                        else:
                            rewritten_children.append(new_id)

                rewritten_parents = []
                if creation.parentIds is not None:
                    for parent in creation.parentIds:
                        new_id = None
                        for existing_element in existing_elements[SampleDefinitionToCreationParser.type]:
                            if existing_element.permId.permId == parent.creationId:
                                new_id = existing_element.permId
                                break
                            elif existing_element.identifier.identifier == parent.creationId:
                                new_id = existing_element.identifier
                                break

                        if new_id is None:
                            rewritten_parents.append(parent)
                        else:
                            rewritten_parents.append(new_id)
                creation.setChildIds(rewritten_children)
                creation.setParentIds(rewritten_parents)

        return self.creations

    def find_all_existing_elements(self):
        search_strategy = [
            {
            'creations_type': VocabularyDefinitionToCreationParser.type,
            'search_criteria_object' : VocabularySearchCriteria,
            'search_operation':SearchVocabulariesOperation,
            'fetch_options':VocabularyFetchOptions
            },
            {
            'creations_type': PropertyTypeDefinitionToCreationParser.type,
            'search_criteria_object' : PropertyTypeSearchCriteria,
            'search_operation': SearchPropertyTypesOperation,
            'fetch_options': PropertyTypeFetchOptions
            },
            {
            'creations_type': SampleTypeDefinitionToCreationParser.type,
            'search_criteria_object' : SampleTypeSearchCriteria,
            'search_operation': SearchSampleTypesOperation,
            'fetch_options': SampleTypeFetchOptions
            },
            {
            'creations_type': ExperimentTypeDefinitionToCreationParser.type,
            'search_criteria_object' : ExperimentTypeSearchCriteria,
            'search_operation': SearchExperimentTypesOperation,
            'fetch_options': ExperimentTypeFetchOptions
            },
            {
            'creations_type': DatasetTypeDefinitionToCreationParser.type,
            'search_criteria_object' : DataSetTypeSearchCriteria,
            'search_operation': SearchDataSetTypesOperation,
            'fetch_options': DataSetTypeFetchOptions
            },
            {
            'creations_type': SpaceDefinitionToCreationParser.type,
            'search_criteria_object' : SpaceSearchCriteria,
            'search_operation': SearchSpacesOperation,
            'fetch_options': SpaceFetchOptions
            },
            {
            'creations_type': ProjectDefinitionToCreationParser.type,
            'search_criteria_object' : ProjectSearchCriteria,
            'search_operation': SearchProjectsOperation,
            'fetch_options': ProjectFetchOptions
            },
            {
            'creations_type': ExperimentDefinitionToCreationParser.type,
            'search_criteria_object' : ExperimentSearchCriteria,
            'search_operation': SearchExperimentsOperation,
            'fetch_options': ExperimentFetchOptions
            },
            {
            'creations_type': SampleDefinitionToCreationParser.type,
            'search_criteria_object' : SampleSearchCriteria,
            'search_operation': SearchSamplesOperation,
            'fetch_options': SampleFetchOptions
            },
            {
            'creations_type': ScriptDefinitionToCreationParser.type,
            'search_criteria_object' : PluginSearchCriteria,
            'search_operation': SearchPluginsOperation,
            'fetch_options': PluginFetchOptions
            }
        ]

        existing_elements = {}
        for strategy in search_strategy:
            creations_type = strategy['creations_type']
            if creations_type in self.creations:
                existing_specific_elements = self._get_existing_elements(**strategy)
                if existing_specific_elements is not None:
                    existing_elements[creations_type] = existing_specific_elements
        return existing_elements

    def _get_distinct_property_name(self, creation_type):
        if creation_type == ScriptDefinitionToCreationParser.type:
            return 'name'
        else:
            return 'code'

    def _get_existing_elements(self, creations_type, search_criteria_object, search_operation, fetch_options):
        search_criteria = self.get_search_criteria(creations_type, search_criteria_object)
        if search_criteria is []:
            return None
        result = self.execute_search_operation(search_operation(search_criteria, fetch_options()))
        return result.getObjects()

    def remove_existing_elements_from_creations(self, all_existing_elements):
        for creations_type, existing_elements in all_existing_elements.items():
            if creations_type == SampleDefinitionToCreationParser.type:
                existing_object_codes = [object.identifier.identifier for object in existing_elements]
                self.creations[creations_type] = list(filter(lambda creation: creation.code is None or self.create_sample_identifier_string(creation) not in existing_object_codes, self.creations[creations_type]))
            else:
                distinct_property_name = self._get_distinct_property_name(creations_type)
                self.creations[creations_type] = self.filter_creations_from_existing_objects(creations_type, existing_elements, distinct_property_name)
        return self.creations

    def create_sample_identifier_string(self, creation):
        spaceId = creation.spaceId.creationId if creation.spaceId is not None else None
        projectId = creation.projectId.creationId if creation.projectId is not None else None
#         experimentId = creation.experimentId.creationId if creation.experimentId is not None else None
        code = creation.code
        sample_identifier = SampleIdentifier(spaceId, projectId, None, code)
        return sample_identifier.identifier

    def get_search_criteria(self, creations_type, search_criteria_class):
        specific_creations = self.creations[creations_type]
        search_criteria = search_criteria_class()

        if creations_type == SampleDefinitionToCreationParser.type:

            search_criterias = []
            for creation in specific_creations:
                search_criteria.withOrOperator()
                if creation.code is not None:
                    search_criteria.withCode().thatEquals(creation.code)
                    if creation.experimentId is not None:
                        search_criteria.withExperiment().withCode().thatEquals(creation.experimentId.creationId)
                    else:
                        search_criteria.withoutExperiment()

                    if creation.projectId is not None:
                        search_criteria.withProject().withCode().thatEquals(creation.projectId.creationId)
                    else:
                        search_criteria.withoutProject()

                    if creation.spaceId is not None:
                        search_criteria.withSpace().withCode().thatEquals(creation.spaceId.creationId)
                    else:
                        search_criteria.withoutSpace()

                    search_criterias.append(search_criteria)
            return search_criteria

        if 'withCodes' in dir(search_criteria):
            search_criteria.withCodes().thatIn([creation.code for creation in specific_creations])
        elif 'withName' in dir(search_criteria):
            for creation in specific_creations:
                search_criteria.withName().thatEquals(creation.name)
            search_criteria.withOrOperator()
        else:
            for creation in specific_creations:
                search_criteria.withCode().thatEquals(creation.code)
            search_criteria.withOrOperator()
        return search_criteria

    def execute_search_operation(self, operation):
        operations = []
        operations.extend(operation if type(operation) == list else [operation])
        return self.api.executeOperations(self.session_token, operations, SynchronousOperationExecutionOptions()).getResults().get(0).getSearchResult()

    def filter_creations_from_existing_objects(self, creations_type, existing_objects, attr):
        existing_object_codes = [getattr(object, attr) for object in existing_objects]
        return list(filter(lambda creation: getattr(creation, attr) not in existing_object_codes, self.creations[creations_type]))
