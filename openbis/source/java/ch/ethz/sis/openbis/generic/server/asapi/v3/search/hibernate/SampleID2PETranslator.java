/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate;

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

public class SampleID2PETranslator implements IID2PETranslator<SamplePE>
{
    private ISampleDAO dao;

    public SampleID2PETranslator(final ISampleDAO dao)
    {
        this.dao = dao;
    }

    @Override
    public List<SamplePE> translate(final Collection<Long> ids)
    {
        return dao.listByIDs(ids);
    }
}
