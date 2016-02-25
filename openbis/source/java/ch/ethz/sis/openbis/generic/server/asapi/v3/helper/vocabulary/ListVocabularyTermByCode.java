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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.vocabulary;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermCode;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author pkupczyk
 */
public class ListVocabularyTermByCode extends AbstractListObjectById<VocabularyTermCode, VocabularyTermPE>
{

    private VocabularyPE vocabulary;

    public ListVocabularyTermByCode(VocabularyPE vocabulary)
    {
        this.vocabulary = vocabulary;
    }

    @Override
    public Class<VocabularyTermCode> getIdClass()
    {
        return VocabularyTermCode.class;
    }

    @Override
    public VocabularyTermCode createId(VocabularyTermPE term)
    {
        return new VocabularyTermCode(term.getCode());
    }

    @Override
    public List<VocabularyTermPE> listByIds(IOperationContext context, List<VocabularyTermCode> ids)
    {
        List<VocabularyTermPE> terms = new LinkedList<VocabularyTermPE>();

        Set<String> codes = new HashSet<String>();
        for (VocabularyTermCode id : ids)
        {
            codes.add(id.getCode());
        }

        for (VocabularyTermPE term : vocabulary.getTerms())
        {
            if (codes.contains(term.getCode()))
            {
                terms.add(term);
            }
        }

        return terms;
    }
}
