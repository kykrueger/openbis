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
from ch.systemsx.cisd.openbis.generic.server import CommonServiceProvider
from ch.ethz.sis.openbis.generic.server.asapi.v3 import ApplicationServerApi
from ch.ethz.sis.openbis.generic.asapi.v3.dto.operation import SynchronousOperationExecutionOptions
from parsers import ExcelToPoiParser, PoiToDefinitionParser, DefinitionToCreationParser, CreationToOperationParser, DuplicatesHandler
from openbis_logic import ServerDuplicatesCreationHandler

TYPES_FOLDER = "%s/life-sciences-types/" % [p for p in sys.path if p.find('core-plugins') >= 0][0];
 
api = CommonServiceProvider.getApplicationContext().getBean(ApplicationServerApi.INTERNAL_SERVICE_NAME);

for excel_file in os.listdir(TYPES_FOLDER):
    excel_file_path = os.path.join(TYPES_FOLDER, excel_file)
    poi_definitions = ExcelToPoiParser.parse(excel_file_path)
    definitions = PoiToDefinitionParser.parse(poi_definitions)
    creations = DefinitionToCreationParser.parse(definitions)
    distinct_creations = DuplicatesHandler.get_distinct_creations(creations)
    sessionToken = api.loginAsSystem()
    server_duplicates_handler = ServerDuplicatesCreationHandler(api, sessionToken, distinct_creations)
    print(server_duplicates_handler)
    print(server_duplicates_handler.creations)
    creations = server_duplicates_handler.remove_already_existing_elements()
    operations = CreationToOperationParser.parse(creations)
    result = api.executeOperations(sessionToken, operations, SynchronousOperationExecutionOptions())
    print("========================eln-life-sciences-types xls ingestion result========================")
    print("Ingested " + excel_file)
    print(result)

