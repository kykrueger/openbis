from ch.ethz.sis.openbis.generic.asapi.v3.dto.operation import SynchronousOperationExecutionOptions
from parsers import get_creations_from, get_definitions_from, get_creation_metadata_from, \
    CreationOrUpdateToOperationParser, Definition, DefinitionToCreationParser, versionable_types
from processors import OpenbisDuplicatesHandler, PropertiesLabelHandler, DuplicatesHandler, \
    unify_properties_representation_of
from search_engines import SearchEngine
from utils import FileHandler
from utils.openbis_utils import get_version_vocabulary_name_for, get_metadata_vocabulary_name_for
from ch.systemsx.cisd.common.exceptions import UserFailureException
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions import VocabularyFetchOptions


def validate_data(xls_byte_arrays, update_mode, xls_name):
    if xls_byte_arrays is None:
        raise UserFailureException('Excel sheet has not been provided. "xls" parameter is None')
    if update_mode not in ['IGNORE_EXISTING', 'FAIL_IF_EXISTS', 'UPDATE_IF_EXISTS']:
        raise UserFailureException(
            'Update mode has to be one of following: IGNORE_EXISTING FAIL_IF_EXISTS UPDATE_IF_EXISTS but was ' + (
                str(update_mode) if update_mode else 'None'))
    if xls_name is None:
        raise UserFailureException('Excel name has not been provided.  parameter is mandatory')


def process(context, parameters):
    """
        Excel import AS service.
        For extensive documentation of usage and Excel layout, 
        please visit https://wiki-bsse.ethz.ch/display/openBISDoc/Excel+import+service
        
        :param context: Standard Openbis AS Service context object
        :param parameters: Contains two elements
                        {
                            'xls' : excel byte blob,    - mandatory
                            'xls_name': identifier of excel file - mandatory
                            'scripts': {                - optional
                                file path: loaded file
                            },
                            update_mode: [IGNORE_EXISTING|FAIL_IF_EXISTS|UPDATE_IF_EXISTS] - optional, default FAIL_IF_EXISTS
                                                                                             This only takes duplicates that are ON THE SERVER
                        }
        :return: Openbis's execute operations result string. It should contain report on what was created.
    """
    api, session_token = context.applicationService, context.sessionToken
    search_engine = SearchEngine(api, session_token)

    xls_byte_arrays = parameters.get('xls', None)
    xls_name = parameters.get('xls_name', None)
    scripts = parameters.get('scripts', {})
    update_mode = parameters.get('update_mode', None)

    validate_data(xls_byte_arrays, update_mode, xls_name)

    definitions = get_definitions_from(xls_byte_arrays)
    creations = get_creations_from(definitions, FileHandler(scripts))
    creations_metadata = get_creation_metadata_from(definitions)
    creations = DuplicatesHandler.get_distinct_creations(creations)

    xls_version_vocabulary_name = get_version_vocabulary_name_for(xls_name)
    xls_version_vocabulary = search_engine.get_excel_version_vocabulary(xls_version_vocabulary_name)
    if xls_version_vocabulary is None:
        definition = Definition()
        definition.type = u'VOCABULARY_TYPE'
        definition.attributes[u'code'] = xls_version_vocabulary_name
        definition.attributes[u'description'] = xls_name + " Version internal vocabulary."
        for creation_type, creation_collection in creations.items():
            if creation_type in versionable_types:
                for creation in creation_collection:
                    code = get_metadata_vocabulary_name_for(creation_type, creation)
                    definition.properties.append({u'code': code, u'label': '1'})
        version_vocabulary_creation = DefinitionToCreationParser.parse([definition], context)
        version_vocabulary_creation_operation = CreationOrUpdateToOperationParser.parse(version_vocabulary_creation)
        vocabulary_ids = api.executeOperations(session_token, version_vocabulary_creation_operation,
                                               SynchronousOperationExecutionOptions()).getResults().get(
            0).getObjectIds()
        vocabulary_id = vocabulary_ids.get(0)
        vocabulary_fetch_options = VocabularyFetchOptions()
        vocabulary_fetch_options.withTerms()
        xls_version_vocabulary = api.getVocabularies(session_token, vocabulary_ids, vocabulary_fetch_options).get(
            vocabulary_id)

    existing_elements = search_engine.find_all_existing_elements(creations)
    entity_kinds = search_engine.find_existing_entity_kind_definitions_for(creations)
    existing_vocabularies = search_engine.find_all_existing_vocabularies()

    existing_unified_kinds = unify_properties_representation_of(creations, entity_kinds, existing_vocabularies,
                                                                existing_elements)
    creations = PropertiesLabelHandler.rewrite_property_labels_to_codes(creations, existing_unified_kinds)
    server_duplicates_handler = OpenbisDuplicatesHandler(creations, creations_metadata, existing_elements,
                                                         xls_version_vocabulary, update_mode)
    server_duplicates_handler.rewrite_parentchild_creationid_to_permid()
    creations = server_duplicates_handler.handle_existing_elements_in_creations()
    operations = CreationOrUpdateToOperationParser.parse(creations)
    print("EOEOOEOEOOEE")
    print(operations)
    res = str(api.executeOperations(session_token, operations, SynchronousOperationExecutionOptions()).getResults())
    return res
