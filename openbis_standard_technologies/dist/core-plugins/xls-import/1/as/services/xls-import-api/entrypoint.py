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
from ch.systemsx.cisd.common.storage import PersistentKeyValueStore
from java.util import Properties
import os
from ch.systemsx.cisd.openbis.generic.server import CommonServiceProvider

REMOVE_VERSIONS = False


def validate_data(xls_byte_arrays, update_mode, xls_name):
    if xls_byte_arrays is None:
        raise UserFailureException('Excel sheet has not been provided. "xls" parameter is None')
    if update_mode not in ['IGNORE_EXISTING', 'FAIL_IF_EXISTS', 'UPDATE_IF_EXISTS']:
        raise UserFailureException(
            'Update mode has to be one of following: IGNORE_EXISTING FAIL_IF_EXISTS UPDATE_IF_EXISTS but was ' + (
                str(update_mode) if update_mode else 'None'))
    if xls_name is None:
        raise UserFailureException('Excel name has not been provided.  parameter is mandatory')


def get_property(key):
    propertyConfigurer = CommonServiceProvider.getApplicationContext().getBean("propertyConfigurer");
    properties = propertyConfigurer.getResolvedProps();
    print("BRBRRBRB")
    print(properties)
    return properties.getProperty(key);


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
    xls_version_filepath = get_property("xls-import.version-data-file")
    if xls_version_filepath is None:
        raise UserFailureException("No access to versioning information. Please xls-import.version-data-file property in service.properties.")
    if REMOVE_VERSIONS:
        if os.path.exists(xls_version_filepath):
            os.remove(xls_version_filepath)
    xls_version_name = get_version_vocabulary_name_for(xls_name)
    key_value_store = PersistentKeyValueStore(xls_version_filepath)
    versioning_information = key_value_store.get(xls_version_name)

    if versioning_information is None:
        versioning_information = {}
        for creation_type, creation_collection in creations.items():
            if creation_type in versionable_types:
                for creation in creation_collection:
                    code = get_metadata_vocabulary_name_for(creation_type, creation)
                    versioning_information[code] = 0
    else:
        for creation_type, creation_collection in creations.items():
            if creation_type in versionable_types:
                for creation in creation_collection:
                    code = get_metadata_vocabulary_name_for(creation_type, creation)
                    version = versioning_information.get(code, None)
                    if version is None:
                        version = 0
                    versioning_information[code] = int(version)

    existing_elements = search_engine.find_all_existing_elements(creations)
    entity_kinds = search_engine.find_existing_entity_kind_definitions_for(creations)
    existing_vocabularies = search_engine.find_all_existing_vocabularies()

    existing_unified_kinds = unify_properties_representation_of(creations, entity_kinds, existing_vocabularies, existing_elements)
    creations = PropertiesLabelHandler.rewrite_property_labels_to_codes(creations, existing_unified_kinds)
    server_duplicates_handler = OpenbisDuplicatesHandler(creations, creations_metadata, existing_elements,
                                                         versioning_information, update_mode)

    creations = server_duplicates_handler.rewrite_parentchild_creationid_to_permid()
    creations = server_duplicates_handler.handle_existing_elements_in_creations()
    operations = CreationOrUpdateToOperationParser.parse(creations)
    res = str(api.executeOperations(session_token, operations, SynchronousOperationExecutionOptions()).getResults())
    key_value_store.put(xls_version_name, versioning_information)
    return res

