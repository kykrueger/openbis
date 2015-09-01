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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Describes detailed search assiciation criteria for with specified entity kind.
 * 
 * @author Piotr Buczek
 */
public class DetailedSearchAssociationCriteria extends AbstractAssociationCriteria
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Collection<Long> ids;

    public DetailedSearchAssociationCriteria(AssociatedEntityKind entityKind, Collection<Long> ids)
    {
        super(entityKind);
        this.ids = ids;
    }

    @Override
    public List<String> getSearchPatterns()
    {
        List<String> patterns = new ArrayList<String>();
        for (Long id : ids)
        {
            patterns.add(id.toString());
        }
        return patterns;
    }

}
