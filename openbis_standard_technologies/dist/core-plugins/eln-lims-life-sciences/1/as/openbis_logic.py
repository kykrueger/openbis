from parsers import VocabularyDefinitionToCreationParser, PropertyTypeDefinitionToCreationParser, SampleTypeDefinitionToCreationParser, \
                    ExperimentTypeDefinitionToCreationParser, DatasetTypeDefinitionToCreationParser, SpaceDefinitionToCreationParser, \
                    ProjectDefinitionToCreationParser, ScriptDefinitionToCreationParser
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search import VocabularySearchCriteria, \
    SearchVocabulariesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search import PropertyTypeSearchCriteria, \
    SearchPropertyTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleTypeSearchCriteria, \
    SearchSampleTypesOperation, SampleSearchCriteria, SearchSamplesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search import ExperimentTypeSearchCriteria, \
    SearchExperimentTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetTypeSearchCriteria, \
    SearchDataSetTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions import VocabularyFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions import PropertyTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleTypeFetchOptions, \
    SampleFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.operation import SynchronousOperationExecutionOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search import SearchOperator
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search import SpaceSearchCriteria, \
    SearchSpacesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions import SpaceFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search import ProjectSearchCriteria, \
    SearchProjectsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions import ProjectFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search import PluginSearchCriteria, \
    SearchPluginsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions import PluginFetchOptions


class ServerDuplicatesCreationHandler(object):
    
    def __init__(self, api, sesstion_token, creations):
        self.api = api
        self.session_token = sesstion_token
        self.creations = creations
    
    def remove_already_existing_elements(self):
        duplicates_removal_strategies = [
            {
            'creations_type': VocabularyDefinitionToCreationParser.type,
            'search_criteria_object' : VocabularySearchCriteria,
            'search_operation':SearchVocabulariesOperation,
            'fetch_options':VocabularyFetchOptions,
            'distinct_property_name': 'code'
            },
            {
            'creations_type': PropertyTypeDefinitionToCreationParser.type,
            'search_criteria_object' : PropertyTypeSearchCriteria,
            'search_operation': SearchPropertyTypesOperation,
            'fetch_options': PropertyTypeFetchOptions,
            'distinct_property_name': 'code'
            },
            {
            'creations_type': SampleTypeDefinitionToCreationParser.type,
            'search_criteria_object' : SampleTypeSearchCriteria,
            'search_operation': SearchSampleTypesOperation,
            'fetch_options': SampleTypeFetchOptions,
            'distinct_property_name': 'code'
            },
            {
            'creations_type': ExperimentTypeDefinitionToCreationParser.type,
            'search_criteria_object' : ExperimentTypeSearchCriteria,
            'search_operation': SearchExperimentTypesOperation,
            'fetch_options': ExperimentTypeFetchOptions,
            'distinct_property_name': 'code'
            },
            {
            'creations_type': DatasetTypeDefinitionToCreationParser.type,
            'search_criteria_object' : DataSetTypeSearchCriteria,
            'search_operation': SearchDataSetTypesOperation,
            'fetch_options': DataSetTypeFetchOptions,
            'distinct_property_name': 'code'
            },
            {
            'creations_type': SpaceDefinitionToCreationParser.type,
            'search_criteria_object' : SpaceSearchCriteria,
            'search_operation': SearchSpacesOperation,
            'fetch_options': SpaceFetchOptions,
            'distinct_property_name': 'code'
            },
            {
            'creations_type': ProjectDefinitionToCreationParser.type,
            'search_criteria_object' : ProjectSearchCriteria,
            'search_operation': SearchProjectsOperation,
            'fetch_options': ProjectFetchOptions,
            'distinct_property_name': 'code'
            },
            {
            'creations_type': ScriptDefinitionToCreationParser.type,
            'search_criteria_object' : PluginSearchCriteria,
            'search_operation': SearchPluginsOperation,
            'fetch_options': PluginFetchOptions,
            'distinct_property_name': 'name'
            }
        ]
        for strategy in duplicates_removal_strategies:
            if strategy['creations_type'] in self.creations:
                self.creations[strategy['creations_type']] = self.remove_specific_existing_elements(**strategy)
        return self.creations

    def remove_specific_existing_elements(self, creations_type, search_criteria_object, search_operation, fetch_options, distinct_property_name):
        if creations_type in self.creations:
            search_criteria = self.get_search_criteria(creations_type, search_criteria_object())
            result = self.execute_search_operation(search_operation(search_criteria, fetch_options()))
            return self.filter_creations_from_existing_objects(creations_type, result.getObjects(), distinct_property_name)

    def get_search_criteria(self, creations_type, search_criteria):
        specific_creations = self.creations[creations_type]
        
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
        return self.api.executeOperations(self.session_token, [operation], SynchronousOperationExecutionOptions()).getResults().get(0).getSearchResult()

    def filter_creations_from_existing_objects(self, creations_type, existing_objects, attr):
        existing_object_codes = [getattr(object, attr) for object in existing_objects]
        return list(filter(lambda creation: getattr(creation, attr) not in existing_object_codes, self.creations[creations_type]))
