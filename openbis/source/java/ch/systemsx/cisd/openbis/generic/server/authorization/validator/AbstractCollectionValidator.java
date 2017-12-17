/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import java.util.Arrays;
import java.util.Collection;

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
public abstract class AbstractCollectionValidator<T> implements ICollectionValidator<T>
{

    protected IAuthorizationDataProvider authorizationDataProvider;

    @Override
    public void init(
            @SuppressWarnings("hiding") IAuthorizationDataProvider authorizationDataProvider)
    {
        this.authorizationDataProvider = authorizationDataProvider;
    }

    @Override
    public boolean isValid(PersonPE person, T value)
    {
        Collection<T> valid = getValid(person, Arrays.asList(value));
        return false == valid.isEmpty();
    }

}
