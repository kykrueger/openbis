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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Vocabulary term code.
 * 
 * @author pkupczyk
 */
@JsonObject("dto.vocabulary.id.VocabularyTermCode")
public class VocabularyTermCode implements IVocabularyTermId, Serializable
{

    private static final long serialVersionUID = 1L;

    private String code;

    /**
     * @param code Vocabulary term code, e.g. "MY_TERM".
     */
    public VocabularyTermCode(String code)
    {
        setCode(code != null ? code.toUpperCase() : null);
    }

    //
    // JSON-RPC
    //

    public String getCode()
    {
        return code;
    }

    @SuppressWarnings("unused")
    private VocabularyTermCode()
    {
        super();
    }

    private void setCode(String code)
    {
        if (code == null)
        {
            throw new IllegalArgumentException("Code cannot be null");
        }
        this.code = code;
    }

    @Override
    public String toString()
    {
        return getCode();
    }

    @Override
    public int hashCode()
    {
        return ((getCode() == null) ? 0 : getCode().hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        VocabularyTermCode other = (VocabularyTermCode) obj;
        return getCode() == null ? getCode() == other.getCode() : getCode().equals(other.getCode());
    }

}
