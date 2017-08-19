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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator;

import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
public abstract class CommonDataSetValidatorSystemTest<O> extends CommonValidatorSystemTest<O>
{

    @Override
    public Object[] provideParams()
    {
        return provideDataSetKinds();
    }

    @Override
    protected void assertWithNonMatchingSpaceAndMatchingProjectUser(PersonPE person, O result, Throwable t, Object param)
    {
        if (DataSetKind.SPACE_SAMPLE.equals(param))
        {
            assertNull(result);
        } else
        {
            super.assertWithNonMatchingSpaceAndMatchingProjectUser(person, result, t, param);
        }
    }

}
