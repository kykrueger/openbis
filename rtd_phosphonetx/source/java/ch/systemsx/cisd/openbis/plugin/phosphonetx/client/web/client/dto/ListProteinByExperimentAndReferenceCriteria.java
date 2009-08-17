/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.DataSetProtein;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ListProteinByExperimentAndReferenceCriteria extends
        DefaultResultSetConfig<String, DataSetProtein> implements IsSerializable
{
    private TechId experimentID;
    private TechId proteinReferenceID;

    public final TechId getExperimentID()
    {
        return experimentID;
    }

    public final void setExperimentID(TechId experimentID)
    {
        this.experimentID = experimentID;
    }

    public final TechId getProteinReferenceID()
    {
        return proteinReferenceID;
    }

    public final void setProteinReferenceID(TechId proteinReferenceID)
    {
        this.proteinReferenceID = proteinReferenceID;
    }

}
