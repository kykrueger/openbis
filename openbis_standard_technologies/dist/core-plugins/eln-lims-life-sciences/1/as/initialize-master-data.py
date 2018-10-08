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
from parsers import get_creations_from, DuplicatesHandler, CreationToOperationParser
from processors import OpenbisDuplicatesHandler, VocabularyLabelHandler, PropertiesLabelHandler, unify_properties_representation_of
from search_engines import SearchEngine
from utils.file_handling import list_xls_files

api = CommonServiceProvider.getApplicationContext().getBean(ApplicationServerApi.INTERNAL_SERVICE_NAME)

creations = get_creations_from(list_xls_files())
distinct_creations = DuplicatesHandler.get_distinct_creations(creations)
sessionToken = api.loginAsSystem()
search_engine = SearchEngine(api, sessionToken)
existing_elements = search_engine.find_all_existing_elements(distinct_creations)
server_duplicates_handler = OpenbisDuplicatesHandler(distinct_creations, existing_elements)
creations = server_duplicates_handler.remove_existing_elements_from_creations()
creations = server_duplicates_handler.rewrite_parentchild_creationid_to_permid()
entity_kinds = search_engine.find_existing_entity_kind_definitions_for(creations)
existing_vocabularies = search_engine.find_all_existing_vocabularies()
existing_unified_kinds = unify_properties_representation_of(creations, entity_kinds, existing_vocabularies, existing_elements)
creations = PropertiesLabelHandler.rewrite_property_labels_to_codes(creations, existing_unified_kinds)
operations = CreationToOperationParser.parse(creations)
result = api.executeOperations(sessionToken, operations, SynchronousOperationExecutionOptions())
print("========================eln-life-sciences-types xls ingestion result========================")
print(result)
print("========================eln-life-sciences-types xls ingestion result========================")

