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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.widget.form.Validator;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * {@link VarcharField} extension for registering {@link VocabularyTerm} that will be validated with
 * and existing {@link Vocabulary}.
 * 
 * @author Piotr Buczek
 */
public class VocabularyTermField extends VarcharField
{

    public VocabularyTermField(String label, boolean mandatory, Vocabulary vocabulary)
    {
        super(label, mandatory);
        setAutoValidate(false);
        setValidator(new VocabularyTermValidator(vocabulary));
        setValidateOnBlur(true);
        setEmptyText("Vocabulary term");
        getMessages().setBlankText("Vocabulary term required");
    }

    @Override
    protected void onFocus(ComponentEvent be)
    {
        // clearing invalid messages on focus is needed because auto validation is turned off
        super.onFocus(be);
        clearInvalid();
    }

    /** {@link Validator} for vocabulary terms from controlled vocabulary. */
    private class VocabularyTermValidator implements Validator<String, VocabularyTermField>
    {
        private final String vocabularyCode;

        private final Set<String> terms;

        public VocabularyTermValidator(Vocabulary vocabulary)
        {
            this.vocabularyCode = vocabulary.getCode();
            this.terms = createTerms(vocabulary.getTerms());
        }

        private Set<String> createTerms(List<VocabularyTerm> vocabularyTerms)
        {
            // we could use a set of VocabularyTerm's but than we would have to reimplement
            // VocabuleryTerm hashCode and equals to use only code
            final Set<String> result = new HashSet<String>(vocabularyTerms.size());
            for (VocabularyTerm vocabularyTerm : vocabularyTerms)
            {
                result.add(vocabularyTerm.getCode());
            }
            return result;
        }

        public String validate(VocabularyTermField field, final String fieldValue)
        {
            final String upperCaseValue = fieldValue.toUpperCase();
            if (terms.contains(upperCaseValue) == false)
            {
                return "Given term does not exist in '" + vocabularyCode
                        + "' controlled vocabulary.";
            }
            // validated value is valid
            return null;
        }
    }

}
