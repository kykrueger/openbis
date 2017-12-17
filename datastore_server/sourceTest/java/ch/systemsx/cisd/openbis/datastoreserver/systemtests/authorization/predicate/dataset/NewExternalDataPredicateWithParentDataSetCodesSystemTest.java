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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.dataset;

import java.util.List;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.DataSetCodeUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestDataSetAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.dataset.DataSetPredicateTestService;

/**
 * @author pkupczyk
 */
public class NewExternalDataPredicateWithParentDataSetCodesSystemTest extends NewExternalDataPredicateSystemTest<String>
{

    @Override
    public Object[] getParams()
    {
        return getDataSetKinds();
    }

    @Override
    protected String createNonexistentObject(Object param)
    {
        return DataSetCodeUtil.createNonexistentObject(param);
    }

    @Override
    protected String createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        return DataSetCodeUtil.createObject(this, spacePE, projectPE, param);
    }

    @Override
    protected void evaluateObjects(ProjectAuthorizationUser user, List<String> objects, Object param)
    {
        NewExternalData data = new NewExternalData();
        data.setParentDataSetCodes(objects);

        getBean(DataSetPredicateTestService.class).testNewExternalDataPredicate(user.getSessionProvider(), data);
    }

    @Override
    protected CommonPredicateSystemTestAssertions<String> getAssertions()
    {
        return new CommonPredicateSystemTestDataSetAssertions<String>(super.getAssertions())
            {
                @Override
                public void assertWithNullObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    if (user.isDisabledProjectUser())
                    {
                        assertAuthorizationFailureExceptionThatNoRoles(t);
                    } else if (user.isInstanceUser())
                    {
                        assertNoException(t);
                    } else
                    {
                        assertException(t, NullPointerException.class, null);
                    }
                }

                @Override
                public void assertWithNullCollection(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    assertException(t, NullPointerException.class, null);
                }

            };
    }

}
