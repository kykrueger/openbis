/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

/**
 * @author pkupczyk
 */
public class SearchProjectTest extends AbstractTest
{

    @Test
    public void testSearchWithIdSetToPermId()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withId().thatEquals(new ProjectPermId("20120814110011738-105"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithIdSetToIdentifier()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withId().thatEquals(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithIdSetToNonexistentPermId()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withId().thatEquals(new ProjectPermId("IDONTEXIST"));
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithIdSetToNonexistentIdentifier()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withId().thatEquals(new ProjectIdentifier("/IDONT/EXIST"));
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPermIdThatEquals()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withPermId().thatEquals("20120814110011738-105");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithCodeThatEquals()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withCode().thatEquals("test-PROJECT");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithCodeThatContains()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withCode().thatContains("pRoJ");
        testSearch(TEST_USER, criteria, "/TESTGROUP/TESTPROJ", "/TEST-SPACE/TEST-PROJECT", "/TEST-SPACE/PROJECT-TO-DELETE");
    }

    @Test
    public void testSearchWithCodeThatStartsWith()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withCode().thatStartsWith("n");
        testSearch(TEST_USER, criteria, "/CISD/NEMO", "/CISD/NOE", "/TEST-SPACE/NOE");
    }

    @Test
    public void testSearchWithCodeThatEndsWith()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withCode().thatEndsWith("t");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT", "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithSpaceWithIdThatEquals()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withSpace().withId().thatEquals(new SpacePermId("CISD"));
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT", "/CISD/NEMO", "/CISD/NOE");
    }

    @Test
    public void testSearchWithSpaceWithCodeThatStartsWith()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withSpace().withCode().thatStartsWith("TEST");
        testSearch(TEST_USER, criteria, "/TESTGROUP/TESTPROJ", "/TEST-SPACE/TEST-PROJECT", "/TEST-SPACE/NOE", "/TEST-SPACE/PROJECT-TO-DELETE");
    }

    @Test
    public void testSearchWithAndOperator()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatContains("TEST");
        criteria.withCode().thatContains("PROJECT");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("20120814110011738-101");
        criteria.withPermId().thatEquals("20120814110011738-105");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT", "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithProjectUnauthorized()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withId().thatEquals(new ProjectIdentifier("/CISD/DEFAULT"));
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT");
        testSearch(TEST_SPACE_USER, criteria);
    }

    private void testSearch(String user, ProjectSearchCriteria criteria, String... expectedIdentifiers)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<Project> searchResult = v3api.searchProjects(sessionToken, criteria, new ProjectFetchOptions());
        List<Project> projects = searchResult.getObjects();

        assertProjectIdentifiers(projects, expectedIdentifiers);
        v3api.logout(sessionToken);
    }

}