/*
 * Copyright 2013 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentTable;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

import java.util.List;
import java.util.Set;

/**
 * Search manager for experiments.
 * 
 * @author Viktor Kovtun
 */
public class ExperimentSearchManager extends AbstractSearchManager<ExperimentSearchCriteria>
{

    public ExperimentSearchManager(ISQLSearchDAO searchDAO, IExperimentTable lister)
    {
        super(searchDAO);
    }

    public List<ExperimentPE> searchForExperiments(final String userId, final ExperimentSearchCriteria criteria)
    {
//        final List<Long> experimentIds = searchForExperimentIDs(userId, criteria);
//        lister.loadByIds(experimentIds);
//        return lister.getExperiments();
        return null;
    }

    public List<Long> searchForExperimentIDs(final String userId, final ExperimentSearchCriteria criteria)
    {
//        return searchDAO.searchForEntityIds(userId, criteria, EXPERIMENT, Collections.emptyList());
        return null;
    }

    @Override
    public Set<Long> searchForIDs(ExperimentSearchCriteria criteria)
    {
        return null;
    }

    @Override
    public Set<Long> filterIDsByUserRights(Long userId, Set<Long> ids) {
        return null;
    }

    @Override
    public List<Long> sortAndPage(Set<Long> ids, ExperimentSearchCriteria criteria, FetchOptions<?> fetchOptions) {
        return null;
    }

}
