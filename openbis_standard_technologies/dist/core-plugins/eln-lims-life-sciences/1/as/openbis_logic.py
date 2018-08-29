from parsers import VocabularyDefinitionToCreationParser, PropertyTypeDefinitionToCreationParser, SampleTypeDefinitionToCreationParser, \
                    ExperimentTypeDefinitionToCreationParser, DatasetTypeDefinitionToCreationParser 
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search import VocabularySearchCriteria, \
    SearchVocabulariesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search import PropertyTypeSearchCriteria, \
    SearchPropertyTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleTypeSearchCriteria, \
    SearchSampleTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search import ExperimentTypeSearchCriteria, \
    SearchExperimentTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetTypeSearchCriteria, \
    SearchDataSetTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions import VocabularyFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions import PropertyTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.operation import SynchronousOperationExecutionOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search import SearchOperator


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
            }
        ]
        for strategy in duplicates_removal_strategies:
            self.creations[strategy['creations_type']] = self.remove_specific_existing_elements(**strategy)
        return self.creations
    
    def remove_specific_existing_elements(self, creations_type, search_criteria_object, search_operation, fetch_options):
        if creations_type in self.creations:
            search_criteria = self.get_search_criteria(creations_type, search_criteria_object())
            result = self.execute_search_operation(search_operation(search_criteria, fetch_options()))
            return self.filter_creations_from_existing_objects(creations_type, result.getObjects())
    
    def get_search_criteria(self, creations_type, search_criteria):
        specific_creations = self.creations[creations_type]
        
        if 'withCodes' in dir(search_criteria):
            search_criteria.withCodes().thatIn([creation.getCode() for creation in specific_creations])
        else:
            for creation in specific_creations:
                search_criteria.withCode().thatEquals(creation.getCode())
            search_criteria.withOrOperator()
        return search_criteria

    def execute_search_operation(self, operation):
        return self.api.executeOperations(self.session_token, [operation], SynchronousOperationExecutionOptions()).getResults().get(0).getSearchResult()

    def filter_creations_from_existing_objects(self, creations_type, existing_objects):
        existing_object_codes = [object.getCode() for object in existing_objects]
        return list(filter(lambda creation: creation.getCode() not in existing_object_codes, self.creations[creations_type]))
