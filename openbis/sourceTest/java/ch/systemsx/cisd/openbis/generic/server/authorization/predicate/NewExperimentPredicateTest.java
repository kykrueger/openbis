/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewExperimentPredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExperiment;

/**
 * Test cases for corresponding {@link NewExperimentPredicate} class.
 * 
 * @author Christian Ribeaud
 */
public final class NewExperimentPredicateTest
{
    private final NewExperiment createNewExperiment()
    {
        return new NewExperiment();
    }

    @Test
    public final void testWithoutInit()
    {
        final NewExperimentPredicate predicate = new NewExperimentPredicate();
        boolean fail = true;
        try
        {
            predicate.evaluate(DatabaseInstanceIdentifierPredicateTest.createPerson(),
                    DatabaseInstanceIdentifierPredicateTest.createAllowedRoles(false),
                    createNewExperiment());
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
    }
}
