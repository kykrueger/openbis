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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;

/**
 * Test cases for corresponding {@link GroupDAO} class.
 * 
 * @author Franz-Josef Elmer
 */
@Test(groups =
    { "db", "group" })
public final class GroupDAOTest extends AbstractDAOTest
{
    @Test
    public void testCreateGroup()
    {
        final String groupCode = "test-group";
        final GroupPE group = createGroup(groupCode);

        final DatabaseInstancePE databaseInstance = daoFactory.getHomeDatabaseInstance();
        final GroupPE retrievedGroup =
                daoFactory.getGroupDAO().tryFindGroupByCodeAndDatabaseInstance(groupCode,
                        databaseInstance);
        AssertJUnit.assertNotNull(retrievedGroup);
        assertEquals(group.getRegistrator(), retrievedGroup.getRegistrator());
    }

    @Test
    public void testListGroups()
    {
        final List<GroupPE> groups = daoFactory.getGroupDAO().listGroups();
        Collections.sort(groups);
        assertEquals("CISD", groups.get(0).getCode());
        assertEquals("TESTGROUP", groups.get(1).getCode());
        assertEquals(2, groups.size());
    }

    @Test
    public void testListGroupsOfHomeDatabaseInstance()
    {
        final List<GroupPE> groups =
                daoFactory.getGroupDAO().listGroups(daoFactory.getHomeDatabaseInstance());
        Collections.sort(groups);
        assertEquals("CISD", groups.get(0).getCode());
        assertEquals("TESTGROUP", groups.get(1).getCode());
        assertEquals(2, groups.size());
    }

    @Test(groups = "broken")
    public void testListGroupsOfAnotherDatabaseInstance()
    {
        final DatabaseInstancePE databaseInstance = createDatabaseInstance("another-db");
        databaseInstance.setOriginalSource(true); // to cheat GroupDAO
        createGroup("test-group", databaseInstance);
        final List<GroupPE> groups = daoFactory.getGroupDAO().listGroups(databaseInstance);
        assertEquals("TEST-GROUP", groups.get(0).getCode());
        assertEquals(1, groups.size());
    }
}
