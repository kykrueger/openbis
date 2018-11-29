'''
@copyright:
2016 ETH Zuerich, SIS
    
@license: 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    
http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
'''

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType


SAMPLE_TYPE_SEARCH_QUERY = "SEARCH_QUERY"
# we use XML to store the properties to avoid indexing of the fields
PROPERTY_TYPES = [
        {
            'code': 'NAME',
            'dataType': DataType.VARCHAR,
            'label': 'Name',
            'mandatory': True,
            'description': 'Human readable name',
            'shownEdit': True
        },
        {
            'code': 'SEARCH_CRITERIA',
            'dataType': DataType.XML,
            'label': 'Search criteria',
            'mandatory': True,
            'description': 'V3 API search criteria',
            'shownEdit': False
        },
        {
            'code': "FETCH_OPTIONS",
            'dataType': DataType.XML,
            'label': 'Fetch options',
            'mandatory': False,
            'description': 'V3 API fetch options',
            'shownEdit': False
        },
        {
            'code': "CUSTOM_DATA",
            'dataType': DataType.XML,
            'label': 'Custom data',
            'mandatory': False,
            'description': 'Additional data in custom format',
            'shownEdit': False
        }
    ]


tr = service.transaction()

sample_type = tr.getOrCreateNewSampleType(SAMPLE_TYPE_SEARCH_QUERY)
sample_type.setGeneratedCodePrefix("Q")

for propert_type_def in PROPERTY_TYPES:
    property_type = tr.getOrCreateNewPropertyType(propert_type_def['code'], propert_type_def['dataType'])
    property_type.setLabel(propert_type_def['label'])
    property_type.setDescription(propert_type_def['description'])
    assignment = tr.assignPropertyType(sample_type, property_type)
    assignment.setMandatory(propert_type_def['mandatory'])
    assignment.setShownEdit(propert_type_def['shownEdit'])
