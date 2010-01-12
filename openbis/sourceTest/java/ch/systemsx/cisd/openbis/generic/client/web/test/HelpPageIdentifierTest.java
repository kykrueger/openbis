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

package ch.systemsx.cisd.openbis.generic.client.web.test;

import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.client.web.client.onlinehelp.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.onlinehelp.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.onlinehelp.HelpPageIdentifier.HelpPageDomain;

/**
 * Test generation of page titles from {@link HelpPageIdentifier}s.
 * 
 * @author Piotr Buczek
 */
@Friend(toClasses =
    { HelpPageIdentifier.class })
public class HelpPageIdentifierTest extends AssertJUnit
{

    @Test
    public void testHelpPageDomainPathForBaseDomain()
    {
        final HelpPageDomain baseDomain = HelpPageDomain.CHANGE_USER_SETTINGS;
        assertNull(baseDomain.getSuperDomainOrNull());
        assertDomainPathsEquals(new HelpPageDomain[]
            { HelpPageDomain.CHANGE_USER_SETTINGS }, baseDomain.getDomainPath());
    }

    @Test
    public void testHelpPageDomainPathForSubDomain()
    {
        final HelpPageDomain subDomain = HelpPageDomain.USERS;
        assertNotNull(subDomain.getSuperDomainOrNull());
        assertDomainPathsEquals(new HelpPageDomain[]
            { HelpPageDomain.ADMINISTRATION, HelpPageDomain.AUTHORIZATION, HelpPageDomain.USERS },
                subDomain.getDomainPath());
    }

    private void assertDomainPathsEquals(HelpPageDomain[] expectedPath,
            List<HelpPageDomain> actualPath)
    {
        assertNotNull(expectedPath);
        assertNotNull(actualPath);
        assertEquals(expectedPath.length, actualPath.size());
        for (int i = 0; i < expectedPath.length; i++)
        {
            assertEquals(String.format("Path mismatch at position %d;", i + 1), expectedPath[i],
                    actualPath.get(i));
        }
    }

    @DataProvider(name = "pageTitlesToTest")
    protected Object[][] getPageTitlesToTest()
    {
        return new Object[][]
            {
                        { HelpPageDomain.CHANGE_USER_SETTINGS, HelpPageAction.ACTION,
                                "CHANGE_USER_SETTINGS.ACTION" },
                        { HelpPageDomain.USERS, HelpPageAction.BROWSE,
                                "ADMINISTRATION.AUTHORIZATION.USERS.BROWSE" },

            };
    }

    @Test(dataProvider = "pageTitlesToTest")
    public void testHelpPageTitleKey(final HelpPageDomain domain, final HelpPageAction action,
            final String expectedKey)
    {
        assertEquals(expectedKey, new HelpPageIdentifier(domain, action).getHelpPageTitleKey());
    }

}
