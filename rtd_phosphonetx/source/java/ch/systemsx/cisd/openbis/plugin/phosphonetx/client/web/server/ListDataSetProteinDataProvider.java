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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.server;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.DataSetProtein;

/**
 * @author Franz-Josef Elmer
 */
public class ListDataSetProteinDataProvider implements IOriginalDataProvider<DataSetProtein>
{
    private final IPhosphoNetXServer server;

    private final String sessionToken;

    private final TechId experimentID;

    private final TechId proteinReferenceID;

    ListDataSetProteinDataProvider(IPhosphoNetXServer server, String sessionToken,
            TechId experimentID, TechId proteinReferenceID)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.experimentID = experimentID;
        this.proteinReferenceID = proteinReferenceID;
    }

    public List<DataSetProtein> getOriginalData() throws UserFailureException
    {
        return server.listProteinsByExperimentAndReference(sessionToken, experimentID,
                proteinReferenceID);
    }

}
