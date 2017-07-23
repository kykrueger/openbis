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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.deletion;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.DeletionUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.deletion.DeletionPredicateTestService;

/**
 * @author pkupczyk
 */
public abstract class RevertDeletionPredicateSystemTest extends CommonPredicateSystemTest<TechId>
{

    @Override
    protected boolean isCollectionPredicate()
    {
        return true;
    }

    @Override
    protected TechId createNonexistentObject(Object param)
    {
        return DeletionUtil.createNonexistentObject(param);
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider sessionProvider, List<TechId> objects, Object param)
    {
        try
        {
            getBean(DeletionPredicateTestService.class).testRevertDeletionPredicate(sessionProvider, objects);
        } finally
        {
            if (objects != null)
            {
                for (TechId object : objects)
                {
                    if (object != null)
                    {
                        getCommonService().untrash(object.getId());
                    }
                }
            }
        }
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t, Object param)
    {
        assertException(t, NullPointerException.class, null);
    }

    @Override
    protected void assertWithNullForInstanceUser(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

    @Override
    protected void assertWithNullCollection(PersonPE person, Throwable t, Object param)
    {
        assertException(t, UserFailureException.class, "No revert deletion technical id specified.");
    }

    @Override
    protected void assertWithNonexistentObject(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

}