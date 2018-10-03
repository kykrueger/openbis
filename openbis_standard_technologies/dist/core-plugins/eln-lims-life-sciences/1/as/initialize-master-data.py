#
# Copyright 2014 ETH Zuerich, Scientific IT Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# MasterDataRegistrationTransaction Class
import os
import sys

from ch.ethz.sis.openbis.generic.asapi.v3.dto.operation import SynchronousOperationExecutionOptions
from ch.ethz.sis.openbis.generic.server.asapi.v3 import ApplicationServerApi
from ch.systemsx.cisd.openbis.generic.server import CommonServiceProvider
from parsers import ExcelToPoiParser, PoiToDefinitionParser, DefinitionToCreationParser, DuplicatesHandler, CreationToOperationParser
from processors import OpenbisDuplicatesHandler, VocabularyLabelHandler
from search_engines import SearchEngine
from utils.file_handling import list_xls_files

api = CommonServiceProvider.getApplicationContext().getBean(ApplicationServerApi.INTERNAL_SERVICE_NAME)

creations = {}
for excel_file_path in list_xls_files():
    poi_definitions = ExcelToPoiParser.parse(excel_file_path)
    definitions = PoiToDefinitionParser.parse(poi_definitions)
    partial_creations = DefinitionToCreationParser.parse(definitions)
    for creation_type, partial_creation in partial_creations.items():
        if creation_type not in creations:
            creations[creation_type] = partial_creation
        else:
            creations[creation_type].extend(partial_creation)
distinct_creations = DuplicatesHandler.get_distinct_creations(creations)
sessionToken = api.loginAsSystem()
search_engine = SearchEngine(api, sessionToken)
existing_elements = search_engine.find_all_existing_elements(distinct_creations)
server_duplicates_handler = OpenbisDuplicatesHandler(distinct_creations, existing_elements)
creations = server_duplicates_handler.remove_existing_elements_from_creations()
creations = server_duplicates_handler.rewrite_parentchild_creationid_to_permid()
entity_types = search_engine.find_existing_vocabularies_in_entity_definitions(creations)

# Build initial Codes/labels and Property Code/labels during parsing
# Map<String, Map<String, String>> propertiesHelperMap
# Map<ENTITY_TYPE_CODE, Map<PROPERTY_CODE|PROPERTY_LABEL, { code: PROPERTY_CODE, dataType: DATA_TYPE, vocabularyCode: VOCABULARY_CODE}> vocabulariesHelperMap
# properties_helper_map = extractProperties(creations, {})

# Build initial Codes/labels and Vocabulary Terms/labels during parsing
# Map<String, Map<String, String>> vocabulariesHelperMap
# Map<VOCABULARY_CODE, Map<VOCABULARY_TERM_CODE|VOCABULARY_TERM_LABEL, VOCABULARY_TERM_CODE> vocabulariesHelperMap
# vocabularies_helper_map = extractVocabularies(creations, entity_types)
# creations = VocabularyLabelHandler.rewrite_vocabularies(creations, vocabularies_helper_map)

operations = CreationToOperationParser.parse(creations)
result = api.executeOperations(sessionToken, operations, SynchronousOperationExecutionOptions())
print("========================eln-life-sciences-types xls ingestion result========================")
print(result)
print("========================eln-life-sciences-types xls ingestion result========================")

