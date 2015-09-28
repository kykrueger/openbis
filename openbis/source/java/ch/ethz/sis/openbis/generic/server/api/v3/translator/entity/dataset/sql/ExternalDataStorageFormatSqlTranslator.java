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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.QueryTool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectToOneRelationTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.vocabulary.sql.IVocabularyTermSqlTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.vocabulary.VocabularyTermFetchOptions;

/**
 * @author pkupczyk
 */
@Component
public class ExternalDataStorageFormatSqlTranslator extends ObjectToOneRelationTranslator<VocabularyTerm, VocabularyTermFetchOptions> implements
        IExternalDataStorageFormatSqlTranslator
{

    @Autowired
    private IVocabularyTermSqlTranslator vocabularyTermTranslator;

    @Override
    protected List<ObjectRelationRecord> loadRecords(LongOpenHashSet objectIds)
    {
        DataSetQuery query = QueryTool.getManagedQuery(DataSetQuery.class);
        return query.getStorageFormatIds(objectIds);
    }

    @Override
    protected Map<Long, VocabularyTerm> translateRelated(TranslationContext context, Collection<Long> relatedIds,
            VocabularyTermFetchOptions relatedFetchOptions)
    {
        return vocabularyTermTranslator.translate(context, relatedIds, relatedFetchOptions);
    }

}
