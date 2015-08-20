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

import sys

from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClauseAttribute
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import SearchOperator

reload(sys)
sys.setdefaultencoding('UTF8')

def process(tr, parameters, tableBuilder):
	sample = findSample(tr)
	dataSet = createDataSet(tr, sample)
	
	tableBuilder.addHeader("DATA_SET_CODE")
	row = tableBuilder.addRow()
	row.setCell("DATA_SET_CODE", dataSet.getDataSetCode())

def findSample(tr):
	criteria = SearchCriteria()
	criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, "20130412140147736-21")) # /PLATONIC/PLATE-2
	samples = tr.getSearchService().searchForSamples(criteria)
	return samples[0]
	
def createDataSet(tr, sample):
	dataSet = tr.createNewDataSet("ALIGNMENT")
	dataSet.setSample(sample)
	tr.createNewFile(dataSet, "test")
	return dataSet