/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.shared.authorization.result_filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * {@link IGroupLoader} for experiments.
 * 
 * @author Izabela Adamczyk
 */
class ExperimentGroupLoader implements IGroupLoader
{

    private final IExperimentDAO dao;

    public ExperimentGroupLoader(IExperimentDAO dao)
    {
        this.dao = dao;
    }

    @Override
    public Map<String, SpacePE> loadGroups(Set<String> keys)
    {
        Map<String, SpacePE> map = new HashMap<String, SpacePE>();
        List<ExperimentPE> experiments = dao.listByPermID(keys);
        for (ExperimentPE e : experiments)
        {
            map.put(e.getPermId(), e.getProject().getSpace());
        }
        return map;
    }
}