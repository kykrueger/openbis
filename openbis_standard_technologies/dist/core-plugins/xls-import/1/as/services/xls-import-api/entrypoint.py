from ch.ethz.sis.openbis.generic.asapi.v3.dto.operation import SynchronousOperationExecutionOptions
from parsers import get_creations_from, CreationOrUpdateToOperationParser
from processors import OpenbisDuplicatesHandler, PropertiesLabelHandler, DuplicatesHandler, unify_properties_representation_of
from search_engines import SearchEngine
from utils import FileHandler
from ch.systemsx.cisd.common.exceptions import UserFailureException


def process(context, parameters):
    """
        Excel import AS service.
        For extensive documentation of usage and Excel layout, 
        please visit https://wiki-bsse.ethz.ch/display/openBISDoc/Excel+import+service
        
        :param context: Standard Openbis AS Service context object
        :param parameters: Contains two elemens
                        {
                            'xls' : excel byte blob,    - mandatory
                            'scripts': {                - optional
                                file path: loaded file
                            },
                            update_mode: [IGNORE_EXISTING|FAIL_IF_EXISTS|UPDATE_IF_EXISTS] - optional, default FAIL_IF_EXISTS
                                                                                             This only takes duplicates that are ON THE SERVER
                        }
        :return: Openbis's execute operations result string. It should contain report on what was created.
    """
    xls_byte_arrays = parameters.get('xls', None)
    scripts = parameters.get('scripts', {})
    update_mode = parameters.get('update_mode', None)
    if xls_byte_arrays is None:
        raise UserFailureException('Excel sheet has not been provided. "xls" parameter is None')
    if update_mode not in ['IGNORE_EXISTING', 'FAIL_IF_EXISTS', 'UPDATE_IF_EXISTS']:
        raise UserFailureException('Update mode has to be one of following: IGNORE_EXISTING FAIL_IF_EXISTS UPDATE_IF_EXISTS but was ' + (str(update_mode) if update_mode else 'None'))
    creations = get_creations_from(xls_byte_arrays, FileHandler(scripts))
    distinct_creations = DuplicatesHandler.get_distinct_creations(creations)
    sessionToken = context.sessionToken
    api = context.applicationService
    search_engine = SearchEngine(api, sessionToken)
    existing_elements = search_engine.find_all_existing_elements(distinct_creations)
    entity_kinds = search_engine.find_existing_entity_kind_definitions_for(creations)
    existing_vocabularies = search_engine.find_all_existing_vocabularies()
    existing_unified_kinds = unify_properties_representation_of(creations, entity_kinds, existing_vocabularies, existing_elements)
    creations = PropertiesLabelHandler.rewrite_property_labels_to_codes(creations, existing_unified_kinds)
    server_duplicates_handler = OpenbisDuplicatesHandler(distinct_creations, existing_elements, update_mode)
    creations = server_duplicates_handler.rewrite_parentchild_creationid_to_permid()
    creations = server_duplicates_handler.handle_existing_elements_in_creations()
    operations = CreationOrUpdateToOperationParser.parse(creations)
    res = str(api.executeOperations(sessionToken, operations, SynchronousOperationExecutionOptions()).getResults())
    return res

