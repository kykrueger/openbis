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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.vocabulary.sql;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationResults;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.vocabulary.VocabularyTermFetchOptions;

/**
 * @author pkupczyk
 */
@Component
public class VocabularyTermSqlTranslator extends AbstractCachingTranslator<Long, VocabularyTerm, VocabularyTermFetchOptions> implements
        IVocabularyTermSqlTranslator
{

    @Autowired
    private IVocabularyTermBaseSqlTranslator baseTranslator;

    @Autowired
    private IVocabularyTermRegistratorSqlTranslator registratorTranslator;

    @Autowired
    private IVocabularyTermVocabularySqlTranslator vocabularyTranslator;

    @Override
    protected VocabularyTerm createObject(TranslationContext context, Long termId, VocabularyTermFetchOptions fetchOptions)
    {
        VocabularyTerm term = new VocabularyTerm();
        term.setFetchOptions(new VocabularyTermFetchOptions());
        return term;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> termIds, VocabularyTermFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IVocabularyTermBaseSqlTranslator.class, baseTranslator.translate(context, termIds, null));

        if (fetchOptions.hasRegistrator())
        {
            relations.put(IVocabularyTermRegistratorSqlTranslator.class,
                    registratorTranslator.translate(context, termIds, fetchOptions.withRegistrator()));
        }

        if (fetchOptions.hasVocabulary())
        {
            relations.put(IVocabularyTermVocabularySqlTranslator.class,
                    vocabularyTranslator.translate(context, termIds, fetchOptions.withVocabulary()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long termId, VocabularyTerm result, Object objectRelations,
            VocabularyTermFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        VocabularyTermBaseRecord baseRecord = relations.get(IVocabularyTermBaseSqlTranslator.class, termId);

        result.setCode(baseRecord.code);
        result.setLabel(baseRecord.label);
        result.setDescription(baseRecord.description);
        result.setOrdinal(baseRecord.ordinal);
        result.setOfficial(baseRecord.isOfficial);
        // TODO: add modification date to vocabulary terms table
        result.setModificationDate(baseRecord.registrationDate);
        result.setRegistrationDate(baseRecord.registrationDate);

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(IVocabularyTermRegistratorSqlTranslator.class, termId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasVocabulary())
        {
            result.setVocabulary(relations.get(IVocabularyTermVocabularySqlTranslator.class, termId));
            result.getFetchOptions().withVocabularyUsing(fetchOptions.withVocabulary());
        }

    }

}
