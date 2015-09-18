/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.dataset.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectToManyRelationTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.dataset.IDataSetTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
public abstract class ObjectToDataSetsSqlTranslator extends ObjectToManyRelationTranslator<DataSet, DataSetFetchOptions> implements
        IObjectToDataSetsSqlTranslator
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IDataSetTranslator dataSetTranslator;

    @Override
    protected Map<Long, DataSet> translateRelated(TranslationContext context, Collection<Long> relatedIds, DataSetFetchOptions relatedFetchOptions)
    {
        List<DataPE> related = daoFactory.getDataDAO().listByIDs(relatedIds);
        Map<DataPE, DataSet> translated = dataSetTranslator.translate(context, related, relatedFetchOptions);
        Map<Long, DataSet> result = new HashMap<Long, DataSet>();

        for (Map.Entry<DataPE, DataSet> entry : translated.entrySet())
        {
            result.put(entry.getKey().getId(), entry.getValue());
        }
        return result;
    }

    @Override
    protected Collection<DataSet> createCollection()
    {
        return new ArrayList<DataSet>();
    }

}