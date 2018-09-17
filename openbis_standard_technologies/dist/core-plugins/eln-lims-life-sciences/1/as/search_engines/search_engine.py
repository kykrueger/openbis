from ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search import SearchOperator
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetTypeSearchCriteria, SearchDataSetTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentTypeFetchOptions, ExperimentFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search import ExperimentTypeSearchCriteria, \
    SearchExperimentTypesOperation, ExperimentSearchCriteria, SearchExperimentsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.operation import SynchronousOperationExecutionOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions import PluginFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search import PluginSearchCriteria, SearchPluginsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions import ProjectFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search import ProjectSearchCriteria, SearchProjectsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions import PropertyTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search import PropertyTypeSearchCriteria, SearchPropertyTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleTypeFetchOptions, SampleFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleTypeSearchCriteria, \
    SearchSampleTypesOperation, SampleSearchCriteria, SearchSamplesOperation, \
    SampleSearchRelation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions import SpaceFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search import SpaceSearchCriteria, SearchSpacesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions import VocabularyFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search import VocabularySearchCriteria, SearchVocabulariesOperation
from parsers import VocabularyDefinitionToCreationParser, PropertyTypeDefinitionToCreationParser, SampleTypeDefinitionToCreationParser, \
                    ExperimentTypeDefinitionToCreationParser, DatasetTypeDefinitionToCreationParser, SpaceDefinitionToCreationParser, \
                    ProjectDefinitionToCreationParser, ExperimentDefinitionToCreationParser, ScriptDefinitionToCreationParser, SampleDefinitionToCreationParser


class SearchEngine():

    def __init__(self, api, sesstion_token):
            self.api = api
            self.session_token = sesstion_token

    def find_all_existing_elements(self, creations):
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
            if creations_type in creations:
                existing_specific_elements = self._get_existing_elements(creations=creations, **strategy)
                if existing_specific_elements is not None:
                    existing_elements[creations_type] = existing_specific_elements
        return existing_elements

    def _get_existing_elements(self, creations, creations_type, search_criteria_object, search_operation, fetch_options):
        search_criteria = self._get_search_criteria(creations_type, creations[creations_type], search_criteria_object)
        if search_criteria is []:
            return None
        result = self._execute_search_operation(search_operation(search_criteria, fetch_options()))
        return result.getObjects()

    def _get_search_criteria(self, creations_type, specific_creations, search_criteria_class):
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

    def _execute_search_operation(self, operation):
        operations = []
        operations.extend(operation if type(operation) == list else [operation])
        return self.api.executeOperations(self.session_token, operations, SynchronousOperationExecutionOptions()).getResults().get(0).getSearchResult()

